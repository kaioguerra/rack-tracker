package com.racktracker.controller;

import com.racktracker.model.Alerta;
import com.racktracker.service.PingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/alertas")
public class AlertaController {

    private final PingService pingService;

    public AlertaController(PingService pingService) {
        this.pingService = pingService;
    }

    // RF09 - historico de alertas
    @GetMapping
    public List<Alerta> listar() {
        return pingService.listarAlertas();
    }
}
