import { createContext, useContext } from 'react'
import type { PerfilLeitor, Usuario } from '../api/tipos'

export interface Sessao {
  /** Aluno que entrou pela matrícula (null antes de entrar). */
  usuario: Usuario | null
  /** Pontos, nível e conquistas do aluno (null enquanto carrega). */
  perfil: PerfilLeitor | null
  entrar: (usuario: Usuario) => void
  sair: () => void
  /** Busca o perfil de novo depois de uma ação que muda os pontos (devolver, avaliar, ler). */
  recarregarPerfil: () => void
}

export const SessaoContexto = createContext<Sessao | null>(null)

export function useSessao(): Sessao {
  const sessao = useContext(SessaoContexto)
  if (!sessao) {
    throw new Error('useSessao precisa estar dentro do SessaoProvider')
  }
  return sessao
}
