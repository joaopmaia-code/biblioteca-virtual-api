import { useCallback, useEffect, useMemo, useState, type ReactNode } from 'react'
import { api } from '../api/cliente'
import type { PerfilLeitor, Usuario } from '../api/tipos'
import { SessaoContexto, type Sessao } from './contexto'

const CHAVE = 'biblioteca-virtual:usuario'

/**
 * A API não tem login: o aluno só se identifica pela matrícula. Guardamos o usuário no
 * navegador para ele continuar "entrado" ao recarregar a página.
 */
function lerUsuarioSalvo(): Usuario | null {
  try {
    const texto = localStorage.getItem(CHAVE)
    return texto ? (JSON.parse(texto) as Usuario) : null
  } catch {
    return null
  }
}

function salvarUsuario(usuario: Usuario | null) {
  try {
    if (usuario) localStorage.setItem(CHAVE, JSON.stringify(usuario))
    else localStorage.removeItem(CHAVE)
  } catch {
    // Navegador sem armazenamento (ex.: aba anônima bloqueada): a sessão vale só até recarregar.
  }
}

export function SessaoProvider({ children }: { children: ReactNode }) {
  const [usuario, setUsuario] = useState<Usuario | null>(lerUsuarioSalvo)
  const [perfil, setPerfil] = useState<PerfilLeitor | null>(null)
  const [versaoPerfil, setVersaoPerfil] = useState(0)

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
        // Usuário salvo que não existe mais na API: volta para a entrada.
        if (ativo) {
          salvarUsuario(null)
          setUsuario(null)
          setPerfil(null)
        }
      })
    return () => {
      ativo = false
    }
  }, [usuario, versaoPerfil])

  // O perfil é zerado aqui (e não no efeito) para não mostrar os pontos de outro aluno.
  const entrar = useCallback((novo: Usuario) => {
    salvarUsuario(novo)
    setPerfil(null)
    setUsuario(novo)
  }, [])

  const sair = useCallback(() => {
    salvarUsuario(null)
    setPerfil(null)
    setUsuario(null)
  }, [])

  const recarregarPerfil = useCallback(() => setVersaoPerfil((v) => v + 1), [])

  const sessao = useMemo<Sessao>(
    () => ({ usuario, perfil, entrar, sair, recarregarPerfil }),
    [usuario, perfil, entrar, sair, recarregarPerfil],
  )

  return <SessaoContexto.Provider value={sessao}>{children}</SessaoContexto.Provider>
}
