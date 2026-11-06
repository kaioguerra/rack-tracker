package com.racktracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Corpo do POST /api/movimentacoes/desalocar (RF05). */
public class DesalocacaoRequest {

    @NotNull(message = "Informe o equipamento")
    private Long equipamentoId;

    @NotBlank(message = "Informe o autor da movimentacao")
    private String autor;

    @NotBlank(message = "Informe o motivo da movimentacao")
    private String motivo;

    public Long getEquipamentoId() { return equipamentoId; }
    public void setEquipamentoId(Long equipamentoId) { this.equipamentoId = equipamentoId; }

    public String getAutor() { return autor; }
    public void setAutor(String autor) { this.autor = autor; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
}
