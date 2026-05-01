package com.sanosysalvos.coincidencias.listener;

import com.sanosysalvos.coincidencias.domain.Coincidencia;
import com.sanosysalvos.coincidencias.dto.GeoCompletadoEvent;
import com.sanosysalvos.coincidencias.service.CoincidenciaService;
import io.awspring.cloud.sqs.annotation.SqsListener;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class CoincidenciasListener {

    private final CoincidenciaService coincidenciaService;
    private final SqsTemplate sqsTemplate;

    @Value("${sqs.queue.notificaciones:#{null}}")
    private String notificacionesUrl;

    @SqsListener("${sqs.queue.geo-completados}")
    public void handleGeoCompletado(GeoCompletadoEvent event) {
        log.info("Evento GeoCompletado recibido via SQS: reporte {} tipo {}",
                event.getReporteId(), event.getTipo());

        try {
            List<Coincidencia> matches = coincidenciaService.procesarEvento(event);

            for (Coincidencia match : matches) {
                match.setNotificado(true);

                // Publicar notificación a cola SQS (si está configurada)
                if (notificacionesUrl != null) {
                    sqsTemplate.send(to -> to.queue(notificacionesUrl).payload(Map.of(
                            "coincidenciaId", match.getId().toString(),
                            "reportePerdidoId", match.getReportePerdidoId().toString(),
                            "reporteEncontradoId", match.getReporteEncontradoId().toString(),
                            "scoreTotal", match.getScoreTotal(),
                            "distanciaKm", match.getDistanciaKm()
                    )));
                } else {
                    log.info("Coincidencia {} encontrada (score={}, distancia={}km) — notificación pendiente de configuración",
                            match.getId(), match.getScoreTotal(), match.getDistanciaKm());
                }
            }

            log.info("Procesamiento completado para reporte {}: {} coincidencias",
                    event.getReporteId(), matches.size());

        } catch (Exception e) {
            log.error("Error procesando GeoCompletado para reporte {}: {}", event.getReporteId(), e.getMessage(), e);
            throw e;
        }
    }
}
