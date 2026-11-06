/* =========================================================
   inventario.js - lista com filtros (RF06), cadastro e edicao
   de equipamentos (RF01, RF02) e ping (RF07)
   ========================================================= */

async function carregarInventario() {
    await preencherFiltros();

    // Monta a URL so com os filtros preenchidos. Ex.: /api/equipamentos?tipo=SWITCH&ip=127.0.0.
    const params = new URLSearchParams();
    const filtros = { tipo: "filtroTipo", sala: "filtroSala", rackId: "filtroRack", ip: "filtroIp" };
    for (const [nome, idCampo] of Object.entries(filtros)) {
        const valor = document.getElementById(idCampo).value.trim();
        if (valor) params.append(nome, valor);
    }

    const lista = await api("GET", "/api/equipamentos?" + params.toString());

    document.getElementById("inventarioVazio").hidden = lista.length > 0;
    document.getElementById("tabelaInventario").innerHTML = lista.map((e) => `
        <tr>
            <td>
                <span class="status-cel">
                    <i class="led ${classeLed(e)}"></i> ${textoStatus(e)}
                    ${e.status === "ONLINE" && e.latenciaMs !== null ? `<small>${e.latenciaMs} ms</small>` : ""}
                </span>
            </td>
            <td><strong>${escapar(e.nome)}</strong></td>
            <td>
                <span class="etiqueta etiqueta-${e.tipo}">${NOMES_TIPO[e.tipo]}</span>
            </td>
            <td>${escapar(e.modelo)}</td>
            <td class="mono">${escapar(e.patrimonio)}</td>
            <td class="mono">${escapar(e.ip || "—")}</td>
            <td class="nowrap">${e.rack ? escapar(e.rack.nome) + " · " + textoPosicao(e) : '<span style="color:var(--texto-3)">Sem rack</span>'}</td>
            <td>
                <div class="acoes">
                    <button class="btn btn-claro btn-mini" ${e.ip ? "" : "disabled title='Sem IP cadastrado'"}
                            onclick="testarPing(${e.id}).then(carregarInventario)">Ping</button>
                    <button class="btn btn-claro btn-mini" onclick="abrirModalEquipamento(${e.id})">Editar</button>
                    <button class="btn btn-perigo-claro btn-mini" onclick="excluirEquipamento(${e.id}, '${escapar(e.nome)}')">Excluir</button>
                </div>
            </td>
        </tr>
    `).join("");
}

/** Preenche os selects de sala e rack com o que existe no banco. */
async function preencherFiltros() {
    const lista = await api("GET", "/api/racks");
    const selectSala = document.getElementById("filtroSala");
    const selectRack = document.getElementById("filtroRack");
    const salaEscolhida = selectSala.value;
    const rackEscolhido = selectRack.value;

    const salas = [...new Set(lista.map((r) => r.sala))];
    selectSala.innerHTML = '<option value="">Todas as salas</option>' +
        salas.map((s) => `<option ${s === salaEscolhida ? "selected" : ""}>${escapar(s)}</option>`).join("");
    selectRack.innerHTML = '<option value="">Todos os racks</option>' +
        lista.map((r) => `<option value="${r.id}" ${String(r.id) === rackEscolhido ? "selected" : ""}>${escapar(r.nome)}</option>`).join("");
}

function limparFiltros() {
    document.getElementById("formFiltros").reset();
    carregarInventario();
}

/* ---------- Ping (RF07) e alerta (RF09) ---------- */

async function testarPing(id) {
    try {
        const r = await api("POST", "/api/equipamentos/" + id + "/ping");
        if (r.online) {
            toast(`${r.ip} respondeu em ${r.latenciaMs} ms`, "sucesso");
        } else {
            toast(`${r.ip} não respondeu (${r.falhasSeguidas} falha(s) seguida(s))`, "erro");
        }
        if (r.alertaGerado) {
            mostrarFaixaAlerta(`ALERTA: o equipamento ${r.ip} falhou no ping ${r.falhasSeguidas} vezes seguidas!`);
            atualizarBadgeAlertas();
        }
    } catch (e) {
        toast(e.message, "erro");
    }
}

/* ---------- Cadastro / edicao (RF01, RF02) ---------- */

