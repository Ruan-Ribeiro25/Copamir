package com.vidaplus.controller;

import com.vidaplus.entity.Usuario;
import com.vidaplus.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/teste/usuarios")
public class TesteApiController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    // 1. CRIAR USUÁRIO (POST) - Versão Simplificada
    @PostMapping("/criar")
    public Usuario criarUsuario(@RequestBody Usuario usuario) {
        // Removemos os 'if' que estavam dando erro.
        // O banco vai salvar exatamente o que vier no JSON.
        return usuarioRepository.save(usuario);
    }

    // 2. LISTAR TODOS (GET)
    @GetMapping("/listar")
    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    // 3. PESQUISAR POR NOME (GET) - Versão com Filtro em Memória
    @GetMapping("/pesquisar")
    public List<Usuario> pesquisarPorNome(@RequestParam String nome) {
        List<Usuario> todos = usuarioRepository.findAll();
        
        if (nome == null) return todos;

        // Filtra na lista trazida do banco para evitar erro de método inexistente
        return todos.stream()
                .filter(u -> u.getNome() != null && 
                             u.getNome().toLowerCase().contains(nome.toLowerCase()))
                .collect(Collectors.toList());
    }
}