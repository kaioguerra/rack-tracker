package com.racktracker.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("NOBREAK")
public class Nobreak extends Equipamento {

    private Integer potenciaVA;

    @Override
    public boolean isCritico() {
        return false; // pelo RF09 so switch e servidor geram alerta
    }

    public Integer getPotenciaVA() { return potenciaVA; }
    public void setPotenciaVA(Integer potenciaVA) { this.potenciaVA = potenciaVA; }
}
