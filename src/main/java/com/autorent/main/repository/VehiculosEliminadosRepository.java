package com.autorent.main.repository;

import com.autorent.main.model.VehiculosEliminados;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VehiculosEliminadosRepository extends JpaRepository<VehiculosEliminados, Integer> {
}