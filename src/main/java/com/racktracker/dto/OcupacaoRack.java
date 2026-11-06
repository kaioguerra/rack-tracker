package com.racktracker.dto;

/** Ocupacao de um rack, usada no dashboard (RF10). */
public class OcupacaoRack {

    private Long rackId;
    private String nome;
    private String sala;
    private int capacidadeU;
    private int unidadesOcupadas;
    private double percentual;

    public OcupacaoRack(Long rackId, String nome, String sala, int capacidadeU, int unidadesOcupadas) {
        this.rackId = rackId;
        this.nome = nome;
        this.sala = sala;
        this.capacidadeU = capacidadeU;
        this.unidadesOcupadas = unidadesOcupadas;
        // arredonda para 1 casa decimal
        this.percentual = Math.round(unidadesOcupadas * 1000.0 / capacidadeU) / 10.0;
    }

    public Long getRackId() { return rackId; }
    public String getNome() { return nome; }
    public String getSala() { return sala; }
    public int getCapacidadeU() { return capacidadeU; }
    public int getUnidadesOcupadas() { return unidadesOcupadas; }
    public double getPercentual() { return percentual; }
}
