import { createContext, useContext } from 'react'
import type { PerfilLeitor, Usuario } from '../api/tipos'

export interface Sessao {
  /** Usuário que entrou com matrícula e senha (null antes de entrar). */
  usuario: Usuario | null
  /** Pontos, nível e conquistas do usuário (null enquanto carrega). */
  perfil: PerfilLeitor | null
  /** Professor ou bibliotecário: pode abrir o painel da equipe. */
  ehEquipe: boolean
  /** Só o bibliotecário registra, renova e devolve empréstimos de outros alunos. */
  ehBibliotecario: boolean
  /** Guarda o token e o usuário devolvidos por POST /auth/login. */
  entrar: (token: string, usuario: Usuario) => void
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
