package com.racktracker.dto;

/** Resposta do teste de ping (RF07). */
public class PingResultado {

    private Long equipamentoId;
    private String ip;
    private boolean online;
    private Long latenciaMs;
    private int falhasSeguidas;
    private boolean alertaGerado;

    public PingResultado(Long equipamentoId, String ip, boolean online, Long latenciaMs,
                         int falhasSeguidas, boolean alertaGerado) {
        this.equipamentoId = equipamentoId;
        this.ip = ip;
        this.online = online;
        this.latenciaMs = latenciaMs;
        this.falhasSeguidas = falhasSeguidas;
        this.alertaGerado = alertaGerado;
    }

    public Long getEquipamentoId() { return equipamentoId; }
    public String getIp() { return ip; }
    public boolean isOnline() { return online; }
    public Long getLatenciaMs() { return latenciaMs; }
    public int getFalhasSeguidas() { return falhasSeguidas; }
    public boolean isAlertaGerado() { return alertaGerado; }
}
