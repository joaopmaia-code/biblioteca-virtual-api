# Front-end da Biblioteca Virtual

Interface web da API, feita com **React 19 + Vite + TypeScript** e React Router. Não usa
biblioteca de componentes: os estilos ficam em `src/index.css`, com as cores e fontes do design.

## Como rodar

Precisa de **Node.js 20 ou mais novo** e da API rodando na porta 8080 (veja o README da raiz).

```bash
cd frontend
npm install        # só na primeira vez
npm run dev        # abre em http://localhost:5173
```

O front chama a API em `/api/...` e o Vite repassa para `http://localhost:8080` sem o prefixo
(`/api/materiais` → `/materiais`), então não é preciso configurar CORS. Se a API estiver em outra
porta, crie `frontend/.env.local` com:

```
API_URL=http://localhost:8081
```

Outros comandos: `npm run build` (confere os tipos e gera `dist/`) e `npm run lint`.

## Telas

| Rota | Tela | Rotas da API usadas |
|---|---|---|
| `/` | Entrada pela matrícula (aluno) ou acesso da equipe | `GET /usuarios` |
| `/inicio` | Continue lendo, nível e recomendações | `/usuarios/{id}/leituras`, `/perfil-leitor`, `/recomendacoes` |
| `/acervo` | Busca com filtros, ordenação e paginação | `/materiais/busca`, `/categorias` |
| `/materiais/:id` | Material, empréstimo e avaliações | `/materiais/{id}`, `POST /emprestimos`, `/avaliacoes` |
| `/leituras` | Progresso, renovar, devolver e histórico | `/emprestimos/{id}/progresso`, `PUT /emprestimos/{id}`, `PATCH /emprestimos/{id}/devolver` |
| `/perfil` | Pontos, nível, conquistas e extrato | `/usuarios/{id}/perfil-leitor` |
| `/ranking` | Ranking de leitores | `/ranking` |
| `/equipe` | Empréstimos de todos os alunos, novo empréstimo, atrasados | `/emprestimos`, `/materiais`, `/emprestimos/atualizar-atrasados` |
| `/equipe/acervo` | Licenças e avaliações de cada material | `/materiais` |

## Limites conhecidos

- **Sem login de verdade:** a API não tem autenticação. A entrada só identifica o aluno pela
  matrícula (procurando em `GET /usuarios`) e guarda o usuário no navegador.
- **Área da equipe aberta:** o `Usuario` não tem perfil de acesso, então qualquer um entra em `/equipe`.
- Cadastros de materiais, autores, editoras e categorias continuam pelo Swagger.

## Estrutura

```
src/
├── api/          # tipos das respostas (tipos.ts) e uma função por rota (cliente.ts)
├── componentes/  # logo, cabeçalhos com abas, capa, barra de progresso, mensagens
├── hooks/        # useCarregar: chama a API e guarda dados, erro e "carregando"
├── paginas/      # uma tela por arquivo (equipe/ para o painel da biblioteca)
├── sessao/       # aluno identificado e perfil de leitor, compartilhados entre as telas
└── util/         # datas, notas, rótulos e níveis
```
