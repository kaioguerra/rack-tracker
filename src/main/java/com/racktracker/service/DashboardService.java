package com.racktracker.service;

import com.racktracker.dto.DashboardResumo;
import com.racktracker.dto.OcupacaoRack;
import com.racktracker.model.Equipamento;
import com.racktracker.model.Rack;
import com.racktracker.model.StatusConexao;
import com.racktracker.repository.AlertaRepository;
import com.racktracker.repository.EquipamentoRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/** Monta os numeros do painel principal (RF10). */
@Service
public class DashboardService {

    private final EquipamentoRepository equipamentoRepository;
    private final AlertaRepository alertaRepository;
    private final RackService rackService;

    public DashboardService(EquipamentoRepository equipamentoRepository, AlertaRepository alertaRepository,
                            RackService rackService) {
        this.equipamentoRepository = equipamentoRepository;
        this.alertaRepository = alertaRepository;
        this.rackService = rackService;
    }

    public DashboardResumo gerarResumo() {
        List<Equipamento> equipamentos = equipamentoRepository.findAll();

        int online = 0, offline = 0, naoTestados = 0, emManutencao = 0;
        for (Equipamento e : equipamentos) {
            if (e.getStatus() == StatusConexao.ONLINE) online++;
            else if (e.getStatus() == StatusConexao.OFFLINE) offline++;
            else naoTestados++;

            if (e.isEmManutencao()) emManutencao++;
        }

        List<OcupacaoRack> racks = new ArrayList<>();
        int capacidadeTotal = 0, ocupadasTotal = 0;
        for (Rack rack : rackService.listar()) {
            int ocupadas = rackService.contarUnidadesOcupadas(rack);
            racks.add(new OcupacaoRack(rack.getId(), rack.getNome(), rack.getSala(), rack.getCapacidadeU(), ocupadas));
            capacidadeTotal += rack.getCapacidadeU();
            ocupadasTotal += ocupadas;
        }

        double ocupacaoGeral = 0;
        if (capacidadeTotal > 0) {
            ocupacaoGeral = Math.round(ocupadasTotal * 1000.0 / capacidadeTotal) / 10.0;
        }

        return new DashboardResumo(equipamentos.size(), online, offline, naoTestados, emManutencao,
                ocupacaoGeral, racks, alertaRepository.findTop5ByOrderByDataHoraDesc());
    }
}
