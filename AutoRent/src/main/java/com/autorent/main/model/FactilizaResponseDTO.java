package com.autorent.main.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true) // Ignora campos que no necesitamos
public class FactilizaResponseDTO {

    // Los nombres aquí deben coincidir EXACTAMENTE con el JSON de la API

    private String placa;
    private String marca;
    private String modelo;
    private String color;

    @JsonProperty("ano_fabricacion") // Mapea 'ano_fabricacion' del JSON a este campo
    private String anoFabricacion;

    @JsonProperty("ano_modelo") // Mapea 'ano_modelo' del JSON a este campo
    private String anoModelo;

    public String getAnio() {
        if (this.anoModelo != null && !this.anoModelo.isEmpty() && !this.anoModelo.equals("n/a")) {
            return this.anoModelo;
        }
        if (this.anoFabricacion != null && !this.anoFabricacion.isEmpty() && !this.anoFabricacion.equals("n/a")) {
            return this.anoFabricacion;
        }
        return null; // No se encontró año
    }
}