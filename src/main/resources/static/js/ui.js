/* =========================================================
   ui.js - funcoes visuais reutilizadas em todas as telas
   ========================================================= */

/** Evita que textos digitados pelo usuario virem HTML (seguranca contra XSS). */
function escapar(texto) {
    if (texto === null || texto === undefined) return "";
    return String(texto)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#39;");
}

/** "2026-11-06T17:45:16.25" -> "06/11/2026 17:45" */
function formatarData(iso) {
    if (!iso) return "—";
    const d = new Date(iso);
    return d.toLocaleDateString("pt-BR") + " " + d.toLocaleTimeString("pt-BR", { hour: "2-digit", minute: "2-digit" });
}

const NOMES_TIPO = { SWITCH: "Switch", SERVIDOR: "Servidor", NOBREAK: "Nobreak" };

/** Classe CSS do LED de acordo com o status do equipamento. */
function classeLed(equip) {
    if (equip.emManutencao) return "led-manut";
    if (equip.status === "ONLINE") return "led-on";
    if (equip.status === "OFFLINE") return "led-off";
    return "led-nt";
}

function textoStatus(equip) {
    if (equip.emManutencao) return "Manutenção";
    if (equip.status === "ONLINE") return "Online";
    if (equip.status === "OFFLINE") return "Offline";
    return "Não testado";
}

/** "U10" ou "U10–U12" */
function textoPosicao(equip) {
    if (!equip.rack) return "Sem rack";
    if (equip.alturaU === 1) return "U" + equip.posicaoInicial;
    return "U" + equip.posicaoInicial + "–U" + equip.posicaoFinal;
}

/* ---------- Toasts (notificacoes rapidas) ---------- */

function toast(mensagem, tipo = "info") {
    const div = document.createElement("div");
    div.className = "toast " + tipo;
    div.textContent = mensagem;
    document.getElementById("toasts").appendChild(div);
    setTimeout(() => {
        div.classList.add("saindo");
        setTimeout(() => div.remove(), 250);
    }, 3500);
}

/* ---------- Modal ---------- */

function abrirModal(titulo, html) {
    document.getElementById("modalTitulo").textContent = titulo;
    document.getElementById("modalCorpo").innerHTML = html;
    document.getElementById("modalFundo").hidden = false;
    const primeiroCampo = document.querySelector("#modalCorpo input, #modalCorpo select");
    if (primeiroCampo) primeiroCampo.focus();
}

function fecharModal() {
    document.getElementById("modalFundo").hidden = true;
}

/** Mostra a mensagem de erro dentro do formulario do modal. */
function erroNoFormulario(mensagem) {
    const caixa = document.getElementById("formErro");
    if (caixa) {
        caixa.textContent = mensagem;
        caixa.hidden = false;
    } else {
        toast(mensagem, "erro");
    }
}

document.addEventListener("keydown", (e) => {
    if (e.key === "Escape") fecharModal();
});

/* ---------- Autor (nome do operador para a auditoria) ---------- */

function autorAtual() {
    return document.getElementById("autorGlobal").value.trim();
}

function iniciarAutor() {
    const campo = document.getElementById("autorGlobal");
    try { campo.value = localStorage.getItem("autor") || ""; } catch (e) { /* navegador sem storage */ }
    campo.addEventListener("input", () => {
        try { localStorage.setItem("autor", campo.value); } catch (e) { /* ignora */ }
    });
}

/* ---------- Alerta critico (RF09): faixa vermelha + som ---------- */

function mostrarFaixaAlerta(texto) {
    document.getElementById("faixaAlertaTexto").textContent = texto;
    document.getElementById("faixaAlerta").hidden = false;
    tocarSirene();
}

function fecharFaixaAlerta() {
    document.getElementById("faixaAlerta").hidden = true;
}

/** Gera um "bip-bip" usando a Web Audio API (nao precisa de arquivo de som). */
function tocarSirene() {
    try {
        const ctx = new (window.AudioContext || window.webkitAudioContext)();
        [0, 0.25, 0.5].forEach((inicio, i) => {
            const osc = ctx.createOscillator();
            const ganho = ctx.createGain();
            osc.type = "square";
            osc.frequency.value = i % 2 === 0 ? 880 : 660;
            ganho.gain.value = 0.06;
            osc.connect(ganho).connect(ctx.destination);
            osc.start(ctx.currentTime + inicio);
            osc.stop(ctx.currentTime + inicio + 0.18);
        });
    } catch (e) { /* navegador sem audio */ }
}

/* ---------- Tooltip do rack ---------- */

function mostrarTooltip(evento, html) {
    const tip = document.getElementById("tooltip");
    tip.innerHTML = html;
    tip.hidden = false;
    moverTooltip(evento);
}

function moverTooltip(evento) {
    const tip = document.getElementById("tooltip");
    let x = evento.clientX + 16;
    let y = evento.clientY + 16;
    // nao deixa o balao sair da tela
    if (x + tip.offsetWidth > window.innerWidth - 8) x = evento.clientX - tip.offsetWidth - 16;
    if (y + tip.offsetHeight > window.innerHeight - 8) y = evento.clientY - tip.offsetHeight - 16;
    tip.style.left = x + "px";
    tip.style.top = y + "px";
}

function esconderTooltip() {
    document.getElementById("tooltip").hidden = true;
}
