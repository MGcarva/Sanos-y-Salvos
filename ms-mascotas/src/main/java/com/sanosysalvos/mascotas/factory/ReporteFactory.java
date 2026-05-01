package com.sanosysalvos.mascotas.factory;

import com.sanosysalvos.mascotas.domain.*;
import com.sanosysalvos.mascotas.dto.ReporteRequestDTO;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ReporteFactory {

    public Reporte crear(ReporteRequestDTO dto, UUID userId, Especie especie, Raza raza) {
        return switch (dto.getTipo().toUpperCase()) {
            case "PERDIDO" -> ReportePerdido.perdidoBuilder()
                    .userId(userId)
                    .especie(especie)
                    .raza(raza)
                    .nombre(dto.getNombre())
                    .color(dto.getColor())
                    .tamano(dto.getTamano())
                    .descripcion(dto.getDescripcion())
                    .lat(dto.getLat())
                    .lng(dto.getLng())
                    .direccion(dto.getDireccion())
                    .fechaEvento(dto.getFechaEvento())
                    .recompensa(dto.getRecompensa())
                    .build();
            case "ENCONTRADO" -> ReporteEncontrado.encontradoBuilder()
                    .userId(userId)
                    .especie(especie)
                    .raza(raza)
                    .nombre(dto.getNombre())
                    .color(dto.getColor())
                    .tamano(dto.getTamano())
                    .descripcion(dto.getDescripcion())
                    .lat(dto.getLat())
                    .lng(dto.getLng())
                    .direccion(dto.getDireccion())
                    .fechaEvento(dto.getFechaEvento())
                    .lugarResguardo(dto.getLugarResguardo())
                    .tieneCollar(dto.isTieneCollar())
                    .build();
            default -> throw new IllegalArgumentException("Tipo de reporte inválido: " + dto.getTipo());
        };
    }
}
