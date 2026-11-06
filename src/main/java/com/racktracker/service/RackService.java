package com.racktracker.service;

import com.racktracker.exception.NaoEncontradoException;
import com.racktracker.exception.RegraNegocioException;
import com.racktracker.model.Equipamento;
import com.racktracker.model.Rack;
import com.racktracker.repository.EquipamentoRepository;
import com.racktracker.repository.RackRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RackService {

    private final RackRepository rackRepository;
    private final EquipamentoRepository equipamentoRepository;

    public RackService(RackRepository rackRepository, EquipamentoRepository equipamentoRepository) {
        this.rackRepository = rackRepository;
        this.equipamentoRepository = equipamentoRepository;
    }

    public List<Rack> listar() {
        return rackRepository.findAll();
    }

    public Rack buscar(Long id) {
        return rackRepository.findById(id)
                .orElseThrow(() -> new NaoEncontradoException("Rack " + id + " nao encontrado"));
    }

    /** RF03 - Cadastrar rack. */
    public Rack criar(Rack rack) {
        if (rackRepository.existsByNomeAndSala(rack.getNome(), rack.getSala())) {
            throw new RegraNegocioException("Ja existe um rack chamado " + rack.getNome() + " na sala " + rack.getSala());
        }
        rack.setId(null);
        return rackRepository.save(rack);
    }

    public Rack atualizar(Long id, Rack dados) {
        Rack rack = buscar(id);

        boolean mudouNomeOuSala = !rack.getNome().equals(dados.getNome()) || !rack.getSala().equals(dados.getSala());
        if (mudouNomeOuSala && rackRepository.existsByNomeAndSala(dados.getNome(), dados.getSala())) {
            throw new RegraNegocioException("Ja existe um rack chamado " + dados.getNome() + " na sala " + dados.getSala());
        }

        // Nao deixa diminuir a capacidade se algum equipamento ficaria "fora" do rack
        for (Equipamento e : equipamentoRepository.findByRack(rack)) {
            if (e.getPosicaoFinal() > dados.getCapacidadeU()) {
                throw new RegraNegocioException("O equipamento " + e.getNome() + " ocupa ate U" + e.getPosicaoFinal()
                        + ". Desaloque-o antes de reduzir a capacidade.");
            }
        }

        rack.setNome(dados.getNome());
        rack.setSala(dados.getSala());
        rack.setCapacidadeU(dados.getCapacidadeU());
        return rackRepository.save(rack);
    }

    public void excluir(Long id) {
        Rack rack = buscar(id);
        if (!equipamentoRepository.findByRack(rack).isEmpty()) {
            throw new RegraNegocioException("O rack ainda tem equipamentos. Desaloque todos antes de excluir.");
        }
        rackRepository.delete(rack);
    }

    /** Equipamentos montados no rack (usado para desenhar o rack no frontend). */
    public List<Equipamento> listarEquipamentos(Long rackId) {
        return equipamentoRepository.findByRack(buscar(rackId));
    }

    /** Soma quantas U estao ocupadas no rack. */
    public int contarUnidadesOcupadas(Rack rack) {
        int total = 0;
        for (Equipamento e : equipamentoRepository.findByRack(rack)) {
            total += e.getAlturaU();
        }
        return total;
    }
}
