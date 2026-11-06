/* =========================================================
   app.js - navegacao entre as telas (pelo # da URL)
   ========================================================= */

const PAGINAS = {
    dashboard: carregarDashboard,
    racks: carregarRacks,
    inventario: carregarInventario,
    auditoria: carregarAuditoria,
    alertas: carregarAlertas
};

function paginaAtual() {
    const nome = location.hash.replace("#", "");
    return PAGINAS[nome] ? nome : "dashboard";
}

async function atualizarPaginaAtual() {
    const nome = paginaAtual();

    // Mostra so a secao da pagina escolhida
    document.querySelectorAll(".pagina").forEach((sec) => {
        sec.hidden = sec.id !== "pagina-" + nome;
    });
    document.querySelectorAll(".menu a").forEach((link) => {
        link.classList.toggle("ativo", link.dataset.pagina === nome);
    });

    try {
        await PAGINAS[nome]();
    } catch (e) {
        toast(e.message, "erro");
    }
}

window.addEventListener("hashchange", atualizarPaginaAtual);

document.addEventListener("DOMContentLoaded", () => {
    iniciarAutor();
    atualizarPaginaAtual();
    atualizarBadgeAlertas();
});
