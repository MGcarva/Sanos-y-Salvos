package com.sanosysalvos.bff.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeoProxyService {

    private final RestTemplate restTemplate;

    @Value("${services.geo-url}")
    private String geoUrl;

    public ResponseEntity<List> getHeatmap(String tipo) {
        String url = geoUrl + "/api/geo/heatmap";
        if (tipo != null && !tipo.isBlank()) {
            url += "?tipo=" + tipo;
        }
        return restTemplate.getForEntity(url, List.class);
    }

    public ResponseEntity<List> getNearby(Double lat, Double lng, Double radiusMeters) {
        return restTemplate.getForEntity(
                geoUrl + "/api/geo/nearby?lat=" + lat + "&lng=" + lng + "&radiusMeters=" + radiusMeters,
                List.class);
    }

    public ResponseEntity<List> getClusters() {
        return restTemplate.getForEntity(geoUrl + "/api/geo/clusters", List.class);
    }
}
