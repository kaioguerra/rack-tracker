# RackTracker

Plataforma integrada desenvolvida para a gestão de inventário, mapeamento físico em armários de bastidor (racks) e monitorização contínua de conectividade de ativos de infraestrutura de TI (switches, servidores e UPS/nobreaks).

---

## 🎯 Funcionalidades Principais

* **Inventário de Ativos:** Registo detalhado com controlo de número de série, património, endereço IP e especificações técnicas.
* **Mapeamento Físico de Racks:** Visualização interativa da disposição vertical em unidades (U), validação de espaço contíguo e prevenção de colisões físicas.
* **Trilha de Auditoria:** Registo cronológico imutável de todas as movimentações, alocações e manutenções de equipamentos.
* **Monitorização em Tempo Real:** Verificação periódica de disponibilidade via rede (Ping/ICMP) com indicação visual de status operacional.
* **Painel de Controlo (Dashboard):** Visão centralizada com taxas de ocupação física, métricas de disponibilidade e alertas visuais de incidentes.

---

## 🛠️ Tecnologias

* **Java 21** (LTS) + **Spring Boot 3** (Web, Data JPA, Validation)
* **MySQL 8**
* **Maven** (o projeto já vem com o `mvnw`, não precisa instalar o Maven)
* **HTML, CSS e JavaScript puro** no frontend (pasta `src/main/resources/static`)
* **Postman** para testar a API (`postman/RackTracker.postman_collection.json`)

> Quem tem um JDK mais novo (ex.: 24) instalado consegue rodar normalmente: o JDK compila o projeto como Java 21.

---

## ▶️ Como rodar

1. Tenha o **MySQL** rodando na porta 3306.
2. Abra `src/main/resources/application.properties` e coloque o usuário e a senha do **seu** MySQL:
   ```properties
   spring.datasource.username=root
   spring.datasource.password=root
   ```
   O banco `rack_tracker` e as tabelas são criados sozinhos.
3. Rode o projeto:
   * **VS Code:** abra `RackTrackerApplication.java` e clique em **Run** (precisa do *Extension Pack for Java*).
   * **Terminal:** `./mvnw spring-boot:run` (Linux/Mac) ou `mvnw.cmd spring-boot:run` (Windows).
4. Acesse **http://localhost:8080** no navegador.

Na primeira execução o sistema cadastra **3 racks e 13 equipamentos de exemplo**. Os IPs `127.0.0.x` respondem ao ping (ficam **online**) e os IPs `192.0.2.x` nunca respondem (ficam **offline** e geram alerta depois de 2 pings).

Para voltar aos dados iniciais, apague o banco no MySQL (`DROP DATABASE rack_tracker;`) e rode de novo.

---

## 📁 Estrutura do código

```
src/main/java/com/racktracker
├── model/          Entidades (tabelas): Equipamento (abstrata), Switch, Servidor, Nobreak,
│                   Rack, Movimentacao (auditoria), Alerta
├── repository/     Interfaces do Spring Data JPA (acesso ao banco)
├── service/        Regras de negócio (colisão no rack, ping, alertas, dashboard)
├── controller/     Endpoints REST chamados pelo frontend e pelo Postman
├── dto/            Objetos de entrada/saída que não são tabelas
├── exception/      Erros do sistema e o tratador que devolve {"erro": "..."}
└── config/         DadosIniciais (carga de exemplo)
```

**Herança:** `Equipamento` é abstrata e `Switch`, `Servidor` e `Nobreak` herdam dela. No MySQL todas ficam na tabela `equipamento` (estratégia `SINGLE_TABLE`), e a coluna `tipo` diz qual é qual. Cada subclasse implementa `isCritico()`: switch e servidor geram alerta, nobreak não.

---

## 🔌 Endpoints da API

| Método | URL | Requisito |
|---|---|---|
| GET | `/api/equipamentos?tipo=&sala=&rackId=&ip=` | RF06 – listar com filtros |
| GET | `/api/equipamentos/{id}` | – |
| POST | `/api/equipamentos` | RF01 – cadastrar |
| PUT | `/api/equipamentos/{id}` | RF02 – editar (inclui modo manutenção) |
| DELETE | `/api/equipamentos/{id}` | RF02 – excluir |
| POST | `/api/equipamentos/{id}/ping` | RF07 – ping sob demanda (+ RF09) |
| GET / POST | `/api/racks` | RF03 – listar / cadastrar rack |
| GET / PUT / DELETE | `/api/racks/{id}` | – |
| GET | `/api/racks/{id}/equipamentos` | equipamentos montados no rack |
| POST | `/api/movimentacoes/alocar` | RF04 – alocar ou remanejar (+ RF08) |
| POST | `/api/movimentacoes/desalocar` | RF05 – desalocar (+ RF08) |
| GET | `/api/movimentacoes` | RF08 – trilha de auditoria |
| GET | `/api/alertas` | RF09 – alertas |
| GET | `/api/dashboard` | RF10 – resumo do painel |

Exemplo de cadastro (o campo `tipo` define a subclasse):

```json
POST /api/equipamentos
{
  "tipo": "SERVIDOR",
  "nome": "SRV-APP-02",
  "modelo": "Dell PowerEdge R760",
  "patrimonio": "PAT-0100",
  "ip": "127.0.0.20",
  "alturaU": 2,
  "sistemaOperacional": "Ubuntu Server 24.04"
}
```

---

## 📬 Postman

Importe `postman/RackTracker.postman_collection.json` no Postman (**Import → File**). A variável `baseUrl` já aponta para `http://localhost:8080`. Cada requisição tem um teste simples de status, então dá para rodar a coleção inteira pelo **Runner** com o banco recém-criado.

---

## 👥 Autores

* **Felipe da Costa**
* **Hugo Gomide**
* **Kaio Guerra**
* **Leonardo Miguel**


*Projeto desenvolvido no âmbito da unidade curricular de Projeto de Software.*
