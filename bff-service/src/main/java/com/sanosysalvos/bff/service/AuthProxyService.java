package com.sanosysalvos.bff.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthProxyService {

    private final RestTemplate restTemplate;

    @Value("${services.auth-url}")
    private String authUrl;

    @SuppressWarnings({"rawtypes", "unchecked"})
    public ResponseEntity<Map<String, Object>> register(Map<String, Object> body) {
        ResponseEntity<Map> r = restTemplate.postForEntity(authUrl + "/api/auth/register", body, Map.class);
        return ResponseEntity.status(r.getStatusCode()).body(r.getBody());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public ResponseEntity<Map<String, Object>> login(Map<String, Object> body) {
        ResponseEntity<Map> r = restTemplate.postForEntity(authUrl + "/api/auth/login", body, Map.class);
        return ResponseEntity.status(r.getStatusCode()).body(r.getBody());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public ResponseEntity<Map<String, Object>> refresh(Map<String, Object> body) {
        ResponseEntity<Map> r = restTemplate.postForEntity(authUrl + "/api/auth/refresh", body, Map.class);
        return ResponseEntity.status(r.getStatusCode()).body(r.getBody());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public ResponseEntity<Map<String, Object>> verifyEmail(String token) {
        ResponseEntity<Map> r = restTemplate.getForEntity(authUrl + "/api/auth/verify-email?token=" + token, Map.class);
        return ResponseEntity.status(r.getStatusCode()).body(r.getBody());
    }
}
