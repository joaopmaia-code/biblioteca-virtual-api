import type {
  Avaliacao,
  CampoErro,
  Categoria,
  Emprestimo,
  FiltroBusca,
  Material,
  Pagina,
  PerfilLeitor,
  PosicaoRanking,
  ProgressoLeitura,
  Recomendacao,
  RespostaDeErro,
  RespostaLogin,
  StatusEmprestimo,
  Usuario,
} from './tipos'

/** Em desenvolvimento o Vite repassa /api para o Spring (vite.config.ts). */
const BASE = import.meta.env.VITE_API_BASE ?? '/api'

/** Token JWT do login, enviado em todas as chamadas (Authorization: Bearer ...). Definido pela sessão. */
let token: string | null = null
/** Chamado quando a API responde 401 com um token (expirou ou é inválido): a sessão faz o logout. */
let aoExpirar: (() => void) | null = null

export function definirToken(novo: string | null) {
  token = novo
}

export function definirAoExpirar(acao: (() => void) | null) {
  aoExpirar = acao
}

/** Erro devolvido pela API, já com a mensagem do ErroResponseDTO. */
export class ErroDaApi extends Error {
  readonly status: number
  readonly campos: CampoErro[]

  constructor(status: number, mensagem: string, campos: CampoErro[] = []) {
    super(mensagem)
    this.name = 'ErroDaApi'
    this.status = status
    this.campos = campos
  }
}

const SEM_CONEXAO = 'Não foi possível falar com a API. Confira se o backend está rodando (./mvnw spring-boot:run).'

type ValorDeQuery = string | number | boolean | undefined | null

/** Monta "?a=1&b=2" ignorando os valores vazios. */
function query(parametros: Record<string, ValorDeQuery>): string {
  const busca = new URLSearchParams()
  for (const [chave, valor] of Object.entries(parametros)) {
    if (valor !== undefined && valor !== null && valor !== '') {
      busca.set(chave, String(valor))
    }
  }
  const texto = busca.toString()
  return texto ? `?${texto}` : ''
}

async function requisitar<T>(metodo: string, caminho: string, corpo?: unknown): Promise<T> {
  const cabecalhos: Record<string, string> = {}
  if (corpo !== undefined) cabecalhos['Content-Type'] = 'application/json'
  if (token) cabecalhos.Authorization = `Bearer ${token}`

  let resposta: Response
  try {
    resposta = await fetch(BASE + caminho, {
      method: metodo,
      headers: cabecalhos,
      body: corpo === undefined ? undefined : JSON.stringify(corpo),
    })
  } catch {
    throw new ErroDaApi(0, SEM_CONEXAO)
  }

  // Token vencido ou inválido: encerra a sessão (o login em si não entra aqui, pois vai sem token).
  if (resposta.status === 401 && token) {
    aoExpirar?.()
  }

  if (resposta.status === 204) {
    return undefined as T
  }

  const texto = await resposta.text()
  let dados: unknown
  try {
    dados = texto ? JSON.parse(texto) : undefined
  } catch {
    // Resposta que não é JSON: normalmente o proxy do Vite sem o backend no ar.
    throw new ErroDaApi(resposta.status, resposta.ok ? 'Resposta inválida da API.' : SEM_CONEXAO)
  }

  if (!resposta.ok) {
    const erro = dados as Partial<RespostaDeErro> | undefined
    if (!erro?.mensagem && resposta.status >= 502) {
      // 502/503/504 sem corpo: o proxy do Vite não alcançou o backend.
      throw new ErroDaApi(resposta.status, SEM_CONEXAO)
    }
    throw new ErroDaApi(resposta.status, erro?.mensagem ?? `Erro ${resposta.status} na API.`, erro?.campos ?? [])
  }
  return dados as T
}

/** Texto para mostrar ao usuário a partir de qualquer erro. */
export function mensagemDeErro(erro: unknown): string {
  if (erro instanceof ErroDaApi) {
    const detalhes = erro.campos.map((campo) => campo.mensagem).join('; ')
    return detalhes ? `${erro.message}: ${detalhes}` : erro.message
  }
  return 'Algo deu errado. Tente de novo.'
}

/** Cada função corresponde a uma rota da API. */
export const api = {
  login: (matricula: string, senha: string) => requisitar<RespostaLogin>('POST', '/auth/login', { matricula, senha }),
  eu: () => requisitar<Usuario>('GET', '/auth/eu'),
  trocarSenha: (senhaAtual: string, novaSenha: string) =>
    requisitar<void>('PUT', '/auth/senha', { senhaAtual, novaSenha }),

  usuarios: () => requisitar<Usuario[]>('GET', '/usuarios'),
  categorias: () => requisitar<Categoria[]>('GET', '/categorias'),

  materiais: () => requisitar<Material[]>('GET', '/materiais'),
  material: (id: number) => requisitar<Material>('GET', `/materiais/${id}`),
  buscarMateriais: (filtro: FiltroBusca) =>
    requisitar<Pagina<Material>>('GET', `/materiais/busca${query({ ...filtro })}`),

  emprestimos: (filtro: { status?: StatusEmprestimo; usuarioId?: number } = {}) =>
    requisitar<Emprestimo[]>('GET', `/emprestimos${query(filtro)}`),
  pegarEmprestado: (usuarioId: number, materialId: number) =>
    requisitar<Emprestimo>('POST', '/emprestimos', { usuarioId, materialId }),
  renovar: (id: number, dataPrevistaDevolucao: string) =>
    requisitar<Emprestimo>('PUT', `/emprestimos/${id}`, { dataPrevistaDevolucao }),
  devolver: (id: number) => requisitar<Emprestimo>('PATCH', `/emprestimos/${id}/devolver`),
  atualizarAtrasados: () => requisitar<{ atualizados: number }>('PATCH', '/emprestimos/atualizar-atrasados'),

  progresso: (emprestimoId: number) => requisitar<ProgressoLeitura>('GET', `/emprestimos/${emprestimoId}/progresso`),
  registrarProgresso: (emprestimoId: number, paginaAtual: number) =>
    requisitar<ProgressoLeitura>('PUT', `/emprestimos/${emprestimoId}/progresso`, { paginaAtual }),
  leituras: (usuarioId: number) => requisitar<ProgressoLeitura[]>('GET', `/usuarios/${usuarioId}/leituras`),

  recomendacoes: (usuarioId: number) => requisitar<Recomendacao[]>('GET', `/usuarios/${usuarioId}/recomendacoes`),

  avaliacoes: (filtro: { materialId?: number; usuarioId?: number }) =>
    requisitar<Avaliacao[]>('GET', `/avaliacoes${query(filtro)}`),
  avaliar: (usuarioId: number, materialId: number, nota: number, comentario: string) =>
    requisitar<Avaliacao>('POST', '/avaliacoes', { usuarioId, materialId, nota, comentario }),
  editarAvaliacao: (id: number, nota: number, comentario: string) =>
    requisitar<Avaliacao>('PUT', `/avaliacoes/${id}`, { nota, comentario }),

  perfil: (usuarioId: number) => requisitar<PerfilLeitor>('GET', `/usuarios/${usuarioId}/perfil-leitor`),
  ranking: (limite: number) => requisitar<PosicaoRanking[]>('GET', `/ranking${query({ limite })}`),
}
