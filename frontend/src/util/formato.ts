import type { NivelLeitor, SerieRecomendada, StatusEmprestimo, TipoMaterial } from '../api/tipos'

/** "2026-11-22" ou "2026-11-22T10:30:00" → "22/11". */
export function dataCurta(data: string | null | undefined): string {
  if (!data) return '—'
  const [ano, mes, dia] = data.slice(0, 10).split('-')
  return ano && mes && dia ? `${dia}/${mes}` : data
}

/** "2026-11-22" → "22/11/2026". */
export function dataCompleta(data: string | null | undefined): string {
  if (!data) return '—'
  const [ano, mes, dia] = data.slice(0, 10).split('-')
  return ano && mes && dia ? `${dia}/${mes}/${ano}` : data
}

/** Data de hoje + N dias no formato da API (yyyy-MM-dd), no fuso local. */
export function hojeMais(dias: number): string {
  const data = new Date()
  data.setDate(data.getDate() + dias)
  const mes = String(data.getMonth() + 1).padStart(2, '0')
  const dia = String(data.getDate()).padStart(2, '0')
  return `${data.getFullYear()}-${mes}-${dia}`
}

/** Devolvido até o dia previsto (mesma regra da gamificação). */
export function devolvidoNoPrazo(dataDevolucao: string | null, dataPrevista: string): boolean {
  return dataDevolucao !== null && dataDevolucao.slice(0, 10) <= dataPrevista
}

/** 4.75 → "4,8". */
export function nota(valor: number | null): string {
  return valor === null ? '—' : valor.toLocaleString('pt-BR', { minimumFractionDigits: 1, maximumFractionDigits: 1 })
}

/** "Ana Beatriz Souza" → "AS". */
export function iniciais(nome: string): string {
  const partes = nome.trim().split(/\s+/)
  const primeira = partes[0]?.[0] ?? ''
  const ultima = partes.length > 1 ? partes[partes.length - 1][0] : ''
  return (primeira + ultima).toUpperCase()
}

export function primeiroNome(nome: string): string {
  return nome.trim().split(/\s+/)[0] ?? nome
}

export function plural(quantidade: number, singular: string, pluralTexto: string): string {
  return `${quantidade} ${quantidade === 1 ? singular : pluralTexto}`
}

/** Cor da capa gerada para cada material (não há imagens no acervo). Todas com texto branco legível. */
const CORES_CAPA = ['#2346D8', '#0F766E', '#9A3412', '#6D28D9', '#1E3A8A', '#7C2D12', '#374151', '#BE185D']

export function corDaCapa(id: number): string {
  return CORES_CAPA[Math.abs(id) % CORES_CAPA.length]
}

export const ROTULO_TIPO: Record<TipoMaterial, string> = {
  LIVRO: 'Livro',
  APOSTILA: 'Apostila',
  ARTIGO: 'Artigo',
  VIDEO: 'Vídeo',
}

export const ROTULO_SERIE: Record<SerieRecomendada, string> = {
  PRIMEIRO_ANO: '1º ano',
  SEGUNDO_ANO: '2º ano',
  TERCEIRO_ANO: '3º ano',
}

export const ROTULO_STATUS: Record<StatusEmprestimo, string> = {
  ATIVO: 'Ativo',
  ATRASADO: 'Atrasado',
  DEVOLVIDO: 'Devolvido',
}

/** Mesmos níveis e mínimos do enum NivelLeitor do backend. */
export const NIVEIS: { codigo: NivelLeitor; nome: string; curto: string; minimo: number }[] = [
  { codigo: 'LEITOR_INICIANTE', nome: 'Leitor Iniciante', curto: 'Iniciante', minimo: 0 },
  { codigo: 'LEITOR', nome: 'Leitor', curto: 'Leitor', minimo: 50 },
  { codigo: 'LEITOR_VORAZ', nome: 'Leitor Voraz', curto: 'Voraz', minimo: 150 },
  { codigo: 'DEVORADOR_DE_LIVROS', nome: 'Devorador de Livros', curto: 'Devorador', minimo: 300 },
  { codigo: 'LENDA_DA_BIBLIOTECA', nome: 'Lenda da Biblioteca', curto: 'Lenda', minimo: 500 },
]

export function nomeDoNivel(codigo: NivelLeitor): string {
  return NIVEIS.find((nivel) => nivel.codigo === codigo)?.nome ?? codigo
}

/** Quanto do nível atual já foi percorrido (0 a 100), para a barra até o próximo nível. */
export function percentualNoNivel(pontos: number, codigo: NivelLeitor): number {
  const indice = NIVEIS.findIndex((nivel) => nivel.codigo === codigo)
  const atual = NIVEIS[indice]
  const proximo = NIVEIS[indice + 1]
  if (!atual || !proximo) return 100
  return Math.round(((pontos - atual.minimo) * 100) / (proximo.minimo - atual.minimo))
}
