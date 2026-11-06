package com.racktracker.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/** Alerta gerado quando um equipamento critico falha no ping varias vezes seguidas (RF09). */
@Entity
public class Alerta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime dataHora;
    private String equipamentoNome;
    private String ip;
    private String mensagem;

    protected Alerta() {
    }

    public Alerta(Equipamento equipamento, String mensagem) {
        this.dataHora = LocalDateTime.now();
        this.equipamentoNome = equipamento.getNome();
        this.ip = equipamento.getIp();
        this.mensagem = mensagem;
    }

    public Long getId() { return id; }
    public LocalDateTime getDataHora() { return dataHora; }
    public String getEquipamentoNome() { return equipamentoNome; }
    public String getIp() { return ip; }
    public String getMensagem() { return mensagem; }
}
