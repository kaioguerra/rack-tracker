package com.racktracker.repository;

import com.racktracker.model.Alerta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlertaRepository extends JpaRepository<Alerta, Long> {

    List<Alerta> findAllByOrderByDataHoraDesc();

    List<Alerta> findTop5ByOrderByDataHoraDesc();
}
