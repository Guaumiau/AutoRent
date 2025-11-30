package com.autorent.main.controller;

import com.autorent.main.model.EstadoReserva;
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
            
            // 3. CÁLCULO DE DÍAS (Lógica "Fracción cuenta como día")
            
            // Calculamos los minutos totales de la reserva
            long minutosTotales = ChronoUnit.MINUTES.between(fechaInicio, fechaFin);
            
            // Un día tiene 1440 minutos (24 * 60)
            // Dividimos y usamos Math.ceil para redondear hacia ARRIBA
            // Ejemplo: 26 horas = 1560 min / 1440 = 1.08 -> Redondea a 2.0
            long dias = (long) Math.ceil(minutosTotales / 1440.0);

            // Si reserva y devuelve el mismo día, cobramos 1 día mínimo
            if (dias < 1) dias = 1;
            
            double costoTotal = dias * vehiculo.getPrecioalquilo();

            // 4. Crear el objeto Reserva
            Reserva reserva = new Reserva();
            reserva.setFechares(LocalDateTime.now());

            reserva.setEstado(EstadoReserva.PENDIENTE);
            
            reserva.setFechainicio(fechaInicio);
            reserva.setFechafin(fechaFin);
            reserva.setCosto((int) costoTotal);
            
            // Relaciones
            reserva.setUsuario(cliente);
            reserva.setVehiculo(vehiculo);

            // 5. Guardar en BD
            reservaRepository.save(reserva);

            // 6. Actualizamos el estado del Vehículo
            vehiculo.setEstveh(EstadoVehiculo.PENDIENTE);
            vehiculoRepository.save(vehiculo);

            // 7. Mensaje de Éxito
            redirectAttributes.addFlashAttribute("mensaje", "✅ ¡Reserva exitosa! Total a pagar: S/. " + costoTotal);
            
            // Redirigir al Dashboard del Cliente (donde pondremos la lista de "Mis Reservas")
            return "redirect:/reserva/catalogo"; 
        }

        redirectAttributes.addFlashAttribute("error", "Error: Vehículo no encontrado.");
        return "redirect:/reserva/catalogo";
    }

    @GetMapping("/mis-reservas")
    public String misReservas(Model model, Principal principal) {
        
        String email = principal.getName();
        Usuario usuarioLogueado = usuarioRepository.findByEmail(email);

        LocalDateTime haceUnDia = LocalDateTime.now().minusDays(1);

        List<Reserva> misReservas = reservaRepository.findMisReservasVisibles(usuarioLogueado, haceUnDia);

        model.addAttribute("reservas", misReservas);
        
        return "reserva/mis_reservas"; // Nombre del HTML
    }

    @GetMapping("/cancelar/{id}")
    public String cancelarReserva(@PathVariable Integer id, Principal principal, RedirectAttributes ra) {
        
        // 1. Buscar la reserva
        Optional<Reserva> reservaOpt = reservaRepository.findById(id);

        if (reservaOpt.isPresent()) {
            Reserva reserva = reservaOpt.get();
            
            // 2. VALIDACIÓN DE SEGURIDAD: ¿La reserva es mía?
            String email = principal.getName();
            if (!reserva.getUsuario().getEmail().equals(email)) {
                ra.addFlashAttribute("error", "❌ No tienes permiso para cancelar esta reserva.");
                return "redirect:/reserva/mis-reservas";
            }

            // 3. VALIDACIÓN DE ESTADO: ¿Se puede cancelar?
            // Solo permitimos cancelar si está Pendiente o Confirmada
            if (reserva.getEstado() == EstadoReserva.PENDIENTE || reserva.getEstado() == EstadoReserva.CONFIRMADA) {
                
                // A. Cambiar estado de la reserva
                reserva.setEstado(EstadoReserva.CANCELADA);
                reserva.setFechafinalizacion(LocalDateTime.now());
                reservaRepository.save(reserva);

                // B. LIBERAR EL VEHÍCULO (¡Muy Importante!)
                Vehiculo vehiculo = reserva.getVehiculo();
                vehiculo.setEstveh(EstadoVehiculo.DISPONIBLE);
                vehiculoRepository.save(vehiculo);

                ra.addFlashAttribute("mensaje", "✅ Reserva cancelada correctamente.");
            } else {
                ra.addFlashAttribute("error", "❌ No se puede cancelar una reserva que ya está en uso o finalizada.");
            }
        }

        return "redirect:/reserva/mis-reservas";
    }

    // 1. CLIENTE CONFIRMA RECEPCIÓN
    // De ESPERANDO_CLIENTE -> EN_USO
    @GetMapping("/confirmar-entrega/{id}")
    public String confirmarRecepcion(@PathVariable Integer id, RedirectAttributes ra) {
        Optional<Reserva> reservaOpt = reservaRepository.findById(id);
        if (reservaOpt.isPresent()) {
            Reserva reserva = reservaOpt.get();
            if (reserva.getEstado() == EstadoReserva.ESPERANDO_CLIENTE) {
                reserva.setEstado(EstadoReserva.EN_USO);
                reservaRepository.save(reserva);
                ra.addFlashAttribute("mensaje", "🚗 ¡Disfruta tu viaje! El alquiler ha comenzado oficialmente.");
            }
        }
        return "redirect:/reserva/mis-reservas";
    }

    // 2. CLIENTE INICIA DEVOLUCIÓN
    // De EN_USO -> ESPERANDO_PROPIETARIO
    @GetMapping("/devolver/{id}")
    public String devolverAuto(@PathVariable Integer id, RedirectAttributes ra) {
        Optional<Reserva> reservaOpt = reservaRepository.findById(id);
        if (reservaOpt.isPresent()) {
            Reserva reserva = reservaOpt.get();
            if (reserva.getEstado() == EstadoReserva.EN_USO) {
                reserva.setEstado(EstadoReserva.ESPERANDO_PROPIETARIO);
                reservaRepository.save(reserva);
                ra.addFlashAttribute("mensaje", "✅ Has marcado la devolución. Espera a que el propietario confirme.");
            }
        }
        return "redirect:/reserva/mis-reservas";
    }

    @GetMapping("/historial")
    public String historial(Model model, Principal principal) {
        String email = principal.getName();
        Usuario usuario = usuarioRepository.findByEmail(email);

        // Calculamos el umbral
        LocalDateTime haceUnDia = LocalDateTime.now().minusDays(1);

        // Buscamos solo las que murieron hace más de 1 día
        List<Reserva> historial = reservaRepository.findMiHistorialAntiguo(usuario, haceUnDia);
        
        model.addAttribute("reservas", historial);
        return "reserva/historial";
    }
}