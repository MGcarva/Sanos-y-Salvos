package com.sanosysalvos.mascotas.repository;

import com.sanosysalvos.mascotas.domain.Especie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EspecieRepository extends JpaRepository<Especie, Integer> {

    Optional<Especie> findByNombreIgnoreCase(String nombre);
}
