package com.racktracker.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Registro da trilha de auditoria (RF08).
 *
 * E imutavel: todas as colunas tem updatable = false, a classe nao tem setters
 * e a API nao tem endpoint para editar ou apagar movimentacoes.
 * Guardamos nome e patrimonio como texto para o historico continuar existindo
 * mesmo se o equipamento for excluido depois.
 */
@Entity
public class Movimentacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dataHora;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private TipoMovimentacao tipo;

    @Column(nullable = false, updatable = false)
    private String autor;

    @Column(nullable = false, updatable = false)
    private String motivo;

    @Column(updatable = false)
    private String equipamentoNome;

    @Column(updatable = false)
    private String equipamentoPatrimonio;

    @Column(updatable = false)
    private String origem;

    @Column(updatable = false)
    private String destino;

    // Construtor vazio exigido pelo JPA
    protected Movimentacao() {
    }

    public Movimentacao(TipoMovimentacao tipo, String autor, String motivo,
                        Equipamento equipamento, String origem, String destino) {
        this.dataHora = LocalDateTime.now();
        this.tipo = tipo;
        this.autor = autor;
        this.motivo = motivo;
        this.equipamentoNome = equipamento.getNome();
        this.equipamentoPatrimonio = equipamento.getPatrimonio();
        this.origem = origem;
        this.destino = destino;
    }

    public Long getId() { return id; }
    public LocalDateTime getDataHora() { return dataHora; }
    public TipoMovimentacao getTipo() { return tipo; }
    public String getAutor() { return autor; }
    public String getMotivo() { return motivo; }
    public String getEquipamentoNome() { return equipamentoNome; }
    public String getEquipamentoPatrimonio() { return equipamentoPatrimonio; }
    public String getOrigem() { return origem; }
    public String getDestino() { return destino; }
}
