# Roadmap

Planejamento da equipe **Compila e Reza**: 3 sprints de uma semana até 04/11/2026, com
05 e 06/11 de folga para entrega e ensaio. A entrega no Moodle é até 06/11.

| Pessoa | Nome | GitHub |
|---|---|---|
| Pessoa 1 | Pedro Affonso | [@pedrohaffonso](https://github.com/pedrohaffonso) |
| Pessoa 2 | João Pedro | [@joaopmaia-code](https://github.com/joaopmaia-code) |
| Pessoa 3 | Bárbara Tavares | [@barbarattavares](https://github.com/barbarattavares) |

Cada integrante faz todas as camadas das próprias entidades (vertical slicing), para ter
commits relevantes e conseguir explicar o fluxo de ponta a ponta.

---

## Visão geral

| Sprint | Período | Foco | Mostrar ao professor |
|---|---|---|---|
| Sprint 1 | 16/10 a 22/10 | Base e CRUDs simples | Requisitos, divisão, proposta da funcionalidade extra para aprovação e 3 CRUDs no Swagger |
| Sprint 2 | 23/10 a 29/10 | CRUDs principais | Os 5 CRUDs prontos e a funcionalidade extra em andamento |
| Sprint 3 | 30/10 a 04/11 | Funcionalidade extra e acabamento | Demo completa, incluindo a recomendação personalizada |
| Folga | 05/11 e 06/11 | Entrega e ensaio | — |

> **Caminho crítico:** Material e Empréstimo. Se travarem, os outros ajudam primeiro neles.

> **Mudança de escopo (06/11):** a funcionalidade extra deixou de ser a fila de espera e passou a ser a
> recomendação personalizada (o plano B do planejamento), porque a fila dependia de Usuário e Empréstimo,
> que só ficaram prontos no fim. Com isso a entidade Reserva não foi criada. Detalhes em
> [REQUISITOS](REQUISITOS.md#funcionalidade-extra-recomendação-personalizada).

### Situação em 08/11

Na `main`: os 5 CRUDs (Material, Autor, Editora, Categoria, Empréstimo), Usuário, regras RN01–RN09,
tratamento de erros padronizado e a recomendação personalizada (PRs #1 a #11), mais busca no acervo,
avaliações, progresso de leitura e gamificação (RN10–RN17, PR #12). O front-end está na branch `feature/front`. Pendências na seção
[O que falta](#o-que-falta).

---

## Sprint 1 (16/10 a 22/10): base e CRUDs simples

Meta: setup e todos os Models na `main` até sábado, 17/10.

### Pessoa 1 — Pedro Affonso
- [x] Model + migration de Autor e Editora (PR #4, 28/10)
- [x] CRUD completo de Autor (PR #5, 01/11)
- [x] CRUD completo de Editora (PR #6, 01/11)

### Pessoa 2 — João Pedro
- [x] Setup do projeto (Spring, banco, Flyway, Swagger)
- [x] Repositório, README inicial e documentação (PR #3)
- [x] Model + migration de Categoria e Material (PRs #2 e #3)
- [x] GlobalExceptionHandler (PR #1)
- [x] CRUD completo de Categoria (PR #2)

### Pessoa 3 — Bárbara Tavares
- [x] Model + migration de Empréstimo e Usuário (`V30`, `V31`; PR #9, 05/11)
- [ ] ~~Model + migration de Reserva~~ (fila substituída pela recomendação)
- [x] Adaptar o Usuário das aulas, com e-mail e matrícula únicos (PR #9, 05/11)
- [x] DTOs e Repository de Empréstimo (PR #9, 05/11)

**Mostrar ao professor:** requisitos, divisão, proposta da funcionalidade extra para aprovação e 3 CRUDs no Swagger.

---

## Sprint 2 (23/10 a 29/10): CRUDs principais

### Pessoa 1 — Pedro Affonso
- [ ] ~~Service e rotas da Reserva~~ (fila substituída pela recomendação)
- [ ] ~~Regras RN08–RN10 da fila~~ (RN08 e RN09 passaram a ser as regras da recomendação)

### Pessoa 2 — João Pedro
- [x] CRUD de Material (N:N com Autor) (PR #7, 02/11)
- [x] Revisar validações e Swagger (PR #7, PR #8)

### Pessoa 3 — Bárbara Tavares
- [x] CRUD completo de Empréstimo (PR #9, 05/11)
- [x] Regras RN01–RN04 (PR #9, 05/11)
- [x] Devolução (PR #9, 05/11)

**Mostrar ao professor:** os 5 CRUDs prontos e a funcionalidade extra em andamento.

---

## Sprint 3 (30/10 a 04/11): funcionalidade extra e acabamento

A integração fila × devolução, que seria feita pelas Pessoas 1 e 3, saiu do escopo com a troca da fila
pela recomendação.

### Pessoa 1 — Pedro Affonso
- [ ] ~~Integração fila × devolução (com a Pessoa 3)~~ (fila substituída pela recomendação)
- [ ] ~~RN11 e correção de bugs da fila~~
- [x] RN06 em Autor e Editora (PR #10, 06/11)
- [x] Funcionalidade extra: recomendação personalizada, `GET /usuarios/{id}/recomendacoes` (PR #10, 06/11)
- [x] Registro do uso de IA (PR #10, 06/11)
- [x] README e documentação atualizados com a troca da fila (PR #11, 06/11)

### Pessoa 2 — João Pedro
- [x] Padronizar erros 404 de rota, 405, 415 e 500 (PR #8, 04/11)
- [ ] Revisão final do código e camadas
- [ ] Testar todos os fluxos no Swagger (incluindo empréstimo e recomendação)

### Pessoa 3 — Bárbara Tavares
- [ ] ~~Integração fila × devolução (com a Pessoa 1)~~ (fila substituída pela recomendação)
- [x] Status ATRASADO (RN05), com agendador a cada hora (PR #9, 05/11)
- [x] Registro do uso de IA (PR #9, 05/11)
- [ ] Seed de dados
- [ ] README final

**Mostrar ao professor:** demo completa, incluindo a recomendação personalizada.

---

## Folga (05/11 e 06/11)

- [ ] Montar o ZIP de entrega
- [ ] Ensaiar a apresentação (10 min de demo, cada um explicando o próprio código)
- [ ] Garantir que o projeto sobe de primeira na máquina da apresentação

---

## Funcionalidades adicionais (08/11)

Para a API se parecer mais com uma biblioteca digital. Detalhes em
[REQUISITOS](REQUISITOS.md#funcionalidades-adicionais-pessoa-2-0811).

### Pessoa 2 — João Pedro
- [x] Busca no acervo: `GET /materiais/busca` com filtros, ordenação e paginação (RF09)
- [x] Avaliações: CRUD em `/avaliacoes`, média e total no material (RF10, RN10–RN12, migration `V40`)
- [x] Progresso de leitura: `/emprestimos/{id}/progresso` e `/usuarios/{id}/leituras` (RF11, RN13–RN14, migration `V41`)
- [x] Licenças disponíveis em cada material
- [x] Gamificação: perfil de leitor, níveis, conquistas e ranking (RF12, RN15–RN17)
- [x] Capas dos livros: campo `urlCapa` (migration `V43`), capas reais do Project Gutenberg no seed e imagem no front
  com o bloco colorido como reserva
- [x] Autenticação e perfis (ALUNO, PROFESSOR, BIBLIOTECARIO): login com JWT, senha BCrypt, permissões por perfil e
  por dono do recurso, migration `V42`, login no front (RF13, RN18–RN22)
- [x] Testes (99 no total) e teste de ponta a ponta num MySQL vazio
- [x] Commit, PR e merge na `main` (PR #12, 08/11)
- [ ] Avisar o grupo: faixa nova de migrations `V40+` e rotas novas sob `/emprestimos` e `/usuarios`
- [ ] Alinhar com o professor (a funcionalidade extra oficial continua sendo a recomendação)

### Front-end (branch `feature/front`, opcional)
- [x] Telas desenhadas (entrada, início, acervo, material, leituras, perfil, ranking, painel da equipe)
- [x] Projeto React + Vite + TypeScript em `frontend/`, com proxy `/api` para o Spring
- [x] Fluxo do aluno e painel de empréstimos da equipe ligados à API
- [ ] Testar no navegador, commit, PR e merge
- [ ] Avisar o grupo e checar com o professor se o front entra na apresentação

---

## O que falta

### Código
- [x] Mensagem de erro da RN01 não manda mais o aluno "entrar na fila de espera" (08/11)
- [ ] Tirar as referências à fila de espera que sobraram em comentários (Pessoa 3):
  - `EmprestimoService` e `EmprestimoRegras`: comentários e o método `podeReceber` falam da fila, de RN11 e da Reserva;
  - `Usuario`: o Javadoc diz que o aluno "entra na fila de espera".
- [x] Seed de dados para a demo (perfil `demo`, `config/DadosDemo.java`): livros reais do Project Gutenberg,
  alunos, empréstimos, progresso e avaliações, com datas relativas a hoje (08/11).
- [x] RN01 sob concorrência: dois pedidos simultâneos pela última licença eram aceitos; corrigido com
  `READ_COMMITTED` no `EmprestimoService.registrar` (08/11, avisar a Pessoa 3).

### Documentação
- [ ] `SPRINTS.md`: preencher as entregas e dificuldades da Pessoa 3 nas três sprints e a linha dela no resumo
  das contribuições (PR #9); trocar "PR da `pedro-dev`" por PRs #10 e #11 na linha da Pessoa 1.
- [ ] `SPRINTS.md`: o próximo passo da Pessoa 2 ainda fala da "demo da fila de espera".
- [ ] README final (Pessoa 3): incluir o seed de dados, se for feito.

### Entrega
- [ ] Revisão final e teste de todos os fluxos no Swagger
- [ ] `./mvnw test` passando com o MySQL rodando
- [x] Registrar no README a versão entregue (tag `np2-entrega`)
- [x] Testar a demo do zero na `main` (banco vazio + perfil `demo`): seed, rotas do roteiro e 99 testes ok (08/11)
- [ ] Montar o ZIP e enviar no Moodle
- [ ] Ensaiar a apresentação

---

## Entrega e apresentação

O ZIP vai para o Moodle até 08/11 (prazo estendido pelo professor; o original era 06/11) e o sistema precisa
estar rodando no início do horário reservado.

### Checklist do ZIP
- [ ] Código-fonte do projeto
- [x] Nomes dos integrantes e usuários do GitHub (README, seção Equipe)
- [x] Link do repositório e versão entregue (README: tag `np2-entrega`; colar também o hash no Moodle)
- [x] Instruções de configuração e execução (README)
- [ ] Descrição das contribuições de cada integrante (falta a Pessoa 3 em `SPRINTS.md`)
- [x] Registro do uso de IA das 3 pessoas (ferramenta, para que foi usada, como o resultado foi verificado)

### Apresentação (até 15 min)
- Até 10 min: projeto, demo das principais funcionalidades (incluindo a recomendação personalizada) e participação de cada um.
- Até 5 min: perguntas individuais, sem IA e sem responder pelo colega; pode consultar o próprio código.
- Todos participam; a ausência de um não prejudica os demais.

---

## Como evitar que o código quebre

1. **Modelagem conjunta primeiro:** todos os Models e migrations vão para a `main` no início, antes de Services e Controllers. Cada um commita os Models das próprias entidades.
2. **Faixas de versão das migrations:** `V1`–`V9` estrutura inicial; Pessoa 1 `V10+`; Pessoa 2 `V20+`; Pessoa 3 `V30+`. Nunca editar uma migration que já está na `main`; criar uma nova.
3. **Atualizar a branch todo dia** com `git pull origin main` ou rebase.
4. **Avisar no grupo** antes de mudar uma entidade que outra pessoa usa.
5. **Rodar o projeto e testar no Swagger** antes de abrir o Pull Request.

Durante o projeto, cada sprint terá um registro do que cada um fez, das dificuldades e do
próximo passo, para os acompanhamentos com o professor e a descrição das contribuições:
[SPRINTS.md](SPRINTS.md).
