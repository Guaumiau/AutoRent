package com.autorent.main.model;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "vehiculo")
public class Vehiculo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idveh")
    Integer id;

    @Column(name = "plaveh")
    String placa;

    @Column(name = "marveh")
    String marca;

    @Column(name = "modveh")
    String modelo;

    @Column(name = "anioveh")
    Integer anio;

    @Column(name = "colveh")
    String color;

    @Column(name = "kilometraje")
    Double kilometraje;

    @Column(name = "precioalquilo")
    Double precioalquilo;

    @Enumerated(EnumType.STRING)
    @Column(name = "estveh")
    private EstadoVehiculo estveh;

    @Column(name = "fecharegistro")
    LocalDate fecharegistro;

    @Column(name = "fotoveh")
    String foto;

    @Transient
    private MultipartFile archivoFoto;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "idprop")
    private Usuario usuario;
}
