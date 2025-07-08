package main

import (
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"os/exec"
	"slices"
	"strings"
	"time"
)

func parseBody(resp *http.Response) map[string]interface{} {
	var result map[string]interface{}
	err := json.NewDecoder(resp.Body).Decode(&result)
	if err != nil {
		fmt.Printf("Error decoding JSON response: %v\n", err)
		return nil
	}
	return result
}

func endpointChecker(apiURL string, method string, authToken string, requestBody string, expectedStatus int, expectedBody map[string]interface{}) *http.Response {
	req, err := http.NewRequest(method, apiURL, nil)
	req.Header.Set("Content-Type", "application/json")
	if authToken != "" {
		req.Header.Set("Authorization", "Bearer "+authToken)
	}

	if err != nil {
		fmt.Printf("Error creating request: %v\n", err)
		return nil
	}

	if requestBody != "" {
		req.Body = io.NopCloser(strings.NewReader(requestBody))
	}

	resp, err := http.DefaultClient.Do(req)
	if err != nil {
		fmt.Printf("Error sending request: %v\n", err)
		return nil
	}

	// Check status code
	if resp.StatusCode != expectedStatus {
		fmt.Printf("Unexpected status code: got %d, expected %d\n", resp.StatusCode, expectedStatus)
		return nil
	}

	// Read response body
	if expectedBody != nil {
		actualBody := parseBody(resp)

		for k, v := range expectedBody {
			if fmt.Sprintf("%v", v) != fmt.Sprintf("%v", actualBody[k]) {
				fmt.Printf("JSON response does not match the expected result.\n")
				fmt.Printf("Expected: %v, Actual: %v\n", v, actualBody[k])
				return nil
			}
		}
	}

	return resp
}

func noCacheChecker(resp *http.Response) bool {
	value := resp.Header.Get("Cache-Control")
	if value == "" {
		fmt.Printf("Cache-Control header not found.")
		return true
	}

	// value to lower case
	// expected value is no-cache, no cache
	// not exact match is fine
	value = strings.ToLower(value)
	if !strings.Contains(value, "no-cache") && !strings.Contains(value, "no cache") {
		fmt.Printf("Cache-Control header does not contain 'no-cache' or 'no cache'.")
		return true
	}

	return false
}

func check2A() bool {
	errorFlag := false
	fmt.Printf("[TEST] - 2A: [GET] /v1/healthcheck... \n")
	resp := endpointChecker("http://localhost:8080/v1/healthcheck", "GET", "", "", 200, nil)
	if resp == nil {
		errorFlag = true
	} else {
		errorFlag = noCacheChecker(resp) || errorFlag
	}

	if !errorFlag {
		fmt.Printf("2A test passed.\n")
	}

	return errorFlag
}

func check2B() bool {
	errorFlag := false
	fmt.Printf("[TEST] - 2B: [GET] /v1/healthcheck?test=test with parameter... \n")
	resp := endpointChecker("http://localhost:8080/v1/healthcheck?test=test", "GET", "", "", 400, nil)
	if resp == nil {
		errorFlag = true
	} else {
		errorFlag = noCacheChecker(resp) || errorFlag
	}

	if !errorFlag {
		fmt.Printf("2B test passed.\n")
	}

	return errorFlag
}

func check2C() bool {
	errorFlag := false
	fmt.Printf("[TEST] - 2C: /v1/healthcheck with invalid method... \n")
	resp := endpointChecker("http://localhost:8080/v1/healthcheck", "POST", "", "", 400, nil)
	if resp == nil {
		errorFlag = true
	} else {
		errorFlag = noCacheChecker(resp) || errorFlag
	}
	resp = endpointChecker("http://localhost:8080/v1/healthcheck", "PUT", "", "", 400, nil)
	if resp == nil {
		errorFlag = true
	} else {
		errorFlag = noCacheChecker(resp) || errorFlag
	}
	resp = endpointChecker("http://localhost:8080/v1/healthcheck", "DELETE", "", "", 400, nil)
	if resp == nil {
		errorFlag = true
	} else {
		errorFlag = noCacheChecker(resp) || errorFlag
	}
	resp = endpointChecker("http://localhost:8080/v1/healthcheck", "PATCH", "", "", 400, nil)
	if resp == nil {
		errorFlag = true
	} else {
		errorFlag = noCacheChecker(resp) || errorFlag
	}

	if !errorFlag {
		fmt.Printf("2C test passed.\n")
	}

	return errorFlag
}

