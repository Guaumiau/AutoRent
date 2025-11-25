package com.autorent.main.repository;

import com.autorent.main.model.Reserva;
import com.autorent.main.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Integer> {
    
    List<Reserva> findByUsuario(Usuario usuario);
}