package com.autorent.main.controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.autorent.main.model.EstadoReserva;
import com.autorent.main.model.EstadoVehiculo;
import com.autorent.main.model.Reserva;
import com.autorent.main.model.Usuario;
import com.autorent.main.model.Vehiculo;
import com.autorent.main.repository.ReservaRepository;
import com.autorent.main.repository.UsuarioRepository;
import com.autorent.main.repository.VehiculoRepository;

@Controller
@RequestMapping("/propietario")
public class PropietarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private VehiculoRepository vehiculoRepository;

    @GetMapping("/dashboard")
    public String dashboardPropietario(Model model, Principal principal) {
        
        String email = principal.getName();
    
        Usuario usuario = usuarioRepository.findByEmail(email);

        model.addAttribute("datosUsuario", usuario);
        
        return "propietario/dashboard";
    }

    // 1. LISTAR SOLICITUDES
    @GetMapping("/solicitudes")
    public String verSolicitudes(Model model, Principal principal) {
        String email = principal.getName();
        Usuario propietario = usuarioRepository.findByEmail(email);

        List<Reserva> solicitudes = reservaRepository.findByVehiculo_UsuarioOrderByFecharesDesc(propietario);
        model.addAttribute("solicitudes", solicitudes);
        
        // 👇 NUEVO: Enviamos la hora actual al HTML
        model.addAttribute("ahora", LocalDateTime.now());
        
        return "propietario/solicitudes";
    }

    // 2. ACEPTAR RESERVA
    @GetMapping("/reserva/aceptar/{id}")
    public String aceptarReserva(@PathVariable Integer id, RedirectAttributes ra) {
        Optional<Reserva> reservaOpt = reservaRepository.findById(id);
        
        if (reservaOpt.isPresent()) {
            Reserva reserva = reservaOpt.get();
            // Cambiamos estado de la reserva
            reserva.setEstado(EstadoReserva.CONFIRMADA);
            reservaRepository.save(reserva);
            
            // El vehículo ya estaba PENDIENTE, ahora lo pasamos a NO_DISPONIBLE (Ocupado)
            Vehiculo vehiculo = reserva.getVehiculo();
            vehiculo.setEstveh(EstadoVehiculo.NO_DISPONIBLE);
            vehiculoRepository.save(vehiculo);

            ra.addFlashAttribute("mensaje", "✅ Reserva CONFIRMADA. El vehículo se ha marcado como ocupado.");
        }
        return "redirect:/propietario/solicitudes";
    }

    // 3. RECHAZAR RESERVA
    @GetMapping("/reserva/rechazar/{id}")
    public String rechazarReserva(@PathVariable Integer id, RedirectAttributes ra) {
        Optional<Reserva> reservaOpt = reservaRepository.findById(id);

        if (reservaOpt.isPresent()) {
            Reserva reserva = reservaOpt.get();
            
            // Cambiamos estado de la reserva
            reserva.setEstado(EstadoReserva.RECHAZADA);
            reserva.setFechafinalizacion(LocalDateTime.now());
            reservaRepository.save(reserva);

            // ¡IMPORTANTE! Liberamos el vehículo para que vuelva al catálogo
            Vehiculo vehiculo = reserva.getVehiculo();
            vehiculo.setEstveh(EstadoVehiculo.DISPONIBLE);
            vehiculoRepository.save(vehiculo);

            ra.addFlashAttribute("error", "❌ Reserva RECHAZADA. El vehículo vuelve a estar disponible.");
        }
        return "redirect:/propietario/solicitudes";
    }

    // 1. PROPIETARIO INICIA ENTREGA
    // De CONFIRMADA -> ESPERANDO_CLIENTE
    @GetMapping("/reserva/entregar/{id}")
    public String iniciarEntrega(@PathVariable Integer id, RedirectAttributes ra) {
        Optional<Reserva> reservaOpt = reservaRepository.findById(id);
        if (reservaOpt.isPresent()) {
            Reserva reserva = reservaOpt.get();
            if (reserva.getEstado() == EstadoReserva.CONFIRMADA) {
                reserva.setEstado(EstadoReserva.ESPERANDO_CLIENTE); // <--- Cambio
                reservaRepository.save(reserva);
                ra.addFlashAttribute("mensaje", "✅ Has marcado el vehículo como entregado. Esperando confirmación del cliente.");
            }
        }
        return "redirect:/propietario/solicitudes";
    }

    // 2. PROPIETARIO CONFIRMA DEVOLUCIÓN FINAL
    // De ESPERANDO_PROPIETARIO -> FINALIZADA
    @GetMapping("/reserva/finalizar/{id}")
    public String confirmarDevolucion(@PathVariable Integer id, RedirectAttributes ra) {
        Optional<Reserva> reservaOpt = reservaRepository.findById(id);
        if (reservaOpt.isPresent()) {
            Reserva reserva = reservaOpt.get();
            // Solo puede finalizar si el cliente ya dijo "Ya lo devolví"
            if (reserva.getEstado() == EstadoReserva.ESPERANDO_PROPIETARIO) { 
                
                reserva.setEstado(EstadoReserva.FINALIZADA);
                reserva.setFechafinalizacion(LocalDateTime.now());
                reservaRepository.save(reserva);

                Vehiculo vehiculo = reserva.getVehiculo();
                vehiculo.setEstveh(EstadoVehiculo.DISPONIBLE);
                vehiculoRepository.save(vehiculo);

                ra.addFlashAttribute("mensaje", "🏁 ¡Alquiler finalizado correctamente!");
            }
        }
        return "redirect:/propietario/solicitudes";
    }
}
