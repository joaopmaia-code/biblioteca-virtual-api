// Tipos das respostas da API (espelham os ResponseDTOs do Spring).

export type TipoMaterial = 'LIVRO' | 'APOSTILA' | 'ARTIGO' | 'VIDEO'
export type SerieRecomendada = 'PRIMEIRO_ANO' | 'SEGUNDO_ANO' | 'TERCEIRO_ANO'
export type StatusEmprestimo = 'ATIVO' | 'DEVOLVIDO' | 'ATRASADO'
export type OrdenacaoMaterial = 'TITULO' | 'ANO' | 'AVALIACAO'
export type NivelLeitor =
  | 'LEITOR_INICIANTE'
  | 'LEITOR'
  | 'LEITOR_VORAZ'
  | 'DEVORADOR_DE_LIVROS'
  | 'LENDA_DA_BIBLIOTECA'

/** Referência curta a outro cadastro (ResumoDTO). */
export interface Resumo {
  id: number
  nome: string
}

export interface Material {
  id: number
  titulo: string
  tipo: TipoMaterial
  anoPublicacao: number
  sinopse: string | null
  urlArquivo: string
  urlCapa: string | null
  numeroPaginas: number | null
  quantidadeLicencas: number
  licencasDisponiveis: number
  categoria: Resumo
  editora: Resumo | null
  autores: Resumo[]
  mediaAvaliacoes: number | null
  totalAvaliacoes: number
}

export interface Pagina<T> {
  conteudo: T[]
  pagina: number
  tamanho: number
  totalElementos: number
  totalPaginas: number
}

export interface Categoria {
  id: number
  nome: string
  descricao: string | null
  areaConhecimento: string
  serieRecomendada: SerieRecomendada
}

export type PerfilUsuario = 'ALUNO' | 'PROFESSOR' | 'BIBLIOTECARIO'

export interface Usuario {
  id: number
  nome: string
  email: string
  matricula: string
  perfil: PerfilUsuario
}

/** Resposta de POST /auth/login. */
export interface RespostaLogin {
  token: string
  tipo: string
  expiraEmSegundos: number
  usuario: Usuario
}

export interface Emprestimo {
  id: number
  usuario: Resumo
  material: Resumo
  dataRetirada: string
  dataPrevistaDevolucao: string
  dataDevolucao: string | null
  status: StatusEmprestimo
}

export interface ProgressoLeitura {
  emprestimoId: number
  usuario: Resumo
  material: Resumo
  urlCapa: string | null
  statusEmprestimo: StatusEmprestimo
  dataPrevistaDevolucao: string
  paginaAtual: number
  numeroPaginas: number | null
  percentual: number | null
  concluido: boolean
  atualizadoEm: string | null
}

export interface Avaliacao {
  id: number
  usuario: Resumo
  material: Resumo
  nota: number
  comentario: string | null
  dataAvaliacao: string
}

export interface Recomendacao {
  material: Material
  pontuacao: number
  motivo: string
}

export interface ConquistaUsuario {
  codigo: string
  nome: string
  descricao: string
  meta: number
  progresso: number
  desbloqueada: boolean
}

export interface Pontuacao {
  motivo: string
  quantidade: number
  pontosCada: number
  total: number
}

export interface PerfilLeitor {
  usuario: Resumo
  pontos: number
  nivel: NivelLeitor
  nomeNivel: string
  proximoNivel: string | null
  pontosParaProximoNivel: number | null
  extrato: Pontuacao[]
  estatisticas: {
    leiturasConcluidas: number
    categoriasLidas: number
    classicosLidos: number
    devolucoesNoPrazo: number
    avaliacoes: number
    avaliacoesComComentario: number
  }
  conquistas: ConquistaUsuario[]
}

export interface PosicaoRanking {
  posicao: number
  usuario: Resumo
  pontos: number
  nivel: NivelLeitor
  conquistasDesbloqueadas: number
}

/** Formato padrão de erro do GlobalExceptionHandler. */
export interface CampoErro {
  campo: string
  mensagem: string
}

export interface RespostaDeErro {
  timestamp: string
  status: number
  erro: string
  mensagem: string
  campos?: CampoErro[]
}

/** Filtros de GET /materiais/busca (todos opcionais). */
export interface FiltroBusca {
  titulo?: string
  autorId?: number
  categoriaId?: number
  tipo?: TipoMaterial
  serie?: SerieRecomendada
  anoMin?: number
  anoMax?: number
  notaMinima?: number
  disponivel?: boolean
  ordenarPor?: OrdenacaoMaterial
  pagina?: number
  tamanho?: number
}
