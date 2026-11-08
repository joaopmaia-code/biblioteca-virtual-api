import { useCallback, useEffect, useState } from 'react'

interface Estado<T> {
  dados: T | undefined
  erro: unknown
  carregando: boolean
}

/**
 * Chama a API quando a tela abre (e quando as dependências mudam) e guarda o resultado.
 * `recarregar()` busca de novo, por exemplo depois de uma ação que muda os dados.
 * Enquanto recarrega, os dados anteriores continuam na tela (sem piscar).
 */
export function useCarregar<T>(carregar: () => Promise<T>, dependencias: unknown[]) {
  const [estado, setEstado] = useState<Estado<T>>({ dados: undefined, erro: undefined, carregando: true })
  const [versao, setVersao] = useState(0)

  useEffect(() => {
    let ativo = true
    setEstado((anterior) => ({ ...anterior, carregando: true, erro: undefined }))
    carregar()
      .then((dados) => {
        if (ativo) setEstado({ dados, erro: undefined, carregando: false })
      })
      .catch((erro: unknown) => {
        if (ativo) setEstado((anterior) => ({ ...anterior, erro, carregando: false }))
      })
    return () => {
      ativo = false
    }
    // As dependências vêm de quem chama; `carregar` muda a cada render e não deve disparar nova busca.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [...dependencias, versao])

  const recarregar = useCallback(() => setVersao((v) => v + 1), [])
  return { ...estado, recarregar }
}
