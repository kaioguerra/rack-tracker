package com.racktracker.service;

import com.racktracker.dto.AlocacaoRequest;
import com.racktracker.dto.DesalocacaoRequest;
import com.racktracker.exception.RegraNegocioException;
import com.racktracker.model.Equipamento;
import com.racktracker.model.Movimentacao;
import com.racktracker.model.Rack;
import com.racktracker.model.TipoMovimentacao;
import com.racktracker.repository.EquipamentoRepository;
import com.racktracker.repository.MovimentacaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Alocacao (RF04), desalocacao (RF05) e trilha de auditoria (RF08).
 *
 * @Transactional: o equipamento e o registro de auditoria sao salvos juntos.
 * Se der erro ao gravar a auditoria, a alocacao inteira e desfeita (rollback).
 */
@Service
public class MovimentacaoService {

    private final EquipamentoRepository equipamentoRepository;
    private final MovimentacaoRepository movimentacaoRepository;
    private final EquipamentoService equipamentoService;
    private final RackService rackService;

    public MovimentacaoService(EquipamentoRepository equipamentoRepository, MovimentacaoRepository movimentacaoRepository,
                               EquipamentoService equipamentoService, RackService rackService) {
        this.equipamentoRepository = equipamentoRepository;
        this.movimentacaoRepository = movimentacaoRepository;
        this.equipamentoService = equipamentoService;
        this.rackService = rackService;
    }

    public List<Movimentacao> listar() {
        return movimentacaoRepository.findAllByOrderByDataHoraDesc();
    }

    /** RF04 - Alocar (ou remanejar) um equipamento em um rack. */
    @Transactional
    public Equipamento alocar(AlocacaoRequest pedido) {
        Equipamento equipamento = equipamentoService.buscar(pedido.getEquipamentoId());
        Rack rack = rackService.buscar(pedido.getRackId());

        int inicio = pedido.getPosicaoInicial();
        int fim = inicio + equipamento.getAlturaU() - 1;

        // 1) Cabe no rack?
        if (fim > rack.getCapacidadeU()) {
            throw new RegraNegocioException("O equipamento tem " + equipamento.getAlturaU() + "U e nao cabe a partir de U"
                    + inicio + ". O rack " + rack.getNome() + " vai ate U" + rack.getCapacidadeU() + ".");
        }

        // 2) Colisao: as posicoes ja estao ocupadas por outro equipamento?
        for (Equipamento outro : equipamentoRepository.findByRack(rack)) {
            if (outro.getId().equals(equipamento.getId())) {
                continue; // ele mesmo (caso de remanejar dentro do mesmo rack)
            }
            // Duas faixas [inicio, fim] se sobrepoem quando uma comeca antes da outra terminar
            boolean sobrepoe = inicio <= outro.getPosicaoFinal() && fim >= outro.getPosicaoInicial();
            if (sobrepoe) {
                throw new RegraNegocioException("Colisao! As posicoes U" + outro.getPosicaoInicial() + "-U"
                        + outro.getPosicaoFinal() + " ja estao ocupadas por " + outro.getNome() + ".");
            }
        }

        // 3) Atualiza o equipamento e grava a auditoria
        String origem = equipamento.descreverLocalizacao();
        TipoMovimentacao tipo = (equipamento.getRack() == null) ? TipoMovimentacao.ALOCACAO : TipoMovimentacao.REMANEJAMENTO;

        equipamento.setRack(rack);
        equipamento.setPosicaoInicial(inicio);
        equipamentoRepository.save(equipamento);

        String destino = equipamento.descreverLocalizacao();
        movimentacaoRepository.save(new Movimentacao(tipo, pedido.getAutor(), pedido.getMotivo(), equipamento, origem, destino));

        return equipamento;
    }

    /** RF05 - Desalocar um equipamento (liberar as posicoes do rack). */
    @Transactional
    public Equipamento desalocar(DesalocacaoRequest pedido) {
        Equipamento equipamento = equipamentoService.buscar(pedido.getEquipamentoId());

        if (equipamento.getRack() == null) {
            throw new RegraNegocioException("O equipamento " + equipamento.getNome() + " nao esta alocado em nenhum rack.");
        }

        String origem = equipamento.descreverLocalizacao();

        equipamento.setRack(null);
        equipamento.setPosicaoInicial(null);
        equipamentoRepository.save(equipamento);

        movimentacaoRepository.save(new Movimentacao(TipoMovimentacao.DESALOCACAO, pedido.getAutor(), pedido.getMotivo(),
                equipamento, origem, "Sem rack"));

        return equipamento;
    }
}
