package com.racktracker.controller;

import com.racktracker.dto.PingResultado;
import com.racktracker.model.Equipamento;
import com.racktracker.service.EquipamentoService;
import com.racktracker.service.PingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/equipamentos")
public class EquipamentoController {

    private final EquipamentoService equipamentoService;
    private final PingService pingService;

    public EquipamentoController(EquipamentoService equipamentoService, PingService pingService) {
        this.equipamentoService = equipamentoService;
        this.pingService = pingService;
    }

    // RF06 - GET /api/equipamentos?tipo=SERVIDOR&sala=Sala 101&rackId=1&ip=192.168.
    @GetMapping
    public List<Equipamento> listar(@RequestParam(required = false) String tipo,
                                    @RequestParam(required = false) String sala,
                                    @RequestParam(required = false) Long rackId,
                                    @RequestParam(required = false) String ip) {
        return equipamentoService.listar(tipo, sala, rackId, ip);
    }

    @GetMapping("/{id}")
    public Equipamento buscar(@PathVariable Long id) {
        return equipamentoService.buscar(id);
    }

    // RF01
    @PostMapping
    public ResponseEntity<Equipamento> criar(@Valid @RequestBody Equipamento equipamento) {
        return ResponseEntity.status(HttpStatus.CREATED).body(equipamentoService.criar(equipamento));
    }

    // RF02
    @PutMapping("/{id}")
    public Equipamento atualizar(@PathVariable Long id, @Valid @RequestBody Equipamento equipamento) {
        return equipamentoService.atualizar(id, equipamento);
    }

    // RF02
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        equipamentoService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    // RF07 - teste de ping sob demanda
    @PostMapping("/{id}/ping")
    public PingResultado ping(@PathVariable Long id) {
        return pingService.testar(id);
    }
}
