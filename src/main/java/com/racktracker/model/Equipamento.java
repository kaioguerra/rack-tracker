package com.racktracker.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDateTime;

/**
 * Classe base (abstrata) de todos os equipamentos.
 *
 * Heranca no banco: SINGLE_TABLE = todas as subclasses ficam na mesma tabela "equipamento",
 * e a coluna "tipo" diz se a linha e um SWITCH, SERVIDOR ou NOBREAK.
 *
 * No JSON tambem usamos o campo "tipo" para o Spring saber qual subclasse criar
 * quando chega um POST (ex.: {"tipo": "SERVIDOR", ...}).
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "tipo")
@JsonSubTypes({
        @JsonSubTypes.Type(value = Switch.class, name = "SWITCH"),
        @JsonSubTypes.Type(value = Servidor.class, name = "SERVIDOR"),
        @JsonSubTypes.Type(value = Nobreak.class, name = "NOBREAK")
})
public abstract class Equipamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome e obrigatorio")
    private String nome;

    @NotBlank(message = "O modelo e obrigatorio")
    private String modelo;

    @NotBlank(message = "O patrimonio/numero de serie e obrigatorio")
    @Column(unique = true)
    private String patrimonio;

    // IP e opcional (um nobreak simples pode nao ter rede), mas se vier precisa ser um IPv4 valido
    @Pattern(regexp = "^$|^((25[0-5]|2[0-4]\\d|1?\\d?\\d)\\.){3}(25[0-5]|2[0-4]\\d|1?\\d?\\d)$",
            message = "IP invalido (use o formato 192.168.0.10)")
    private String ip;

    // Quantas unidades (U) o equipamento ocupa no rack
    @Min(value = 1, message = "O tamanho minimo e 1U")
    @Max(value = 42, message = "O tamanho maximo e 42U")
    private int alturaU = 1;

    // ---- Localizacao no rack (preenchida pela alocacao, RF04) ----
    @ManyToOne
    private Rack rack;

    private Integer posicaoInicial; // U mais baixa ocupada

    // ---- Monitoramento (preenchido pelo ping, RF07) ----
    @Enumerated(EnumType.STRING)
    private StatusConexao status = StatusConexao.NAO_TESTADO;

    private Long latenciaMs;
    private LocalDateTime ultimoPing;
    private int falhasSeguidas = 0;

    // Modo manutencao: quando ligado, o equipamento nao gera alertas (RF09)
    private boolean emManutencao = false;

    /**
     * Cada subclasse diz se e um equipamento critico.
     * So equipamentos criticos geram alerta quando ficam offline (RF09).
     */
    public abstract boolean isCritico();

    /** Ultima posicao ocupada no rack. Ex.: comeca em U10 e tem 3U -> termina em U12. */
    public Integer getPosicaoFinal() {
        if (posicaoInicial == null) {
            return null;
        }
        return posicaoInicial + alturaU - 1;
    }

    /** Texto amigavel da localizacao, usado na auditoria. Ex.: "Rack A1 (Sala 101) U10-U12". */
    public String descreverLocalizacao() {
        if (rack == null) {
            return "Sem rack";
        }
        String posicoes = "U" + posicaoInicial;
        if (alturaU > 1) {
            posicoes += "-U" + getPosicaoFinal();
        }
        return rack.getNome() + " (" + rack.getSala() + ") " + posicoes;
    }

    // ---- Getters e setters ----

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }

    public String getPatrimonio() { return patrimonio; }
    public void setPatrimonio(String patrimonio) { this.patrimonio = patrimonio; }

    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }

    public int getAlturaU() { return alturaU; }
    public void setAlturaU(int alturaU) { this.alturaU = alturaU; }

    public Rack getRack() { return rack; }
    public void setRack(Rack rack) { this.rack = rack; }

    public Integer getPosicaoInicial() { return posicaoInicial; }
    public void setPosicaoInicial(Integer posicaoInicial) { this.posicaoInicial = posicaoInicial; }

    public StatusConexao getStatus() { return status; }
    public void setStatus(StatusConexao status) { this.status = status; }

    public Long getLatenciaMs() { return latenciaMs; }
    public void setLatenciaMs(Long latenciaMs) { this.latenciaMs = latenciaMs; }

    public LocalDateTime getUltimoPing() { return ultimoPing; }
    public void setUltimoPing(LocalDateTime ultimoPing) { this.ultimoPing = ultimoPing; }

    public int getFalhasSeguidas() { return falhasSeguidas; }
    public void setFalhasSeguidas(int falhasSeguidas) { this.falhasSeguidas = falhasSeguidas; }

    public boolean isEmManutencao() { return emManutencao; }
    public void setEmManutencao(boolean emManutencao) { this.emManutencao = emManutencao; }
}
