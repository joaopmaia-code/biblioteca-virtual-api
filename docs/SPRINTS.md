# Acompanhamento das sprints

Registro do que cada integrante entregou em cada sprint, das dificuldades e do próximo passo.
Serve para os acompanhamentos com o professor e para a descrição das contribuições na entrega.
O planejado está no [ROADMAP](ROADMAP.md); aqui fica o que de fato aconteceu, com o número do PR.

Cada pessoa atualiza a própria parte. Datas no horário de Brasília.

---

## Sprint 1 (16/10 a 22/10): base e CRUDs simples

### Pessoa 1 — Pedro Affonso
- **Entregas:** o Model e as migrations de Autor e Editora ficaram para a Sprint 2 (PR #4, 28/10).
- **Dificuldades:** começo atrasado; a parte da Pessoa 1 foi dividida em blocos menores (Model e
  migrations, CRUD de Autor, CRUD de Editora, documentação) para recuperar o prazo.

### Pessoa 2 — João Pedro
- **Entregas (16/10):**
  - Setup do projeto: Spring Boot 3.5, MySQL 8.4 no Docker, Flyway, Swagger, Maven Wrapper.
  - PR #1: `GlobalExceptionHandler` e formato padrão de erro (400, 404, 409), com testes.
  - PR #2: CRUD completo de Categoria (nome único com 409), migration `V20`, com testes.
  - PR #3: Model de Material (migration `V21`), README, REQUISITOS, ROADMAP e registro de uso de IA.
- **Dificuldades:**
  - O Spring Initializr só oferece o Spring Boot 4; o `pom.xml` foi montado com a última versão 3.5.x.
  - Requisições com acento falhavam no `curl` do Git Bash por causa da codificação do terminal, não da API;
    resolvido enviando o corpo a partir de um arquivo UTF-8.
- **Próximo passo:** CRUD de Material, que depende das tabelas `autores` e `editoras` da Pessoa 1.

### Pessoa 3 — Bárbara Tavares
- **Entregas:** _a preencher pela Pessoa 3._
- **Dificuldades:** _a preencher pela Pessoa 3._

---

## Sprint 2 (23/10 a 29/10): CRUDs principais

### Pessoa 1 — Pedro Affonso
- **Entregas (28/10):** PR #4: entidades Autor e Editora, repositories e migrations `V10` e `V11`.
- **Dificuldades:** o Docker Desktop travou (erro 500 na API) e as migrations só puderam ser conferidas no
  MySQL depois do merge; ficou resolvido encerrando o Docker e rodando `wsl --shutdown`. As duas
  migrations subiram sem erro na primeira verificação (01/11).

### Pessoa 2 — João Pedro
- **Entregas:** nenhuma entrega na `main` nesta sprint. O CRUD de Material precisava das tabelas `autores` e
  `editoras` (chave estrangeira e tabela `material_autor`), que chegaram na `main` no fim da sprint (PR #4).
- **Próximo passo:** migration `V22` e CRUD de Material.

### Pessoa 3 — Bárbara Tavares
- **Entregas:** _a preencher pela Pessoa 3._
- **Dificuldades:** _a preencher pela Pessoa 3._

---

## Sprint 3 (30/10 a 04/11): fila completa e acabamento

### Pessoa 1 — Pedro Affonso
- **Entregas:**
  - PR #5 (01/11): CRUD completo de Autor.
  - PR #6 (01/11): CRUD completo de Editora, com CNPJ único (409) e CNPJ salvo só com dígitos.
  - Branch `pedro-dev` (06/11): RN06 em Autor e Editora (409 ao excluir com materiais vinculados) e a
    funcionalidade extra, recomendação personalizada (`GET /usuarios/{id}/recomendacoes`), com testes.
- **Dificuldades:**
  - A porta 3306 estava ocupada pelo MySQL instalado no Windows; o MySQL do Docker passou a rodar na
    3308 pela variável `DB_PORT`, num `.env` local.
  - O computador tem 4 GB de RAM e a JVM falhava por falta de memória; a aplicação passou a rodar com
    `-Xmx384m`.
  - A fila de espera dependia de Usuário e Empréstimo (Pessoa 3), que só ficaram prontos em 05/11. A
    equipe trocou a funcionalidade extra pela recomendação personalizada (plano B do planejamento), que
    usa o histórico de empréstimos; para isso a `barbara-dev` foi trazida para a `pedro-dev`.
- **Próximo passo:** PR da `pedro-dev` (depois do PR da Pessoa 3) e ensaio da apresentação.

### Pessoa 2 — João Pedro
- **Entregas:**
  - PR #7 (02/11): CRUD completo de Material.
    - Migration `V22`: editora opcional e tabela `material_autor` (N:N com Autor).
    - Validações: título, tipo, ano de 1500 até o ano atual (anotação própria `@AnoNaoFuturo`), URL,
      páginas, licenças e pelo menos um autor.
    - 404 quando a categoria, a editora ou algum autor informado não existe, dizendo qual ID faltou.
    - RN06 na Categoria: não exclui categoria com materiais (409).
    - Falha de constraint do banco vira 409 em vez de 500.
  - Branch `joao-dev`: o `GlobalExceptionHandler` passa a responder no formato padrão também para rota
    inexistente (404), método não aceito (405), corpo que não é JSON (415) e erro inesperado (500, sem
    detalhes internos); e este arquivo de acompanhamento.
- **Dificuldades:**
  - Na máquina de desenvolvimento, o JDK 27 não compila o Lombok da versão do Spring Boot usada; foi
    instalado o JDK 21. O README passou a pedir JDK 17 ou 21.
  - A porta 3306 estava ocupada por um MySQL instalado no Windows; o MySQL do Docker passou a rodar na
    3307 pela variável `DB_PORT`, sem mudar nenhum arquivo. O README explica como fazer.
- **Próximo passo:** revisão final do código e das camadas; testar todos os fluxos no Swagger, incluindo a
  demo da fila de espera.

### Pessoa 3 — Bárbara Tavares
- **Entregas:** _a preencher pela Pessoa 3._
- **Dificuldades:** _a preencher pela Pessoa 3._

---

## Resumo das contribuições (para a entrega)

| Pessoa | Pull Requests | O que entregou |
|---|---|---|
| Pessoa 1 — Pedro Affonso | #4, #5, #6 e PR da `pedro-dev` | Autor e Editora (Model, migrations, CRUDs completos com Swagger), RN06 em Autor e Editora e a funcionalidade extra (recomendação personalizada) |
| Pessoa 2 — João Pedro | #1, #2, #3, #7 | Setup, tratamento de erros, CRUDs de Categoria e Material, documentação |
| Pessoa 3 — Bárbara Tavares | _a preencher_ | _a preencher_ |
