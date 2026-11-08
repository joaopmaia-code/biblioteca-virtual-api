# Guia do projeto: passo a passo e como tudo funciona

Este guia junta, num lugar só, **como rodar** a Biblioteca Virtual e **como cada parte funciona por dentro**.
Para a lista completa de rotas, corpos e códigos de erro, veja o [README](../README.md); para requisitos e regras
numeradas, o [REQUISITOS](REQUISITOS.md).

**Sumário**

1. [O que é o projeto](#1-o-que-é-o-projeto)
2. [Passo a passo para rodar](#2-passo-a-passo-para-rodar)
3. [Usuários de teste](#3-usuários-de-teste)
4. [Como usar o Swagger com login](#4-como-usar-o-swagger-com-login)
5. [Como funciona: arquitetura](#5-como-funciona-arquitetura)
6. [Como funciona: cada parte do sistema](#6-como-funciona-cada-parte-do-sistema)
7. [Como funciona: autenticação e perfis](#7-como-funciona-autenticação-e-perfis)
8. [Como funciona: o front-end](#8-como-funciona-o-front-end)
9. [Roteiro da demonstração](#9-roteiro-da-demonstração)
10. [Testes](#10-testes)
11. [Entrega](#11-entrega)
12. [Problemas comuns](#12-problemas-comuns)
13. [Onde fica cada coisa](#13-onde-fica-cada-coisa)

---

## 1. O que é o projeto

API REST de uma biblioteca **digital** de escola de ensino médio. Os alunos pegam emprestadas **licenças** de livros,
apostilas, artigos e vídeos (a editora vende um número limitado de acessos simultâneos), registram até que página leram,
avaliam o que leram e ganham pontos e conquistas. Professores cadastram o acervo e acompanham os alunos; a equipe da
biblioteca gerencia usuários e empréstimos.

| Parte | Tecnologia |
|---|---|
| Backend | Java 17/21, Spring Boot 3.5 (Web, Data JPA, Validation, Security), Flyway, MySQL 8.4 |
| Documentação da API | Swagger (springdoc-openapi) em `/swagger-ui.html` |
| Front-end (opcional) | React 19 + Vite + TypeScript, em `frontend/` |
| Banco | MySQL no Docker (`docker-compose.yml`) |

---

## 2. Passo a passo para rodar

### 2.1. Instalar (uma vez)

1. **JDK 17 ou 21** (`java -version`). JDK mais novo (ex.: 27) quebra o Lombok; veja [Problemas comuns](#12-problemas-comuns).
2. **Docker Desktop**, aberto e rodando (é ele que sobe o MySQL).
3. **Node.js 20 ou mais novo**, só se for usar o front.
4. Clonar o repositório:
   ```bash
   git clone https://github.com/joaopmaia-code/biblioteca-virtual-api.git
   cd biblioteca-virtual-api
   ```

### 2.2. Subir o banco

```powershell
docker compose up -d       # sobe o MySQL na porta 3306
docker compose ps          # espere o status "healthy"
```

### 2.3. Subir a API

Para a **apresentação**, use o perfil `demo`, que enche o banco com dados de exemplo (só funciona com o banco vazio):

```powershell
docker compose down -v; docker compose up -d                  # zera o banco
mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=demo"    # Windows
./mvnw spring-boot:run -Dspring-boot.run.profiles=demo         # Linux, macOS ou Git Bash
```

Para **desenvolver**, sem dados de exemplo: `mvnw.cmd spring-boot:run`.

A API está pronta quando o log mostra `Started BibliotecaVirtualApplication`. Com o perfil demo, aparece também
`Perfil demo: seed carregado (...)`. Abra **http://localhost:8080/swagger-ui.html**.

Na subida acontecem, nesta ordem:

1. O **Flyway** roda as migrations que faltam (`V10` a `V42`) e cria ou altera as tabelas.
2. O **Hibernate** confere se as entidades batem com as tabelas (`ddl-auto=validate`); se não baterem, a aplicação não sobe.
3. Com o perfil demo, o **seed** (`DadosDemo`) cadastra os dados de exemplo.
4. O **BibliotecarioInicial** cria um bibliotecário `admin` / `admin123` se o banco não tiver nenhum.
5. A rotina de **atrasos** (RN05) marca como ATRASADO os empréstimos vencidos, e repete a cada hora.

### 2.4. Subir o front (opcional)

Em outro terminal, com a API rodando:

```powershell
cd frontend
npm install        # só na primeira vez
npm run dev        # abre em http://localhost:5173
```

### 2.5. Parar e recomeçar

| Para | Comando |
|---|---|
| Parar a API | `Ctrl+C` no terminal dela |
| Parar o banco (mantém os dados) | `docker compose down` |
| Apagar os dados e recomeçar do zero | `docker compose down -v` |

---

## 3. Usuários de teste

Com o perfil `demo`, a senha de **todos** é `senha123`:

| Matrícula | Nome | Perfil | Para que serve na demo |
|---|---|---|---|
| `2026A0142` | Ana Souza (id 1) | ALUNO | Tem um empréstimo atrasado (O Cortiço) e um ativo (Dom Casmurro) |
| `2026A0150` | Bruno Lima (id 2) | ALUNO | "Outro aluno", para mostrar o 403 |
| `2026A0101` | Carla Mendes (id 3) | ALUNO | 1ª do ranking, 5 conquistas |
| `2026A0160` | Diego Fernandes (id 8) | ALUNO | Sem histórico: telas vazias, fora do ranking |
| `PROF01` | Helena Martins (id 9) | PROFESSOR | Cadastra acervo e consulta alunos |
| `BIB01` | Rosa Almeida (id 10) | BIBLIOTECARIO | Faz tudo |

Sem o perfil demo, só existe o `admin` / `admin123` (bibliotecário), criado na primeira subida.

---

## 4. Como usar o Swagger com login

1. Em `POST /auth/login`, clique em **Try it out** e envie:
   ```json
   { "matricula": "2026A0142", "senha": "senha123" }
   ```
2. Copie o valor de `token` da resposta (o texto longo começando com `eyJ`).
3. Clique no botão **Authorize** (cadeado, no topo), cole **só o token** e confirme.
4. Pronto: as próximas chamadas vão com `Authorization: Bearer <token>`. Para trocar de usuário, clique em
   **Authorize → Logout** e repita com outra matrícula.

Rotas de consulta do acervo (`GET /materiais`, `/categorias`, `/autores`, `/editoras`, `/avaliacoes`, `/conquistas`)
funcionam **sem** login.

---

## 5. Como funciona: arquitetura

### 5.1. Camadas

Cada requisição passa pelas mesmas camadas, cada uma com uma responsabilidade:

```
Requisição HTTP
   │
   ▼
Spring Security (SegurancaConfig)   → confere o token e se o perfil pode usar a rota (401/403)
   │
   ▼
Controller (controller/)            → recebe o JSON, valida com @Valid, define o status HTTP
   │      usa DTOs (dto/): XxxRequestDTO na entrada, XxxResponseDTO na saída
   ▼
Service (service/)                  → regras de negócio (RN01...RN22); lança exceções de negócio
   │
   ▼
Repository (repository/)            → Spring Data JPA: consultas ao banco
   │
   ▼
MySQL (tabelas criadas pelas migrations do Flyway)
```

Regras do projeto: o controller nunca acessa o repository direto e nunca devolve entidade (sempre DTO); quem decide
regra de negócio é o service.

### 5.2. Exemplo completo: um aluno pega um livro emprestado

`POST /emprestimos` com `{ "usuarioId": 1, "materialId": 1 }` e o token da Ana:

1. **Segurança:** o Spring Security valida o token (assinatura e validade) e descobre que é a Ana, perfil ALUNO.
2. **Controller** (`EmprestimoController.registrar`): o `@Valid` confere o corpo; o `UsuarioLogado` confere que o
   `usuarioId` é da própria Ana (senão, 403).
3. **Service** (`EmprestimoService.registrar`):
   - busca o usuário (404 se não existe);
   - **trava** a linha do material no banco (`SELECT ... FOR UPDATE`), para dois pedidos simultâneos não pegarem a
     mesma última licença;
   - RN01: licenças livres = total − empréstimos ATIVOS ou ATRASADOS (409 se não há);
   - RN03: a Ana já está com esse material? (409);
   - RN04: ela já tem 3 empréstimos em uso? (409);
   - RN02: sem data informada, o prazo é hoje + 14 dias;
   - grava o empréstimo como ATIVO.
4. **Resposta:** 201 Created com o empréstimo em JSON e o cabeçalho `Location`.

### 5.3. Erros: sempre no mesmo formato

Toda falha volta como `{ timestamp, status, erro, mensagem, campos }`. Quem monta é o `GlobalExceptionHandler`
(e, para 401/403 antes do controller, o `RespostasDeSeguranca`):

| Situação | Exceção | Status |
|---|---|---|
| Campo inválido no corpo ou na URL | validação (`@Valid`, `@Min`...) | 400 (com `campos`) |
| Dado obrigatório que a validação não cobre (ex.: senha no cadastro) | `RequisicaoInvalidaException` | 400 |
| Sem token, token inválido, login errado | — / `CredenciaisInvalidasException` | 401 |
| Perfil sem permissão | `AccessDeniedException` | 403 |
| ID que não existe | `RecursoNaoEncontradoException` | 404 |
| Regra de negócio violada | `RegraNegocioException` | 409 |
| Constraint do banco (rede de segurança) | `DataIntegrityViolationException` | 409 |
| Erro inesperado (sem detalhes internos) | qualquer outra | 500 |

### 5.4. Banco e migrations

- As tabelas **só** são criadas ou alteradas por migrations em `src/main/resources/db/migration`
  (`V<n>__descricao.sql`). Migration que já está na `main` nunca é editada: cria-se outra.
- Faixas por pessoa: `V10+` Pessoa 1, `V20+` Pessoa 2, `V30+` Pessoa 3, `V40+` funcionalidades que dependem de
  Usuário e Empréstimo. Uma chave estrangeira exige número maior que o da tabela referenciada, porque num banco vazio
  o Flyway aplica em ordem de versão.

| Migration | O que cria |
|---|---|
| V10, V11 | `autores`, `editoras` |
| V20, V21, V22 | `categorias`, `materiais`, `material_autor` (N:N) e a editora do material |
| V30, V31 | `usuarios`, `emprestimos` |
| V40, V41 | `avaliacoes`, `progressos_leitura` |
| V42 | `perfil` e `senha_hash` em `usuarios` |

---

## 6. Como funciona: cada parte do sistema

### 6.1. Os 5 CRUDs

| CRUD | Rota | Destaques |
|---|---|---|
| Categoria | `/categorias` | Nome único (RN07). Não exclui categoria com materiais (RN06) |
| Autor | `/autores` | Não exclui autor com materiais (RN06) |
| Editora | `/editoras` | CNPJ validado pelos dígitos e único (RN07); salvo só com os 14 dígitos. Não exclui editora com materiais (RN06) |
| Material | `/materiais` | Pertence a uma categoria, tem um ou mais autores (N:N) e editora opcional, todos enviados pelo ID. Ano até o atual (`@AnoNaoFuturo`) |
| Empréstimo | `/emprestimos` | Regras RN01 a RN05 (abaixo). Editar = renovar o prazo |

O **Usuário** (`/usuarios`) é o das aulas, reaproveitado: nome, e-mail e matrícula únicos, perfil e senha. Não dá para
excluir usuário com empréstimos.

### 6.2. Empréstimo (RN01 a RN05)

| Regra | Como funciona |
|---|---|
| RN01 | Só empresta com licença livre. A conta é feita na hora (total − empréstimos ATIVOS/ATRASADOS): devolver libera a licença automaticamente |
| RN02 | Prazo padrão de 14 dias |
| RN03 | O aluno não pode estar com o mesmo material duas vezes |
| RN04 | No máximo 3 empréstimos em uso por aluno |
| RN05 | Vencidos viram ATRASADO: ao subir a aplicação, a cada hora (`EmprestimoAtrasoAgendador`) e em `PATCH /emprestimos/atualizar-atrasados` |

Status: **ATIVO** → (prazo vence) → **ATRASADO** → (renovar) → ATIVO; e **DEVOLVIDO** ao devolver
(`PATCH /emprestimos/{id}/devolver`). Os valores 14 e 3 ficam no `application.properties`.

### 6.3. Recomendação personalizada (funcionalidade extra)

`GET /usuarios/{id}/recomendacoes`: para cada material que o aluno ainda não pegou, soma +1 ponto por empréstimo
anterior da mesma categoria e +2 por empréstimo anterior do mesmo autor; devolve os 10 com mais pontos e o motivo.

### 6.4. Busca no acervo

`GET /materiais/busca` combina filtros opcionais (título, autor, categoria, editora, tipo, série, ano, nota mínima,
disponível agora), ordena (título, ano, melhor avaliados) e pagina. Por dentro, cada filtro informado vira uma
condição de uma `Specification` (os vazios são ignorados).

Cada material mostra também `licencasDisponiveis`, `mediaAvaliacoes` e `totalAvaliacoes`. Não são colunas: o Hibernate
calcula com subconsultas (`@Formula`) na mesma consulta do material, então nunca ficam desatualizados.

### 6.5. Avaliações

`/avaliacoes`: nota de 1 a 5 e comentário opcional. Só avalia quem já pegou o material emprestado (RN11), uma vez por
material (RN12, com `UNIQUE` no banco). Ninguém avalia em nome de outro.

### 6.6. Progresso de leitura

`PUT /emprestimos/{id}/progresso` com `{ "paginaAtual": 120 }`: grava a página e devolve o percentual lido e se
terminou. Só vale para empréstimos ATIVOS ou ATRASADOS, e a página vai de 1 até o total do material (vídeo não tem
páginas). `GET /usuarios/{id}/leituras` mostra tudo o que o aluno está lendo. Fica numa tabela própria, ligada 1:1 ao
empréstimo.

### 6.7. Gamificação

Calculada a cada consulta a partir do histórico (nada é gravado):

| Ação | Pontos |
|---|---|
| Terminar a leitura de um material (cada material conta uma vez) | 10 |
| Devolver no prazo | 3 |
| Avaliar | 2 (+1 com comentário) |

Níveis: Iniciante (0), Leitor (50), Voraz (150), Devorador (300), Lenda (500). Conquistas: Primeira Página,
Maratonista, Explorador, Crítico, Pontual, Clássico. Rotas: `GET /usuarios/{id}/perfil-leitor`, `GET /ranking`
(nome abreviado, empatados na mesma posição), `GET /conquistas`.

### 6.8. Dados de demonstração (seed)

`config/DadosDemo.java`, só com o perfil `demo` e o banco vazio. Os 16 livros são clássicos em domínio público do
Project Gutenberg (obtidos pela API Gutendex), com o link real de leitura; as páginas vieram do Open Library. As datas
são relativas a hoje, então sempre há empréstimos ativos, atrasados e um histórico com avaliações e conquistas.

---

## 7. Como funciona: autenticação e perfis

### 7.1. Login e token

1. `POST /auth/login` com matrícula e senha.
2. O `AutenticacaoService` busca o usuário pela matrícula e compara a senha com o **hash BCrypt** guardado (a senha em
   si nunca é salva). Matrícula inexistente e senha errada dão a mesma mensagem, para não revelar quais matrículas
   existem.
3. O `TokenService` gera um **token JWT** assinado (HS256) com o ID, o nome, a matrícula e o **perfil**, válido por
   8 horas.
4. Nas próximas requisições, o token vai no cabeçalho `Authorization: Bearer <token>`. O Spring Security confere a
   assinatura e a validade: se alguém alterar o token (por exemplo, trocar o perfil), a assinatura deixa de bater e a
   resposta é 401.

Não existe sessão no servidor: tudo o que ele precisa saber está no token.

### 7.2. Perfis e permissões

| O que | Sem login | ALUNO | PROFESSOR | BIBLIOTECARIO |
|---|---|---|---|---|
| Consultar acervo, avaliações e conquistas | sim | sim | sim | sim |
| Cadastrar, editar e excluir acervo | — | — | sim | sim |
| Pegar emprestado, renovar, devolver, registrar leitura | — | só os seus | só os seus | de qualquer aluno |
| Ver empréstimos, leituras, perfil de leitor e recomendações | — | só os seus | de qualquer aluno | de qualquer aluno |
| Avaliar | — | em seu nome | em seu nome | em seu nome |
| Excluir avaliação de outro (moderação) | — | — | — | sim |
| Ranking | — | sim | sim | sim |
| Gerenciar usuários (perfil e senha), excluir empréstimo, verificar atrasados | — | — | — | sim |

### 7.3. Onde cada permissão é checada

Em duas camadas:

1. **Por perfil e por rota**, no `SegurancaConfig` (ex.: `POST /materiais` só PROFESSOR ou BIBLIOTECARIO). A ordem das
   regras importa: vale a primeira que bater.
2. **Por dono do recurso**:
   - quando o dono está na URL (`/usuarios/{id}`, `/emprestimos/{id}`, `/avaliacoes/{id}`), quem decide é o
     `RegrasDeAcesso`, que lê o `{id}` e, se precisar, busca de quem é o empréstimo ou a avaliação;
   - quando o dono está no corpo (`usuarioId` ao pegar emprestado ou avaliar), quem checa é o controller, com o
     `UsuarioLogado`.

**401 × 403:** 401 = "não sei quem você é" (sem token ou token inválido); 403 = "sei quem você é, mas você não pode".

### 7.4. Primeiro acesso

Só o bibliotecário cadastra usuários. Para o primeiro acesso não ficar impossível, o `BibliotecarioInicial` cria
`admin` / `admin123` quando o banco não tem nenhum bibliotecário (variáveis `ADMIN_MATRICULA` e `ADMIN_SENHA`). Qualquer
usuário troca a própria senha em `PUT /auth/senha`. A chave que assina os tokens vem da variável `JWT_SECRET` (mínimo de
32 caracteres; o padrão do `application.properties` serve só para desenvolvimento).

---

## 8. Como funciona: o front-end

- **Proxy:** o front chama `/api/...` e o Vite repassa para `http://localhost:8080/...` (sem o `/api`). Por isso não
  precisa configurar CORS. API em outra porta: `frontend/.env.local` com `API_URL=http://localhost:8081`.
- **Login:** a tela de entrada pede matrícula e senha; o token fica no navegador (`localStorage`) e vai em todas as
  chamadas. Se a API responder 401 (token vencido), o front volta para a entrada.
- **Áreas:** o aluno usa a área do leitor (Início, Acervo, Minhas leituras, Ranking, Perfil de leitor). O professor vê
  também o painel da equipe, só para consulta. O bibliotecário entra direto no painel e registra, renova e devolve
  empréstimos.
- Esconder botões no front é só conforto: quem garante as permissões é a API.
- **Organização:** `api/` (uma função por rota), `sessao/` (usuário logado e pontos), `hooks/useCarregar` (chama a
  API e guarda dados, erro e "carregando"), `paginas/` (uma tela por arquivo), `componentes/` (cabeçalho, abas, capa,
  barra de progresso).

---

## 9. Roteiro da demonstração

Antes: banco zerado e API com o perfil `demo` (passo 2.3). Pelo Swagger (passo 4) ou pelo front.

| # | O que fazer | O que mostra |
|---|---|---|
| 1 | Sem login: `GET /materiais/busca?serie=TERCEIRO_ANO&disponivel=true&ordenarPor=AVALIACAO` | Catálogo público, busca com filtros, média e licenças livres |
| 2 | Sem login: `GET /ranking` | 401: rota protegida |
| 3 | Login como Ana (`2026A0142`) e Authorize | Token JWT |
| 4 | `GET /usuarios/1/leituras` | Dom Casmurro ativo e O Cortiço ATRASADO (RN05), com o percentual lido |
| 5 | `GET /usuarios/2/leituras` (dados do Bruno) | 403: aluno só vê os próprios dados |
| 6 | `POST /emprestimos` com `{"usuarioId":1,"materialId":4}` | 409: O Cortiço tem 1 licença, e ela está com a própria Ana, atrasada (RN01) |
| 7 | `POST /emprestimos` com `{"usuarioId":1,"materialId":3}` (Quincas Borba) | 201: empréstimo com prazo de 14 dias (RN02) |
| 8 | `PUT /emprestimos/{id}/progresso` com `{"paginaAtual":270}` | Leitura concluída (última página) |
| 9 | `PATCH /emprestimos/{id}/devolver` e `POST /avaliacoes` | Devolução no prazo e avaliação: pontos sobem |
| 10 | `GET /usuarios/1/perfil-leitor` e `GET /usuarios/1/recomendacoes` | Pontos, nível, conquistas e recomendações |
| 11 | `POST /materiais` com o token da Ana | 403: aluno não cadastra acervo |
| 12 | Login como Rosa (`BIB01`) e `DELETE /categorias/1` | 409: categoria com materiais (RN06) |
| 13 | Como Rosa: `POST /materiais` com `anoPublicacao: 2030` | 400 com o campo e a mensagem |

---

## 10. Testes

```powershell
mvnw.cmd test        # precisa do MySQL rodando (docker compose up -d)
```

São 106 testes. Os de service usam Mockito (sem banco) e cobrem as regras de negócio, login e senha; o
`BibliotecaVirtualApplicationTests` sobe a aplicação inteira e confirma que as migrations rodam e que o Hibernate aceita
o schema. No front: `npm run build` (confere os tipos) e `npm run lint`.

---

## 11. Entrega

- A versão entregue é a tag `np2-entrega` (`git rev-parse np2-entrega` mostra o hash).
- ZIP limpo, só com os arquivos versionados (sem `node_modules`, `target` ou `.env`):
  ```bash
  git archive --format=zip --output=../biblioteca-virtual-api-np2.zip np2-entrega
  ```

---

## 12. Problemas comuns

| Problema | Solução |
|---|---|
| `ExceptionInInitializerError` ao compilar | JDK muito novo. Use o 17 ou o 21 (`JAVA_HOME`) |
| Docker diz "ports are not available" (3306 ocupada) | Use outra porta: `$env:DB_PORT = "3307"` antes do `docker compose up -d` e do `mvnw` |
| O seed não carregou | O banco não estava vazio. `docker compose down -v`, `docker compose up -d` e suba de novo com o perfil demo |
| 401 em tudo no Swagger | Faltou o Authorize, ou o token venceu (8 h): faça login de novo |
| 403 | O usuário logado não tem permissão (veja a tabela da seção 7.2) |
| Front mostra "Não foi possível falar com a API" | A API não está rodando na 8080 (ou ajuste `API_URL` em `frontend/.env.local`) |
| Usuário antigo não consegue entrar | Usuários criados antes da autenticação não têm senha: o bibliotecário define em `PUT /usuarios/{id}` |

---

## 13. Onde fica cada coisa

```
src/main/java/com/np2/bibliotecavirtual/
├── config/        SegurancaConfig (permissões), OpenApiConfig (Swagger), DadosDemo (seed),
│                  BibliotecarioInicial, AgendamentoConfig (rotina de atrasos)
├── controller/    Uma classe por recurso: rotas, @Valid e status HTTP
├── dto/           Entrada (RequestDTO) e saída (ResponseDTO) de cada rota
├── exception/     Exceções de negócio e GlobalExceptionHandler (formato de erro)
├── model/         Entidades JPA e enums (TipoMaterial, StatusEmprestimo, PerfilUsuario...)
├── repository/    Consultas ao banco (Spring Data JPA) e MaterialFiltros (busca)
├── seguranca/     TokenService (JWT), UsuarioLogado, RegrasDeAcesso, RespostasDeSeguranca
├── service/       Regras de negócio (um service por recurso, mais Gamificacao e Recomendacao)
└── validacao/     @AnoNaoFuturo
src/main/resources/
├── application.properties       Banco, JWT, prazos e Swagger
└── db/migration/                Migrations do Flyway (V10 a V42)
src/test/                        Testes (JUnit 5 + Mockito)
frontend/                        Front-end React (veja frontend/README.md)
docs/                            Este guia, requisitos, roadmap, sprints e registro de uso de IA
```
