# Biblioteca Virtual API

API REST (sem interface visual) de uma biblioteca pública virtual para uma escola de ensino médio.
Trabalho da NP2 – Tópicos Web, feito pela equipe **Compila e Reza** (3 pessoas), com entrega e apresentação até 06/11/2026.

Repositório: https://github.com/joaopmaia-code/biblioteca-virtual-api

## Funcionalidades

- **5 CRUDs completos** (cadastrar, editar, listar, consultar por ID e excluir): Material, Autor, Editora, Categoria e Empréstimo.
- **Funcionalidade extra: recomendação personalizada.** `GET /usuarios/{id}/recomendacoes` sugere até 10 materiais que o
  aluno ainda não pegou, da mesma categoria ou dos mesmos autores do seu histórico de empréstimos, com a pontuação e o
  motivo de cada sugestão. (A ideia inicial era uma fila de espera; ela foi trocada pela recomendação, o plano B do
  planejamento. Ver [Requisitos](docs/REQUISITOS.md#funcionalidade-extra-recomendação-personalizada).)
- **Regras de empréstimo:** só empresta com licença livre, prazo padrão de 14 dias, sem repetir material, até 3 ativos por
  usuário, e os vencidos passam a `ATRASADO` automaticamente. Como o acervo é digital, o empréstimo é de uma **licença**
  (a editora vende um número limitado de acessos simultâneos); cada material mostra quantas licenças estão livres.
- **Busca no acervo:** `GET /materiais/busca` combina filtros (título, autor, categoria, editora, tipo, série, ano,
  nota mínima e disponibilidade), ordena por título, ano ou avaliação e pagina o resultado.
- **Avaliações:** o aluno dá nota de 1 a 5 e um comentário aos materiais que já pegou emprestado; a média e o total de
  avaliações aparecem em cada material.
- **Progresso de leitura:** o aluno registra a página em que parou em cada material que está com ele, e a API devolve o
  percentual lido; `GET /usuarios/{id}/leituras` mostra tudo o que ele está lendo.
- **Gamificação:** cada aluno tem um perfil de leitor com pontos, nível e conquistas, e há um ranking de leitores.
  Os pontos vêm de leituras concluídas, devoluções no prazo e avaliações.
- O **Usuário** das aulas é reaproveitado e não conta como CRUD.
- **Front-end** (opcional, em `frontend/`): telas web em React para o aluno (acervo, leituras, avaliações, perfil de
  leitor e ranking) e para a equipe da biblioteca (empréstimos). Como rodar: [frontend/README.md](frontend/README.md).

## Stack

| Tecnologia | Uso |
|---|---|
| Java 17 ou 21 | Linguagem (código compilado para Java 17) |
| Spring Boot 3.5.16 | Base da aplicação |
| Spring Web | API REST |
| Spring Data JPA | Repositories e persistência (Hibernate) |
| Bean Validation | Validação de entrada (`@Valid`, `@NotBlank`, `@Size`...) |
| Flyway + flyway-mysql | Migrations do banco |
| MySQL 8.4 (Docker) | Banco de dados |
| Lombok | Getters, setters e construtores |
| springdoc-openapi 2.9.1 | Swagger UI e documento OpenAPI |
| Maven Wrapper (Maven 3.9.16) | Build sem precisar instalar o Maven |

## Pré-requisitos

- **JDK 17 ou 21** (`java -version` para conferir). Um JDK mais novo, como o 27, não compila o projeto:
  o Lombok da versão do Spring Boot usada aqui falha com `ExceptionInInitializerError`. Se tiver mais de um JDK,
  aponte o `JAVA_HOME` para o 17 ou o 21.
- **Docker Desktop** rodando
- **Git**

Não é preciso instalar o Maven: o `mvnw` (Maven Wrapper) baixa a versão certa sozinho na primeira execução.

## Como executar

```bash
# 1. Clonar o repositório
git clone https://github.com/joaopmaia-code/biblioteca-virtual-api.git
cd biblioteca-virtual-api

# 2. Subir o MySQL
docker compose up -d
docker compose ps          # esperar o status ficar "healthy"

# 3. Rodar a aplicação (o Flyway cria as tabelas na subida)
./mvnw spring-boot:run     # Windows: mvnw.cmd spring-boot:run
```

Com a aplicação no ar, abra o Swagger: **http://localhost:8080/swagger-ui.html**

### Dados de demonstração (seed)

Para a apresentação, suba com o perfil `demo`. Num banco vazio, ele cadastra 4 categorias, 12 autores, 20 materiais,
8 alunos e um histórico de 28 empréstimos (ativos, 2 atrasados e devolvidos), progresso de leitura e 16 avaliações,
com datas relativas a hoje. Se o banco já tiver usuários ou materiais, o seed não faz nada.

```bash
docker compose down -v && docker compose up -d                    # banco zerado
./mvnw spring-boot:run -Dspring-boot.run.profiles=demo              # Linux/macOS/Git Bash
mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=demo"          # Windows (PowerShell/CMD)
```

Os 16 livros são clássicos em domínio público do [Project Gutenberg](https://www.gutenberg.org) (dados obtidos pela
API [Gutendex](https://gutendex.com)), com o link real de leitura em `urlArquivo`; o número de páginas veio do
[Open Library](https://openlibrary.org). Os 4 materiais da "Editora Escolar (exemplo)" são fictícios, para mostrar
apostila, vídeo e artigo. Matrículas para testar: `2026A0142` (Ana, com um empréstimo atrasado) e `2026A0101`
(Carla, 1ª do ranking). O seed fica em `config/DadosDemo.java`.

Para usar as telas web, em outro terminal (precisa do Node.js 20+):

```bash
cd frontend
npm install                # só na primeira vez
npm run dev                # abre em http://localhost:5173
```

```bash
# Parar
Ctrl+C                     # encerra a aplicação
docker compose down        # para o MySQL (os dados ficam no volume)

# Zerar o banco (apaga o volume; o Flyway recria tudo na próxima subida)
docker compose down -v
```

## Variáveis de ambiente

| Variável | Padrão | Uso |
|---|---|---|
| `DB_HOST` | `localhost` | Host do MySQL |
| `DB_PORT` | `3306` | Porta do MySQL |
| `DB_NAME` | `biblioteca_virtual` | Nome do banco |
| `DB_USERNAME` | `biblioteca` | Usuário do banco |
| `DB_PASSWORD` | `biblioteca` | Senha do banco |
| `DB_ROOT_PASSWORD` | `root` | Senha do root (só no `docker compose`) |

Os valores padrão servem para o banco local do `docker-compose.yml`, então o projeto sobe sem configuração extra.
Para usar outras credenciais, copie o `.env.example` para `.env` e ajuste. O `docker compose` lê o `.env` automaticamente;
para a aplicação, exporte as mesmas variáveis no terminal ou na configuração de execução da IDE.
O `.env` está no `.gitignore` e não vai para o Git.

**Porta 3306 ocupada** (ex.: MySQL instalado no Windows): o `docker compose up -d` falha com
"ports are not available". Use outra porta nos dois comandos, sem mudar nenhum arquivo:

```powershell
# PowerShell
$env:DB_PORT = "3307"
docker compose up -d
.\mvnw.cmd spring-boot:run
```

```bash
# Git Bash / Linux / macOS
export DB_PORT=3307
docker compose up -d
./mvnw spring-boot:run
```

## Testes

```bash
./mvnw test                # Windows: mvnw.cmd test
```

Precisa do MySQL rodando: o teste `contextLoads` sobe a aplicação inteira e confirma que o Flyway aplica as
migrations e que o Hibernate (`ddl-auto=validate`) aceita o schema.

## Rotas da API

### Implementadas

Em todas: `POST` devolve **201 Created** com o cabeçalho `Location`, `GET` e `PUT` devolvem **200 OK**,
`DELETE` devolve **204 No Content**, e um ID que não é número (ex.: `/materiais/abc`) devolve **400**.

**Categorias** (Pessoa 2)

| Método | Rota | Erros possíveis |
|---|---|---|
| POST | `/categorias` | 400, 409 (nome repetido) |
| GET | `/categorias` (ordenado por nome) | — |
| GET | `/categorias/{id}` | 404 |
| PUT | `/categorias/{id}` | 400, 404, 409 (nome repetido) |
| DELETE | `/categorias/{id}` | 404, 409 (tem materiais vinculados – RN06) |

**Materiais** (Pessoa 2)

| Método | Rota | Erros possíveis |
|---|---|---|
| POST | `/materiais` | 400, 404 (categoria, editora ou autor inexistente) |
| GET | `/materiais` (ordenado por título) | — |
| GET | `/materiais/busca` (filtros, ordenação e paginação; ver abaixo) | 400 |
| GET | `/materiais/{id}` | 404 |
| PUT | `/materiais/{id}` | 400, 404 |
| DELETE | `/materiais/{id}` (as avaliações dele são apagadas junto) | 404, 409 (tem empréstimos) |

Categoria e autores (pelo menos um) são obrigatórios e a editora é opcional. Todos são enviados pelo ID
(`categoriaId`, `editoraId`, `autoresIds`) e voltam na resposta resumidos com `id` e `nome`. A resposta também traz
`licencasDisponiveis`, `mediaAvaliacoes` (uma casa decimal; `null` sem avaliações) e `totalAvaliacoes`, calculados
pelo banco a cada consulta. Exemplo de corpo:

```json
{
  "titulo": "Dom Casmurro",
  "tipo": "LIVRO",
  "anoPublicacao": 1899,
  "sinopse": "Bentinho narra sua história com Capitu.",
  "urlArquivo": "https://acervo.escola.edu.br/livros/dom-casmurro.pdf",
  "numeroPaginas": 256,
  "quantidadeLicencas": 3,
  "categoriaId": 1,
  "editoraId": 1,
  "autoresIds": [1, 2]
}
```

**Busca no acervo** (Pessoa 2): `GET /materiais/busca`. Todos os parâmetros são opcionais e combinados com "e":

| Parâmetro | Filtra por |
|---|---|
| `titulo` | Parte do título, sem diferenciar maiúsculas |
| `autorId`, `categoriaId`, `editoraId` | Autor (qualquer um dos autores do material), categoria, editora |
| `tipo` | `LIVRO`, `APOSTILA`, `ARTIGO` ou `VIDEO` |
| `serie` | Série recomendada da categoria: `PRIMEIRO_ANO`, `SEGUNDO_ANO` ou `TERCEIRO_ANO` |
| `anoMin`, `anoMax` | Ano de publicação entre (inclusive) |
| `notaMinima` | Média das avaliações maior ou igual (1 a 5); materiais sem avaliação ficam de fora |
| `disponivel` | `true`: só com licença livre agora; `false`: só os esgotados |
| `ordenarPor` | `TITULO` (padrão), `ANO` (mais recentes primeiro) ou `AVALIACAO` (maior média primeiro) |
| `pagina`, `tamanho` | Página (começa em 0) e itens por página (1 a 50, padrão 10) |

Exemplo: `GET /materiais/busca?serie=TERCEIRO_ANO&disponivel=true&ordenarPor=AVALIACAO`. A resposta vem como
`{ "conteudo": [...], "pagina": 0, "tamanho": 10, "totalElementos": 23, "totalPaginas": 3 }`.

**Avaliações** (Pessoa 2)

| Método | Rota | Erros possíveis |
|---|---|---|
| POST | `/avaliacoes` | 400 (nota fora de 1 a 5), 404 (usuário ou material inexistente), 409 (nunca pegou o material ou já avaliou) |
| GET | `/avaliacoes` (filtros opcionais `?materialId=` e `?usuarioId=`; mais recentes primeiro) | 400 |
| GET | `/avaliacoes/{id}` | 404 |
| PUT | `/avaliacoes/{id}` (troca nota e comentário) | 400, 404 |
| DELETE | `/avaliacoes/{id}` | 404 |

Corpo do POST: `{ "usuarioId": 1, "materialId": 1, "nota": 5, "comentario": "Ótimo para o ENEM." }` (comentário opcional,
até 1000 caracteres). Só avalia quem já pegou o material emprestado (qualquer status), e uma vez só por material.

**Progresso de leitura** (Pessoa 2)

| Método | Rota | Erros possíveis |
|---|---|---|
| PUT | `/emprestimos/{id}/progresso` (cria ou atualiza) | 400, 404 (empréstimo inexistente), 409 (já devolvido, material sem páginas ou página acima do total) |
| GET | `/emprestimos/{id}/progresso` (página 0 se nunca registrou) | 404 |
| GET | `/usuarios/{id}/leituras` (empréstimos ativos ou atrasados com o progresso de cada um) | 404 (usuário inexistente) |

Corpo do PUT: `{ "paginaAtual": 120 }`. A resposta traz `paginaAtual`, `numeroPaginas`, `percentual` (ex.: `46.9`) e
`concluido` (`true` na última página).

**Gamificação** (Pessoa 2)

| Método | Rota | Erros possíveis |
|---|---|---|
| GET | `/usuarios/{id}/perfil-leitor` (pontos, nível, extrato e conquistas) | 404 (usuário inexistente) |
| GET | `/ranking` (`?limite=` de 1 a 50, padrão 10) | 400 |
| GET | `/conquistas` (catálogo) | — |

| Ação | Pontos |
|---|---|
| Terminar a leitura de um material (progresso na última página; cada material conta uma vez) | 10 |
| Devolver um empréstimo até a data prevista | 3 |
| Avaliar um material | 2 (+1 com comentário) |

Níveis: Leitor Iniciante (0), Leitor (50), Leitor Voraz (150), Devorador de Livros (300) e Lenda da Biblioteca (500).
Conquistas: Primeira Página, Maratonista (5 leituras), Explorador (3 categorias), Crítico (5 avaliações com comentário),
Pontual (10 devoluções no prazo) e Clássico (material publicado antes de 1900). Nada é gravado: tudo é calculado a cada
consulta. O ranking deixa de fora quem tem 0 ponto, mostra o nome abreviado (ex.: `Ana S.`) e põe empatados na mesma
posição.

**Autores** (Pessoa 1)

| Método | Rota | Erros possíveis |
|---|---|---|
| POST | `/autores` | 400 |
| GET | `/autores` (ordenado por nome) | — |
| GET | `/autores/{id}` | 404 |
| PUT | `/autores/{id}` | 400, 404 |
| DELETE | `/autores/{id}` | 404, 409 (tem materiais vinculados – RN06) |

**Editoras** (Pessoa 1)

| Método | Rota | Erros possíveis |
|---|---|---|
| POST | `/editoras` | 400, 409 (CNPJ repetido) |
| GET | `/editoras` (ordenado por nome) | — |
| GET | `/editoras/{id}` | 404 |
| PUT | `/editoras/{id}` | 400, 404, 409 (CNPJ repetido) |
| DELETE | `/editoras/{id}` | 404, 409 (tem materiais vinculados – RN06) |

O CNPJ é aceito com ou sem pontuação, validado pelos dígitos verificadores e salvo só com os 14 dígitos.

**Usuários** (Pessoa 3, reaproveitado das aulas, não conta como CRUD)

| Método | Rota | Erros possíveis |
|---|---|---|
| POST | `/usuarios` | 400, 409 (e-mail ou matrícula repetidos) |
| GET | `/usuarios` (ordenado por nome) | — |
| GET | `/usuarios/{id}` | 404 |
| PUT | `/usuarios/{id}` | 400, 404, 409 (e-mail ou matrícula repetidos) |
| DELETE | `/usuarios/{id}` | 404, 409 (tem empréstimos) |

**Empréstimos** (Pessoa 3)

| Método | Rota | Erros possíveis |
|---|---|---|
| POST | `/emprestimos` | 400, 404 (usuário ou material inexistente), 409 (sem licença livre, material repetido ou limite de 3) |
| GET | `/emprestimos` (filtros opcionais `?status=` e `?usuarioId=`) | 400 |
| GET | `/emprestimos/{id}` | 404 |
| PUT | `/emprestimos/{id}` (renovar o prazo) | 400, 404, 409 (já devolvido) |
| PATCH | `/emprestimos/{id}/devolver` | 404, 409 (já devolvido) |
| DELETE | `/emprestimos/{id}` | 404 |
| PATCH | `/emprestimos/atualizar-atrasados` (RN05; também roda sozinho a cada hora) | — |

Sem `dataPrevistaDevolucao` no corpo, o prazo é de 14 dias.

**Recomendações** (Pessoa 1, funcionalidade extra)

| Método | Rota | Erros possíveis |
|---|---|---|
| GET | `/usuarios/{id}/recomendacoes` | 404 (usuário inexistente) |

Cada material que o usuário ainda não pegou ganha +1 ponto por empréstimo anterior da mesma categoria e +2 por
empréstimo anterior do mesmo autor; voltam os 10 com mais pontos, cada um com o `motivo`. Sem empréstimos, a lista
vem vazia.

A documentação completa de cada rota (corpo, exemplos e respostas) está no Swagger.

## Formato de erro

Todos os erros seguem o mesmo formato (`ErroResponseDTO`), montado pelo `GlobalExceptionHandler`:

| Campo | Descrição |
|---|---|
| `timestamp` | Momento do erro |
| `status` | Código HTTP |
| `erro` | Nome do status HTTP |
| `mensagem` | Explicação do erro |
| `campos` | Lista `{ campo, mensagem }`, só em erros de validação |

Exemplo de **400** (validação):

```json
{
  "timestamp": "2026-10-16T15:30:00",
  "status": 400,
  "erro": "Bad Request",
  "mensagem": "Existem campos inválidos na requisição",
  "campos": [
    { "campo": "nome", "mensagem": "O nome é obrigatório" },
    { "campo": "serieRecomendada", "mensagem": "A série recomendada é obrigatória" }
  ]
}
```

**404** indica um ID que não existe (ex.: `"Categoria não encontrada com id 99"`) e **409** indica uma regra de negócio
violada (ex.: `"Já existe uma categoria com o nome 'Matemática'"`), ambos sem o campo `campos`.

Se o banco recusar uma operação por constraint (ex.: excluir um registro que outra tabela ainda usa) sem que o Service
tenha checado antes, o `GlobalExceptionHandler` também devolve **409**, com uma mensagem genérica, em vez de erro 500.

Os demais erros também seguem o mesmo formato:

| Status | Quando |
|---|---|
| 400 | Parâmetro da URL fora do permitido (ex.: `/materiais/busca?tamanho=500`); `campos` diz qual parâmetro e por quê |
| 404 | Rota que não existe (ex.: `GET /materiaisss`) |
| 405 | Rota existe, mas não com esse método (ex.: `PATCH /categorias/1`); a mensagem lista os métodos aceitos |
| 415 | Corpo enviado em outro formato que não `application/json` |
| 500 | Erro inesperado; a resposta não expõe detalhes internos e o erro completo fica no log da aplicação |

## Estrutura do projeto

```
biblioteca-virtual-api/
├── src/main/java/com/np2/bibliotecavirtual/
│   ├── config/        # Configurações (ex.: OpenApiConfig do Swagger)
│   ├── controller/    # Controllers REST: recebem a requisição e definem o status HTTP
│   ├── dto/           # XxxRequestDTO (entrada validada) e XxxResponseDTO (saída)
│   ├── exception/     # Exceções de negócio e GlobalExceptionHandler
│   ├── model/         # Entidades JPA e enums
│   ├── repository/    # Interfaces Spring Data JPA
│   ├── service/       # Regras de negócio
│   └── validacao/     # Anotações de validação próprias (ex.: @AnoNaoFuturo)
├── src/main/resources/
│   ├── application.properties   # Banco, JPA, Flyway e Swagger
│   └── db/migration/            # Migrations do Flyway (V<n>__descricao.sql)
├── src/test/          # Testes automatizados
├── docs/              # Planejamento, requisitos, roadmap e registro de uso de IA
├── docker-compose.yml # MySQL 8.4 para desenvolvimento
└── mvnw / mvnw.cmd    # Maven Wrapper
```

## Migrations

- As tabelas são criadas e alteradas **somente** por migrations do Flyway, em `src/main/resources/db/migration`.
- `spring.jpa.hibernate.ddl-auto=validate`: o Hibernate só confere se as entidades batem com o banco e impede a subida se algo estiver diferente.
- Faixas de versão por pessoa, para evitar conflito de número:

| Faixa | Dono |
|---|---|
| `V1`–`V9` | Estrutura inicial |
| `V10+` | Pessoa 1 |
| `V20+` | Pessoa 2 |
| `V30+` | Pessoa 3 |
| `V40+` | Funcionalidades novas que dependem de Usuário/Empréstimo (Pessoa 2: avaliações e progresso de leitura) |

- Uma migration com chave estrangeira precisa de número maior que a da tabela referenciada: num banco zerado o Flyway
  aplica em ordem de versão. Por isso `avaliacoes` (`V40`) e `progressos_leitura` (`V41`), que dependem de `usuarios`
  e `emprestimos` (`V30`/`V31`), não ficaram na `V2x`.
- `spring.flyway.out-of-order=true` permite aplicar uma migration de número menor que chegue na `main` depois de uma maior.
- **Nunca editar uma migration que já está na `main`**: criar uma nova.

## Como contribuir

1. Criar uma branch a partir da `main`: `git checkout -b feature/<nome>` (ex.: `feature/autor`).
2. Fazer commits em português no padrão Conventional Commits, ex.:
   `feat(emprestimo): valida limite de 3 empréstimos ativos por usuário`.
3. Antes de abrir o PR, atualizar a branch com `git pull origin main`.
4. Rodar o projeto e testar as rotas no Swagger.
5. Abrir o Pull Request para a `main`.

## Equipe: Compila e Reza

| Pessoa | Nome | GitHub | Responsabilidades |
|---|---|---|---|
| Pessoa 1 | Pedro Affonso | [@pedrohaffonso](https://github.com/pedrohaffonso) | CRUD de Autor, CRUD de Editora (CNPJ único), RN06 em Autor e Editora e a funcionalidade extra (recomendação personalizada) |
| Pessoa 2 | João Pedro | [@joaopmaia-code](https://github.com/joaopmaia-code) | Setup do projeto (Spring, banco, Flyway, Swagger), GlobalExceptionHandler, CRUD de Categoria, CRUD de Material (N:N com Autor), README inicial e documentação |
| Pessoa 3 | Bárbara Tavares | [@barbarattavares](https://github.com/barbarattavares) | CRUD de Empréstimo, devolução e regras RN01–RN05, integração com Usuário, seed de dados e README final |

## Documentação

- [Requisitos](docs/REQUISITOS.md)
- [Roadmap](docs/ROADMAP.md)
- [Acompanhamento das sprints](docs/SPRINTS.md) (o que cada um entregou, dificuldades e próximo passo)
- [Registro do uso de IA](docs/USO_DE_IA.md)
- [Planejamento completo (PDF)](docs/NP2%20%E2%80%93%20Biblioteca%20Virtual%20Planejamento%20Completo.pdf)
