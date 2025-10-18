package com.autorent.main.controller;

import com.autorent.main.model.Mantenimiento;
import com.autorent.main.model.Vehiculo;
import com.autorent.main.repository.MantenimientoRepository;
import com.autorent.main.repository.VehiculoRepository;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("vehiculos/mantenimientos")
public class MantenimientoController {

    @Autowired
    Cloudinary cloudinary;

    @Autowired
    MantenimientoRepository mantenimientoRepository;

    @Autowired
    VehiculoRepository vehiculoRepository;

    @GetMapping("registro")
    public String nuevoMantenimiento(Model model) {
        if (!model.containsAttribute("mantenimiento")) {
            model.addAttribute("mantenimiento", new Mantenimiento());
        }
        model.addAttribute("vehiculos", vehiculoRepository.findAll()); // Para seleccionar vehículo
        return "vehiculos/mantenimientos/registrarMantenimiento";
    }

    @PostMapping("registro")
    public String registrarMantenimiento(Mantenimiento mantenimiento, RedirectAttributes ra) {
        mantenimiento.setFecha(LocalDate.now());

        MultipartFile archivo = mantenimiento.getArchivoFoto();

        if (!archivo.isEmpty()) {
            try {
                Map uploadResult = cloudinary.uploader().upload(
                        archivo.getBytes(),
                        ObjectUtils.asMap(
                                "folder", "autoventas_vehiculos"
                        )
                );
                String urlFoto = uploadResult.get("secure_url").toString();
                mantenimiento.setFoto(urlFoto);
            } catch (Exception e) {
                System.err.println("Error al subir a Cloudinary: " + e.getMessage());
            }
        }

        try {
            mantenimientoRepository.save(mantenimiento);
            ra.addFlashAttribute("mensaje", "✅ ¡Mantenimiento registrado con éxito!");
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            String errorMessage = "❌ Error: El mantenimiento ya está registrado.";
            ra.addFlashAttribute("error", errorMessage);
            System.err.println("Error de Integridad de Datos: " + e.getMessage());
            ra.addFlashAttribute("mantenimiento", mantenimiento);
        } catch (Exception e) {
            ra.addFlashAttribute("error", "❌ Error inesperado al registrar el mantenimiento.");
            System.err.println("Error de BD: " + e.getMessage());
            ra.addFlashAttribute("mantenimiento", mantenimiento);
        }

        return "redirect:/vehiculos/mantenimientos/registro";
    }
}