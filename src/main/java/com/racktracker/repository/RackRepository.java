package com.racktracker.repository;

import com.racktracker.model.Rack;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RackRepository extends JpaRepository<Rack, Long> {

    // O Spring cria a consulta sozinho a partir do nome do metodo
    boolean existsByNomeAndSala(String nome, String sala);
}
