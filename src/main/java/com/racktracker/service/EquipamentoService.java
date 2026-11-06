package com.racktracker.service;

import com.racktracker.exception.NaoEncontradoException;
import com.racktracker.exception.RegraNegocioException;
import com.racktracker.model.Equipamento;
import com.racktracker.model.StatusConexao;
import com.racktracker.repository.EquipamentoRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class EquipamentoService {

    private final EquipamentoRepository equipamentoRepository;

    public EquipamentoService(EquipamentoRepository equipamentoRepository) {
        this.equipamentoRepository = equipamentoRepository;
    }

    /**
     * RF06 - Consultar inventario com filtros. Todos os filtros sao opcionais.
     * tipo: SWITCH, SERVIDOR ou NOBREAK | sala: nome da sala | rackId: id do rack | ip: comeco do IP (ex.: "192.168.1.")
     */
    public List<Equipamento> listar(String tipo, String sala, Long rackId, String ip) {
        List<Equipamento> resultado = new ArrayList<>();

        for (Equipamento e : equipamentoRepository.findAll()) {
            // O nome da classe (Switch, Servidor, Nobreak) e igual ao tipo, so muda maiuscula/minuscula
            if (temValor(tipo) && !e.getClass().getSimpleName().equalsIgnoreCase(tipo)) {
                continue;
            }
            if (temValor(sala) && (e.getRack() == null || !e.getRack().getSala().equalsIgnoreCase(sala))) {
                continue;
            }
            if (rackId != null && (e.getRack() == null || !e.getRack().getId().equals(rackId))) {
                continue;
            }
            if (temValor(ip) && (e.getIp() == null || !e.getIp().startsWith(ip))) {
                continue;
            }
            resultado.add(e);
        }
        return resultado;
    }

    public Equipamento buscar(Long id) {
        return equipamentoRepository.findById(id)
                .orElseThrow(() -> new NaoEncontradoException("Equipamento " + id + " nao encontrado"));
    }

    /** RF01 - Cadastrar equipamento. */
    public Equipamento criar(Equipamento equipamento) {
        if (equipamentoRepository.existsByPatrimonio(equipamento.getPatrimonio())) {
            throw new RegraNegocioException("Ja existe um equipamento com o patrimonio " + equipamento.getPatrimonio());
        }
        // Equipamento novo comeca fora de rack e sem teste de ping
        equipamento.setId(null);
        equipamento.setRack(null);
        equipamento.setPosicaoInicial(null);
        equipamento.setStatus(StatusConexao.NAO_TESTADO);
        equipamento.setFalhasSeguidas(0);
        return equipamentoRepository.save(equipamento);
    }

    /** RF02 - Editar equipamento (inclui ligar/desligar o modo manutencao). */
    public Equipamento atualizar(Long id, Equipamento dados) {
        Equipamento atual = buscar(id);

        if (!atual.getClass().equals(dados.getClass())) {
            throw new RegraNegocioException("Nao e possivel mudar o tipo do equipamento. Exclua e cadastre de novo.");
        }
        if (!atual.getPatrimonio().equals(dados.getPatrimonio())
                && equipamentoRepository.existsByPatrimonio(dados.getPatrimonio())) {
            throw new RegraNegocioException("Ja existe um equipamento com o patrimonio " + dados.getPatrimonio());
        }
        if (atual.getRack() != null && atual.getAlturaU() != dados.getAlturaU()) {
            throw new RegraNegocioException("Desaloque o equipamento antes de mudar o tamanho (U).");
        }

        // Localizacao e monitoramento NAO mudam pela edicao: so pela alocacao e pelo ping
        dados.setId(id);
        dados.setRack(atual.getRack());
        dados.setPosicaoInicial(atual.getPosicaoInicial());
        dados.setStatus(atual.getStatus());
        dados.setLatenciaMs(atual.getLatenciaMs());
        dados.setUltimoPing(atual.getUltimoPing());
        dados.setFalhasSeguidas(atual.getFalhasSeguidas());
        return equipamentoRepository.save(dados);
    }

    /** RF02 - Excluir equipamento. */
    public void excluir(Long id) {
        Equipamento equipamento = buscar(id);
        if (equipamento.getRack() != null) {
            // Obriga a desalocar antes, para a saida do rack ficar registrada na auditoria
            throw new RegraNegocioException("Desaloque o equipamento do rack antes de excluir.");
        }
        equipamentoRepository.delete(equipamento);
    }

    private boolean temValor(String texto) {
        return texto != null && !texto.isBlank();
    }
}
