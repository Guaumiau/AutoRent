package com.autorent.main.controller;

import com.autorent.main.model.Propietario;
import com.autorent.main.model.Vehiculo;
import com.autorent.main.repository.PropietarioRepository;
import com.autorent.main.repository.VehiculoRepository;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("vehiculos")
public class VehiculoController {

    @Autowired
    Cloudinary cloudinary;
    @Autowired
    VehiculoRepository vehiculoRepository;
    @Autowired
    PropietarioRepository propietarioRepository;

    @GetMapping("registro")
    String nuevoVehiculo(Model model)
    {
        if (!model.containsAttribute("vehiculo")) {
            model.addAttribute("vehiculo", new Vehiculo());
        }
        return "vehiculos/registrarVehiculo";

    }

    @PostMapping("registro")
    String registrarVehiculo(Model model, Vehiculo vehiculo, RedirectAttributes ra)
    {
        //temporalmente hasta desarrollar el modulo de usuarios, se trabajara con el propietario 1
        Propietario propietarioPorDefecto = propietarioRepository.getReferenceById(1);

        vehiculo.setFecharegistro(LocalDate.now());
        vehiculo.setPropietario(propietarioPorDefecto);

        MultipartFile archivo = vehiculo.getArchivoFoto();

        // Verifica que se haya subido un archivo
        if (!archivo.isEmpty()) {
            try {
                // Sube el archivo a Cloudinary
                Map uploadResult = cloudinary.uploader().upload(
                        archivo.getBytes(), // El contenido binario del archivo
                        ObjectUtils.asMap(
                                "folder", "autoventas_vehiculos" // Opcional: define una carpeta en Cloudinary
                        )
                );

                // Obtiene la URL pública del resultado
                String urlFoto = uploadResult.get("secure_url").toString();

                // Establece la URL en el campo 'foto'
                vehiculo.setFoto(urlFoto);

            } catch (Exception e) {
                // Manejo de errores de Cloudinary (ej. clave incorrecta, fallo de red)
                System.err.println("Error al subir a Cloudinary: " + e.getMessage());
            }
        }

        try {
            // Guardar en la BD
            vehiculoRepository.save(vehiculo);

            // Éxito: Añadir un mensaje flash
            ra.addFlashAttribute("mensaje", "✅ ¡Vehículo registrado con éxito!");
        } catch (org.springframework.dao.DataIntegrityViolationException e) {

            String errorMessage = "❌ Error: La placa " + vehiculo.getPlaca() + " ya está registrada.";
            ra.addFlashAttribute("error", errorMessage);
            System.err.println("Error de Integridad de Datos: " + e.getMessage());

            ra.addFlashAttribute("vehiculo", vehiculo);
        } catch (Exception e) {
            // Error: Capturar y añadir un mensaje de error
            ra.addFlashAttribute("error", "❌ Error inesperado al registrar el vehículo.");
            System.err.println("Error de BD: " + e.getMessage());

            // Añadir el objeto vehiculo de vuelta para rellenar el formulario
            ra.addFlashAttribute("vehiculo", vehiculo);
        }

        return "redirect:/vehiculos/registro";
    }

    @GetMapping("/lista")
    public String listarVehiculos(Model model) {

        List<Vehiculo> vehiculos = vehiculoRepository.findAll();

        model.addAttribute("listaVehiculos", vehiculos);

        return "vehiculos/listaVehiculos";
    }

    @PostMapping("/detalles") //Esto debe ejecutarse en el boton de Ver Detalles en listaVehiculo.html
    public String verDetallesVehiculo(@PathVariable Integer id, Model model, RedirectAttributes ra) {

        Vehiculo vehiculo = vehiculoRepository.findById(id).orElse(null);

        if (vehiculo == null) {
            ra.addFlashAttribute("error", "❌ Error inesperado al buscar el vehículo.");
            return "redirect:/vehiculos/lista";
        }

        model.addAttribute("vehiculo", vehiculo);

        return "vehiculos/detallesVehiculo";
    }

    @PostMapping("/confirmar") //Esto debe ejecutarse en el boton de eliminar en listaVehiculo.html
    public String confirmarEliminarVehiculo(@PathVariable Integer id, Model model, RedirectAttributes ra) {

        Vehiculo vehiculo = vehiculoRepository.findById(id).orElse(null);

        if (vehiculo == null) {
            ra.addFlashAttribute("error", "❌ No se encontró el vehículo.");
            return "redirect:/vehiculos/lista";
        }

        model.addAttribute("vehiculo", vehiculo);

        return "vehiculos/eliminarVehiculo";
    }

    @PostMapping("/eliminar") //Esto debe ejecutarse en el boton de eliminar en eliminarVehiculo.html
    public String eliminarVehiculo(@RequestParam Integer id, RedirectAttributes ra) {
        try {
            vehiculoRepository.deleteById(id);
            ra.addFlashAttribute("mensaje", "✅ Vehículo eliminado correctamente.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "❌ No se pudo eliminar el vehículo.");
        }
        return "redirect:/vehiculos/lista";
    }
}
