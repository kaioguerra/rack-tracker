package com.racktracker.controller;

import com.racktracker.model.Equipamento;
import com.racktracker.model.Rack;
import com.racktracker.service.RackService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/racks")
public class RackController {

    private final RackService rackService;

    public RackController(RackService rackService) {
        this.rackService = rackService;
    }

    @GetMapping
    public List<Rack> listar() {
        return rackService.listar();
    }

    @GetMapping("/{id}")
    public Rack buscar(@PathVariable Long id) {
        return rackService.buscar(id);
    }

    // Equipamentos montados neste rack (para desenhar o rack)
    @GetMapping("/{id}/equipamentos")
    public List<Equipamento> listarEquipamentos(@PathVariable Long id) {
        return rackService.listarEquipamentos(id);
    }

    // RF03
    @PostMapping
    public ResponseEntity<Rack> criar(@Valid @RequestBody Rack rack) {
        return ResponseEntity.status(HttpStatus.CREATED).body(rackService.criar(rack));
    }

    @PutMapping("/{id}")
    public Rack atualizar(@PathVariable Long id, @Valid @RequestBody Rack rack) {
        return rackService.atualizar(id, rack);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        rackService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
