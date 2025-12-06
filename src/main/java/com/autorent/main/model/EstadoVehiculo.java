package com.autorent.main.model;

public enum EstadoVehiculo {
    DISPONIBLE,         // Visible en catálogo
    OCUPADO,            // Reservado o en viaje (No visible)
    EN_MANTENIMIENTO,   // Post-viaje o en taller (No visible)
    ELIMINADO           // Borrado lógico (No visible)
}
