package com.sanosysalvos.mascotas.service;

import com.sanosysalvos.mascotas.domain.Reporte;
import com.sanosysalvos.mascotas.events.ReporteNuevoEvent;
import io.awspring.cloud.sqs.operations.SqsOperations;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventPublisher {

    private final SqsOperations sqsTemplate;

    @Value("${sqs.queue.reportes-nuevos}")
    private String reportesNuevosUrl;

    public void publishReporteNuevo(Reporte reporte) {
        try {
            ReporteNuevoEvent event = ReporteNuevoEvent.builder()
                    .reporteId(reporte.getId())
                    .userId(reporte.getUserId())
                    .tipo(reporte.getTipo())
                    .especie(reporte.getEspecie() != null ? reporte.getEspecie().getNombre() : null)
                    .raza(reporte.getRaza() != null ? reporte.getRaza().getNombre() : null)
                    .color(reporte.getColor())
                    .tamano(reporte.getTamano() != null ? reporte.getTamano().name() : null)
                    .fotoUrl(reporte.getFotoUrl())
                    .lat(reporte.getLat())
                    .lng(reporte.getLng())
                    .direccion(reporte.getDireccion())
                    .build();

            sqsTemplate.send(reportesNuevosUrl, event);
            log.info("Evento ReporteNuevo publicado en SQS para reporte {}", reporte.getId());
        } catch (Exception e) {
            log.error("Error publicando evento para reporte {}: {}", reporte.getId(), e.getMessage());
        }
    }
}
