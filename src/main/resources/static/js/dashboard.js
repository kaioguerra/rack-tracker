/* =========================================================
   dashboard.js - painel principal (RF10)
   ========================================================= */

async function carregarDashboard() {
    const resumo = await api("GET", "/api/dashboard");

    document.getElementById("kpiTotal").textContent = resumo.totalEquipamentos;
    document.getElementById("kpiOnline").textContent = resumo.online;
    document.getElementById("kpiOffline").textContent = resumo.offline;
    document.getElementById("kpiNaoTestados").textContent = resumo.naoTestados;
    document.getElementById("kpiManutencao").textContent =
        resumo.emManutencao > 0 ? resumo.emManutencao + " em manutenção" : "";

    // Anel de ocupacao geral (conic-gradient usa a variavel --p)
    document.getElementById("anelOcupacao").style.setProperty("--p", resumo.ocupacaoGeral);
    document.getElementById("ocupacaoGeral").textContent = resumo.ocupacaoGeral + "%";

    // Barras por rack
    const barras = document.getElementById("barrasRacks");
    barras.innerHTML = resumo.racks.map((r) => `
        <div class="barra-item" onclick="irParaRack(${r.rackId})" title="Abrir ${escapar(r.nome)}">
            <div class="barra-topo">
                <span class="barra-nome">${escapar(r.nome)}<small>${escapar(r.sala)}</small></span>
                <span class="barra-valor">${r.unidadesOcupadas}/${r.capacidadeU}U · ${r.percentual}%</span>
            </div>
            <div class="barra"><div class="barra-preench ${r.percentual >= 80 ? "alta" : ""}" data-largura="${r.percentual}"></div></div>
        </div>
    `).join("") || '<p class="vazio">Nenhum rack cadastrado.</p>';

    // anima as barras depois de desenhar
    requestAnimationFrame(() => {
        barras.querySelectorAll(".barra-preench").forEach((b) => (b.style.width = b.dataset.largura + "%"));
    });

    document.getElementById("ultimosAlertas").innerHTML = htmlAlertas(resumo.ultimosAlertas);
}

function htmlAlertas(alertas) {
    if (alertas.length === 0) {
        return '<li class="sem-itens"><i class="led led-on"></i> Nenhum alerta registrado. Tudo tranquilo por aqui.</li>';
    }
    return alertas.map((a) => `
        <li>
            <i class="led led-off"></i>
            <span class="alerta-texto">${escapar(a.mensagem)}<small>${escapar(a.ip || "")}</small></span>
            <time>${formatarData(a.dataHora)}</time>
        </li>
    `).join("");
}

/** Faz ping em todos os equipamentos que tem IP, todos ao mesmo tempo. */
async function pingarTodos() {
    const botao = document.getElementById("btnPingTodos");
    botao.disabled = true;
    botao.classList.add("carregando");

    try {
        const equipamentos = await api("GET", "/api/equipamentos");
        const comIp = equipamentos.filter((e) => e.ip);

        const resultados = await Promise.all(
            comIp.map((e) => api("POST", "/api/equipamentos/" + e.id + "/ping").catch(() => null))
        );

        const online = resultados.filter((r) => r && r.online).length;
        toast(`Ping concluído: ${online} de ${comIp.length} online`, "sucesso");

        const comAlerta = comIp.filter((e, i) => resultados[i] && resultados[i].alertaGerado);
        if (comAlerta.length > 0) {
            mostrarFaixaAlerta("ALERTA: " + comAlerta.map((e) => e.nome).join(", ") + " sem resposta ao ping!");
            atualizarBadgeAlertas();
        }
    } catch (e) {
        toast(e.message, "erro");
    } finally {
        botao.disabled = false;
        botao.classList.remove("carregando");
        atualizarPaginaAtual();
    }
}
