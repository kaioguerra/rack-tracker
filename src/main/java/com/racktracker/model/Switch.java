package com.racktracker.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("SWITCH")
public class Switch extends Equipamento {

    private Integer quantidadePortas;

    @Override
    public boolean isCritico() {
        return true; // se o switch cai, a rede inteira cai
    }

    public Integer getQuantidadePortas() { return quantidadePortas; }
    public void setQuantidadePortas(Integer quantidadePortas) { this.quantidadePortas = quantidadePortas; }
}
