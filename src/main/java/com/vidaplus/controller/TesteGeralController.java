package com.vidaplus.controller;

import com.vidaplus.entity.*;
import com.vidaplus.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

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

    // --- HELPER DE VISUALIZAÇÃO ---
    private Map<String, Object> simplificar(Object obj) {
        Map<String, Object> map = new HashMap<>();
        if (obj == null) return map;
        
        try {
            if (obj instanceof Polo) {
                Polo p = (Polo) obj;
                map.put("id", p.getId());
                map.put("nome", p.getNome());
                map.put("cidade", p.getCidade());
                map.put("ativo", p.isAtivo());
            } else if (obj instanceof Produto) {
                Produto p = (Produto) obj;
                map.put("id", p.getId());
                map.put("nome", p.getNome());
            } else if (obj instanceof Ambulancia) {
                Ambulancia a = (Ambulancia) obj;
                map.put("id", a.getId());
                map.put("placa", a.getPlaca());
            } else if (obj instanceof Leito) {
                Leito l = (Leito) obj;
                map.put("id", l.getId());
                map.put("numero", l.getNumero());
                map.put("status", l.getStatus());
            } else if (obj instanceof Laboratorio) {
                Laboratorio l = (Laboratorio) obj;
                map.put("id", l.getId());
                map.put("exame", l.getNomeExame());
            } else if (obj instanceof TransacaoFinanceira) {
                TransacaoFinanceira f = (TransacaoFinanceira) obj;
                map.put("id", f.getId());
                map.put("descricao", f.getDescricao());
                map.put("valor", f.getValor());
            } else if (obj instanceof Log) {
                Log l = (Log) obj;
                map.put("id", l.getId());
                map.put("acao", l.getAcao());
            }
        } catch (Exception e) {
            map.put("erro_visualizacao", e.getMessage());
        }
        return map;
    }

    // ================== POLOS ==================
    @GetMapping("/polos/listar")
    public List<Map<String, Object>> listarPolos() { return poloRepo.findAll().stream().map(this::simplificar).collect(Collectors.toList()); }
    
    @PostMapping("/polos/criar")
    public Object criarPolo(@RequestBody Map<String, Object> dados) {
        try {
            Polo p = new Polo();
            if(dados.get("nome") != null) p.setNome((String) dados.get("nome"));
            if(dados.get("cidade") != null) p.setCidade((String) dados.get("cidade"));
            if(dados.get("ativo") != null) p.setAtivo((Boolean) dados.get("ativo"));
            return simplificar(poloRepo.save(p));
        } catch (Exception e) { return criarErro(e); }
    }
    
    @DeleteMapping("/polos/excluir/{id}")
    public String deletarPolo(@PathVariable Long id) { poloRepo.deleteById(id); return "Polo excluído"; }

    // ================== ESTOQUE ==================
    @GetMapping("/estoque/listar")
    public List<Map<String, Object>> listarEstoque() { return prodRepo.findAll().stream().map(this::simplificar).collect(Collectors.toList()); }
    
    @PostMapping("/estoque/criar")
    public Object criarProduto(@RequestBody Map<String, Object> dados) {
        try {
            Produto p = new Produto();
            if(dados.get("nome") != null) p.setNome((String) dados.get("nome"));
            if(dados.get("quantidade") != null) {
                p.setQuantidade(Integer.parseInt(dados.get("quantidade").toString()));
            }
            return simplificar(prodRepo.save(p));
        } catch (Exception e) { return criarErro(e); }
    }
    
    @DeleteMapping("/estoque/excluir/{id}")
    public String deletarProduto(@PathVariable Long id) { prodRepo.deleteById(id); return "Produto excluído"; }

    // ================== AMBULANCIAS ==================
    @GetMapping("/ambulancias/listar")
    public List<Map<String, Object>> listarAmbu() { return ambuRepo.findAll().stream().map(this::simplificar).collect(Collectors.toList()); }
    
    @PostMapping("/ambulancias/criar")
    public Object criarAmbu(@RequestBody Map<String, Object> dados) {
        try {
            Ambulancia a = new Ambulancia();
            if(dados.get("placa") != null) a.setPlaca((String) dados.get("placa"));
            return simplificar(ambuRepo.save(a));
        } catch (Exception e) { return criarErro(e); }
    }
    
    @DeleteMapping("/ambulancias/excluir/{id}")
    public String deletarAmbu(@PathVariable Long id) { ambuRepo.deleteById(id); return "Ambulância excluída"; }

    // ================== FINANCEIRO (CORRIGIDO / DESATIVADO) ==================
    @GetMapping("/financeiro/listar")
    public List<Map<String, Object>> listarFin() { return finRepo.findAll().stream().map(this::simplificar).collect(Collectors.toList()); }
    
    @PostMapping("/financeiro/criar")
    public Object criarFin(@RequestBody Map<String, Object> dados) {
        try {
            TransacaoFinanceira f = new TransacaoFinanceira();
            // --- COMENTADO: A CLASSE NÃO TEM SETTERS AINDA ---
            // if(dados.get("descricao") != null) f.setDescricao((String) dados.get("descricao"));
            // if(dados.get("valor") != null) f.setValor(Double.parseDouble(dados.get("valor").toString()));
            // --------------------------------------------------
            
            // Vai salvar vazio (ou dar erro de banco se for obrigatório, mas o Java compila)
            return simplificar(finRepo.save(f)); 
        } catch (Exception e) { return criarErro(e); }
    }
    
    @DeleteMapping("/financeiro/excluir/{id}")
    public String deletarFin(@PathVariable Long id) { finRepo.deleteById(id); return "Transação excluída"; }

    // ================== LEITOS ==================
    @GetMapping("/leitos/listar")
    public List<Map<String, Object>> listarLeitos() { return leitoRepo.findAll().stream().map(this::simplificar).collect(Collectors.toList()); }
    
    @PostMapping("/leitos/criar")
    public Object criarLeito(@RequestBody Map<String, Object> dados) {
        try {
            Leito l = new Leito();
            if(dados.get("numero") != null) l.setNumero((String) dados.get("numero"));
            if(dados.get("status") != null) l.setStatus((String) dados.get("status"));
            return simplificar(leitoRepo.save(l));
        } catch (Exception e) { return criarErro(e); }
    }
    
    @DeleteMapping("/leitos/excluir/{id}")
    public String deletarLeito(@PathVariable Long id) { leitoRepo.deleteById(id); return "Leito excluído"; }

    // ================== LABORATORIO ==================
    @GetMapping("/laboratorio/listar")
    public List<Map<String, Object>> listarLab() { return labRepo.findAll().stream().map(this::simplificar).collect(Collectors.toList()); }
    
    @PostMapping("/laboratorio/criar")
    public Object criarLab(@RequestBody Map<String, Object> dados) {
        try {
            Laboratorio l = new Laboratorio();
            if(dados.get("nomeExame") != null) l.setNomeExame((String) dados.get("nomeExame"));
            return simplificar(labRepo.save(l));
        } catch (Exception e) { return criarErro(e); }
    }
    
    @DeleteMapping("/laboratorio/excluir/{id}")
    public String deletarLab(@PathVariable Long id) { labRepo.deleteById(id); return "Exame excluído"; }
    
    // ================== LOGS ==================
    @GetMapping("/logs/listar")
    public List<Map<String, Object>> listarLogs() { return logRepo.findAll().stream().limit(50).map(this::simplificar).collect(Collectors.toList()); }
    
    @PostMapping("/logs/criar")
    public Object criarLog(@RequestBody Map<String, Object> dados) {
        try {
            Log l = new Log();
            String acao = (String) dados.get("acao");
            l.setAcao(acao != null ? acao : "Log Genérico");
            l.setDataHora(LocalDateTime.now());
            return simplificar(logRepo.save(l));
        } catch (Exception e) { return criarErro(e); }
    }
    
    @DeleteMapping("/logs/excluir/{id}")
    public String deletarLog(@PathVariable Long id) { logRepo.deleteById(id); return "Log excluído"; }
    @DeleteMapping("/logs/limpar-tudo")
    public String limparTodosLogs() { logRepo.deleteAll(); return "Todos os logs foram apagados!"; }

    // --- HELPER DE ERRO ---
    private Map<String, String> criarErro(Exception e) {
        Map<String, String> erro = new HashMap<>();
        erro.put("status", "erro");
        erro.put("mensagem", "Ocorreu um erro no servidor. Verifique o console.");
        erro.put("detalhe", e.getMessage());
        e.printStackTrace();
        return erro;
    }
}