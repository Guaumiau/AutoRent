package com.autorent.main.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "vehiculos_eliminados")
public class VehiculosEliminados {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idveh", nullable = false)
    private Vehiculo vehiculo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idprop", nullable = false)
    private Usuario usuario;

    @Column(name = "fecharegistro", nullable = false)
    private LocalDate fecharegistro;

    @Column(name = "razon", nullable = false)
    private String razon;
}