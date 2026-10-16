# Registro do uso de IA

O professor permite o uso de IA desde que seja registrado: ferramenta, para que foi usada e
como o resultado foi verificado. Cada integrante registra aqui o próprio uso.

---

## Pessoa 2 — Sprint 1 (16/10/2026)

### Ferramenta

- **Claude Code** (CLI da Anthropic), modelo **Claude Opus 5.5**, rodando no terminal da
  máquina do integrante, com acesso à pasta do projeto, ao Git, ao Docker e ao `gh`.
- Os commits feitos com a IA trazem a linha `Co-Authored-By: Claude Opus 5.5`, o que
  permite identificá-los no histórico.

### Para que foi usada

| Etapa | O que a IA fez |
|---|---|
| Ambiente | Verificou JDK, Maven, Git e `gh`; instalou o JDK 21 (Temurin) via `winget`; adotou o Maven Wrapper (`mvnw`) no lugar de instalar o Maven |
| Setup | Criou o `pom.xml` (Spring Boot 3.5.16, springdoc 2.9.1), os pacotes das camadas, `application.properties`, `docker-compose.yml`, `.gitignore`, `.env.example`, `OpenApiConfig` e `CLAUDE.md` |
| GitHub | Criou o repositório público, fez o commit/push do setup e enviou os convites de colaborador |
| Tratamento de erros | Escreveu `GlobalExceptionHandler`, `ErroResponseDTO`, `CampoErroDTO`, `RecursoNaoEncontradoException`, `RegraNegocioException` e o teste `GlobalExceptionHandlerTest`; abriu o PR #1 |
| Categoria | Escreveu `SerieRecomendada`, `Categoria`, `V20__create_categorias.sql`, os DTOs, `CategoriaRepository`, `CategoriaService`, `CategoriaController` e `CategoriaServiceTest`; abriu o PR #2 |
| Documentação | Escreveu as mensagens de commit, as descrições dos PRs e este arquivo |

O escopo foi definido pelo integrante a partir do planejamento
(`docs/NP2 – Biblioteca Virtual Planejamento Completo.pdf`): somente a parte da Pessoa 2
na Sprint 1, sem implementar nada das outras pessoas.

### Como o resultado foi verificado

| O quê | Como | Resultado |
|---|---|---|
| Compilação | `./mvnw clean test` | BUILD SUCCESS |
| Testes automatizados | `GlobalExceptionHandlerTest` (6), `CategoriaServiceTest` (6), `BibliotecaVirtualApplicationTests` (1) | 13 testes, 0 falhas |
| Migration + `ddl-auto=validate` | Subida da aplicação com o MySQL do `docker-compose` e `SHOW CREATE TABLE categorias` | V20 aplicada; tabela com `UNIQUE KEY uk_categorias_nome`; Hibernate validou sem erro |
| Swagger | `GET /swagger-ui.html` e `GET /v3/api-docs` | UI abre (302 → 200); 5 rotas na tag *Categorias* com as respostas corretas |
| Rotas de Categoria | Requisições com `curl` contra a aplicação rodando | 201, 200 e 204 nos fluxos normais; 400 (campos vazios, enum inválido, JSON quebrado, id não numérico, nome com 101 caracteres); 404 (GET/PUT/DELETE de id inexistente); 409 (nome duplicado no cadastro, com outra caixa/espaços e sem acento, e na atualização) |
| Dados no banco | `SELECT * FROM categorias` no container | Acentos gravados corretamente (utf8mb4) |
| Convites | `gh api repos/.../invitations` | Dois convites pendentes com permissão `write` |

### Problemas encontrados e como foram resolvidos

- **Spring Initializr sem Spring Boot 3:** o site já só oferece o Boot 4.x. O `pom.xml` foi
  montado à mão com a última versão 3.5.x publicada no Maven Central (3.5.16) e o springdoc
  compatível (2.9.1, que declara o Boot 3.5.16 como base). Do Initializr só foram
  aproveitados os arquivos do Maven Wrapper.
- **Teste com acentos falhando:** as primeiras requisições com "Matemática" voltaram 400.
  A investigação mostrou que o problema era o `curl` do Git Bash enviando o argumento fora
  de UTF-8; com o corpo enviado a partir de um arquivo UTF-8, a API respondeu 201 e gravou
  os acentos corretamente. Nenhuma mudança no código foi necessária.
- **Usuário do GitHub inexistente:** `barabarattavares` não existe (404). A IA encontrou
  `barbarattavares` (conta criada no mesmo dia) e só enviou o convite depois da confirmação
  do integrante.

### Decisões que o integrante revisou e precisa saber explicar

- `serieRecomendada` é **obrigatória**: no planejamento, os campos opcionais estão marcados
  como "opcional" e este não está.
- Tamanhos: `nome` e `areaConhecimento` até 100 caracteres; `descricao` até 500.
- Nome duplicado é comparado sem diferenciar maiúsculas/minúsculas (`IgnoreCase`) e sem
  espaços nas pontas; a collation do MySQL (`utf8mb4_0900_ai_ci`) também ignora acentos.
- RN06 (não excluir categoria com materiais) fica para a Sprint 2, junto com Material.

### Segunda parte (16/10/2026): Model de Material e documentação

**Para que foi usada:** o trabalho foi dividido entre agentes do Claude Code rodando em
paralelo, cada um com um arquivo e uma especificação fechada:

| Agente | O que fez |
|---|---|
| Java | `TipoMaterial` (enum), `Material` (entidade, ligada a Categoria) e `MaterialRepository` |
| SQL | `V21__create_materiais.sql` com FK para `categorias` |
| README | `README.md`: execução, variáveis de ambiente, rotas, formato de erro, estrutura, equipe |
| Requisitos | `docs/REQUISITOS.md`: versão em Markdown do planejamento (entidades, RF, RN, RNF, fila) |
| Roadmap | `docs/ROADMAP.md`: planejamento das sprints por pessoa, folga, entrega e apresentação |

A especificação de cada coluna (nome, tipo, `NULL`/`NOT NULL`) foi definida antes e passada
igual aos agentes Java e SQL, para entidade e migration baterem. Os commits desta parte são
feitos pelo próprio integrante, depois de revisar os arquivos.

**Como foi verificado:**
- Revisão manual da entidade contra a migration, campo a campo.
- `./mvnw clean test` num banco descartável (`biblioteca_verificacao`, apagado depois), para
  não registrar a V21 no banco de desenvolvimento antes do commit: V20 e V21 aplicadas,
  `ddl-auto=validate` aceitou `Material`, 13 testes e 0 falhas.
- `SHOW CREATE TABLE materiais`: colunas, tipos e a FK `fk_materiais_categoria` conferidos.
- Documentos revisados contra o PDF; links entre `README.md` e `docs/` conferidos.

**Decisões:** `anoPublicacao` e `quantidadeLicencas` obrigatórios (o PDF não os marca como
opcionais); editora e autores ficam para a V22, porque dependem das tabelas da Pessoa 1.

---

<!-- Pessoa 1 e Pessoa 3: adicionem suas seções abaixo, no mesmo formato. -->
