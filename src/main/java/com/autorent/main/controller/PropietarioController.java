package com.autorent.main.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.autorent.main.model.Usuario;
import com.autorent.main.repository.UsuarioRepository;

@Controller
public class PropietarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping("/propietario/dashboard")
    public String dashboardPropietario(Model model, Principal principal) {
        
        String email = principal.getName();
    
        Usuario usuario = usuarioRepository.findByEmail(email);

        model.addAttribute("datosUsuario", usuario);
        
        return "propietario/dashboard";
    }
}
