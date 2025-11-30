package com.autorent.main.repository;

import com.autorent.main.model.EstadoVehiculo;
import com.autorent.main.model.Usuario;
import com.autorent.main.model.Vehiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehiculoRepository extends JpaRepository<Vehiculo, Integer> {
    List<Vehiculo> findByEstveh(EstadoVehiculo estveh);
    List<Vehiculo> findByUsuario(Usuario usuario);
}
