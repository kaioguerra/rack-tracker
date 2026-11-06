package com.racktracker.controller;

import com.racktracker.dto.AlocacaoRequest;
import com.racktracker.dto.DesalocacaoRequest;
import com.racktracker.model.Equipamento;
import com.racktracker.model.Movimentacao;
import com.racktracker.service.MovimentacaoService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/movimentacoes")
public class MovimentacaoController {

    private final MovimentacaoService movimentacaoService;

    public MovimentacaoController(MovimentacaoService movimentacaoService) {
        this.movimentacaoService = movimentacaoService;
    }

    // RF08 - trilha de auditoria (somente leitura, mais recente primeiro)
    @GetMapping
    public List<Movimentacao> listar() {
        return movimentacaoService.listar();
    }

    // RF04
    @PostMapping("/alocar")
    public Equipamento alocar(@Valid @RequestBody AlocacaoRequest pedido) {
        return movimentacaoService.alocar(pedido);
    }

    // RF05
    @PostMapping("/desalocar")
    public Equipamento desalocar(@Valid @RequestBody DesalocacaoRequest pedido) {
        return movimentacaoService.desalocar(pedido);
    }
}
