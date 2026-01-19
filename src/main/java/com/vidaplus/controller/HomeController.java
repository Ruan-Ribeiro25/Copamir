package com.vidaplus.controller;

import com.vidaplus.entity.Usuario;
import com.vidaplus.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class HomeController {

    @Autowired private UsuarioRepository usuarioRepository;

    // Redireciona a raiz para o login
    @GetMapping("/")
    public String root() {
        return "redirect:/login";
    }

    @GetMapping("/home")
    public String home(Model model, Principal principal) {
        // 1. Verificação de Segurança
        // Como a rota /home está liberada no SecurityConfig (permitAll),
        // precisamos garantir manualmente que o usuário esteja logado para ver o painel.
        if (principal == null) return "redirect:/login";

        // 2. Carrega apenas o Usuário (para exibir "Olá, Nome")
        Usuario usuario = usuarioRepository.findByUsernameOrCpf(principal.getName());
        
        // Proteção extra caso o usuário não seja encontrado no banco
        if (usuario == null) return "redirect:/login?error=user_sync";
        
        model.addAttribute("usuario", usuario);

        // 3. Retorna a view correta
        // CORREÇÃO: Apontando para "pages/home" pois o arquivo deve estar na subpasta 'pages'
        return "pages/home"; 
    }
}