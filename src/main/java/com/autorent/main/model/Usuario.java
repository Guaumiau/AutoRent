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
    private Integer id;

    @Column(name = "nomprop")
    private String nombres;

    @Column(name = "apeprop")
    private String apellidos;

    @Column(name = "dniprop")
    private String dni;

    @Column(name = "emailprop")
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "estprop")
    private Boolean estado;

    @Enumerated(EnumType.STRING)
    @Column(name = "rol")
    private Rol rol;

    @OneToMany(mappedBy = "usuario", fetch = FetchType.LAZY)
    private List<Vehiculo> vehiculos;
}
