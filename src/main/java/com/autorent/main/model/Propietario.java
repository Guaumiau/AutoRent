package com.autorent.main.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Entity
@Table(name = "propietario")
public class Propietario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idprop")
    Integer id;

    @Column(name = "nomprop")
    String nombres;

    @Column(name = "apeprop")
    String apellidos;

    @Column(name = "dniprop")
    String dni;

    @Column(name = "emailprop")
    String email;

    @Column(name = "estprop")
    Boolean estado;

    @OneToMany(mappedBy = "propietario",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    private List<Vehiculo> vehiculos;
}
