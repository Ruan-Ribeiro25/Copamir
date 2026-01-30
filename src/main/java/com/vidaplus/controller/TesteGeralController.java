package com.vidaplus.controller;

import com.vidaplus.entity.*;
import com.vidaplus.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
// Import de Data removido para evitar erros

@RestController
@RequestMapping("/api/teste")
public class TesteGeralController {

    @Autowired private PoloRepository poloRepo;
    @Autowired private LogRepository logRepo;
    @Autowired private AmbulanciaRepository ambuRepo;
    @Autowired private LeitoRepository leitoRepo;      
    @Autowired private LaboratorioRepository labRepo;  
    @Autowired private TransacaoFinanceiraRepository finRepo; 
    @Autowired private ProdutoRepository prodRepo;

    // --- HELPER (MODO ULTRA SEGURO) ---
    private Map<String, Object> simplificar(Object obj) {
        Map<String, Object> map = new HashMap<>();
        if (obj == null) return map;
        
        // Polos
        if (obj instanceof Polo) {
            Polo p = (Polo) obj;
            map.put("id", p.getId());
            map.put("nome", p.getNome());
            map.put("cidade", p.getCidade());
            map.put("ativo", p.isAtivo());
        } 
        // Produtos
        else if (obj instanceof Produto) {
            Produto p = (Produto) obj;
            map.put("id", p.getId());
            map.put("nome", p.getNome());
            map.put("quantidade", p.getQuantidade());
        } 
        // Ambulancias
        else if (obj instanceof Ambulancia) {
            Ambulancia a = (Ambulancia) obj;
            map.put("id", a.getId());
            map.put("placa", a.getPlaca());
            map.put("modelo", a.getModelo());
        } 
        // Leitos
        else if (obj instanceof Leito) {
            Leito l = (Leito) obj;
            map.put("id", l.getId());
            map.put("numero", l.getNumero());
            map.put("status", l.getStatus());
        } 
        // Laboratorios (SEM getStatus)
        else if (obj instanceof Laboratorio) {
            Laboratorio l = (Laboratorio) obj;
            map.put("id", l.getId());
            map.put("exame", l.getNomeExame());
        } 
        // Financeiro
        else if (obj instanceof TransacaoFinanceira) {
            TransacaoFinanceira f = (TransacaoFinanceira) obj;
            map.put("id", f.getId());
            map.put("descricao", f.getDescricao());
            map.put("valor", f.getValor());
            map.put("tipo", f.getTipo());
        } 
        // Logs (SEM DATA para garantir que compila)
        else if (obj instanceof Log) {
            Log l = (Log) obj;
            map.put("id", l.getId());
            map.put("acao", l.getAcao());
            // map.put("data", l.getDataHora()); // Removido por segurança
        }
        return map;
    }

    // ================== MÉTODOS ==================
    
    // POLOS
    @GetMapping("/polos/listar")
    public List<Map<String, Object>> listarPolos() { return poloRepo.findAll().stream().map(this::simplificar).collect(Collectors.toList()); }
    @PostMapping("/polos/criar")
    public Map<String, Object> criarPolo(@RequestBody Polo p) { return simplificar(poloRepo.save(p)); }
    @DeleteMapping("/polos/excluir/{id}")
    public String deletarPolo(@PathVariable Long id) { poloRepo.deleteById(id); return "Polo excluído"; }

    // ESTOQUE
    @GetMapping("/estoque/listar")
    public List<Map<String, Object>> listarEstoque() { return prodRepo.findAll().stream().map(this::simplificar).collect(Collectors.toList()); }
    @PostMapping("/estoque/criar")
    public Map<String, Object> criarProduto(@RequestBody Produto p) { return simplificar(prodRepo.save(p)); }
    @DeleteMapping("/estoque/excluir/{id}")
    public String deletarProduto(@PathVariable Long id) { prodRepo.deleteById(id); return "Produto excluído"; }

    // AMBULANCIAS
    @GetMapping("/ambulancias/listar")
    public List<Map<String, Object>> listarAmbu() { return ambuRepo.findAll().stream().map(this::simplificar).collect(Collectors.toList()); }
    @PostMapping("/ambulancias/criar")
    public Map<String, Object> criarAmbu(@RequestBody Ambulancia a) { return simplificar(ambuRepo.save(a)); }
    @DeleteMapping("/ambulancias/excluir/{id}")
    public String deletarAmbu(@PathVariable Long id) { ambuRepo.deleteById(id); return "Ambulância excluída"; }

    // FINANCEIRO
    @GetMapping("/financeiro/listar")
    public List<Map<String, Object>> listarFin() { return finRepo.findAll().stream().map(this::simplificar).collect(Collectors.toList()); }
    @PostMapping("/financeiro/criar")
    public Map<String, Object> criarFin(@RequestBody TransacaoFinanceira f) { return simplificar(finRepo.save(f)); }
    @DeleteMapping("/financeiro/excluir/{id}")
    public String deletarFin(@PathVariable Long id) { finRepo.deleteById(id); return "Transação excluída"; }

    // LEITOS
    @GetMapping("/leitos/listar")
    public List<Map<String, Object>> listarLeitos() { return leitoRepo.findAll().stream().map(this::simplificar).collect(Collectors.toList()); }
    @PostMapping("/leitos/criar")
    public Map<String, Object> criarLeito(@RequestBody Leito l) { return simplificar(leitoRepo.save(l)); }
    @DeleteMapping("/leitos/excluir/{id}")
    public String deletarLeito(@PathVariable Long id) { leitoRepo.deleteById(id); return "Leito excluído"; }

    // LABORATORIO
    @GetMapping("/laboratorio/listar")
    public List<Map<String, Object>> listarLab() { return labRepo.findAll().stream().map(this::simplificar).collect(Collectors.toList()); }
    @PostMapping("/laboratorio/criar")
    public Map<String, Object> criarLab(@RequestBody Laboratorio l) { return simplificar(labRepo.save(l)); }
    @DeleteMapping("/laboratorio/excluir/{id}")
    public String deletarLab(@PathVariable Long id) { labRepo.deleteById(id); return "Exame excluído"; }
    
    // LOGS (Simplificado ao Máximo)
    @GetMapping("/logs/listar")
    public List<Map<String, Object>> listarLogs() { return logRepo.findAll().stream().limit(50).map(this::simplificar).collect(Collectors.toList()); }
    
    @PostMapping("/logs/criar")
    public Map<String, Object> criarLog(@RequestBody Log l) {
        // Não tentamos setar data, deixamos o banco decidir ou ficar null
        return simplificar(logRepo.save(l));
    }
    
    @DeleteMapping("/logs/excluir/{id}")
    public String deletarLog(@PathVariable Long id) { logRepo.deleteById(id); return "Log excluído"; }
    @DeleteMapping("/logs/limpar-tudo")
    public String limparTodosLogs() { logRepo.deleteAll(); return "Todos os logs foram apagados!"; }
}