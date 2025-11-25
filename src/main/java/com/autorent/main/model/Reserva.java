package com.autorent.main.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "reserva")
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idres")
    private Integer id;

    // Fecha en que se hizo el registro (DEFAULT CURRENT_TIMESTAMP)
    @Column(name = "fechares")
    private LocalDateTime fechares;

    @Column(name = "fechainicio")
    private LocalDateTime fechainicio;

    @Column(name = "fechafin")
    private LocalDateTime fechafin;

    @Column(name = "costo")
    private Integer costo;

    // RELACIÓN CON USUARIO (Cliente)
    // En la BD es 'idcli', aquí traemos el objeto completo Usuario
    @ManyToOne(fetch = FetchType.EAGER) 
    @JoinColumn(name = "idcli")
    private Usuario usuario;

    // RELACIÓN CON VEHÍCULO
    // En la BD es 'idveh', aquí traemos el objeto completo Vehiculo
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "idveh")
    private Vehiculo vehiculo;
}