func checkAssignment2() {
	errorFlag := false

	fmt.Printf("Checking Assignment 2...\n")

	errorFlag = check2A() || errorFlag
	errorFlag = check2B() || errorFlag
	errorFlag = check2C() || errorFlag

	if !errorFlag {
		fmt.Printf("All Assignment 2 tests passed.\n")
	}
}

func check3A() bool {
	errorFlag := false
	fmt.Printf("[TEST] - 3A: Check Database Connection with healthcheck endpoint \n")
	resp := endpointChecker("http://localhost:8080/v1/healthcheck", "GET", "", "", 200, nil)
	if resp == nil {
		errorFlag = true
	} else {
		errorFlag = noCacheChecker(resp) || errorFlag
	}

	exec.Command("bash", "-c", "sudo iptables -A OUTPUT -p tcp --dport 3306 -j DROP").Run()
	exec.Command("bash", "-c", "sleep 5").Run()

	resp = endpointChecker("http://localhost:8080/v1/healthcheck", "GET", "", "", 503, nil)
	if resp == nil {
		errorFlag = true
	} else {
		errorFlag = noCacheChecker(resp) || errorFlag
	}

	exec.Command("bash", "-c", "sudo iptables -D OUTPUT -p tcp --dport 3306 -j DROP").Run()
	exec.Command("bash", "-c", "sleep 5").Run()

	resp = endpointChecker("http://localhost:8080/v1/healthcheck", "GET", "", "", 200, nil)
	if resp == nil {
		errorFlag = true
	} else {
		errorFlag = noCacheChecker(resp) || errorFlag
	}

	if !errorFlag {
		fmt.Printf("3A test passed.\n")
	}

	return errorFlag
}

func check3B() bool {
	errorFlag := false
	fmt.Printf("[TEST] - 3B: Check [GET] /v1/movie/{id} endpoint ... \n")
	fmt.Printf("	THIS TEST DEPRECATED. DUE TO THE CHANGES IN ASSIGNMENT 4\n")

	return errorFlag
}

func check3C() bool {
	errorFlag := false
	fmt.Printf("[TEST] - 3C: Check [POST] /v1/movie endpoint ... \n")
	fmt.Printf("	THIS TEST DEPRECATED. DUE TO THE CHANGES IN ASSIGNMENT 4\n")

	return errorFlag
}

func checkAssignment3() {
	fmt.Printf("Checking Assignment 3...\n")
	errorFlag := check3A()
	errorFlag = check3B() || errorFlag
	errorFlag = check3C() || errorFlag

	if !errorFlag {
		fmt.Printf("All Assignment 3 tests passed.\n")
	}
}

func check4A() (string, bool) {
	// check register and login
	errorFlag := false
	email := fmt.Sprintf("testuser.%s@example.com", time.Now().Format("20060102150405"))
	fmt.Printf("[TEST] - 4A: Check [POST] /v1/register endpoint ... \n")
	resp := endpointChecker("http://localhost:8080/v1/register", "POST", "", fmt.Sprintf(`{"email":"%s", "password":"testpassword"}`, email), 201, nil)
	if resp == nil {
		errorFlag = true
	}

	fmt.Printf("[TEST] - 4A: Check [POST] /v1/login endpoint ... \n")
	resp = endpointChecker("http://localhost:8080/v1/login", "POST", "", fmt.Sprintf(`{"email":"%s", "password":"testpassword"}`, email), 200, nil)
	var kv map[string]interface{}
	if resp == nil {
		fmt.Printf("Login has no response body\n")
		errorFlag = true

		return "", errorFlag
	} else {
		// expect with token
		kv = parseBody(resp)
		if kv["token"] == nil {
			fmt.Printf("Token not found in response body\n")
			errorFlag = true

			return "", errorFlag
		}
	}

	if !errorFlag {
		fmt.Printf("4A test passed.\n")
	}

	return kv["token"].(string), errorFlag
}

