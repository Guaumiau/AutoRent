package com.autorent.main.repository;

import com.autorent.main.model.Reserva;
import com.autorent.main.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Integer> {
    
    List<Reserva> findByUsuario(Usuario usuario);
    List<Reserva> findByUsuarioOrderByFechainicioDesc(Usuario usuario);
    List<Reserva> findByVehiculo_UsuarioOrderByFecharesDesc(Usuario propietario);

    // 1. MIS RESERVAS (Activas + Recientes)
    // Muestra si:
    // A) El estado NO es terminal (Pendiente, Confirmada, En Uso...)
    // B) O SI ES terminal, pero la fecha de finalización es POSTERIOR al "umbral" (hace poco)
    @Query("SELECT r FROM Reserva r WHERE r.usuario = :usuario AND " +
           "(r.estado NOT IN ('FINALIZADA', 'RECHAZADA', 'CANCELADA') " +
           "OR (r.fechafinalizacion > :umbral)) " +
           "ORDER BY r.fechainicio DESC")
    List<Reserva> findMisReservasVisibles(@Param("usuario") Usuario usuario, @Param("umbral") LocalDateTime umbral);

    // 2. HISTORIAL (Solo antiguas)
    // Muestra si es terminal Y la fecha de finalización es ANTERIOR al "umbral"
    @Query("SELECT r FROM Reserva r WHERE r.usuario = :usuario AND " +
           "r.fechafinalizacion <= :umbral " +
           "ORDER BY r.fechainicio DESC")
    List<Reserva> findMiHistorialAntiguo(@Param("usuario") Usuario usuario, @Param("umbral") LocalDateTime umbral);
}