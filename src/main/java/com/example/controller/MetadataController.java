package com.example.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import com.example.model.Metadata;

import jakarta.annotation.PostConstruct;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/v2")
public class MetadataController {
    private Metadata metadata;
    private String metadataToken;

    @PostConstruct
    public void init(){
        this.metadataToken = fetchMetadataToken();
        String instanceId = fetchMetadata("instance-id");
        String availabilityZone = fetchMetadata("placement/availability-zone");
        this.metadata = new Metadata(instanceId, availabilityZone);
    }

    @GetMapping("/metadata")
    public ResponseEntity<Map<String, String>> getMetadata() {
        Map<String, String> payload = Map.of(
            "aws_instance_id", metadata.getInstanceId(),
            "aws_availability_zone_id", metadata.getAvailabilityZone()
        );

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
        headers.add(HttpHeaders.PRAGMA, "no-cache");
        headers.add(HttpHeaders.CONTENT_TYPE, "application/json");
        headers.add("X-Content-Type-Options", "nosniff");

        return ResponseEntity.status(HttpStatus.OK)
                            .headers(headers)
                            .body(payload);
    }

    private String fetchMetadataToken() {
        try {
            URI uri = new URI("http://169.254.169.254/latest/api/token");
            HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
            conn.setRequestMethod("PUT");
            conn.setRequestProperty("X-aws-ec2-metadata-token-ttl-seconds", "21600");
            conn.setConnectTimeout(2000);
            conn.setReadTimeout(2000);
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String token = in.readLine();
            in.close();
            return token;
        } catch (Exception e) {
            return "Unavailable";
        }
    }

    private String fetchMetadata(String path) {
        try {
            URI uri = new URI("http://169.254.169.254/latest/meta-data/" + path);
            HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("X-aws-ec2-metadata-token", metadataToken);
            conn.setConnectTimeout(2000);
            conn.setReadTimeout(2000);
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String response = in.readLine();
            in.close();
            return response;
        } catch (Exception e) {
            return "Unavailable";
        }
    }
}
