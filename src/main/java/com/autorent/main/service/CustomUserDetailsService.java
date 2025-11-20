package com.autorent.main.service;

import com.autorent.main.model.Usuario;
import com.autorent.main.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        Usuario usuario = usuarioRepository.findByEmail(email);

        if (usuario == null) {
            throw new UsernameNotFoundException("Usuario no encontrado");
        }

        // rol es un enum → conviértelo a ROLE_...
        String rol = usuario.getRol().name().equalsIgnoreCase("cliente")
                ? "ROLE_USER"
                : "ROLE_PROP";

        return User.builder()
                .username(usuario.getEmail())      // ✔ getEmail()
                .password(usuario.getPassword())   // ✔ getPassword()
                .authorities(rol)
                .disabled(!usuario.getEstado())    // ✔ getEstado()
                .build();
    }
}