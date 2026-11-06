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

## Pessoa 2 — Sprint 2/3 (02/11/2026): CRUD de Material e RN06

### Ferramenta

- **Claude Code**, modelo **Claude Opus 5.5**, na extensão do VS Code, usado como assistente de
  programação. Os commits e o push são feitos pelo próprio integrante, depois de revisar os arquivos.

### Como o trabalho foi conduzido

O integrante comparou o andamento da `main` com o planejamento (PDF e ROADMAP) e definiu as
prioridades da Sprint 2/3: o CRUD de Material, que estava atrasado e bloqueava Empréstimo e Reserva,
e a RN06 na exclusão de Categoria. O escopo e as regras seguem o planejamento; o integrante revisou
as decisões de implementação listadas no fim desta seção.

### Para que a IA foi usada

| Etapa | Uso da IA |
|---|---|
| Levantamento | Apoio na comparação entre o código da `main` e o planejamento |
| Ambiente | Diagnóstico de dois problemas: o JDK 27 não compila o Lombok do projeto (instalado o JDK 21) e a porta 3306 estava ocupada por um MySQL do Windows (MySQL do Docker na 3307 via `DB_PORT`) |
| Código | Geração da `V22`, das camadas de Material (entidade, repository, DTOs, service, controller), da anotação `@AnoNaoFuturo` e da checagem da RN06 |
| Testes | Escrita de `MaterialServiceTest`, `MaterialRequestDTOTest` e dos testes novos de Categoria e do `GlobalExceptionHandler` |
| Documentação | Atualização do README, REQUISITOS, ROADMAP, `CLAUDE.md` e rascunho deste registro |
| Tratamento de erros (branch `joao-dev`) | Ampliação do `GlobalExceptionHandler` para 404 de rota inexistente, 405, 415 e 500 no formato padrão, com testes |
| Acompanhamento | Rascunho do `docs/SPRINTS.md` a partir do histórico do Git (datas e PRs); as partes das outras pessoas ficaram para elas preencherem |

### Como o resultado foi verificado

| O quê | Como | Resultado |
|---|---|---|
| Testes automatizados | `mvnw test` com JDK 21 e MySQL do `docker-compose` | 49 testes, 0 falhas (antes eram 30) |
| Migration + `ddl-auto=validate` | `contextLoads` sobe a aplicação inteira | V22 aplicada; Hibernate aceitou a entidade com editora e autores |
| Rotas de Material | Requisições com `curl` contra a aplicação rodando (corpos em arquivos UTF-8) | 201/200/204 nos fluxos normais; autores voltam em ordem alfabética; ID de autor repetido é ignorado; `PUT` troca os autores e remove a editora |
| Erros de Material | Mesmas requisições com dados errados | 400 (corpo vazio, ano futuro, URL inválida, 0 licenças, 0 páginas, lista de autores vazia, tipo `REVISTA`, id `abc`); 404 (categoria, editora, autores 9998 e 9999 inexistentes, material inexistente) |
| RN06 | `DELETE` de categoria e autor ligados a material | Categoria: 409 com a mensagem da regra; autor: 409 pela rede de segurança (antes seria 500); depois de excluir os materiais, as exclusões deram 204 |
| Swagger | `/swagger-ui.html` e `/v3/api-docs` | 5 rotas na tag *Materiais* com 200/201/204, 400 e 404; `DELETE /categorias/{id}` com 409 |
| Erros novos (`joao-dev`) | `GlobalExceptionHandlerTest` (11 testes) e `curl` contra a aplicação rodando | `GET /materiaisss` → 404, `PATCH /categorias/1` → 405 com os métodos aceitos, corpo `text/plain` → 415, todos no formato padrão; rotas normais e Swagger sem mudança |

### Decisões que o integrante revisou e precisa saber explicar

- **Por que IDs no corpo** (`categoriaId`, `editoraId`, `autoresIds`) e não objetos: o cliente só referencia
  cadastros que já existem; o Service busca cada um e devolve 404 se não achar.
- **Autores numa consulta só** (`findAllById`): se o tamanho do resultado for diferente do número de IDs, algum
  não existe, e a mensagem diz qual.
- **`Set<Autor>`** na entidade, porque a chave primária de `material_autor` não aceita o mesmo autor duas vezes.
- **`@EntityGraph` na listagem**: traz categoria, editora e autores na mesma consulta, em vez de uma consulta por
  material (problema N+1), já que as relações são LAZY e `open-in-view=false`.
