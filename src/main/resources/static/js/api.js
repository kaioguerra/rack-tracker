/* =========================================================
   api.js - comunicacao com o backend (Spring Boot)
   Todas as chamadas passam por aqui.
   ========================================================= */

/**
 * Faz uma requisicao para a API e devolve o JSON da resposta.
 * Se o backend devolver erro ({"erro": "..."}), lanca um Error com a mensagem.
 */
async function api(metodo, url, corpo) {
    const opcoes = { method: metodo, headers: {} };
    if (corpo !== undefined) {
        opcoes.headers["Content-Type"] = "application/json";
        opcoes.body = JSON.stringify(corpo);
    }

    let resposta;
    try {
        resposta = await fetch(url, opcoes);
    } catch (e) {
        // Servidor desligado ou sem rede
        mostrarErroConexao(true);
        throw new Error("Sem conexão com o servidor");
    }

    // 503 = o backend nao conseguiu falar com o MySQL
    mostrarErroConexao(resposta.status === 503);

    if (resposta.status === 204) {
        return null; // DELETE com sucesso nao tem corpo
    }

    const dados = await resposta.json().catch(() => null);
    if (!resposta.ok) {
        throw new Error(dados && dados.erro ? dados.erro : "Erro " + resposta.status);
    }
    return dados;
}

function mostrarErroConexao(mostrar) {
    document.getElementById("faixaErro").hidden = !mostrar;
}
