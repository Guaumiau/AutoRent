package com.autorent.main.service;

import com.autorent.main.model.Usuario;
import com.autorent.main.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    // Listar todos los usuarios
    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    // Guardar (crear o actualizar)
    public Usuario guardar(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    // Buscar por ID
    public Usuario buscarPorId(Integer id) {
        return usuarioRepository.findById(id).orElse(null);
    }

    // Eliminar
    public void eliminar(Integer id) {
        usuarioRepository.deleteById(id);
    }
}
