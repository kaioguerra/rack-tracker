package com.racktracker.dto;

import com.racktracker.model.Alerta;

import java.util.List;

/** Tudo que o painel principal precisa em uma chamada so (RF10). */
public class DashboardResumo {

    private int totalEquipamentos;
    private int online;
    private int offline;
    private int naoTestados;
    private int emManutencao;
    private double ocupacaoGeral;
    private List<OcupacaoRack> racks;
    private List<Alerta> ultimosAlertas;

    public DashboardResumo(int totalEquipamentos, int online, int offline, int naoTestados, int emManutencao,
                           double ocupacaoGeral, List<OcupacaoRack> racks, List<Alerta> ultimosAlertas) {
        this.totalEquipamentos = totalEquipamentos;
        this.online = online;
        this.offline = offline;
        this.naoTestados = naoTestados;
        this.emManutencao = emManutencao;
        this.ocupacaoGeral = ocupacaoGeral;
        this.racks = racks;
        this.ultimosAlertas = ultimosAlertas;
    }

    public int getTotalEquipamentos() { return totalEquipamentos; }
    public int getOnline() { return online; }
    public int getOffline() { return offline; }
    public int getNaoTestados() { return naoTestados; }
    public int getEmManutencao() { return emManutencao; }
    public double getOcupacaoGeral() { return ocupacaoGeral; }
    public List<OcupacaoRack> getRacks() { return racks; }
    public List<Alerta> getUltimosAlertas() { return ultimosAlertas; }
}
