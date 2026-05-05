package com.sanosysalvos.mascotas.factory;

import com.sanosysalvos.mascotas.domain.*;
import com.sanosysalvos.mascotas.domain.Reporte.Tamano;
import com.sanosysalvos.mascotas.dto.ReporteRequestDTO;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class ReporteFactoryTest {

    private final ReporteFactory factory = new ReporteFactory();

    @Test
    void crear_perdido_returnsReportePerdido() {
        UUID userId = UUID.randomUUID();
        
        Especie especie = new Especie();
        especie.setId(1);
        especie.setNombre("Perro");
        
        Raza raza = new Raza();
        raza.setId(1);
        raza.setNombre("Husky");
        raza.setEspecie(especie);

        ReporteRequestDTO dto = ReporteRequestDTO.builder()
                .tipo("PERDIDO")
                .especieId(1)
                .razaId(1)
                .nombre("Luna")
                .color("Blanco y negro")
                .tamano(Tamano.GRANDE)
                .descripcion("Husky perdido en parque")
                .lat(4.711)
                .lng(-74.072)
                .direccion("Calle 123")
                .recompensa(BigDecimal.valueOf(200000))
                .build();

        Reporte result = factory.crear(dto, userId, especie, raza);

        assertThat(result).isInstanceOf(ReportePerdido.class);
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getEspecie().getNombre()).isEqualTo("Perro");
        assertThat(result.getRaza().getNombre()).isEqualTo("Husky");
        assertThat(result.getTipo()).isEqualTo("PERDIDO");
        assertThat(((ReportePerdido) result).getRecompensa()).isEqualTo(BigDecimal.valueOf(200000));
    }

    @Test
    void crear_encontrado_returnsReporteEncontrado() {
        UUID userId = UUID.randomUUID();
        
        Especie especie = new Especie();
        especie.setId(2);
        especie.setNombre("Gato");
        
        Raza raza = new Raza();
        raza.setId(2);
        raza.setNombre("Siamés");
        raza.setEspecie(especie);

        ReporteRequestDTO dto = ReporteRequestDTO.builder()
                .tipo("ENCONTRADO")
                .especieId(2)
                .razaId(2)
                .color("Crema")
                .tamano(Tamano.PEQUENO)
                .descripcion("Gato encontrado en calle")
                .lugarResguardo("Mi casa")
                .tieneCollar(true)
                .build();

        Reporte result = factory.crear(dto, userId, especie, raza);

        assertThat(result).isInstanceOf(ReporteEncontrado.class);
        assertThat(result.getTipo()).isEqualTo("ENCONTRADO");
        assertThat(((ReporteEncontrado) result).getLugarResguardo()).isEqualTo("Mi casa");
        assertThat(((ReporteEncontrado) result).isTieneCollar()).isTrue();
    }

    @Test
    void crear_invalidTipo_throwsException() {
        ReporteRequestDTO dto = ReporteRequestDTO.builder()
                .tipo("INVALIDO")
                .especieId(1)
                .descripcion("Test")
                .build();

        Especie especie = new Especie();
        especie.setId(1);
        especie.setNombre("Perro");
        
        Raza raza = new Raza();
        raza.setId(1);

        assertThatThrownBy(() -> factory.crear(dto, UUID.randomUUID(), especie, raza))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("inválido");
    }

    @Test
    void crear_perdido_caseInsensitive() {
        ReporteRequestDTO dto = ReporteRequestDTO.builder()
                .tipo("perdido")
                .especieId(1)
                .descripcion("Test")
                .build();

        Especie especie = new Especie();
        especie.setId(1);
        especie.setNombre("Perro");
        
        Raza raza = new Raza();
        raza.setId(1);

        Reporte result = factory.crear(dto, UUID.randomUUID(), especie, raza);

        assertThat(result).isInstanceOf(ReportePerdido.class);
    }
}
