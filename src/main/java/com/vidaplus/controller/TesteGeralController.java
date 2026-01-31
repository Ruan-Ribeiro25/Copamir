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
import java.math.BigDecimal; // IMPORTANTE: Para o dinheiro funcionar!

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

    // --- MÁGICA V3: AGORA SUPORTA BIGDECIMAL E ENUMS ---
    private void preencherAutomaticamente(Object destino, Map<String, Object> origem) {
        Method[] metodos = destino.getClass().getMethods();
        for (Method m : metodos) {
            if (m.getName().startsWith("set") && m.getParameterCount() == 1) {
                String atributo = m.getName().substring(3).toLowerCase(); 
                
                for (String key : origem.keySet()) {
                    String keyLimpa = key.replace("_", "").toLowerCase();
                    
                    if (keyLimpa.equals(atributo) || atributo.startsWith(keyLimpa)) {
                        try {
                            Object valor = origem.get(key);
                            Class<?> tipoParametro = m.getParameterTypes()[0];

                            // 1. Conversão para BigDecimal (DINHEIRO)
                            if (tipoParametro == BigDecimal.class) {
                                m.invoke(destino, new BigDecimal(valor.toString()));
                            }
                            // 2. Conversão String -> Enum (CATEGORIA, TIPO, STATUS)
                            else if (tipoParametro.isEnum() && valor instanceof String) {
                                Object[] constantes = tipoParametro.getEnumConstants();
                                for(Object objEnum : constantes) {
                                    if(objEnum.toString().equalsIgnoreCase((String)valor)) {
                                        m.invoke(destino, objEnum);
                                        break;
                                    }
                                }
                            }
                            // 3. Conversão Número -> Double
                            else if (valor instanceof Integer && tipoParametro == Double.class) {
                                m.invoke(destino, ((Integer) valor).doubleValue());
                            } 
                            // 4. Padrão
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
            // Tenta pegar descrição se existir
            try { map.put("info", obj.toString()); } catch (Exception e) {}
        } catch (Exception e) { map.put("erro_visualizacao", e.getMessage()); }
        return map;
    }

    // ================== FINANCEIRO (CORRIGIDO COM BIGDECIMAL) ==================
    @GetMapping("/financeiro/listar")
    public List<Map<String, Object>> listarFin() { return finRepo.findAll().stream().map(this::simplificar).collect(Collectors.toList()); }
    
    @PostMapping("/financeiro/criar")
    public Object criarFin(@RequestBody Map<String, Object> dados) {
        TransacaoFinanceira f = new TransacaoFinanceira();
        try {
            // 1. Tenta preencher tudo (Valor, Descrição, etc)
            preencherAutomaticamente(f, dados);
            
            // 2. PREENCHER OBRIGATÓRIOS QUE FALTARAM (Via Reflection para não errar o Enum)
            for(Method m : f.getClass().getMethods()) {
                if(m.getName().startsWith("set") && m.getParameterCount() == 1) {
                    Class<?> tipo = m.getParameterTypes()[0];
                    
                    // Se for Categoria e ainda estiver null, tenta setar o primeiro da lista
                    if(m.getName().contains("Categoria") && tipo.isEnum()) {
                        if(verificarSeNulo(f, m.getName())) setarEnumPadrao(f, m);
                    }
                    // Se for Tipo e ainda estiver null
                    if(m.getName().contains("Tipo") && tipo.isEnum()) {
                        if(verificarSeNulo(f, m.getName())) setarEnumPadrao(f, m);
                    }
                    // Se for Status e ainda estiver null
                    if(m.getName().contains("Status") && tipo.isEnum()) {
                        if(verificarSeNulo(f, m.getName())) setarEnumPadrao(f, m);
                    }
                }
            }

            return simplificar(finRepo.save(f)); 
        } catch (Exception e) { return criarErro(e); }
    }
    
    // Auxiliares para preencher Enums padrão
    private boolean verificarSeNulo(Object obj, String nomeSetter) {
        try {
            String nomeGetter = "get" + nomeSetter.substring(3);
            Method getter = obj.getClass().getMethod(nomeGetter);
            return getter.invoke(obj) == null;
        } catch (Exception e) { return true; }
    }
    private void setarEnumPadrao(Object obj, Method setter) {
        try {
            Object[] enums = setter.getParameterTypes()[0].getEnumConstants();
            if(enums.length > 0) setter.invoke(obj, enums[0]); // Pega o primeiro item da lista
        } catch (Exception e) {}
    }

    @DeleteMapping("/financeiro/excluir/{id}")
    public String deletarFin(@PathVariable Long id) { finRepo.deleteById(id); return "Transação excluída"; }

    // ================== OUTROS (MANTIDOS) ==================
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