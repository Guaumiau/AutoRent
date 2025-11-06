package com.autorent.main.model;

import jakarta.persistence.*;

@Entity
@Table(name = "detalle_mantenimiento")
public class DetalleMantenimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle")
    private Integer id;

    @Column(name = "tipo_mantenimiento", nullable = false, length = 50)
    private String tipoMantenimiento;

    @Column(name = "nombre_detalle", nullable = false, length = 255)
    private String nombreDetalle;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTipoMantenimiento() {
        return tipoMantenimiento;
    }

    public void setTipoMantenimiento(String tipoMantenimiento) {
        this.tipoMantenimiento = tipoMantenimiento;
    }

    public String getNombreDetalle() {
        return nombreDetalle;
    }

    public void setNombreDetalle(String nombreDetalle) {
        this.nombreDetalle = nombreDetalle;
    }
}
