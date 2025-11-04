package com.autorent.main.repository;

import com.autorent.main.model.DetalleMantenimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DetalleMantenimientoRepository extends JpaRepository<DetalleMantenimiento, Integer> {
    List<DetalleMantenimiento> findByTipoMantenimiento(String tipoMantenimiento);
}
