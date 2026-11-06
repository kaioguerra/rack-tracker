package com.racktracker.config;

import com.racktracker.dto.AlocacaoRequest;
import com.racktracker.model.*;
import com.racktracker.repository.RackRepository;
import com.racktracker.service.EquipamentoService;
import com.racktracker.service.MovimentacaoService;
import com.racktracker.service.RackService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Cadastra racks e equipamentos de exemplo na primeira vez que o sistema roda
 * (so quando o banco ainda nao tem nenhum rack).
 *
 * IPs de exemplo:
 *  - 127.0.0.x  -> a propria maquina, sempre responde ao ping (aparece ONLINE)
 *  - 192.0.2.x  -> faixa reservada para documentacao, nunca responde (aparece OFFLINE e gera alerta)
 */
@Component
public class DadosIniciais implements CommandLineRunner {

    private static final String AUTOR = "Sistema (carga inicial)";

    private final RackRepository rackRepository;
    private final RackService rackService;
    private final EquipamentoService equipamentoService;
    private final MovimentacaoService movimentacaoService;

    public DadosIniciais(RackRepository rackRepository, RackService rackService,
                         EquipamentoService equipamentoService, MovimentacaoService movimentacaoService) {
        this.rackRepository = rackRepository;
        this.rackService = rackService;
        this.equipamentoService = equipamentoService;
        this.movimentacaoService = movimentacaoService;
    }

    @Override
    public void run(String... args) {
        if (rackRepository.count() > 0) {
            return; // ja tem dados, nao faz nada
        }

        Rack a1 = rackService.criar(new Rack("Rack A1", "Sala 101 - Datacenter", 42));
        Rack a2 = rackService.criar(new Rack("Rack A2", "Sala 101 - Datacenter", 42));
        Rack b1 = rackService.criar(new Rack("Rack B1", "Sala 204 - Laboratorio", 24));

        // ---- Rack A1 ----
        alocar(nobreak("Nobreak A1", "APC Smart-UPS 3000", "PAT-0001", "127.0.0.10", 2, 3000), a1, 1);
        alocar(servidor("SRV-DB-01", "Dell PowerEdge R750", "PAT-0002", "127.0.0.11", 2, "Ubuntu Server 24.04"), a1, 4);
        alocar(servidor("SRV-WEB-01", "Dell PowerEdge R650", "PAT-0003", "127.0.0.12", 1, "Debian 12"), a1, 7);
        alocar(servidor("SRV-BACKUP", "HPE ProLiant DL380", "PAT-0004", "192.0.2.20", 2, "Windows Server 2022"), a1, 9);
        alocar(switchRede("SW-CORE-01", "Cisco Catalyst 9300", "PAT-0005", "127.0.0.1", 1, 48), a1, 40);
        alocar(switchRede("SW-CORE-02", "Cisco Catalyst 9300", "PAT-0006", "127.0.0.2", 1, 48), a1, 41);

        // ---- Rack A2 ----
        alocar(nobreak("Nobreak A2", "SMS Sinus Triad 2200", "PAT-0007", null, 2, 2200), a2, 1);
        alocar(servidor("SRV-ERP", "Lenovo ThinkSystem SR650", "PAT-0008", "127.0.0.13", 2, "Red Hat Enterprise Linux 9"), a2, 10);
        alocar(switchRede("SW-ACESSO-01", "HPE Aruba 2930F", "PAT-0009", "192.0.2.30", 1, 24), a2, 38);

        // ---- Rack B1 (laboratorio) ----
        alocar(switchRede("SW-LAB-01", "TP-Link TL-SG3428", "PAT-0010", "127.0.0.3", 1, 24), b1, 22);
        alocar(servidor("SRV-LAB-TESTES", "Supermicro SYS-1019", "PAT-0011", "127.0.0.14", 1, "Proxmox VE 8"), b1, 12);

        // ---- No estoque (sem rack), prontos para a demonstracao de alocacao ----
        servidor("SRV-NOVO", "Dell PowerEdge R660", "PAT-0012", "127.0.0.15", 1, "Ubuntu Server 24.04");
        switchRede("SW-RESERVA", "Cisco Catalyst 9200", "PAT-0013", "127.0.0.4", 1, 24);
    }

    // ---------- Metodos auxiliares ----------

    private Servidor servidor(String nome, String modelo, String patrimonio, String ip, int alturaU, String so) {
        Servidor s = new Servidor();
        preencher(s, nome, modelo, patrimonio, ip, alturaU);
        s.setSistemaOperacional(so);
        return (Servidor) equipamentoService.criar(s);
    }

    private Switch switchRede(String nome, String modelo, String patrimonio, String ip, int alturaU, int portas) {
        Switch s = new Switch();
        preencher(s, nome, modelo, patrimonio, ip, alturaU);
        s.setQuantidadePortas(portas);
        return (Switch) equipamentoService.criar(s);
    }

    private Nobreak nobreak(String nome, String modelo, String patrimonio, String ip, int alturaU, int potenciaVA) {
        Nobreak n = new Nobreak();
        preencher(n, nome, modelo, patrimonio, ip, alturaU);
        n.setPotenciaVA(potenciaVA);
        return (Nobreak) equipamentoService.criar(n);
    }

    private void preencher(Equipamento e, String nome, String modelo, String patrimonio, String ip, int alturaU) {
        e.setNome(nome);
        e.setModelo(modelo);
        e.setPatrimonio(patrimonio);
        e.setIp(ip);
        e.setAlturaU(alturaU);
    }

    private void alocar(Equipamento equipamento, Rack rack, int posicaoInicial) {
        AlocacaoRequest pedido = new AlocacaoRequest();
        pedido.setEquipamentoId(equipamento.getId());
        pedido.setRackId(rack.getId());
        pedido.setPosicaoInicial(posicaoInicial);
        pedido.setAutor(AUTOR);
        pedido.setMotivo("Montagem inicial do rack");
        movimentacaoService.alocar(pedido);
    }
}
