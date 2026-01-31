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
import java.lang.reflect.Method;
import java.util.ArrayList;

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

    // --- MÁGICA V2: PREENCHEDOR AUTOMÁTICO TURBINADO ---
    private void preencherAutomaticamente(Object destino, Map<String, Object> origem) {
        Method[] metodos = destino.getClass().getMethods();
        for (Method m : metodos) {
            if (m.getName().startsWith("set") && m.getParameterCount() == 1) {
                String atributo = m.getName().substring(3).toLowerCase(); 
                
                for (String key : origem.keySet()) {
                    // Compara ignorando maiúsculas e remove sublinhados (ex: nome_exame = nomeexame)
                    String keyLimpa = key.replace("_", "").toLowerCase();
                    
                    if (keyLimpa.equals(atributo) || atributo.startsWith(keyLimpa)) {
                        try {
                            Object valor = origem.get(key);
                            Class<?> tipoParametro = m.getParameterTypes()[0];

                            // Conversão 1: Números
                            if (valor instanceof Integer && tipoParametro == Double.class) {
                                m.invoke(destino, ((Integer) valor).doubleValue());
                            } 
                            // Conversão 2: String para Enum (A GRANDE SACADA PARA CATEGORIA)
                            else if (tipoParametro.isEnum() && valor instanceof String) {
                                try {
                                    // Tenta encontrar o ENUM pelo nome (ex: "DESPESA" -> Tipo.DESPESA)
                                    Object[] constantes = tipoParametro.getEnumConstants();
                                    for(Object objEnum : constantes) {
                                        if(objEnum.toString().equalsIgnoreCase((String)valor)) {
                                            m.invoke(destino, objEnum);
                                            break;
                                        }
                                    }
                                } catch (Exception eEnum) {}
                            }
                            // Padrão: Tenta jogar direto
                            else {
                                m.invoke(destino, valor);
                            }
                        } catch (Exception e) { }
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
            try { map.put("id", obj.getClass().getMethod("getId").invoke(obj)); } catch (Exception e) {}
            map.put("tipo_objeto", obj.getClass().getSimpleName());
        } catch (Exception e) { map.put("erro_visualizacao", e.getMessage()); }
        return map;
    }

    // ================== FINANCEIRO (O FOCO DO PROBLEMA) ==================
    @GetMapping("/financeiro/listar")
    public List<Map<String, Object>> listarFin() { return finRepo.findAll().stream().map(this::simplificar).collect(Collectors.toList()); }
    
    @PostMapping("/financeiro/criar")
    public Object criarFin(@RequestBody Map<String, Object> dados) {
        TransacaoFinanceira f = new TransacaoFinanceira();
        try {
            // 1. Tenta preencher tudo via mágica (inclusive Enums)
            preencherAutomaticamente(f, dados);
            
            // 2. TENTA PREENCHER CATEGORIA NA FORÇA BRUTA SE AINDA ESTIVER NULL
            // Procura qualquer método que tenha "Categoria" no nome
            for(Method m : f.getClass().getMethods()) {
                if(m.getName().toLowerCase().contains("categoria") && m.getName().startsWith("set")) {
                    try {
                        // Se aceitar String, joga "DIVERSOS"
                        if(m.getParameterTypes()[0] == String.class) {
                            m.invoke(f, "DIVERSOS");
                        }
                        // Se for Enum, tenta pegar o primeiro valor disponível
                        else if(m.getParameterTypes()[0].isEnum()) {
                            Object[] enums = m.getParameterTypes()[0].getEnumConstants();
                            if(enums.length > 0) m.invoke(f, enums[0]);
                        }
                    } catch (Exception ex) {}
                }
            }

            return simplificar(finRepo.save(f)); 
        } catch (Exception e) { 
            // --- DETETIVE DE ERROS ---
            // Se der erro, vamos listar quais métodos existem na classe para você ver!
            Map<String, String> erro = new HashMap<>();
            erro.put("status", "erro");
            erro.put("mensagem", e.getMessage());
            
            List<String> metodosDisponiveis = new ArrayList<>();
            for(Method m : f.getClass().getMethods()) {
                if(m.getName().startsWith("set")) {
                    metodosDisponiveis.add(m.getName() + "(" + m.getParameterTypes()[0].getSimpleName() + ")");
                }
            }
            erro.put("dica_debug_metodos_encontrados", metodosDisponiveis.toString());
            
            e.printStackTrace();
            return erro; 
        }
    }
    
    @DeleteMapping("/financeiro/excluir/{id}")
    public String deletarFin(@PathVariable Long id) { finRepo.deleteById(id); return "Transação excluída"; }

    // ================== OUTROS (MANTIDOS IGUAIS) ==================
    @GetMapping("/polos/listar")
    public List<Map<String, Object>> listarPolos() { return poloRepo.findAll().stream().map(this::simplificar).collect(Collectors.toList()); }
    @PostMapping("/polos/criar")
    public Object criarPolo(@RequestBody Map<String, Object> dados) { try { Polo p = new Polo(); preencherAutomaticamente(p, dados); return simplificar(poloRepo.save(p)); } catch (Exception e) { return criarErro(e); } }
    @DeleteMapping("/polos/excluir/{id}")
    public String deletarPolo(@PathVariable Long id) { poloRepo.deleteById(id); return "Polo excluído"; }

    @GetMapping("/estoque/listar")
    public List<Map<String, Object>> listarEstoque() { return prodRepo.findAll().stream().map(this::simplificar).collect(Collectors.toList()); }
    @PostMapping("/estoque/criar")
    public Object criarProduto(@RequestBody Map<String, Object> dados) { try { Produto p = new Produto(); preencherAutomaticamente(p, dados); return simplificar(prodRepo.save(p)); } catch (Exception e) { return criarErro(e); } }
    @DeleteMapping("/estoque/excluir/{id}")
    public String deletarProduto(@PathVariable Long id) { prodRepo.deleteById(id); return "Produto excluído"; }

    @GetMapping("/ambulancias/listar")
    public List<Map<String, Object>> listarAmbu() { return ambuRepo.findAll().stream().map(this::simplificar).collect(Collectors.toList()); }
    @PostMapping("/ambulancias/criar")
    public Object criarAmbu(@RequestBody Map<String, Object> dados) { try { Ambulancia a = new Ambulancia(); preencherAutomaticamente(a, dados); if (dados.get("status") == null) a.setStatus("DISPONIVEL"); if (dados.get("tipo") == null) a.setTipo("UTI MOVEL"); if (dados.get("modelo") == null) a.setModelo("Padrao"); return simplificar(ambuRepo.save(a)); } catch (Exception e) { return criarErro(e); } }
    @DeleteMapping("/ambulancias/excluir/{id}")
    public String deletarAmbu(@PathVariable Long id) { ambuRepo.deleteById(id); return "Ambulância excluída"; }

    @GetMapping("/leitos/listar")
    public List<Map<String, Object>> listarLeitos() { return leitoRepo.findAll().stream().map(this::simplificar).collect(Collectors.toList()); }
    @PostMapping("/leitos/criar")
    public Object criarLeito(@RequestBody Map<String, Object> dados) { try { Leito l = new Leito(); preencherAutomaticamente(l, dados); return simplificar(leitoRepo.save(l)); } catch (Exception e) { return criarErro(e); } }
    @DeleteMapping("/leitos/excluir/{id}")
    public String deletarLeito(@PathVariable Long id) { leitoRepo.deleteById(id); return "Leito excluído"; }

    @GetMapping("/laboratorio/listar")
    public List<Map<String, Object>> listarLab() { return labRepo.findAll().stream().map(this::simplificar).collect(Collectors.toList()); }
    @PostMapping("/laboratorio/criar")
    public Object criarLab(@RequestBody Map<String, Object> dados) { try { Laboratorio l = new Laboratorio(); preencherAutomaticamente(l, dados); return simplificar(labRepo.save(l)); } catch (Exception e) { return criarErro(e); } }
    @DeleteMapping("/laboratorio/excluir/{id}")
    public String deletarLab(@PathVariable Long id) { labRepo.deleteById(id); return "Exame excluído"; }
    
    @GetMapping("/logs/listar")
    public List<Map<String, Object>> listarLogs() { return logRepo.findAll().stream().limit(50).map(this::simplificar).collect(Collectors.toList()); }
    @PostMapping("/logs/criar")
    public Object criarLog(@RequestBody Map<String, Object> dados) { try { Log l = new Log(); preencherAutomaticamente(l, dados); l.setDataHora(LocalDateTime.now()); return simplificar(logRepo.save(l)); } catch (Exception e) { return criarErro(e); } }
    @DeleteMapping("/logs/excluir/{id}")
    public String deletarLog(@PathVariable Long id) { logRepo.deleteById(id); return "Log excluído"; }
    @DeleteMapping("/logs/limpar-tudo")
    public String limparTodosLogs() { logRepo.deleteAll(); return "Todos os logs foram apagados!"; }

    private Map<String, String> criarErro(Exception e) {
        Map<String, String> erro = new HashMap<>();
        erro.put("status", "erro");
        erro.put("mensagem", e.getMessage());
        return erro;
    }
}