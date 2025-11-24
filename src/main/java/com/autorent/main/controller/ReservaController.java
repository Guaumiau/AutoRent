package com.autorent.main.controller;

import com.autorent.main.model.EstadoVehiculo;
import com.autorent.main.model.Usuario;
import com.autorent.main.model.Vehiculo;
import com.autorent.main.repository.UsuarioRepository;
import com.autorent.main.repository.VehiculoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/reserva")
public class ReservaController {

    @Autowired
    private VehiculoRepository vehiculoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping("/catalogo")
    public String mostrarCatalogo(Model model, Principal principal) {
        List<Vehiculo> autosDisponibles = vehiculoRepository.findByEstveh(EstadoVehiculo.DISPONIBLE);
        
        model.addAttribute("vehiculos", autosDisponibles);

        if (principal != null) {
            String email = principal.getName();
            Usuario usuario = usuarioRepository.findByEmail(email);
            // Enviamos el ID del usuario logueado para comparar en el HTML
            model.addAttribute("idUsuarioLogueado", usuario.getId());
        } else {
            // Si es un invitado (no logueado), enviamos null
            model.addAttribute("idUsuarioLogueado", null);
        }
        
        return "reserva/catalogo";
    }

    @GetMapping("/crear/{id}")
    public String mostrarFormularioReserva(@PathVariable("id") Integer id, Model model) {
        
        Optional<Vehiculo> vehiculoOpt = vehiculoRepository.findById(id);

        if (vehiculoOpt.isPresent()) {
            model.addAttribute("vehiculo", vehiculoOpt.get());
            
            return "reserva/formulario_reserva"; 
        } else {
            // Si no existe el ID, redirigimos al catálogo con un error (opcional)
            return "redirect:/reserva/catalogo";
        }
    }
}