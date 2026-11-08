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
| `/` | Login com matrícula e senha (o bibliotecário vai direto para o painel) | `POST /auth/login` |
| `/inicio` | Continue lendo, nível e recomendações | `/usuarios/{id}/leituras`, `/perfil-leitor`, `/recomendacoes` |
| `/acervo` | Busca com filtros, ordenação e paginação | `/materiais/busca`, `/categorias` |
| `/materiais/:id` | Material, empréstimo e avaliações | `/materiais/{id}`, `POST /emprestimos`, `/avaliacoes` |
| `/leituras` | Progresso, renovar, devolver e histórico | `/emprestimos/{id}/progresso`, `PUT /emprestimos/{id}`, `PATCH /emprestimos/{id}/devolver` |
| `/perfil` | Pontos, nível, conquistas e extrato | `/usuarios/{id}/perfil-leitor` |
| `/ranking` | Ranking de leitores | `/ranking` |
| `/equipe` | Empréstimos de todos os alunos, novo empréstimo, atrasados | `/emprestimos`, `/materiais`, `/emprestimos/atualizar-atrasados` |
| `/equipe/acervo` | Licenças e avaliações de cada material | `/materiais` |

## Login e perfis

- O login (`POST /auth/login`) devolve um token JWT, que o front guarda no navegador e envia em todas as chamadas
  (`Authorization: Bearer ...`). Se a API responder 401 (token vencido), o front volta para a tela de entrada.
- **Aluno:** a área do leitor. **Professor:** a área do leitor e o painel da equipe, só para consultar.
  **Bibliotecário:** entra direto no painel e registra, renova e devolve empréstimos de qualquer aluno.
- Esconder botões no front é só conforto: quem garante as permissões é a API (`SegurancaConfig`).

## Limites conhecidos

- Cadastros de materiais, autores, editoras, categorias e usuários continuam pelo Swagger.

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
