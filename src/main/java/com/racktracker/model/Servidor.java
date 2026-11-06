package com.racktracker.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("SERVIDOR")
public class Servidor extends Equipamento {

    private String sistemaOperacional;

    @Override
    public boolean isCritico() {
        return true; // servidor fora do ar = sistema fora do ar
    }

    public String getSistemaOperacional() { return sistemaOperacional; }
    public void setSistemaOperacional(String sistemaOperacional) { this.sistemaOperacional = sistemaOperacional; }
}
