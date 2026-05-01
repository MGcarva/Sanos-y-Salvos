package com.sanosysalvos.mascotas.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "especies")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Especie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 50, unique = true)
    private String nombre; // "PERRO", "GATO", "AVE", "CONEJO", "OTRO"
}
