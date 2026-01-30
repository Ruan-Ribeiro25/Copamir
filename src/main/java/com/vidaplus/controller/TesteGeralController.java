package com.vidaplus.controller;

// --- IMPORTS OBRIGATÓRIOS (ISSO CORRIGE OS ERROS VERMELHOS) ---
import com.vidaplus.entity.Ambulancia;
import com.vidaplus.entity.Laboratorio;
import com.vidaplus.entity.Leito;
import com.vidaplus.entity.Log;
import com.vidaplus.entity.Polo;
import com.vidaplus.entity.TransacaoFinanceira; // Seu nome correto

import com.vidaplus.repository.AmbulanciaRepository;
import com.vidaplus.repository.LaboratorioRepository; // Agora vai funcionar
import com.vidaplus.repository.LeitoRepository;       // Agora vai funcionar
import com.vidaplus.repository.LogRepository;
import com.vidaplus.repository.PoloRepository;
import com.vidaplus.repository.TransacaoFinanceiraRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
// ---------------------------------------------------------------

@RestController
@RequestMapping("/api/teste")
public class TesteGeralController {

    @Autowired private PoloRepository poloRepo;
    @Autowired private LogRepository logRepo;
    @Autowired private AmbulanciaRepository ambuRepo;
    @Autowired private LeitoRepository leitoRepo;      
    @Autowired private LaboratorioRepository labRepo;  
    @Autowired private TransacaoFinanceiraRepository finRepo; 

    // --- HELPER PARA JSON LIMPO ---
    private Map<String, Object> simplificar(Object obj) {
        Map<String, Object> map = new HashMap<>();
        if (obj == null) return map;
        
        if (obj instanceof Polo) {
            Polo p = (Polo) obj;
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
        return map;
    }

    // ================== AMBULANCIAS ==================
    @GetMapping("/ambulancias/listar")
    public List<Map<String, Object>> listarAmbu() { return ambuRepo.findAll().stream().map(this::simplificar).collect(Collectors.toList()); }
    @PostMapping("/ambulancias/criar")
    public Map<String, Object> criarAmbu(@RequestBody Ambulancia a) { return simplificar(ambuRepo.save(a)); }
    @DeleteMapping("/ambulancias/excluir/{id}")
    public String deletarAmbu(@PathVariable Long id) { ambuRepo.deleteById(id); return "OK"; }

    // ================== FINANCEIRO ==================
    @GetMapping("/financeiro/listar")
    public List<Map<String, Object>> listarFin() { return finRepo.findAll().stream().map(this::simplificar).collect(Collectors.toList()); }
    @PostMapping("/financeiro/criar")
    public Map<String, Object> criarFin(@RequestBody TransacaoFinanceira f) { return simplificar(finRepo.save(f)); }
    @DeleteMapping("/financeiro/excluir/{id}")
    public String deletarFin(@PathVariable Long id) { finRepo.deleteById(id); return "OK"; }

    // ================== LEITOS ==================
    @GetMapping("/leitos/listar")
    public List<Map<String, Object>> listarLeitos() { return leitoRepo.findAll().stream().map(this::simplificar).collect(Collectors.toList()); }
    @PostMapping("/leitos/criar")
    public Map<String, Object> criarLeito(@RequestBody Leito l) { return simplificar(leitoRepo.save(l)); }
    @DeleteMapping("/leitos/excluir/{id}")
    public String deletarLeito(@PathVariable Long id) { leitoRepo.deleteById(id); return "OK"; }

    // ================== LABORATORIO ==================
    @GetMapping("/laboratorio/listar")
    public List<Map<String, Object>> listarLab() { return labRepo.findAll().stream().map(this::simplificar).collect(Collectors.toList()); }
    @PostMapping("/laboratorio/criar")
    public Map<String, Object> criarLab(@RequestBody Laboratorio l) { return simplificar(labRepo.save(l)); }
    @DeleteMapping("/laboratorio/excluir/{id}")
    public String deletarLab(@PathVariable Long id) { labRepo.deleteById(id); return "OK"; }
    
    // ================== POLOS E LOGS ==================
    @GetMapping("/polos/listar")
    public List<Map<String, Object>> listarPolos() { return poloRepo.findAll().stream().map(this::simplificar).collect(Collectors.toList()); }
    
    @GetMapping("/logs/listar")
    public List<Map<String, Object>> listarLogs() { return logRepo.findAll().stream().limit(50).map(this::simplificar).collect(Collectors.toList()); }
}