- **`@AnoNaoFuturo`**: o `@Max` só aceita valor fixo; o validador compara com `Year.now()` a cada requisição.
- **Rede de segurança 409**: os Services de Autor e Editora (Pessoa 1) ainda não checam a RN06; até checarem,
  o banco recusa a exclusão e o `GlobalExceptionHandler` devolve 409 em vez de 500. As consultas
  `existsByAutoresId` e `existsByEditoraId` já estão no `MaterialRepository` para a Pessoa 1 usar.

---

<!-- Pessoa 1 e Pessoa 3: adicionem suas seções abaixo, no mesmo formato. -->

## Pessoa 3 — Sprint 3 (05/11/2026): Usuário e CRUD de Empréstimo

### Ferramenta

- **Claude Code**, modelo **Claude Opus 5.5**, no app desktop do Claude, usado como assistente de
  programação. Os commits e o push são feitos pela própria integrante, depois de revisar os arquivos.

### Para que a IA foi usada

| Etapa | Uso da IA |
|---|---|
| Levantamento | Comparação entre a `main` e uma versão anterior do código de empréstimo feita pela integrante fora do repositório (outro pacote, banco H2/Postgres e outro formato de erro) |
| Código | Adaptação para o padrão do repositório: migrations `V30` (usuários) e `V31` (empréstimos) em MySQL, entidades com Lombok, DTOs, repositories, services, controllers com `@ApiResponses`, e `RegraNegocioException` (409) no lugar da exceção própria da versão anterior |
| Regras | RN01 (licença livre, com trava na linha do material), RN02 (prazo padrão de 14 dias), RN03, RN04 (máximo 3), RN05 (rotina agendada a cada hora e rota manual) e a devolução |
| Testes | Escrita de `EmprestimoServiceTest` (9) e `UsuarioServiceTest` (5) |
| Documentação | Rascunho deste registro |

### Como o resultado foi verificado

| O quê | Como | Resultado |
|---|---|---|
| Testes automatizados | `mvnw test` com JDK 21 e MySQL do `docker-compose` | 67 testes, 0 falhas (14 novos) |
| Migrations + `ddl-auto=validate` | `contextLoads` sobe a aplicação inteira num banco vazio | V30 e V31 aplicadas; Hibernate aceitou `Usuario` e `Emprestimo` |
| Rotas de Usuário | Requisições com `curl` contra a aplicação rodando (corpos em arquivo UTF-8) | 201/200/204 nos fluxos normais; 400 (nome vazio, e-mail inválido); 404 (id inexistente); 409 (e-mail e matrícula de outro usuário; excluir usuário com empréstimo) |
| Rotas de Empréstimo | Mesmas requisições | 201 com prazo padrão de 14 dias ou data informada; 409 sem licença (material com 1 licença), material repetido (RN03) e 4º empréstimo (RN04); 404 (usuário, material ou empréstimo inexistente); 400 (corpo vazio, data no passado, `status=PERDIDO`); devolução 200 e segunda devolução 409; editar devolvido 409; após devolver, outro usuário consegue a licença |
| RN05 | Data prevista vencida direto no banco + `PATCH /emprestimos/atualizar-atrasados` | `{"atualizados":1}`, status ATRASADO no `GET`; renovar o prazo voltou a ATIVO |
| Exclusões | `DELETE` de empréstimo, de material com empréstimo e de usuário sem empréstimo | 204; material com empréstimo deu 409 pela rede de segurança da FK; 404 para id inexistente |
| Swagger | `/swagger-ui.html` e `/v3/api-docs` | UI abre; tags *Usuários* e *Empréstimos* com todas as rotas |

Observação: e-mail com espaços nas pontas é recusado pelo `@Email` (400) antes de chegar ao Service.

### Decisões que a integrante revisou e precisa saber explicar

- **Nenhum arquivo de outra pessoa foi alterado.** O `@EnableScheduling` está numa classe nova
  (`AgendamentoConfig`), e a busca do material com trava fica no `EmprestimoRepository`.
- **Licença em uso** = empréstimo ATIVO ou ATRASADO. Disponíveis = `quantidadeLicencas` − em uso.
- **Trava pessimista** (`PESSIMISTIC_WRITE`) no material ao registrar: duas requisições ao mesmo
  tempo não pegam a mesma última licença.
- **`EmprestimoRegras` separado do Service**, para a fila de espera (RN11) reaproveitar
  `podeReceber` e `registrar` ao atender uma reserva.
- **Edição = renovar o prazo**; renovar um ATRASADO o deixa ATIVO de novo. Empréstimo devolvido não muda.
- **Excluir material ou usuário com empréstimo** devolve 409 (Usuário checa no Service; Material
  pela rede de segurança da FK no `GlobalExceptionHandler`).
- **Fila × devolução (RF09)** ainda não está ligada: entra junto com a Reserva (Pessoa 1).
