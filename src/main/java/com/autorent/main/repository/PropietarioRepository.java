package com.autorent.main.repository;

import com.autorent.main.model.Propietario;
import com.autorent.main.model.Vehiculo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PropietarioRepository extends JpaRepository<Propietario, Integer> {
}
