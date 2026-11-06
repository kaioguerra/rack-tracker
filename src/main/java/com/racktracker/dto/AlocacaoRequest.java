package com.racktracker.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Corpo do POST /api/movimentacoes/alocar (RF04). */
public class AlocacaoRequest {

    @NotNull(message = "Informe o equipamento")
    private Long equipamentoId;

    @NotNull(message = "Informe o rack")
    private Long rackId;

    @NotNull(message = "Informe a posicao inicial (U)")
    @Min(value = 1, message = "A posicao inicial minima e U1")
    private Integer posicaoInicial;

    @NotBlank(message = "Informe o autor da movimentacao")
    private String autor;

    @NotBlank(message = "Informe o motivo da movimentacao")
    private String motivo;

    public Long getEquipamentoId() { return equipamentoId; }
    public void setEquipamentoId(Long equipamentoId) { this.equipamentoId = equipamentoId; }

    public Long getRackId() { return rackId; }
    public void setRackId(Long rackId) { this.rackId = rackId; }

    public Integer getPosicaoInicial() { return posicaoInicial; }
    public void setPosicaoInicial(Integer posicaoInicial) { this.posicaoInicial = posicaoInicial; }

    public String getAutor() { return autor; }
    public void setAutor(String autor) { this.autor = autor; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
}
