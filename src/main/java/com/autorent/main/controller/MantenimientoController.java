package com.autorent.main.controller;

import com.autorent.main.model.DetalleMantenimiento;
import com.autorent.main.model.Mantenimiento;
import com.autorent.main.model.Reserva;
import com.autorent.main.model.Usuario; // Importar Usuario
import com.autorent.main.model.Vehiculo;
import com.autorent.main.repository.DetalleMantenimientoRepository;
import com.autorent.main.repository.MantenimientoRepository;
import com.autorent.main.repository.ReservaRepository;
import com.autorent.main.repository.UsuarioRepository;
import com.autorent.main.repository.VehiculoRepository;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal; // Importar Principal
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/vehiculos/mantenimientos")
public class MantenimientoController {

    @Autowired
    private Cloudinary cloudinary;

    @Autowired
    private MantenimientoRepository mantenimientoRepository;

    @Autowired
    private VehiculoRepository vehiculoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private DetalleMantenimientoRepository detalleRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    // Mostrar formulario (FILTRADO POR DUEÑO)
    @GetMapping("/registro")
    public String nuevoMantenimiento(Model model, Principal principal) { // Agregamos Principal
        
        if (!model.containsAttribute("mantenimiento")) {
            model.addAttribute("mantenimiento", new Mantenimiento());
        }

        // 1. Obtener Usuario Logueado
        String email = principal.getName();
        Usuario usuario = usuarioRepository.findByEmail(email);

        // 2. Cargar SOLO los vehículos de este propietario
        List<Vehiculo> misVehiculos = vehiculoRepository.findByUsuario(usuario);
        
        model.addAttribute("vehiculos", misVehiculos);
        
        model.addAttribute("usuarioLogueado", usuario);

        return "vehiculos/mantenimientos/registrarMantenimiento";
    }

    // Registrar mantenimiento
    @PostMapping("/registro")
    public String registrarMantenimiento(Mantenimiento mantenimiento, 
        @RequestParam(value = "reserva", required = false) Integer idReserva, RedirectAttributes ra, Principal principal) {
        
        if (mantenimiento.getFecha().isAfter(LocalDate.now())) {
            ra.addFlashAttribute("error", "❌ La fecha de mantenimiento no puede ser futura.");
            // Devolvemos el objeto para que no tenga que escribir todo de nuevo
            ra.addFlashAttribute("mantenimiento", mantenimiento); 
            return "redirect:/vehiculos/mantenimientos/registro";
        }

        // 1. Obtener Usuario Logueado
        String email = principal.getName();
        Usuario usuario = usuarioRepository.findByEmail(email);

        // 2. Asignar propietario y vehículo
        if (mantenimiento.getVehiculo() != null) {
            Vehiculo vehiculo = vehiculoRepository.findById(mantenimiento.getVehiculo().getId()).orElse(null);
            
            // Validación de seguridad: ¿El auto realmente es mío?
            if (vehiculo != null && vehiculo.getUsuario().getId().equals(usuario.getId())) {
                mantenimiento.setVehiculo(vehiculo);
                mantenimiento.setUsuario(usuario); // El usuario logueado es el dueño
            } else {
                ra.addFlashAttribute("error", "❌ No puedes registrar mantenimiento a un vehículo ajeno.");
                return "redirect:/vehiculos/mantenimientos/listarmantenimiento";
            }
        }

        if (idReserva != null) {
            Optional<Reserva> reservaOpt = reservaRepository.findById(idReserva);
            if (reservaOpt.isPresent()) {
                mantenimiento.setReserva(reservaOpt.get());
            }
        }

        MultipartFile archivo = mantenimiento.getArchivoFoto();
        if (archivo != null && !archivo.isEmpty()) {
            try {
                Map uploadResult = cloudinary.uploader().upload(
                        archivo.getBytes(),
                        ObjectUtils.asMap("folder", "autorent_mantenimientos"));
                String urlFoto = uploadResult.get("secure_url").toString();
                mantenimiento.setFoto(urlFoto);
            } catch (Exception e) {
                System.err.println("Error al subir a Cloudinary: " + e.getMessage());
            }
        }

        try {
            mantenimientoRepository.save(mantenimiento);
            ra.addFlashAttribute("mensaje", "✅ ¡Mantenimiento registrado con éxito!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "❌ Error al registrar el mantenimiento.");
            System.err.println("Error al guardar: " + e.getMessage());

            ra.addFlashAttribute("mantenimiento", mantenimiento);
            return "redirect:/vehiculos/mantenimientos/registro";
        }

        return "redirect:/vehiculos/mantenimientos/listarmantenimiento";
    }

    // Metodo para listar mantenimientos (FILTRADO)
    @GetMapping("/listarmantenimiento")
    public String listarMantenimientos(Model model, Principal principal) {
        
        // 1. Obtener Usuario Logueado
        String email = principal.getName();
        Usuario usuario = usuarioRepository.findByEmail(email);

        // 2. Buscar SOLO los mantenimientos de este usuario
        // Necesitas crear este método en tu Repositorio: findByUsuario(usuario)
        List<Mantenimiento> lista = mantenimientoRepository.findByUsuario(usuario);

        model.addAttribute("listaMantenimientos", lista);

        return "vehiculos/mantenimientos/listarMantenimiento";
    }

    // Endpoint AJAX para traer detalles según tipo
    @GetMapping("/detalles/{tipo}")
    @ResponseBody
    public ResponseEntity<List<DetalleMantenimiento>> obtenerDetallesPorTipo(@PathVariable String tipo) {
        List<DetalleMantenimiento> detalles = detalleRepository.findByTipoMantenimiento(tipo);
        return ResponseEntity.ok(detalles);
    }
    
    @GetMapping("/ultima-reserva/{idVehiculo}")
    @ResponseBody
    public Map<String, Object> obtenerUltimaReserva(@PathVariable Integer idVehiculo) {
        Map<String, Object> respuesta = new HashMap<>();
        
        Optional<Reserva> reservaOpt = reservaRepository.findUltimaReservaFinalizada(idVehiculo);
        
        if (reservaOpt.isPresent()) {
            Reserva r = reservaOpt.get();
            respuesta.put("encontrada", true);
            respuesta.put("id", r.getId());
            respuesta.put("descripcion", "Reserva #" + r.getId() + " - Finalizada el: " + r.getFechafinalizacion().toLocalDate());
        } else {
            respuesta.put("encontrada", false);
        }
        return respuesta;
    }
}