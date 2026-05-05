package com.sanosysalvos.geo.listener;

import com.sanosysalvos.geo.domain.UbicacionReporte;
import com.sanosysalvos.geo.dto.GeoCompletadoEvent;
import com.sanosysalvos.geo.dto.ReporteNuevoEvent;
import com.sanosysalvos.geo.repository.UbicacionReporteRepository;
import com.sanosysalvos.geo.service.ClusteringService;
import com.sanosysalvos.geo.service.GeocodingService;
import io.awspring.cloud.sqs.annotation.SqsListener;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class GeolocalizacionListener {

    private final UbicacionReporteRepository repository;
    private final GeocodingService geocodingService;
    private final ClusteringService clusteringService;
    private final SqsTemplate sqsTemplate;

    @Value("${sqs.queue.geo-completados}")
    private String geoCompletadosUrl;

    @SqsListener("${sqs.queue.reportes-nuevos}")
    public void handleReporteNuevo(ReporteNuevoEvent event) {
        log.info("Evento recibido via SQS: reporte {} tipo {}", event.getReporteId(), event.getTipo());

        try {
            Double lat = event.getLat();
            Double lng = event.getLng();
            String direccionGeo = null;

            // Geocodificación inversa si tenemos coordenadas
            if (lat != null && lng != null) {
                direccionGeo = geocodingService.reverseGeocode(lat, lng);
            }
            // Geocodificación directa si tenemos dirección pero no coordenadas
            else if (event.getDireccion() != null && !event.getDireccion().isBlank()) {
                double[] coords = geocodingService.geocodeAddress(event.getDireccion());
                if (coords != null) {
                    lat = coords[0];
                    lng = coords[1];
                    direccionGeo = event.getDireccion();
                }
            }

            if (lat == null || lng == null) {
                log.warn("No se pudo determinar coordenadas para reporte {}", event.getReporteId());
                return;
            }

            UbicacionReporte ubicacion = UbicacionReporte.builder()
                    .reporteId(event.getReporteId())
                    .userId(event.getUserId())
                    .tipo(event.getTipo())
                    .lat(lat)
                    .lng(lng)
                    .direccion(event.getDireccion())
                    .direccionGeocodificada(direccionGeo)
                    .especie(event.getEspecie())
                    .raza(event.getRaza())
                    .color(event.getColor())
                    .tamano(event.getTamano())
                    .fotoUrl(event.getFotoUrl())
                    .geocodificado(true)
                    .build();

            ubicacion = repository.save(ubicacion);
            log.info("Ubicación guardada para reporte {}: ({}, {})", event.getReporteId(), lat, lng);

            // Re-cluster
            clusteringService.runDBSCAN();

            // Recargar con cluster actualizado
            ubicacion = repository.findByReporteId(event.getReporteId()).orElse(ubicacion);

            // Publicar evento para ms-coincidencias via SQS
            GeoCompletadoEvent geoEvent = GeoCompletadoEvent.builder()
                    .reporteId(ubicacion.getReporteId())
                    .userId(ubicacion.getUserId())
                    .tipo(ubicacion.getTipo())
                    .especie(ubicacion.getEspecie())
                    .raza(ubicacion.getRaza())
                    .color(ubicacion.getColor())
                    .tamano(ubicacion.getTamano())
                    .fotoUrl(ubicacion.getFotoUrl())
                    .lat(ubicacion.getLat())
                    .lng(ubicacion.getLng())
                    .direccion(ubicacion.getDireccionGeocodificada())
                    .clusterId(ubicacion.getClusterId())
                    .build();

            sqsTemplate.send(to -> to.queue(geoCompletadosUrl).payload(geoEvent));
            log.info("GeoCompletadoEvent publicado en SQS para reporte {}", event.getReporteId());

        } catch (Exception e) {
            log.error("Error procesando reporte {}: {}", event.getReporteId(), e.getMessage(), e);
            throw e;
        }
    }
}
