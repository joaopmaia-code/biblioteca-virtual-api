# CLAUDE.md

Convenções do projeto **Biblioteca Virtual API** (NP2 – Tópicos Web). O planejamento
completo está em `docs/NP2 – Biblioteca Virtual Planejamento Completo.pdf`; em caso de
dúvida, ele é a fonte da verdade.

## Projeto

API REST (sem interface visual) de uma biblioteca virtual de escola de ensino médio,
feita por 3 pessoas. Entrega e apresentação até 06/11/2026.

- Java 17+ (código compilado com `java.version=17`), Spring Boot 3.5, Maven (wrapper `mvnw`)
- Spring Web, Spring Data JPA, Bean Validation, Flyway (+ `flyway-mysql`), MySQL 8.4, Lombok
- Swagger com springdoc-openapi em `/swagger-ui.html`
- Pacote base: `com.np2.bibliotecavirtual`

## Como rodar

```bash
docker compose up -d          # sobe o MySQL (localhost:3306)
./mvnw spring-boot:run        # Windows: mvnw.cmd spring-boot:run
# Swagger: http://localhost:8080/swagger-ui.html
./mvnw test                   # precisa do MySQL rodando
```

Credenciais do banco vêm das variáveis `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`,
`DB_PASSWORD` (padrões de desenvolvimento em `application.properties` e `.env.example`).

## Camadas (pacotes)

| Pacote | Conteúdo |
|---|---|
| `model` | Entidades `@Entity` e enums |
| `dto` | `XxxRequestDTO` (entrada, com Bean Validation) e `XxxResponseDTO` (saída) |
| `repository` | Interfaces `extends JpaRepository<Entidade, Long>` |
| `service` | Regras de negócio; converte DTO ↔ entidade; lança exceções de negócio |
| `controller` | `@RestController`; `@Valid` no corpo; define status HTTP; documentação Swagger |
| `exception` | Exceções de negócio e `GlobalExceptionHandler` (`@RestControllerAdvice`) |
| `config` | Configurações (ex.: `OpenApiConfig`) |

Regras:
- Controller nunca acessa Repository direto e nunca recebe/devolve entidade; sempre DTO.
- Service lança `RecursoNaoEncontradoException` (404) e `RegraNegocioException` (409);
  o `GlobalExceptionHandler` transforma em resposta de erro padrão
  (`timestamp`, `status`, `erro`, `mensagem`, `campos`). Validação inválida vira 400.
- Unicidade (CNPJ da editora, nome da categoria): constraint `UNIQUE` na migration **e**
  checagem `existsBy...` no Service retornando 409.
- Toda rota tem `@Operation` e `@ApiResponses` no Swagger.

## Nomes

- Classes no singular, PascalCase, sem acento: `Material`, `Autor`, `Editora`,
  `Categoria`, `Emprestimo`, `Reserva`.
- Tabelas no plural e snake_case via `@Table`: `materiais`, `autores`, `editoras`,
  `categorias`, `emprestimos`, `reservas`; ligação N:N `material_autor`.
- Rotas no plural: `/materiais`, `/autores`, `/editoras`, `/categorias`,
  `/emprestimos`, `/reservas`.
- Atributos em camelCase no Java e snake_case no banco (`@Column(name = "area_conhecimento")`).
- DTOs: `CategoriaRequestDTO` / `CategoriaResponseDTO`.
- Enums gravados como texto (`@Enumerated(EnumType.STRING)`, coluna `VARCHAR`).

## Migrations (Flyway)

- Ficam em `src/main/resources/db/migration`, nome `V<n>__descricao.sql`.
- `spring.jpa.hibernate.ddl-auto=validate`: tabela só é criada/alterada por migration.
- Faixas de versão por pessoa (evita conflito de número):

| Faixa | Dono |
|---|---|
| `V1`–`V9` | Estrutura inicial |
| `V10+` | Pessoa 1 (Autor, Editora, Reserva) |
| `V20+` | Pessoa 2 (Categoria, Material, `material_autor`) |
| `V30+` | Pessoa 3 (Empréstimo, Usuário) |

- `spring.flyway.out-of-order=true` permite aplicar uma migration de número menor que
  chegue na `main` depois de uma maior.
- **Nunca editar uma migration que já está na `main`**; criar uma nova.

## Divisão da equipe (vertical slicing)

Equipe **Compila e Reza**:

| Pessoa | Quem | Responsabilidades |
|---|---|---|
| Pessoa 1 | Pedro Affonso (@pedrohaffonso) | CRUD de Autor, CRUD de Editora (CNPJ único), entidade Reserva e fila de espera |
| Pessoa 2 | João Pedro (@joaopmaia-code) | Setup do projeto, GlobalExceptionHandler, CRUD de Categoria, CRUD de Material (N:N com Autor), README inicial e documentação |
| Pessoa 3 | Bárbara Tavares (@barbarattavares) | CRUD de Empréstimo, devolução e regras RN01–RN05, Usuário, seed de dados e README final |

Cada pessoa mexe só nas próprias entidades; avisar no grupo antes de alterar uma
entidade de outra pessoa.

## Git

- Uma branch por funcionalidade (`feature/categoria`, `feature/emprestimo`) com Pull
  Request para a `main`. Atualizar a branch com `git pull origin main` antes de abrir PR.
- Commits em português no padrão Conventional Commits:
  `tipo(escopo): descrição` — ex.: `feat(categoria): cria CRUD de categorias`,
  `fix(emprestimo): ...`, `chore(setup): ...`, `docs: ...`.
- Rodar o projeto e testar no Swagger antes de abrir o PR.
- Uso de IA deve ser registrado em `docs/USO_DE_IA.md` (ferramenta, uso, verificação).