async function abrirModalEquipamento(id) {
    const editando = Boolean(id);
    const e = editando ? await api("GET", "/api/equipamentos/" + id) : { tipo: "SERVIDOR", alturaU: 1 };
    const v = (campo) => (e[campo] === null || e[campo] === undefined ? "" : escapar(e[campo]));

    abrirModal(editando ? "Editar equipamento" : "Novo equipamento", `
        <form class="form" onsubmit="event.preventDefault(); salvarEquipamento(${editando ? id : "null"});">
            <div class="campo">
                <label>Tipo</label>
                <select id="eqTipo" ${editando ? "disabled" : ""} onchange="trocarCamposTipo()">
                    <option value="SERVIDOR" ${e.tipo === "SERVIDOR" ? "selected" : ""}>Servidor</option>
                    <option value="SWITCH" ${e.tipo === "SWITCH" ? "selected" : ""}>Switch</option>
                    <option value="NOBREAK" ${e.tipo === "NOBREAK" ? "selected" : ""}>Nobreak</option>
                </select>
            </div>
            <div class="campo">
                <label>Nome</label>
                <input id="eqNome" required value="${v("nome")}" placeholder="Ex.: SRV-DB-02">
            </div>
            <div class="campo">
                <label>Modelo</label>
                <input id="eqModelo" required value="${v("modelo")}" placeholder="Ex.: Dell PowerEdge R750">
            </div>
            <div class="campo">
                <label>Patrimônio / nº de série</label>
                <input id="eqPatrimonio" required value="${v("patrimonio")}" placeholder="Ex.: PAT-0020">
            </div>
            <div class="campo">
                <label>IP</label>
                <input id="eqIp" value="${v("ip")}" placeholder="Ex.: 192.168.0.10">
            </div>
            <div class="campo">
                <label>Tamanho (U)</label>
                <input id="eqAltura" type="number" min="1" max="42" required value="${e.alturaU}">
            </div>

            <div class="campo campo-cheio" data-tipo="SERVIDOR">
                <label>Sistema operacional</label>
                <input id="eqSo" value="${v("sistemaOperacional")}" placeholder="Ex.: Ubuntu Server 24.04">
            </div>
            <div class="campo campo-cheio" data-tipo="SWITCH">
                <label>Quantidade de portas</label>
                <input id="eqPortas" type="number" min="1" value="${v("quantidadePortas")}" placeholder="Ex.: 48">
            </div>
            <div class="campo campo-cheio" data-tipo="NOBREAK">
                <label>Potência (VA)</label>
                <input id="eqPotencia" type="number" min="1" value="${v("potenciaVA")}" placeholder="Ex.: 3000">
            </div>

            <label class="campo campo-check campo-cheio">
                <input id="eqManutencao" type="checkbox" ${e.emManutencao ? "checked" : ""}>
                <span>Modo manutenção <small style="color:var(--texto-3)">(não gera alertas)</small></span>
            </label>

            <p class="form-erro" id="formErro" hidden></p>
            <div class="form-botoes">
                <button type="button" class="btn btn-claro" onclick="fecharModal()">Cancelar</button>
                <button type="submit" class="btn btn-primario">Salvar</button>
            </div>
        </form>`);

    trocarCamposTipo();
}

/** Mostra so os campos especificos do tipo escolhido. */
function trocarCamposTipo() {
    const tipo = document.getElementById("eqTipo").value;
    document.querySelectorAll("[data-tipo]").forEach((div) => {
        div.hidden = div.dataset.tipo !== tipo;
    });
}

async function salvarEquipamento(id) {
    const numero = (idCampo) => {
        const valor = document.getElementById(idCampo).value;
        return valor === "" ? null : Number(valor);
    };

    // O campo "tipo" diz ao Spring qual subclasse criar (Switch, Servidor ou Nobreak)
    const dados = {
        tipo: document.getElementById("eqTipo").value,
        nome: document.getElementById("eqNome").value,
        modelo: document.getElementById("eqModelo").value,
        patrimonio: document.getElementById("eqPatrimonio").value,
        ip: document.getElementById("eqIp").value.trim() || null,
        alturaU: numero("eqAltura"),
        emManutencao: document.getElementById("eqManutencao").checked,
        sistemaOperacional: document.getElementById("eqSo").value || null,
        quantidadePortas: numero("eqPortas"),
        potenciaVA: numero("eqPotencia")
    };

    try {
        if (id) {
            await api("PUT", "/api/equipamentos/" + id, dados);
        } else {
            await api("POST", "/api/equipamentos", dados);
        }
        fecharModal();
        toast("Equipamento salvo", "sucesso");
        atualizarPaginaAtual();
    } catch (e) {
        erroNoFormulario(e.message);
    }
}

async function excluirEquipamento(id, nome) {
    if (!confirm(`Excluir o equipamento ${nome}?`)) return;
    try {
        await api("DELETE", "/api/equipamentos/" + id);
        toast("Equipamento excluído", "sucesso");
        carregarInventario();
    } catch (e) {
        toast(e.message, "erro");
    }
}
