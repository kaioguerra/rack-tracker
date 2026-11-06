/* =========================================================
   racks.js - desenho do rack e alocacao (RF03, RF04, RF05)
   ========================================================= */

let racks = [];          // lista de racks cadastrados
let rackAtualId = null;  // rack que esta aberto na tela
let equipamentosDoRack = [];

async function carregarRacks() {
    racks = await api("GET", "/api/racks");

    if (racks.length === 0) {
        rackAtualId = null;
    } else if (!racks.some((r) => r.id === rackAtualId)) {
        rackAtualId = racks[0].id;
    }

    desenharAbas();
    await desenharRack();
    await desenharEstoque();
}

function irParaRack(id) {
    rackAtualId = id;
    location.hash = "#racks";
}

function desenharAbas() {
    document.getElementById("abasRacks").innerHTML = racks.map((r) => `
        <button class="aba-rack ${r.id === rackAtualId ? "ativa" : ""}" onclick="trocarRack(${r.id})">
            ${escapar(r.nome)}<small>${escapar(r.sala)}</small>
        </button>
    `).join("");
}

async function trocarRack(id) {
    rackAtualId = id;
    desenharAbas();
    await desenharRack();
}

function rackAtual() {
    return racks.find((r) => r.id === rackAtualId);
}

/**
 * Desenha o rack com CSS Grid.
 * A linha 1 do grid e o topo (U mais alta) e a ultima linha e a U01 (base).
 * Formula: linha do grid = capacidade - U + 1
 */
async function desenharRack() {
    const rack = rackAtual();
    const slots = document.getElementById("rackSlots");

    if (!rack) {
        slots.innerHTML = "";
        document.getElementById("rackTitulo").textContent = "Nenhum rack cadastrado";
        return;
    }

    equipamentosDoRack = await api("GET", "/api/racks/" + rack.id + "/equipamentos");
    const cap = rack.capacidadeU;

    document.getElementById("rackTitulo").textContent = rack.nome.toUpperCase() + " · " + cap + "U";

    // Numeros das U dos dois lados (de cima para baixo: 42, 41, ..., 1)
    let numeros = "";
    for (let u = cap; u >= 1; u--) {
        numeros += `<span>${String(u).padStart(2, "0")}</span>`;
    }
    document.getElementById("rackNumerosEsq").innerHTML = numeros;
    document.getElementById("rackNumerosDir").innerHTML = numeros;

    // Marca quais U estao ocupadas
    const ocupada = new Array(cap + 1).fill(false);
    let unidadesOcupadas = 0;
    equipamentosDoRack.forEach((e) => {
        for (let u = e.posicaoInicial; u <= e.posicaoFinal; u++) ocupada[u] = true;
        unidadesOcupadas += e.alturaU;
    });

    let html = "";

    // Posicoes vazias (clicaveis para alocar)
    for (let u = 1; u <= cap; u++) {
        if (!ocupada[u]) {
            html += `<div class="slot-vazio" style="grid-row:${cap - u + 1}" title="U${u} livre"
                          onclick="abrirModalAlocar(${u})"></div>`;
        }
    }

    // Equipamentos montados
    equipamentosDoRack.forEach((e) => {
        const linha = cap - e.posicaoFinal + 1;
        html += `
            <div class="equip equip-${e.tipo} ${e.alturaU > 1 ? "equip-alto" : ""} ${e.emManutencao ? "manutencao" : ""}"
                 style="grid-row:${linha} / span ${e.alturaU}"
                 data-id="${e.id}"
                 onclick="abrirDetalheEquipamento(${e.id})">
                <i class="led ${classeLed(e)}"></i>
                <span class="equip-nome">${escapar(e.nome)}</span>
                <span class="equip-detalhe">${desenhoFrontal(e)}</span>
            </div>`;
    });

    slots.style.gridTemplateRows = `repeat(${cap}, var(--altura-u))`;
    slots.innerHTML = html;

    // Tooltip ao passar o mouse
    slots.querySelectorAll(".equip").forEach((div) => {
        const e = equipamentosDoRack.find((x) => x.id === Number(div.dataset.id));
        div.addEventListener("mouseenter", (ev) => mostrarTooltip(ev, htmlTooltip(e)));
        div.addEventListener("mousemove", moverTooltip);
        div.addEventListener("mouseleave", esconderTooltip);
    });

    // Painel lateral com as informacoes do rack
    const percentual = Math.round((unidadesOcupadas / cap) * 1000) / 10;
    document.getElementById("infoRackNome").textContent = rack.nome;
    document.getElementById("infoRackSala").textContent = rack.sala;
    document.getElementById("infoRackCapacidade").textContent = cap + "U";
    document.getElementById("infoRackOcupado").textContent = `${unidadesOcupadas}U (${percentual}%)`;
    const barra = document.getElementById("infoRackBarra");
    barra.classList.toggle("alta", percentual >= 80);
    requestAnimationFrame(() => (barra.style.width = percentual + "%"));
}

