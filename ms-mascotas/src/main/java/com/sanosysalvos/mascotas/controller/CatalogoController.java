package com.sanosysalvos.mascotas.controller;

import com.sanosysalvos.mascotas.domain.Especie;
import com.sanosysalvos.mascotas.domain.Raza;
import com.sanosysalvos.mascotas.repository.EspecieRepository;
import com.sanosysalvos.mascotas.repository.RazaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mascotas")
@RequiredArgsConstructor
public class CatalogoController {

    private final EspecieRepository especieRepository;
    private final RazaRepository razaRepository;

    /**
     * GET /api/mascotas/especies
     * Devuelve la lista de todas las especies disponibles.
     * Acceso público (sin JWT).
     */
    @GetMapping("/especies")
    public ResponseEntity<List<Especie>> listarEspecies() {
        return ResponseEntity.ok(especieRepository.findAll());
    }

    /**
     * GET /api/mascotas/razas?especieId=1
     * Devuelve las razas de una especie específica.
     * Acceso público (sin JWT).
     */
    @GetMapping("/razas")
    public ResponseEntity<List<Raza>> listarRazas(
            @RequestParam(required = false) Integer especieId) {
        List<Raza> razas = especieId != null
                ? razaRepository.findByEspecieId(especieId)
                : razaRepository.findAll();
        return ResponseEntity.ok(razas);
    }
}