func check4B(token string) bool {
	errorFlag := false
	fmt.Printf("[TEST] - 4B: Check [GET] /v1/movie/{id} endpoint ... \n")

	resp := endpointChecker("http://localhost:8080/v1/movie/1", "GET", token, "", 200, nil)
	if resp == nil {
		errorFlag = true
	} else {
		kv := parseBody(resp)
		if kv["movieId"] == nil || int(kv["movieId"].(float64)) != 1 {
			fmt.Printf("Expected movieId 1, Actual: %v\n", kv["movieId"])
			errorFlag = true
		}

		if kv["title"] == nil || kv["title"] != "Toy Story (1995)" {
			fmt.Printf("Expected title 'Toy Story (1995)', Actual: %v\n", kv["title"])
			errorFlag = true
		}

		if kv["genres"] == nil {
			fmt.Printf("Genres not found in response body\n")
			errorFlag = true
		}

		expectGenres := []string{"Adventure", "Animation", "Children", "Comedy", "Fantasy"}

		for _, v := range kv["genres"].([]interface{}) {
			if !slices.Contains(expectGenres, v.(string)) {
				fmt.Printf("Expected genre %s not found in response body\n", v)
				errorFlag = true
			}
		}
	}

	fmt.Printf("[TEST] - 4B: Check [GET] /v1/movie/{id} endpoint with invalid id... \n")
	resp = endpointChecker("http://localhost:8080/v1/movie/999999", "GET", token, "", 404, nil)
	if resp == nil {
		errorFlag = true
	}

	fmt.Printf("[TEST] - 4B: Check [GET] /v1/movie?id={id} endpoint ... \n")
	resp = endpointChecker("http://localhost:8080/v1/movie?id=1", "GET", token, "", 200, nil)
	if resp == nil {
		errorFlag = true
	} else {
		kv := parseBody(resp)
		if kv["movieId"] == nil || int(kv["movieId"].(float64)) != 1 {
			fmt.Printf("Expected movieId 1, Actual: %v\n", kv["movieId"])
			errorFlag = true
		}

		if kv["title"] == nil || kv["title"] != "Toy Story (1995)" {
			fmt.Printf("Expected title 'Toy Story (1995)', Actual: %v\n", kv["title"])
			errorFlag = true
		}

		if kv["genres"] == nil {
			fmt.Printf("Genres not found in response body\n")
			errorFlag = true
		}

		expectGenres := []string{"Adventure", "Animation", "Children", "Comedy", "Fantasy"}

		for _, v := range kv["genres"].([]interface{}) {
			if !slices.Contains(expectGenres, v.(string)) {
				fmt.Printf("Expected genre %s not found in response body\n", v)
				errorFlag = true
			}
		}
	}

	fmt.Printf("[TEST] - 4B: Check [GET] /v1/movie?id={id} endpoint with invalid id... \n")
	resp = endpointChecker("http://localhost:8080/v1/movie?id=999999", "GET", token, "", 404, map[string]interface{}{"error": "Movie not found"})
	if resp == nil {
		errorFlag = true
	}

	if !errorFlag {
		fmt.Printf("4B test passed.\n")
	}

	return errorFlag
}

