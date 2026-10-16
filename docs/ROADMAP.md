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
| Sprint 1 | 16/10 a 22/10 | Base e CRUDs simples | Requisitos, divisão, proposta da fila para aprovação e 3 CRUDs no Swagger |
| Sprint 2 | 23/10 a 29/10 | CRUDs principais | Os 5 CRUDs prontos e a fila em andamento |
| Sprint 3 | 30/10 a 04/11 | Fila completa e acabamento | Demo completa da fila de espera |
| Folga | 05/11 e 06/11 | Entrega e ensaio | — |

> **Caminho crítico:** Material e Empréstimo. Se travarem, os outros ajudam primeiro neles.

---

## Sprint 1 (16/10 a 22/10): base e CRUDs simples

Meta: setup e todos os Models na `main` até sábado, 17/10.

### Pessoa 1 — Pedro Affonso
- [ ] Model + migration de Autor e Editora
- [ ] CRUD completo de Autor
- [ ] CRUD completo de Editora

### Pessoa 2 — João Pedro
- [ ] Setup do projeto (Spring, banco, Flyway, Swagger)
- [ ] Repositório, README inicial e documentação
- [ ] Model + migration de Categoria e Material
- [ ] GlobalExceptionHandler
- [ ] CRUD completo de Categoria

### Pessoa 3 — Bárbara Tavares
- [ ] Model + migration de Empréstimo e Reserva
- [ ] Adaptar o Usuário das aulas
- [ ] DTOs e Repository de Empréstimo

**Mostrar ao professor:** requisitos, divisão, proposta da fila para aprovação e 3 CRUDs no Swagger.

---

## Sprint 2 (23/10 a 29/10): CRUDs principais

### Pessoa 1 — Pedro Affonso
- [ ] Service e rotas da Reserva
- [ ] Regras RN08–RN10

### Pessoa 2 — João Pedro
- [ ] CRUD de Material (N:N com Autor)
- [ ] Revisar validações e Swagger

### Pessoa 3 — Bárbara Tavares
- [ ] CRUD completo de Empréstimo
- [ ] Regras RN01–RN04
- [ ] Devolução

**Mostrar ao professor:** os 5 CRUDs prontos e a fila em andamento.

---

## Sprint 3 (30/10 a 04/11): fila completa e acabamento

A integração fila × devolução é feita em conjunto pelas Pessoas 1 e 3, registrada com
`Co-authored-by:` nos commits.

### Pessoa 1 — Pedro Affonso
- [ ] Integração fila × devolução (com a Pessoa 3)
- [ ] RN11 e correção de bugs da fila
- [ ] Registro do uso de IA

### Pessoa 2 — João Pedro
- [ ] Revisão final do código e camadas
- [ ] Testar todos os fluxos no Swagger

### Pessoa 3 — Bárbara Tavares
- [ ] Integração fila × devolução (com a Pessoa 1)
- [ ] Status ATRASADO (RN05)
- [ ] Seed de dados + README final

**Mostrar ao professor:** demo completa da fila de espera.

---

## Folga (05/11 e 06/11)

- [ ] Montar o ZIP de entrega
- [ ] Ensaiar a apresentação (10 min de demo, cada um explicando o próprio código)
- [ ] Garantir que o projeto sobe de primeira na máquina da apresentação

> Se o tempo apertar, RN05 e RN11 podem ser simplificadas.

---

## Entrega e apresentação

O ZIP vai para o Moodle até 06/11 e o sistema precisa estar rodando no início do horário reservado.

### Checklist do ZIP
- [ ] Código-fonte do projeto
- [ ] Nomes dos integrantes e usuários do GitHub
- [ ] Link do repositório e commit da versão entregue
- [ ] Instruções de configuração e execução
- [ ] Descrição das contribuições de cada integrante
- [ ] Registro do uso de IA (ferramenta, para que foi usada, como o resultado foi verificado)

### Apresentação (até 15 min)
- Até 10 min: projeto, demo das principais funcionalidades (incluindo a fila) e participação de cada um.
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
próximo passo, para os acompanhamentos com o professor e a descrição das contribuições.