/** Pequeno desenho do painel frontal, diferente para cada tipo. */
function desenhoFrontal(e) {
    if (e.tipo === "SWITCH") {
        return `<span class="portas">${"<i></i>".repeat(12)}</span>`;
    }
    if (e.tipo === "SERVIDOR") {
        return `<span class="baias">${"<i></i>".repeat(4)}</span>`;
    }
    return `<span class="visor">${e.potenciaVA ? e.potenciaVA + "VA" : "UPS"}</span>`;
}

function htmlTooltip(e) {
    return `
        <strong><i class="led ${classeLed(e)}"></i>${escapar(e.nome)}</strong>
        <dl>
            <dt>Tipo</dt><dd>${NOMES_TIPO[e.tipo]}</dd>
            <dt>Modelo</dt><dd>${escapar(e.modelo)}</dd>
            <dt>Patrimônio</dt><dd>${escapar(e.patrimonio)}</dd>
            <dt>IP</dt><dd>${escapar(e.ip || "—")}</dd>
            <dt>Posição</dt><dd>${textoPosicao(e)} (${e.alturaU}U)</dd>
            <dt>Status</dt><dd>${textoStatus(e)}${e.latenciaMs !== null && e.status === "ONLINE" ? " · " + e.latenciaMs + " ms" : ""}</dd>
        </dl>`;
}

/** Lista dos equipamentos que nao estao em nenhum rack. */
async function desenharEstoque() {
    const todos = await api("GET", "/api/equipamentos");
    const semRack = todos.filter((e) => !e.rack);

    document.getElementById("listaEstoque").innerHTML = semRack.length === 0
        ? '<li class="vazio-mini">Todos os equipamentos estão em racks.</li>'
        : semRack.map((e) => `
            <li>
                <span class="etiqueta etiqueta-${e.tipo}">${NOMES_TIPO[e.tipo]}</span>
                <span class="estoque-nome">${escapar(e.nome)}</span>
                <span class="mono" style="color:var(--texto-3)">${e.alturaU}U</span>
            </li>`).join("");
}

/* ---------- Alocar (clique numa U vazia) ---------- */