func check4C(token string) bool {
	// [GET] /v1/rating/<movieId>
	errorFlag := false
	fmt.Printf("[TEST] - 4C: Check [GET] /v1/rating/{id} endpoint ... \n")
	resp := endpointChecker("http://localhost:8080/v1/rating/1", "GET", token, "", 200, nil)
	if resp == nil {
		errorFlag = true
	} else {
		kv := parseBody(resp)
		if kv["movieId"] == nil || int(kv["movieId"].(float64)) != 1 {
			fmt.Printf("Expected movieId 1, Actual: %v\n", kv["movieId"])
			errorFlag = true
		}

		if kv["average_rating"] == nil {
			fmt.Printf("\"average_rating\" not found in response body\n")
			errorFlag = true
		}
	}

	fmt.Printf("[TEST] - 4C: Check [GET] /v1/rating/{id} endpoint with invalid id... \n")
	resp = endpointChecker("http://localhost:8080/v1/rating/999999", "GET", token, "", 404, map[string]interface{}{"error": "No ratings found for this movie"})
	if resp == nil {
		errorFlag = true
	}

	if !errorFlag {
		fmt.Printf("4C test passed.\n")
	}

	return errorFlag
}

func check4D(token string) bool {
	// [GET] /v1/link/<movieId>
	errorFlag := false
	fmt.Printf("[TEST] - 4D: Check [GET] /v1/link/{id} endpoint ... \n")
	resp := endpointChecker("http://localhost:8080/v1/link/1", "GET", token, "", 200, nil)
	if resp == nil {
		errorFlag = true
	} else {
		kv := parseBody(resp)
		if kv["movieId"] == nil || int(kv["movieId"].(float64)) != 1 {
			fmt.Printf("Expected movieId 1, Actual: %v\n", kv["movieId"])
			errorFlag = true
		}

		if kv["imdbId"] == nil || kv["imdbId"] != "0114709" {
			fmt.Printf("Expected imdbId '0114709', Actual: %v\n", kv["imdbId"])
			errorFlag = true
		}

		if kv["tmdbId"] == nil || kv["tmdbId"] != "862" {
			fmt.Printf("Expected tmdbId '862', Actual: %v\n", kv["tmdbId"])
			errorFlag = true
		}
	}

	fmt.Printf("[TEST] - 4D: Check [GET] /v1/link/{id} endpoint with invalid id... \n")
	resp = endpointChecker("http://localhost:8080/v1/link/999999", "GET", token, "", 404, nil)
	if resp == nil {
		errorFlag = true
	}

	if !errorFlag {
		fmt.Printf("4D test passed.\n")
	}

	return errorFlag
}

func check4E() bool {
	// All endpoints should return 401 Unauthorized without token
	errorFlag := false
	fmt.Printf("[TEST] - 4E: Check [GET] /v1/movie/{id} endpoint without token... \n")
	if resp := endpointChecker("http://localhost:8080/v1/movie/1", "GET", "", "", 401, nil); resp == nil {
		errorFlag = true
	}

	fmt.Printf("[TEST] - 4E: Check [GET] /v1/movie?id={id} endpoint without token... \n")
	if resp := endpointChecker("http://localhost:8080/v1/movie?id=1", "GET", "", "", 401, nil); resp == nil {
		errorFlag = true
	}

	fmt.Printf("[TEST] - 4E: Check [GET] /v1/rating/{id} endpoint without token... \n")
	if resp := endpointChecker("http://localhost:8080/v1/rating/1", "GET", "", "", 401, nil); resp == nil {
		errorFlag = true
	}

	fmt.Printf("[TEST] - 4E: Check [GET] /v1/link/{id} endpoint without token... \n")
	if resp := endpointChecker("http://localhost:8080/v1/link/1", "GET", "", "", 401, nil); resp == nil {
		errorFlag = true
	}

	if !errorFlag {
		fmt.Printf("4E test passed.\n")
	}

	return errorFlag
}

func checkAssignment4() {
	fmt.Printf("Checking Assignment 4...\n")
	token, errorFlag := check4A()
	if token == "" {
		fmt.Printf("Following tests (4B, 4C, 4D) are dependent on the token. Skipping the tests.\n")
	} else {
		errorFlag = check4B(token) || errorFlag
		errorFlag = check4C(token) || errorFlag
		errorFlag = check4D(token) || errorFlag
	}
	errorFlag = check4E() || errorFlag

	if !errorFlag {
		fmt.Printf("All Assignment 4 tests passed.\n")
	}
}

func main() {
	checkAssignment2()
	checkAssignment3()
	checkAssignment4()
}
