package com.racktracker.repository;

import com.racktracker.model.Equipamento;
import com.racktracker.model.Rack;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EquipamentoRepository extends JpaRepository<Equipamento, Long> {

    boolean existsByPatrimonio(String patrimonio);

    List<Equipamento> findByRack(Rack rack);
}
