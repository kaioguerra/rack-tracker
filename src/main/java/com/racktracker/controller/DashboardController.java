package com.racktracker.controller;

import com.racktracker.dto.DashboardResumo;
import com.racktracker.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    // RF10
    @GetMapping
    public DashboardResumo resumo() {
        return dashboardService.gerarResumo();
    }
}
