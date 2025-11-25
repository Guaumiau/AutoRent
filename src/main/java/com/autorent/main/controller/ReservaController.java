package com.autorent.main.controller;

import com.autorent.main.model.EstadoVehiculo;
import com.autorent.main.model.Reserva;
import com.autorent.main.model.Usuario;
import com.autorent.main.model.Vehiculo;
import com.autorent.main.repository.ReservaRepository;
import com.autorent.main.repository.UsuarioRepository;
import com.autorent.main.repository.VehiculoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/reserva")
public class ReservaController {

    @Autowired
    private VehiculoRepository vehiculoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ReservaRepository reservaRepository;

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

    @PostMapping("/guardar")
    public String guardarReserva(
            @RequestParam("vehiculoId") Integer vehiculoId,
            @RequestParam("fechaInicio") LocalDateTime fechaInicio,
            @RequestParam("fechaFin") LocalDateTime fechaFin,
            Principal principal, // Para saber quién reserva
            RedirectAttributes redirectAttributes
    ) {
        // 1. Validaciones de Fecha
        if (fechaInicio.isBefore(LocalDateTime.now())) {
            redirectAttributes.addFlashAttribute("error", "❌ No puedes reservar en el pasado.");
            return "redirect:/reserva/crear/" + vehiculoId;
        }
        
        if (fechaFin.isBefore(fechaInicio)) {
            redirectAttributes.addFlashAttribute("error", "❌ La fecha final debe ser posterior a la inicial.");
            return "redirect:/reserva/crear/" + vehiculoId;
        }

        // 2. Obtener Usuario y Vehículo
        String email = principal.getName();
        Usuario cliente = usuarioRepository.findByEmail(email);
        
        Optional<Vehiculo> vehiculoOpt = vehiculoRepository.findById(vehiculoId);

        if (vehiculoOpt.isPresent()) {
            Vehiculo vehiculo = vehiculoOpt.get();

            // 3. Calcular Días y Costo
            long dias = ChronoUnit.DAYS.between(fechaInicio, fechaFin);
            
            // Si reserva y devuelve el mismo día, cobramos 1 día mínimo
            if (dias == 0) dias = 1; 
            
            double costoTotal = dias * vehiculo.getPrecioalquilo();

            // 4. Crear el objeto Reserva
            Reserva reserva = new Reserva();
            reserva.setFechares(LocalDateTime.now());
            reserva.setFechainicio(fechaInicio);
            reserva.setFechafin(fechaFin);
            reserva.setCosto((int) costoTotal);
            
            // Relaciones
            reserva.setUsuario(cliente);
            reserva.setVehiculo(vehiculo);

            // 5. Guardar en BD
            reservaRepository.save(reserva);

            // 6. Mensaje de Éxito
            redirectAttributes.addFlashAttribute("mensaje", "✅ ¡Reserva exitosa! Total a pagar: S/. " + costoTotal);
            
            // Redirigir al Dashboard del Cliente (donde pondremos la lista de "Mis Reservas")
            return "redirect:/reserva/catalogo"; 
        }

        redirectAttributes.addFlashAttribute("error", "Error: Vehículo no encontrado.");
        return "redirect:/reserva/catalogo";
    }
}