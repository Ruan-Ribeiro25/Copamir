package com.vidaplus.controller;

import com.vidaplus.entity.Usuario;
import com.vidaplus.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*; // Importa PutMapping, DeleteMapping, PathVariable, etc.

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/teste/usuarios")
public class TesteApiController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    // 1. CRIAR (POST)
    @PostMapping("/criar")
    public Usuario criarUsuario(@RequestBody Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    // 2. LISTAR TODOS (GET)
    @GetMapping("/listar")
    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    // 3. PESQUISAR (GET)
    @GetMapping("/pesquisar")
    public List<Usuario> pesquisarPorNome(@RequestParam String nome) {
        List<Usuario> todos = usuarioRepository.findAll();
        if (nome == null) return todos;
        return todos.stream()
                .filter(u -> u.getNome() != null && 
                             u.getNome().toLowerCase().contains(nome.toLowerCase()))
                .collect(Collectors.toList());
    }

    // 4. ATUALIZAR (PUT) - [NOVO]
    @PutMapping("/atualizar/{id}")
    public Usuario atualizarUsuario(@PathVariable Long id, @RequestBody Usuario dadosNovos) {
        return usuarioRepository.findById(id).map(usuarioExistente -> {
            // Atualiza apenas os dados enviados
            if(dadosNovos.getNome() != null) usuarioExistente.setNome(dadosNovos.getNome());
            if(dadosNovos.getEmail() != null) usuarioExistente.setEmail(dadosNovos.getEmail());
            if(dadosNovos.getPerfil() != null) usuarioExistente.setPerfil(dadosNovos.getPerfil());
            if(dadosNovos.getCpf() != null) usuarioExistente.setCpf(dadosNovos.getCpf());
            // Salva as alterações
            return usuarioRepository.save(usuarioExistente);
        }).orElse(null); // Retorna nulo se não achar o ID
    }

    // 5. EXCLUIR (DELETE) - [NOVO]
    @DeleteMapping("/excluir/{id}")
    public String excluirUsuario(@PathVariable Long id) {
        if (usuarioRepository.existsById(id)) {
            usuarioRepository.deleteById(id);
            return "Usuário com ID " + id + " foi excluído com sucesso.";
        } else {
            return "Usuário não encontrado.";
        }
    }
}