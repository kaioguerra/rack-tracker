package com.racktracker.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * Armario de rede (bastidor). As posicoes vao de U1 (base) ate U[capacidade] (topo).
 * Regra RF03: o nome do rack nao pode se repetir dentro da mesma sala.
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"nome", "sala"}))
public class Rack {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome do rack e obrigatorio")
    private String nome;

    @NotBlank(message = "A sala/setor e obrigatoria")
    private String sala;

    @Min(value = 1, message = "A capacidade deve ser maior que zero")
    @Max(value = 42, message = "A capacidade maxima e 42U")
    private int capacidadeU = 42;

    public Rack() {
    }

    public Rack(String nome, String sala, int capacidadeU) {
        this.nome = nome;
        this.sala = sala;
        this.capacidadeU = capacidadeU;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getSala() { return sala; }
    public void setSala(String sala) { this.sala = sala; }

    public int getCapacidadeU() { return capacidadeU; }
    public void setCapacidadeU(int capacidadeU) { this.capacidadeU = capacidadeU; }
}
