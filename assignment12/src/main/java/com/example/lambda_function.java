package com.example;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.io.*;
import java.util.Map;
import java.util.List;

import org.json.JSONObject;

public class lambda_function implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final String API_KEY = System.getenv("TMDB_API_KEY");
    private static final String EMAIL = System.getenv("LINK_EMAIL");
    private static final String PASSWORD = System.getenv("LINK_PASSWORD");
    private static final String EC2_IP = System.getenv("LINK_API_IP");

    private static final String BASE_URL = "http://" + EC2_IP + ":8080";
    private static final int TIMEOUT = 10000;
    
    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();

        System.out.println("Received event: " + event);
    
        Map<String, String> pathParams = event.getPathParameters();
        System.out.println("Path parameters: " + pathParams);
        
        if (pathParams == null || !pathParams.containsKey("movie_id")) {
            return generateErrorResponse(400, "Movie id is required!");
        }

        String movieIdString = pathParams.get("movie_id");

        if (movieIdString == null || movieIdString.isEmpty()) {
            return generateErrorResponse(400, "Movie id is required!");
        }

        int movieId;
        try {
            movieId = Integer.parseInt(movieIdString);
        } catch (NumberFormatException e) {
            return generateErrorResponse(400, "Invalid movie id!");
        }

        // if (movieId < 1 || movieId > 999999) {
        //     return generateErrorResponse(400, "Movie id is out of range!");
        // }
            
        try {
            System.out.println("Attempting login to: " + BASE_URL + "/v1/login");
            String token = loginAndGetToken();
            System.out.println("Token obtained: " + (token != null));
            System.out.println("Token obtained: " + token);
            if (token == null) {
                return generateErrorResponse(401, "Unable to login to Webapp!");
            }

            String tmdbId = fetchTmdbIdFromWebapp(movieId, token);
            System.out.println("TMDB ID from Link API: " + tmdbId);
            if (tmdbId == null) {
                return generateErrorResponse(404, "No tmdbId found for movie " + movieId + "!");
            }

            // String posterUrl = "https://example.com/posters/" + movieId + ".jpg";
            String posterPath = getPosterPathFromTMDB(tmdbId);
            System.out.println("Poster Path from TMDB: " + posterPath);
            if (posterPath == null) {
                return generateErrorResponse(404, "Poster not found in TMDB for tmdbId " + tmdbId + "!");
            }

            String fullPosterUrl = "https://image.tmdb.org/t/p/w500" + posterPath;
            String responseBody = String.format("{\"movie_id\": %d, \"poster_url\": \"%s\", \"poster_size\": 1024000, \"message\": \"post details fetched successfully\"}", movieId, fullPosterUrl);
            
            response.setStatusCode(200);
            response.setBody(responseBody);
            response.setHeaders(Map.of("Content-Type", "application/json"));
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return generateErrorResponse(500, "Internal server error: " + e.getMessage());
        }
    }

    private String loginAndGetToken() {
        try {         
            URI uri = new URI(BASE_URL + "/v1/login");
            URL url = uri.toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(TIMEOUT);
            conn.setReadTimeout(TIMEOUT);

            String jsonBody = String.format("{\"email\": \"%s\", \"password\": \"%s\"}", EMAIL, PASSWORD);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonBody.getBytes());
                os.flush();
            }

            int status = conn.getResponseCode();
            System.out.println("Login response status: " + status);
            if (status != 200) {
                System.err.println("Login failed, status: " + status);
                return null;
            }

            String response = readResponse(conn);
            return extractJsonField(response, "token");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } 
    }

    private String fetchTmdbIdFromWebapp(int movieId, String token) {
        try {
            URI uri = new URI(BASE_URL + "/v1/link/" + movieId);
            URL url = uri.toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            String authToken = "Bearer " + token;
            System.out.println("Sending request to: " + url);
            System.out.println("Authorization Header: " + authToken);

            conn.setRequestProperty("Authorization", authToken);
            conn.setConnectTimeout(TIMEOUT);
            conn.setReadTimeout(TIMEOUT);

            for (Map.Entry<String, List<String>> header : conn.getRequestProperties().entrySet()) {
                System.out.println("Request Header: " + header.getKey() + " = " + header.getValue());
            }

            int status = conn.getResponseCode();
            if (status != 200) {
                System.err.println("Link API failed, status: " + status);
                InputStream errorStream = conn.getErrorStream();
                if (errorStream != null) {
                    String errorResponse = new String(errorStream.readAllBytes(), StandardCharsets.UTF_8);
                    System.err.println("Error response: " + errorResponse);
                }
                return null;
            }

            String response = readResponse(conn);
            System.out.println("API Response: " + response);

            return extractJsonField(response, "tmdbId");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getPosterPathFromTMDB(String tmdbId) {
        try {
            String urlString = "https://api.themoviedb.org/3/movie/" + tmdbId + "?api_key=" + API_KEY;
            URI uri = new URI(urlString);
            URL url = uri.toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(TIMEOUT);
            conn.setReadTimeout(TIMEOUT);

            System.out.println("Response Headers:");
            Map<String, List<String>> headers = conn.getHeaderFields();
            for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
                System.out.println(entry.getKey() + ": " + entry.getValue());
            }

            int status = conn.getResponseCode();
            if (status != 200) {
                System.err.println("TMDB fetch failed, status: " + status);
                return null;
            }

            String response = readResponse(conn);
            return extractJsonField(response, "poster_path");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        } 
    }

    private String readResponse(HttpURLConnection conn) {
        try {
            BufferedReader in  = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder body = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                body.append(line);
            }
            return body.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String extractJsonField(String json, String key) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            return jsonObject.optString(key, null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private APIGatewayProxyResponseEvent generateErrorResponse(int statusCode, String errorMessage) {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setStatusCode(statusCode);
        response.setBody(String.format("{\"error\": \"%s\"}", errorMessage));
        response.setHeaders(Map.of("Content-Type", "application/json"));
        return response;
    }
}