async function abrirModalAlocar(u) {
    const rack = rackAtual();
    const todos = await api("GET", "/api/equipamentos");
    const semRack = todos.filter((e) => !e.rack);

    if (semRack.length === 0) {
        toast("Não há equipamentos sem rack. Cadastre um novo ou desaloque algum.", "info");
        return;
    }

    abrirModal(`Alocar em ${rack.nome} · U${u}`, `
        <form class="form" onsubmit="event.preventDefault(); confirmarAlocacao();">
            <div class="campo campo-cheio">
                <label>Equipamento</label>
                <select id="alocEquip">
                    ${semRack.map((e) => `<option value="${e.id}">${escapar(e.nome)} — ${NOMES_TIPO[e.tipo]} (${e.alturaU}U)</option>`).join("")}
                </select>
            </div>
            <div class="campo">
                <label>Posição inicial (U)</label>
                <input id="alocPosicao" type="number" min="1" max="${rack.capacidadeU}" value="${u}" required>
            </div>
            <div class="campo">
                <label>Autor</label>
                <input id="alocAutor" type="text" value="${escapar(autorAtual())}" required>
            </div>
            <div class="campo campo-cheio">
                <label>Motivo</label>
                <textarea id="alocMotivo" required placeholder="Ex.: Instalação de novo servidor de banco"></textarea>
            </div>
            <p class="form-erro" id="formErro" hidden></p>
            <div class="form-botoes">
                <button type="button" class="btn btn-claro" onclick="fecharModal()">Cancelar</button>
                <button type="submit" class="btn btn-primario">Alocar</button>
            </div>
        </form>`);
}

async function confirmarAlocacao() {
    try {
        await api("POST", "/api/movimentacoes/alocar", {
            equipamentoId: Number(document.getElementById("alocEquip").value),
            rackId: rackAtualId,
            posicaoInicial: Number(document.getElementById("alocPosicao").value),
            autor: document.getElementById("alocAutor").value,
            motivo: document.getElementById("alocMotivo").value
        });
        fecharModal();
        toast("Equipamento alocado com sucesso", "sucesso");
        await carregarRacks();
    } catch (e) {
        erroNoFormulario(e.message);
    }
}

/* ---------- Detalhe do equipamento (clique no equipamento) ---------- */

async function abrirDetalheEquipamento(id) {
    esconderTooltip();
    const e = await api("GET", "/api/equipamentos/" + id);

    abrirModal(e.nome, `
        <dl class="detalhe-equip">
            <dt>Status</dt><dd><i class="led ${classeLed(e)}"></i> ${textoStatus(e)}${e.latenciaMs !== null && e.status === "ONLINE" ? " · " + e.latenciaMs + " ms" : ""}</dd>
            <dt>Tipo</dt><dd>${NOMES_TIPO[e.tipo]}</dd>
            <dt>Modelo</dt><dd>${escapar(e.modelo)}</dd>
            <dt>Patrimônio</dt><dd>${escapar(e.patrimonio)}</dd>
            <dt>IP</dt><dd>${escapar(e.ip || "—")}</dd>
            <dt>Local</dt><dd>${e.rack ? escapar(e.rack.nome) + " · " + textoPosicao(e) : "Sem rack"}</dd>
            <dt>Último ping</dt><dd>${formatarData(e.ultimoPing)}</dd>
        </dl>
        <div class="acoes-modal">
            <button class="btn btn-primario" ${e.ip ? "" : "disabled title='Sem IP cadastrado'"}
                    onclick="testarPing(${e.id}).then(() => abrirDetalheEquipamento(${e.id}))">Testar ping</button>
            <button class="btn btn-claro" onclick="abrirModalEquipamento(${e.id})">Editar</button>
        </div>

        <form class="form secao-modal" onsubmit="event.preventDefault(); confirmarMovimentacao(${e.id});">
            <div class="campo">
                <label>Ação</label>
                <select id="movAcao" onchange="document.getElementById('camposRemanejar').hidden = this.value !== 'remanejar'">
                    <option value="desalocar">Desalocar (tirar do rack)</option>
                    <option value="remanejar">Remanejar (mudar posição/rack)</option>
                </select>
            </div>
            <div class="campo">
                <label>Autor</label>
                <input id="movAutor" type="text" value="${escapar(autorAtual())}" required>
            </div>
            <div class="form campo-cheio" id="camposRemanejar" hidden>
                <div class="campo">
                    <label>Rack de destino</label>
                    <select id="movRack">
                        ${racks.map((r) => `<option value="${r.id}" ${r.id === e.rack?.id ? "selected" : ""}>${escapar(r.nome)} (${escapar(r.sala)})</option>`).join("")}
                    </select>
                </div>
                <div class="campo">
                    <label>Nova posição inicial (U)</label>
                    <input id="movPosicao" type="number" min="1" value="${e.posicaoInicial || 1}">
                </div>
            </div>
            <div class="campo campo-cheio">
                <label>Motivo</label>
                <textarea id="movMotivo" required placeholder="Ex.: Enviado para reparo"></textarea>
            </div>
            <p class="form-erro" id="formErro" hidden></p>
            <div class="form-botoes">
                <button type="submit" class="btn btn-perigo-claro">Confirmar movimentação</button>
            </div>
        </form>`);
}

