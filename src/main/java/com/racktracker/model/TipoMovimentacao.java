package com.racktracker.model;

public enum TipoMovimentacao {
    ALOCACAO,      // equipamento entrou num rack
    REMANEJAMENTO, // mudou de posicao ou de rack
    DESALOCACAO    // saiu do rack
}
