package com.autorent.main.controller;

import com.autorent.main.model.DetalleMantenimiento;
import com.autorent.main.model.Mantenimiento;
import com.autorent.main.model.Vehiculo;
import com.autorent.main.repository.DetalleMantenimientoRepository;
import com.autorent.main.repository.MantenimientoRepository;
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

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    // Mostrar formulario
    @GetMapping("/registro")
    public String nuevoMantenimiento(Model model) {
        if (!model.containsAttribute("mantenimiento")) {
            model.addAttribute("mantenimiento", new Mantenimiento());
        }
        model.addAttribute("vehiculos", vehiculoRepository.findAll());
        model.addAttribute("propietarios", usuarioRepository.findAll());
        return "vehiculos/mantenimientos/registrarmantenimiento";
    }

    // Registrar mantenimiento
    @PostMapping("/registro")
    public String registrarMantenimiento(Mantenimiento mantenimiento, RedirectAttributes ra) {
        mantenimiento.setFecha(LocalDate.now());

        // Asignar propietario automáticamente según vehículo seleccionado
        if (mantenimiento.getVehiculo() != null) {
            Vehiculo vehiculo = vehiculoRepository.findById(mantenimiento.getVehiculo().getId()).orElse(null);
            if (vehiculo != null) {
                mantenimiento.setVehiculo(vehiculo);
                mantenimiento.setUsuario(vehiculo.getUsuario());
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

    // Endpoint AJAX para traer detalles según tipo
    @GetMapping("/detalles/{tipo}")
    @ResponseBody
    public ResponseEntity<List<DetalleMantenimiento>> obtenerDetallesPorTipo(@PathVariable String tipo) {
        List<DetalleMantenimiento> detalles = detalleRepository.findByTipoMantenimiento(tipo);
        return ResponseEntity.ok(detalles);
    }

    // Metodo para listar mantenimientos
    @GetMapping("/listarmantenimiento")
    public String listarMantenimientos(Model model) {
        // Aquí llamas al repositorio para traer los mantenimientos con sus relaciones
        List<Mantenimiento> lista = mantenimientoRepository.findAllWithRelations();

        // Agregas la lista al modelo para que Thymeleaf pueda usarla en la vista
        model.addAttribute("listaMantenimientos", lista);

        // Retornas el nombre de la plantilla HTML
        return "vehiculos/mantenimientos/listarmantenimiento";
    }

    // Endpoint AJAX para traer propietario según vehículo
    @GetMapping("/vehiculos/propietario/{vehiculoId}")
    @ResponseBody
    public Map<String, Object> obtenerPropietario(@PathVariable Integer vehiculoId) {
        Vehiculo v = vehiculoRepository.findById(vehiculoId).orElse(null);
        Map<String, Object> datos = new HashMap<>();

        if (v != null && v.getUsuario() != null) {
            datos.put("id", v.getUsuario().getId());
            datos.put("nombreCompleto",
                    v.getUsuario().getNombres() + " " + v.getUsuario().getApellidos());
        } else {
            datos.put("id", "");
            datos.put("nombreCompleto", "");
        }

        return datos;
    }

}
