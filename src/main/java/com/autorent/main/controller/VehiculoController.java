package com.autorent.main.controller;

import com.autorent.main.model.EstadoVehiculo;
import com.autorent.main.model.Usuario;
import com.autorent.main.model.Vehiculo;
import com.autorent.main.model.VehiculosEliminados;
import com.autorent.main.repository.UsuarioRepository;
import com.autorent.main.repository.VehiculoRepository;
import com.autorent.main.repository.VehiculosEliminadosRepository;
import com.autorent.main.service.ApiFactiliza;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("vehiculos")
public class VehiculoController {

    @Autowired
    Cloudinary cloudinary;
    @Autowired
    VehiculoRepository vehiculoRepository;
    @Autowired
    UsuarioRepository usuarioRepository;
    @Autowired
    VehiculosEliminadosRepository vehiculosEliminadosRepository;
    @Autowired
    private ApiFactiliza apiFactiliza;

    @GetMapping("buscar")
    String buscarVehiculo(Model model)
    {
        return "vehiculos/buscarPlacaVehiculo";

    }

    @PostMapping("/buscar-y-cargar")
    public String buscarYcargarFormulario(@RequestParam String placa, Model model, RedirectAttributes redirectAttributes) {

        try {
            // ¡Esta línea ahora devuelve un objeto 'Vehiculo' listo!
            Vehiculo vehiculoParaForm = apiFactiliza.consultarPlacaFactiliza(placa);

            // Añade el objeto al modelo
            model.addAttribute("vehiculo", vehiculoParaForm);

            return "vehiculos/registrarVehiculo";

        } catch (Exception e) {
            // Si la API falla...
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/vehiculos/buscar";
        }
    }

    @PostMapping("registro")
    String registrarVehiculo(Model model, Vehiculo vehiculo, RedirectAttributes ra, Principal principal)
    {
        String email = principal.getName();
        Usuario usuario = usuarioRepository.findByEmail(email);
        vehiculo.setUsuario(usuario);

        vehiculo.setEstveh(EstadoVehiculo.DISPONIBLE);
        vehiculo.setFecharegistro(LocalDate.now());
        vehiculo.setUsuario(usuario);

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

            return "redirect:/vehiculos/registro";
        } catch (Exception e) {
            // Error: Capturar y añadir un mensaje de error
            ra.addFlashAttribute("error", "❌ Error inesperado al registrar el vehículo.");
            System.err.println("Error de BD: " + e.getMessage());

            // Añadir el objeto vehiculo de vuelta para rellenar el formulario
            ra.addFlashAttribute("vehiculo", vehiculo);

            return "redirect:/vehiculos/registro";
        }

        return "redirect:/vehiculos/lista";
    }

    @GetMapping("/lista")
    public String listarVehiculos(Model model, Principal principal) {

        String email = principal.getName();
        
        Usuario usuarioLogueado = usuarioRepository.findByEmail(email);

        List<Vehiculo> vehiculos = vehiculoRepository.findByUsuario(usuarioLogueado);

        model.addAttribute("listaVehiculos", vehiculos);

        return "vehiculos/listaVehiculos";
    }

    @PostMapping("/detalles/{id}") //Esto debe ejecutarse en el boton de Ver Detalles en listaVehiculo.html
    public String verDetallesVehiculo(@PathVariable Integer id, Model model, RedirectAttributes ra) {

        Vehiculo vehiculo = vehiculoRepository.findById(id).orElse(null);

        if (vehiculo == null) {
            ra.addFlashAttribute("error", "❌ Error inesperado al buscar el vehículo.");
            return "redirect:/vehiculos/lista";
        }

        model.addAttribute("vehiculo", vehiculo);

        return "vehiculos/detallesVehiculo";
    }

    @GetMapping("/confirmar/{id}") //Esto debe ejecutarse en el boton de eliminar en listaVehiculo.html
    public String confirmarEliminarVehiculo(@PathVariable Integer id, Model model, RedirectAttributes ra) {

        Vehiculo vehiculo = vehiculoRepository.findById(id).orElse(null);

        if (vehiculo == null) {
            ra.addFlashAttribute("error", "❌ No se encontró el vehículo.");
            return "redirect:/vehiculos/lista";
        }

        model.addAttribute("vehiculo", vehiculo);

        return "vehiculos/eliminarVehiculo";
    }

    @PostMapping("/eliminar/{id}") //Esto debe ejecutarse en el boton de eliminar en eliminarVehiculo.html
    @Transactional
    public String eliminarVehiculo(@PathVariable Integer id, @RequestParam("razon") String razon, RedirectAttributes ra) {
        try {
            Optional<Vehiculo> optionalVehiculo = vehiculoRepository.findById(id);
            if (optionalVehiculo.isPresent()) {
                Vehiculo vehiculo = optionalVehiculo.get();

                VehiculosEliminados registro = new VehiculosEliminados();
                registro.setVehiculo(vehiculo);
                registro.setUsuario(vehiculo.getUsuario());
                registro.setFecharegistro(LocalDate.now());
                registro.setRazon(razon);

                vehiculosEliminadosRepository.save(registro);

                vehiculo.setEstveh(EstadoVehiculo.ELIMINADO);
                vehiculoRepository.save(vehiculo);

                ra.addFlashAttribute("mensaje", "✅ Vehículo eliminado correctamente.");
            } else {
                ra.addFlashAttribute("error", "❌ Vehículo no encontrado.");
            }
        } catch (Exception e) {
            ra.addFlashAttribute("error", "❌ No se pudo eliminar el vehículo.");
        }
        return "redirect:/vehiculos/lista";
    }
    
    // REACTIVAR VEHÍCULO (De Mantenimiento -> Disponible)
    @GetMapping("/habilitar/{id}")
    public String habilitarVehiculo(@PathVariable Integer id, RedirectAttributes ra) {
        Optional<Vehiculo> vehiculoOpt = vehiculoRepository.findById(id);
        
        if (vehiculoOpt.isPresent()) {
            Vehiculo vehiculo = vehiculoOpt.get();
            
            // Solo si está en mantenimiento lo activamos
            if (vehiculo.getEstveh() == EstadoVehiculo.EN_MANTENIMIENTO) {
                vehiculo.setEstveh(EstadoVehiculo.DISPONIBLE);
                vehiculoRepository.save(vehiculo);
                ra.addFlashAttribute("mensaje", "✅ Vehículo habilitado y visible en el catálogo nuevamente.");
            }
        }
        return "redirect:/vehiculos/lista";
    }
}
