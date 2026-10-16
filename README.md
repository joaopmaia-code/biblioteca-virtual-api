# Biblioteca Virtual API

API REST (sem interface visual) de uma biblioteca pública virtual para uma escola de ensino médio.
Trabalho da NP2 – Tópicos Web, feito pela equipe **Compila e Reza** (3 pessoas), com entrega e apresentação até 06/11/2026.

Repositório: https://github.com/joaopmaia-code/biblioteca-virtual-api

## Funcionalidades

- **5 CRUDs completos** (cadastrar, editar, listar, consultar por ID e excluir): Material, Autor, Editora, Categoria e Empréstimo.
- **Funcionalidade extra: fila de espera (Reserva).** Quando todas as licenças de um material estão em uso, o aluno entra numa fila.
  Na devolução, o primeiro da fila (reserva `AGUARDANDO` mais antiga) recebe o empréstimo automaticamente e a reserva vira `ATENDIDA`.
  A posição na fila não é gravada no banco: é calculada ordenando as reservas por `dataSolicitacao`.
- O **Usuário** das aulas é reaproveitado e não conta como CRUD.

## Stack

| Tecnologia | Uso |
|---|---|
| Java 17+ | Linguagem (código compilado para Java 17) |
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

- **JDK 17 ou superior** (`java -version` para conferir)
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

## Testes

```bash
./mvnw test                # Windows: mvnw.cmd test
```

Precisa do MySQL rodando: o teste `contextLoads` sobe a aplicação inteira e confirma que o Flyway aplica as
migrations e que o Hibernate (`ddl-auto=validate`) aceita o schema.

## Rotas da API

### Implementadas (Sprint 1 – PR #2)

| Método | Rota | Sucesso | Erros possíveis |
|---|---|---|---|
| POST | `/categorias` | 201 Created (com `Location`) | 400, 409 |
| GET | `/categorias` | 200 OK (ordenado por nome) | — |
| GET | `/categorias/{id}` | 200 OK | 400, 404 |
| PUT | `/categorias/{id}` | 200 OK | 400, 404, 409 |
| DELETE | `/categorias/{id}` | 204 No Content | 400, 404 |

### Planejadas

| Rota | Descrição | Responsável |
|---|---|---|
| `/autores` | CRUD de Autor | Pessoa 1 |
| `/editoras` | CRUD de Editora (CNPJ único) | Pessoa 1 |
| `/materiais` | CRUD de Material (N:N com Autor) | Pessoa 2 |
| `/emprestimos` | CRUD de Empréstimo e devolução | Pessoa 3 |
| `POST /materiais/{id}/reservas` | Entrar na fila de espera | Pessoa 1 |
| `GET /materiais/{id}/fila` | Ver a fila do material com posições | Pessoa 1 |
| `GET /usuarios/{id}/reservas` | Ver as reservas e posições do aluno | Pessoa 1 |
| `PATCH /reservas/{id}/cancelar` | Sair da fila | Pessoa 1 |

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
│   └── service/       # Regras de negócio
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
| Pessoa 1 | Pedro Affonso | [@pedrohaffonso](https://github.com/pedrohaffonso) | CRUD de Autor, CRUD de Editora (CNPJ único), entidade Reserva e toda a fila de espera |
| Pessoa 2 | João Pedro | [@joaopmaia-code](https://github.com/joaopmaia-code) | Setup do projeto (Spring, banco, Flyway, Swagger), GlobalExceptionHandler, CRUD de Categoria, CRUD de Material (N:N com Autor), README inicial e documentação |
| Pessoa 3 | Bárbara Tavares | [@barbarattavares](https://github.com/barbarattavares) | CRUD de Empréstimo, devolução e regras RN01–RN05, integração com Usuário, seed de dados e README final |

## Documentação

- [Requisitos](docs/REQUISITOS.md)
- [Roadmap](docs/ROADMAP.md)
- [Registro do uso de IA](docs/USO_DE_IA.md)
- [Planejamento completo (PDF)](docs/NP2%20%E2%80%93%20Biblioteca%20Virtual%20Planejamento%20Completo.pdf)
