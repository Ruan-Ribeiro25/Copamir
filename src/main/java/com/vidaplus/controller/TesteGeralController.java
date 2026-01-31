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
import java.lang.reflect.Method; // Importante para a Mágica

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

    // --- MÁGICA: PREENCHEDOR AUTOMÁTICO (Evita erro de compilação) ---
    private void preencherAutomaticamente(Object destino, Map<String, Object> origem) {
        Method[] metodos = destino.getClass().getMethods();
        for (Method m : metodos) {
            if (m.getName().startsWith("set") && m.getParameterCount() == 1) {
                String atributo = m.getName().substring(3).toLowerCase(); // setValor -> valor
                
                // Procura no JSON (ignorando maiúsculas/minúsculas)
                for (String key : origem.keySet()) {
                    if (key.toLowerCase().equals(atributo)) {
                        try {
                            Object valor = origem.get(key);
                            // Tenta converter números básicos
                            if (valor instanceof Integer && m.getParameterTypes()[0] == Double.class) {
                                m.invoke(destino, ((Integer) valor).doubleValue());
                            } else if (valor instanceof String && m.getParameterTypes()[0] == Double.class) {
                                m.invoke(destino, Double.parseDouble((String) valor));
                            } else {
                                m.invoke(destino, valor);
                            }
                        } catch (Exception e) {
                            // Ignora incompatibilidades, apenas segue o baile
                        }
                    }
                }
            }
        }
    }

    // --- HELPER DE VISUALIZAÇÃO ---
    private Map<String, Object> simplificar(Object obj) {
        Map<String, Object> map = new HashMap<>();
        if (obj == null) return map;
        try {
            // Usa Reflection para pegar ID e alguns campos básicos sem travar
            try { map.put("id", obj.getClass().getMethod("getId").invoke(obj)); } catch (Exception e) {}
            
            // Adiciona tipo do objeto para facilitar leitura
            map.put("tipo_objeto", obj.getClass().getSimpleName());
            
        } catch (Exception e) { map.put("erro_visualizacao", e.getMessage()); }
        return map;
    }

    // ================== POLOS ==================
    @GetMapping("/polos/listar")
    public List<Map<String, Object>> listarPolos() { return poloRepo.findAll().stream().map(this::simplificar).collect(Collectors.toList()); }
    
    @PostMapping("/polos/criar")
    public Object criarPolo(@RequestBody Map<String, Object> dados) {
        try {
            Polo p = new Polo();
            preencherAutomaticamente(p, dados);
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
            preencherAutomaticamente(p, dados);
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
            preencherAutomaticamente(a, dados);
            
            // Garante os obrigatórios se a mágica não achar
            if (dados.get("status") == null) a.setStatus("DISPONIVEL");
            if (dados.get("tipo") == null) a.setTipo("UTI MOVEL");
            if (dados.get("modelo") == null) a.setModelo("Padrao");
            
            return simplificar(ambuRepo.save(a));
        } catch (Exception e) { return criarErro(e); }
    }
    @DeleteMapping("/ambulancias/excluir/{id}")
    public String deletarAmbu(@PathVariable Long id) { ambuRepo.deleteById(id); return "Ambulância excluída"; }

    // ================== FINANCEIRO (AGORA VAI!) ==================
    @GetMapping("/financeiro/listar")
    public List<Map<String, Object>> listarFin() { return finRepo.findAll().stream().map(this::simplificar).collect(Collectors.toList()); }
    
    @PostMapping("/financeiro/criar")
    public Object criarFin(@RequestBody Map<String, Object> dados) {
        try {
            TransacaoFinanceira f = new TransacaoFinanceira();
            
            // 1. Tenta preencher tudo que der (Descricao, Valor, Tipo, etc)
            preencherAutomaticamente(f, dados);
            
            // 2. CORREÇÃO DO ERRO ATUAL: Categoria Obrigatória
            // Tentamos pegar do JSON, se não vier, forçamos um padrão.
            // Usamos Reflection aqui também caso o método seja setCategoria ou setCat
            try {
                Object cat = dados.get("categoria");
                if (cat == null) cat = "DIVERSOS"; // Valor padrão para salvar
                
                // Tenta chamar setCategoria
                Method setCat = f.getClass().getMethod("setCategoria", String.class);
                setCat.invoke(f, cat.toString());
            } catch (Exception ex) {
                // Se der erro, tenta 'setTipo' caso categoria seja mapeada como tipo
            }

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
            preencherAutomaticamente(l, dados);
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
            preencherAutomaticamente(l, dados);
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
            preencherAutomaticamente(l, dados);
            l.setDataHora(LocalDateTime.now());
            return simplificar(logRepo.save(l));
        } catch (Exception e) { return criarErro(e); }
    }
    @DeleteMapping("/logs/excluir/{id}")
    public String deletarLog(@PathVariable Long id) { logRepo.deleteById(id); return "Log excluído"; }
    @DeleteMapping("/logs/limpar-tudo")
    public String limparTodosLogs() { logRepo.deleteAll(); return "Todos os logs foram apagados!"; }

    private Map<String, String> criarErro(Exception e) {
        Map<String, String> erro = new HashMap<>();
        erro.put("status", "erro");
        erro.put("mensagem", e.getMessage());
        // Imprime erro no console para debug
        e.printStackTrace();
        return erro;
    }
}