package com.sanosysalvos.bff.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class MascotasProxyService {

    private final RestTemplate restTemplate;

    @Value("${services.mascotas-url}")
    private String mascotasUrl;

    @SuppressWarnings({"rawtypes", "unchecked"})
    public ResponseEntity<List<Object>> listarActivos() {
        ResponseEntity<List> r = restTemplate.getForEntity(mascotasUrl + "/api/reportes", List.class);
        return ResponseEntity.status(r.getStatusCode()).body(r.getBody());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public ResponseEntity<List<Object>> listarTodos() {
        ResponseEntity<List> r = restTemplate.getForEntity(mascotasUrl + "/api/reportes/todos", List.class);
        return ResponseEntity.status(r.getStatusCode()).body(r.getBody());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public ResponseEntity<Map<String, Object>> obtenerPorId(String id) {
        ResponseEntity<Map> r = restTemplate.getForEntity(mascotasUrl + "/api/reportes/" + id, Map.class);
        return ResponseEntity.status(r.getStatusCode()).body(r.getBody());
    }

    @SuppressWarnings({"rawtypes", "unchecked", "null"})
    public ResponseEntity<Map<String, Object>> crear(String reporteJson, MultipartFile foto, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.setBearerAuth(token);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        HttpHeaders jsonHeaders = new HttpHeaders();
        jsonHeaders.setContentType(MediaType.APPLICATION_JSON);
        body.add("reporte", new HttpEntity<>(reporteJson, jsonHeaders));

        if (foto != null && !foto.isEmpty()) {
            try {
                HttpHeaders fileHeaders = new HttpHeaders();
                fileHeaders.setContentType(MediaType.parseMediaType(
                        foto.getContentType() != null ? foto.getContentType() : "application/octet-stream"));
                ByteArrayResource resource = new ByteArrayResource(foto.getBytes()) {
                    @Override
                    public String getFilename() {
                        return foto.getOriginalFilename();
                    }
                };
                body.add("foto", new HttpEntity<>(resource, fileHeaders));
            } catch (Exception e) {
                log.error("Error procesando foto: {}", e.getMessage());
            }
        }

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<Map> r = restTemplate.postForEntity(mascotasUrl + "/api/reportes", requestEntity, Map.class);
        return ResponseEntity.status(r.getStatusCode()).body(r.getBody());
    }

    @SuppressWarnings({"rawtypes", "unchecked", "null"})
    public ResponseEntity<List<Object>> listarPorUsuario(String userId, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<?> entity = new HttpEntity<>(headers);
        ResponseEntity<List> r = restTemplate.exchange(mascotasUrl + "/api/reportes/usuario/" + userId, HttpMethod.GET, entity, List.class);
        return ResponseEntity.status(r.getStatusCode()).body(r.getBody());
    }

    @SuppressWarnings({"rawtypes", "unchecked", "null"})
    public ResponseEntity<Map<String, Object>> actualizarEstado(String id, String estado, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<?> entity = new HttpEntity<>(headers);
        ResponseEntity<Map> r = restTemplate.exchange(
                mascotasUrl + "/api/reportes/" + id + "/estado?estado=" + estado,
                HttpMethod.PATCH, entity, Map.class);
        return ResponseEntity.status(r.getStatusCode()).body(r.getBody());
    }
}
