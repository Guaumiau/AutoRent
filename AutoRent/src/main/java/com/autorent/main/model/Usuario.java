package com.autorent.main.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
@Table(name = "usuario")
public class Usuario {
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

    @Column(name = "tipoUsuario")
    String tipoUsuario;

    @OneToMany(mappedBy = "usuario")
    private List<Vehiculo> vehiculos;
}
