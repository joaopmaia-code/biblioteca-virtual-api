import { useState, type ReactNode } from 'react'
import { Link } from 'react-router-dom'
import { mensagemDeErro } from '../api/cliente'
import type { StatusEmprestimo } from '../api/tipos'
import { corDaCapa, ROTULO_STATUS } from '../util/formato'

interface PropsCapa {
  id: number
  titulo: string
  /** Imagem de capa; sem ela (ou se não carregar, ex.: sem internet), mostra um bloco colorido com o título. */
  urlCapa?: string | null
  rotulo?: string
  grande?: boolean
}

/** Capa do material. É decorativa (o título já aparece ao lado), por isso fica escondida dos leitores de tela. */
export function Capa({ id, titulo, urlCapa, rotulo, grande = false }: PropsCapa) {
  const [falhou, setFalhou] = useState(false)
  const temImagem = Boolean(urlCapa) && !falhou

  return (
    <div className={grande ? 'capa capa-grande' : 'capa'} style={{ background: corDaCapa(id) }} aria-hidden="true">
      {temImagem && (
        <img className="capa-imagem" src={urlCapa!} alt="" loading="lazy" onError={() => setFalhou(true)} />
      )}
      {rotulo && <span className="capa-rotulo">{rotulo}</span>}
      {!temImagem && <span className="capa-titulo">{titulo}</span>}
    </div>
  )
}

export function BarraProgresso({ valor, cor = 'azul', rotulo }: { valor: number; cor?: 'azul' | 'ambar' | 'cinza'; rotulo: string }) {
  const limitado = Math.max(0, Math.min(100, valor))
  return (
    <div
      className={`barra barra-${cor}`}
      role="progressbar"
      aria-label={rotulo}
      aria-valuemin={0}
      aria-valuemax={100}
      aria-valuenow={Math.round(limitado)}
    >
      <div className="barra-preenchida" style={{ width: `${limitado}%` }} />
    </div>
  )
}

export function SeloStatus({ status, prazo }: { status: StatusEmprestimo; prazo?: string }) {
  const classe = status === 'ATIVO' ? 'selo-ok' : status === 'ATRASADO' ? 'selo-perigo' : 'selo-neutro'
  return (
    <span className={`selo ${classe}`}>
      {ROTULO_STATUS[status]}
      {prazo ? ` · ${prazo}` : ''}
    </span>
  )
}

export function Carregando({ texto = 'Carregando…' }: { texto?: string }) {
  return (
    <p className="estado" role="status">
      {texto}
    </p>
  )
}

export function MensagemErro({ erro, onTentarDeNovo }: { erro: unknown; onTentarDeNovo?: () => void }) {
  return (
    <div className="aviso aviso-perigo" role="alert">
      <span>{mensagemDeErro(erro)}</span>
      {onTentarDeNovo && (
        <button type="button" className="botao botao-secundario botao-pequeno" onClick={onTentarDeNovo}>
          Tentar de novo
        </button>
      )}
    </div>
  )
}

export function Vazio({ children }: { children: ReactNode }) {
  return <p className="estado estado-vazio">{children}</p>
}

/** Mensagem de resultado de uma ação (sucesso ou erro), anunciada para leitores de tela. */
export function Retorno({ tipo, children }: { tipo: 'ok' | 'erro'; children: ReactNode }) {
  return (
    <p className={tipo === 'ok' ? 'aviso aviso-ok' : 'aviso aviso-perigo'} role={tipo === 'ok' ? 'status' : 'alert'}>
      {children}
    </p>
  )
}

export function Estrela({ tamanho = 16 }: { tamanho?: number }) {
  return (
    <svg width={tamanho} height={tamanho} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" aria-hidden="true">
      <path d="M12 3l2.6 5.6 6 .7-4.5 4.1 1.2 6L12 16.4 6.7 19.4l1.2-6L3.4 9.3l6-.7z" />
    </svg>
  )
}

export function NaoEncontrada() {
  return (
    <main className="conteudo">
      <h1 className="titulo-pagina">Página não encontrada</h1>
      <p>
        <Link to="/">Voltar para a entrada</Link>
      </p>
    </main>
  )
}
