/* =========================================================
   auditoria.js - trilha de auditoria (RF08) e alertas (RF09)
   ========================================================= */

const NOMES_MOVIMENTACAO = {
    ALOCACAO: "ALOCAÇÃO",
    REMANEJAMENTO: "REMANEJAMENTO",
    DESALOCACAO: "DESALOCAÇÃO"
};

async function carregarAuditoria() {
    const lista = await api("GET", "/api/movimentacoes");

    document.getElementById("listaAuditoria").innerHTML = lista.length === 0
        ? '<p class="vazio">Nenhuma movimentação registrada.</p>'
        : lista.map((m) => `
            <li class="mov-${m.tipo}">
                <div class="painel mov-card">
                    <div class="mov-topo">
                        <span class="mov-tipo">${NOMES_MOVIMENTACAO[m.tipo]}</span>
                        <strong>${escapar(m.equipamentoNome)}</strong>
                        <span class="mono" style="color:var(--texto-3)">${escapar(m.equipamentoPatrimonio)}</span>
                        <time>${formatarData(m.dataHora)}</time>
                    </div>
                    <div class="mov-rota">
                        <span>${escapar(m.origem)}</span> <b>→</b> <span>${escapar(m.destino)}</span>
                    </div>
                    <div class="mov-rodape">por <em>${escapar(m.autor)}</em> · ${escapar(m.motivo)}</div>
                </div>
            </li>`).join("");
}

async function carregarAlertas() {
    const alertas = await api("GET", "/api/alertas");
    document.getElementById("listaAlertas").innerHTML = htmlAlertas(alertas);

    // Marca todos como vistos (o numero vermelho do menu some)
    if (alertas.length > 0) {
        try { localStorage.setItem("ultimoAlertaVisto", alertas[0].id); } catch (e) { /* ignora */ }
    }
    atualizarBadgeAlertas(alertas);
}

/** Numero vermelho no menu = alertas que chegaram depois da ultima visita a tela de alertas. */
async function atualizarBadgeAlertas(alertas) {
    try {
        if (!alertas) alertas = await api("GET", "/api/alertas");
        let ultimoVisto = 0;
        try { ultimoVisto = Number(localStorage.getItem("ultimoAlertaVisto")) || 0; } catch (e) { /* ignora */ }

        const novos = alertas.filter((a) => a.id > ultimoVisto).length;
        const badge = document.getElementById("badgeAlertas");
        badge.textContent = novos;
        badge.hidden = novos === 0;
    } catch (e) { /* sem conexao: a faixa de erro ja aparece */ }
}
