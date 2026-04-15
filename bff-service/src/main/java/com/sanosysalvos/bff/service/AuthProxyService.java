package com.sanosysalvos.bff.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthProxyService {

    private final RestTemplate restTemplate;

    @Value("${services.auth-url}")
    private String authUrl;

    public ResponseEntity<Map> register(Map<String, Object> body) {
        return restTemplate.postForEntity(authUrl + "/api/auth/register", body, Map.class);
    }

    public ResponseEntity<Map> login(Map<String, Object> body) {
        return restTemplate.postForEntity(authUrl + "/api/auth/login", body, Map.class);
    }

    public ResponseEntity<Map> refresh(Map<String, Object> body) {
        return restTemplate.postForEntity(authUrl + "/api/auth/refresh", body, Map.class);
    }

    public ResponseEntity<Map> verifyEmail(String token) {
        return restTemplate.getForEntity(authUrl + "/api/auth/verify-email?token=" + token, Map.class);
    }
}
