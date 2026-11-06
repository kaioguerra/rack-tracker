package com.racktracker.exception;

/** Erro de regra do sistema (ex.: patrimonio repetido, posicao ocupada). Vira HTTP 400. */
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
