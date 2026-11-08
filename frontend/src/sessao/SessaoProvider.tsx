import { useCallback, useEffect, useMemo, useState, type ReactNode } from 'react'
import { api, definirAoExpirar, definirToken } from '../api/cliente'
import type { PerfilLeitor, Usuario } from '../api/tipos'
import { SessaoContexto, type Sessao } from './contexto'

const CHAVE = 'biblioteca-virtual:sessao'

interface SessaoSalva {
  token: string
  usuario: Usuario
}

/** O token do login fica no navegador para o usuário continuar dentro ao recarregar a página. */
function lerSessaoSalva(): SessaoSalva | null {
  try {
    const texto = localStorage.getItem(CHAVE)
    const salva = texto ? (JSON.parse(texto) as SessaoSalva) : null
    return salva?.token && salva.usuario?.perfil ? salva : null
  } catch {
    return null
  }
}

function salvarSessao(sessao: SessaoSalva | null) {
  try {
    if (sessao) localStorage.setItem(CHAVE, JSON.stringify(sessao))
    else localStorage.removeItem(CHAVE)
  } catch {
    // Navegador sem armazenamento (ex.: aba anônima bloqueada): a sessão vale só até recarregar.
  }
}

const sessaoInicial = lerSessaoSalva()
definirToken(sessaoInicial?.token ?? null)

export function SessaoProvider({ children }: { children: ReactNode }) {
  const [usuario, setUsuario] = useState<Usuario | null>(sessaoInicial?.usuario ?? null)
  const [perfil, setPerfil] = useState<PerfilLeitor | null>(null)
  const [versaoPerfil, setVersaoPerfil] = useState(0)

  // O perfil é zerado aqui (e não num efeito) para não mostrar os pontos de outra pessoa.
  const sair = useCallback(() => {
    salvarSessao(null)
    definirToken(null)
    setPerfil(null)
    setUsuario(null)
  }, [])

  const entrar = useCallback((token: string, novo: Usuario) => {
    salvarSessao({ token, usuario: novo })
    definirToken(token)
    setPerfil(null)
    setUsuario(novo)
  }, [])

  // Token vencido (a API respondeu 401): volta para a tela de entrada.
  useEffect(() => {
    definirAoExpirar(sair)
    return () => definirAoExpirar(null)
  }, [sair])

  useEffect(() => {
    if (!usuario) {
      return
    }
    let ativo = true
    api
      .perfil(usuario.id)
      .then((dados) => {
        if (ativo) setPerfil(dados)
      })
      .catch(() => {
        // Sem perfil de leitor (ex.: falha de rede): o cabeçalho só não mostra os pontos.
      })
    return () => {
      ativo = false
    }
  }, [usuario, versaoPerfil])

  const recarregarPerfil = useCallback(() => setVersaoPerfil((v) => v + 1), [])

  const sessao = useMemo<Sessao>(
    () => ({
      usuario,
      perfil,
      ehEquipe: usuario?.perfil === 'PROFESSOR' || usuario?.perfil === 'BIBLIOTECARIO',
      ehBibliotecario: usuario?.perfil === 'BIBLIOTECARIO',
      entrar,
      sair,
      recarregarPerfil,
    }),
    [usuario, perfil, entrar, sair, recarregarPerfil],
  )

  return <SessaoContexto.Provider value={sessao}>{children}</SessaoContexto.Provider>
}
