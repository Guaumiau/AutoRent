package com.autorent.main.repository;

import com.autorent.main.model.Mantenimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MantenimientoRepository extends JpaRepository<Mantenimiento, Integer> {

    @Query("SELECT m FROM Mantenimiento m " +
            "LEFT JOIN FETCH m.vehiculo v " +
            "LEFT JOIN FETCH v.propietario " +
            "LEFT JOIN FETCH m.detalle d")
    List<Mantenimiento> findAllWithRelations();
}