async function confirmarMovimentacao(equipamentoId) {
    const acao = document.getElementById("movAcao").value;
    const autor = document.getElementById("movAutor").value;
    const motivo = document.getElementById("movMotivo").value;

    try {
        if (acao === "desalocar") {
            await api("POST", "/api/movimentacoes/desalocar", { equipamentoId, autor, motivo });
            toast("Equipamento desalocado", "sucesso");
        } else {
            await api("POST", "/api/movimentacoes/alocar", {
                equipamentoId,
                rackId: Number(document.getElementById("movRack").value),
                posicaoInicial: Number(document.getElementById("movPosicao").value),
                autor,
                motivo
            });
            toast("Equipamento remanejado", "sucesso");
        }
        fecharModal();
        atualizarPaginaAtual();
    } catch (e) {
        erroNoFormulario(e.message);
    }
}

/* ---------- Cadastro / edicao de rack (RF03) ---------- */

function abrirModalRack(rack) {
    const editando = Boolean(rack);
    abrirModal(editando ? "Editar rack" : "Novo rack", `
        <form class="form" onsubmit="event.preventDefault(); salvarRack(${editando ? rack.id : "null"});">
            <div class="campo campo-cheio">
                <label>Nome</label>
                <input id="rackNome" type="text" required value="${editando ? escapar(rack.nome) : ""}" placeholder="Ex.: Rack C1">
            </div>
            <div class="campo">
                <label>Sala / setor</label>
                <input id="rackSala" type="text" required value="${editando ? escapar(rack.sala) : ""}" placeholder="Ex.: Sala 101 - Datacenter">
            </div>
            <div class="campo">
                <label>Capacidade (U)</label>
                <input id="rackCapacidade" type="number" min="1" max="42" required value="${editando ? rack.capacidadeU : 42}">
            </div>
            <p class="form-erro" id="formErro" hidden></p>
            <div class="form-botoes">
                <button type="button" class="btn btn-claro" onclick="fecharModal()">Cancelar</button>
                <button type="submit" class="btn btn-primario">Salvar</button>
            </div>
        </form>`);
}

async function salvarRack(id) {
    const dados = {
        nome: document.getElementById("rackNome").value,
        sala: document.getElementById("rackSala").value,
        capacidadeU: Number(document.getElementById("rackCapacidade").value)
    };
    try {
        const salvo = id
            ? await api("PUT", "/api/racks/" + id, dados)
            : await api("POST", "/api/racks", dados);
        rackAtualId = salvo.id;
        fecharModal();
        toast("Rack salvo", "sucesso");
        await carregarRacks();
    } catch (e) {
        erroNoFormulario(e.message);
    }
}

function editarRackAtual() {
    if (rackAtual()) abrirModalRack(rackAtual());
}

async function excluirRackAtual() {
    const rack = rackAtual();
    if (!rack || !confirm(`Excluir o ${rack.nome}?`)) return;
    try {
        await api("DELETE", "/api/racks/" + rack.id);
        toast("Rack excluído", "sucesso");
        rackAtualId = null;
        await carregarRacks();
    } catch (e) {
        toast(e.message, "erro");
    }
}
