package com.racktracker.service;

import com.racktracker.dto.PingResultado;
import com.racktracker.exception.RegraNegocioException;
import com.racktracker.model.Alerta;
import com.racktracker.model.Equipamento;
import com.racktracker.model.StatusConexao;
import com.racktracker.repository.AlertaRepository;
import com.racktracker.repository.EquipamentoRepository;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.List;

/** Teste de conectividade (RF07) e geracao de alertas (RF09). */
@Service
public class PingService {

    private static final int TEMPO_LIMITE_MS = 2000;   // espera ate 2 segundos pela resposta
    private static final int FALHAS_PARA_ALERTA = 2;   // quantas falhas seguidas geram alerta

    private final EquipamentoService equipamentoService;
    private final EquipamentoRepository equipamentoRepository;
    private final AlertaRepository alertaRepository;

    public PingService(EquipamentoService equipamentoService, EquipamentoRepository equipamentoRepository,
                       AlertaRepository alertaRepository) {
        this.equipamentoService = equipamentoService;
        this.equipamentoRepository = equipamentoRepository;
        this.alertaRepository = alertaRepository;
    }

    public PingResultado testar(Long equipamentoId) {
        Equipamento equipamento = equipamentoService.buscar(equipamentoId);

        if (equipamento.getIp() == null || equipamento.getIp().isBlank()) {
            throw new RegraNegocioException("O equipamento " + equipamento.getNome() + " nao tem IP cadastrado.");
        }

        // Faz o ping e mede o tempo de resposta
        long inicio = System.currentTimeMillis();
        boolean online = pingar(equipamento.getIp());
        long latencia = System.currentTimeMillis() - inicio;

        equipamento.setUltimoPing(LocalDateTime.now());
        boolean alertaGerado = false;

        if (online) {
            equipamento.setStatus(StatusConexao.ONLINE);
            equipamento.setLatenciaMs(latencia);
            equipamento.setFalhasSeguidas(0);
        } else {
            equipamento.setStatus(StatusConexao.OFFLINE);
            equipamento.setLatenciaMs(null);
            equipamento.setFalhasSeguidas(equipamento.getFalhasSeguidas() + 1);

            // RF09: alerta so para equipamento critico, fora de manutencao, que chegou no limite de falhas
            if (equipamento.isCritico()
                    && !equipamento.isEmManutencao()
                    && equipamento.getFalhasSeguidas() == FALHAS_PARA_ALERTA) {
                alertaRepository.save(new Alerta(equipamento,
                        equipamento.getNome() + " nao respondeu ao ping " + FALHAS_PARA_ALERTA + " vezes seguidas"));
                alertaGerado = true;
            }
        }

        equipamentoRepository.save(equipamento);

        return new PingResultado(equipamento.getId(), equipamento.getIp(), online,
                equipamento.getLatenciaMs(), equipamento.getFalhasSeguidas(), alertaGerado);
    }

    public List<Alerta> listarAlertas() {
        return alertaRepository.findAllByOrderByDataHoraDesc();
    }

    /** Envia um "ping" (ICMP) para o IP. Retorna true se respondeu dentro do tempo limite. */
    private boolean pingar(String ip) {
        try {
            return InetAddress.getByName(ip).isReachable(TEMPO_LIMITE_MS);
        } catch (Exception e) {
            return false;
        }
    }
}
