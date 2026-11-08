import { useState, type FormEvent } from 'react'
import { Link, useParams } from 'react-router-dom'
import { api, mensagemDeErro } from '../api/cliente'
import type { Avaliacao } from '../api/tipos'
import { BarraProgresso, Capa, Carregando, MensagemErro, Retorno, SeloStatus } from '../componentes/Basicos'
import { useCarregar } from '../hooks/useCarregar'
import { useSessao } from '../sessao/contexto'
import { dataCurta, nota, plural, ROTULO_TIPO } from '../util/formato'

export function MaterialDetalhe() {
  const materialId = Number(useParams().id)
  const { usuario, recarregarPerfil } = useSessao()
  const usuarioId = usuario!.id

  const material = useCarregar(() => api.material(materialId), [materialId])
  const avaliacoes = useCarregar(() => api.avaliacoes({ materialId }), [materialId])
  const meusEmprestimos = useCarregar(() => api.emprestimos({ usuarioId }), [usuarioId])

  const desteMaterial = meusEmprestimos.dados?.filter((e) => e.material.id === materialId) ?? []
  const emUso = desteMaterial.find((e) => e.status !== 'DEVOLVIDO')
  const jaPegou = desteMaterial.length > 0
  const minhaAvaliacao = avaliacoes.dados?.find((a) => a.usuario.id === usuarioId)

  const progresso = useCarregar(
    () => (emUso ? api.progresso(emUso.id) : Promise.resolve(null)),
    [emUso?.id],
  )

  const [acao, setAcao] = useState<{ tipo: 'ok' | 'erro'; texto: string } | null>(null)
  const [pegando, setPegando] = useState(false)

  async function pegarEmprestado() {
    setPegando(true)
    setAcao(null)
    try {
      const emprestimo = await api.pegarEmprestado(usuarioId, materialId)
      setAcao({ tipo: 'ok', texto: `Pronto! Devolva até ${dataCurta(emprestimo.dataPrevistaDevolucao)}.` })
      material.recarregar()
      meusEmprestimos.recarregar()
    } catch (erro) {
      setAcao({ tipo: 'erro', texto: mensagemDeErro(erro) })
    } finally {
      setPegando(false)
    }
  }

  function depoisDeAvaliar() {
    avaliacoes.recarregar()
    material.recarregar()
    recarregarPerfil()
  }

  if (material.erro !== undefined && !material.dados) {
    return (
      <main className="conteudo">
        <MensagemErro erro={material.erro} onTentarDeNovo={material.recarregar} />
        <Link to="/acervo">Voltar ao acervo</Link>
      </main>
    )
  }
  if (!material.dados) {
    return (
      <main className="conteudo">
        <Carregando />
      </main>
    )
  }

  const m = material.dados

  return (
    <main className="conteudo">
      <nav aria-label="Trilha" className="trilha">
        <Link to="/acervo">Acervo</Link>
        <span aria-hidden="true">/</span>
        <span>{m.categoria.nome}</span>
        <span aria-hidden="true">/</span>
        <span aria-current="page">{m.titulo}</span>
      </nav>

      <section className="colunas detalhe">
        <Capa
          id={m.id}
          titulo={m.titulo}
          urlCapa={m.urlCapa}
          rotulo={m.urlCapa ? undefined : m.autores.map((a) => a.nome).join(', ').toUpperCase()}
          grande
        />

        <div className="coluna-principal pilha">
          <div className="pilha pilha-justa">
            <span className="etiqueta">{ROTULO_TIPO[m.tipo].toUpperCase()}</span>
            <h1 className="titulo-detalhe">{m.titulo}</h1>
            <p className="subtitulo">{m.autores.map((a) => a.nome).join(', ')}</p>
          </div>
          <ul className="metadados">
            <li>{m.anoPublicacao}</li>
            {m.numeroPaginas !== null && <li>{m.numeroPaginas} páginas</li>}
            <li>{m.categoria.nome}</li>
            {m.editora && <li>{m.editora.nome}</li>}
          </ul>
          <p>
            <strong className="nota-grande">★ {nota(m.mediaAvaliacoes)}</strong>{' '}
            <span className="texto-suave">· {plural(m.totalAvaliacoes, 'avaliação', 'avaliações')}</span>
          </p>

          <div className="cartao pilha">
            {meusEmprestimos.carregando && !meusEmprestimos.dados ? (
              <Carregando />
            ) : emUso ? (
              <>
                <div className="linha-entre">
                  <strong>Este material está com você</strong>
                  <SeloStatus
                    status={emUso.status}
                    prazo={emUso.status === 'ATRASADO' ? `venceu ${dataCurta(emUso.dataPrevistaDevolucao)}` : `até ${dataCurta(emUso.dataPrevistaDevolucao)}`}
                  />
                </div>
                {progresso.dados && progresso.dados.numeroPaginas !== null && (
                  <>
                    <BarraProgresso valor={progresso.dados.percentual ?? 0} rotulo="Seu progresso de leitura" />
                    <span className="texto-pequeno">
                      Página {progresso.dados.paginaAtual} de {progresso.dados.numeroPaginas} · {nota(progresso.dados.percentual)}%
                    </span>
                  </>
                )}
                <div className="linha linha-quebra">
                  <a href={m.urlArquivo} target="_blank" rel="noreferrer" className="botao botao-primario">
                    Abrir material
                  </a>
                  <Link to="/leituras" className="botao botao-secundario">
                    Atualizar progresso
                  </Link>
                </div>
              </>
            ) : m.licencasDisponiveis > 0 ? (
              <>
                <strong className="texto-ok">
                  {m.licencasDisponiveis} de {m.quantidadeLicencas} licenças livres agora
                </strong>
                <span className="texto-pequeno">Prazo de 14 dias. Cada aluno pode ter até 3 empréstimos ao mesmo tempo.</span>
                <button type="button" className="botao botao-primario botao-grande alinhar-inicio" onClick={pegarEmprestado} disabled={pegando}>
                  {pegando ? 'Pegando…' : 'Pegar emprestado'}
                </button>
              </>
            ) : (
              <>
                <strong className="texto-perigo">Todas as {m.quantidadeLicencas} licenças estão em uso</strong>
                <span className="texto-pequeno">Assim que alguém devolver, o empréstimo volta a ficar disponível.</span>
              </>
            )}
            {acao && <Retorno tipo={acao.tipo}>{acao.texto}</Retorno>}
          </div>

          {m.sinopse && (
            <div className="pilha pilha-justa">
              <h2 className="titulo-secao-pequeno">Sinopse</h2>
              <p className="texto-corrido">{m.sinopse}</p>
            </div>
          )}
        </div>
      </section>

      <section className="pilha">
        <h2 className="titulo-secao">Avaliações</h2>
        <div className="colunas">
          <ResumoNotas avaliacoes={avaliacoes.dados ?? []} media={m.mediaAvaliacoes} />
          <div className="coluna-principal pilha">
            {jaPegou ? (
              <FormularioAvaliacao
                key={minhaAvaliacao?.id ?? 'nova'}
                usuarioId={usuarioId}
                materialId={materialId}
                existente={minhaAvaliacao}
                onSalvo={depoisDeAvaliar}
              />
            ) : (
              <p className="caixa-tracejada">Você poderá avaliar este material depois de pegá-lo emprestado.</p>
            )}
            {avaliacoes.erro !== undefined && <MensagemErro erro={avaliacoes.erro} onTentarDeNovo={avaliacoes.recarregar} />}
            {avaliacoes.dados?.length === 0 && <p className="texto-suave">Ninguém avaliou ainda.</p>}
            {avaliacoes.dados?.map((a) => (
              <article key={a.id} className="cartao pilha pilha-justa">
                <div className="linha-entre texto-pequeno">
                  <strong>{a.usuario.id === usuarioId ? 'Você' : a.usuario.nome}</strong>
                  <span className="texto-suave">
                    <span className="estrelas" aria-label={`Nota ${a.nota} de 5`}>
                      {'★'.repeat(a.nota)}
                      {'☆'.repeat(5 - a.nota)}
                    </span>{' '}
                    · {dataCurta(a.dataAvaliacao)}
                  </span>
                </div>
                {a.comentario && <p className="texto-corrido">{a.comentario}</p>}
              </article>
            ))}
          </div>
        </div>
      </section>
    </main>
  )
}

function ResumoNotas({ avaliacoes, media }: { avaliacoes: Avaliacao[]; media: number | null }) {
  const total = avaliacoes.length
  return (
    <div className="cartao coluna-lateral pilha">
      <div className="linha linha-base">
        <span className="numero-destaque">{nota(media)}</span>
        <span className="texto-suave texto-pequeno">de 5 · {plural(total, 'avaliação', 'avaliações')}</span>
      </div>
      {[5, 4, 3, 2, 1].map((estrelas) => {
        const quantidade = avaliacoes.filter((a) => a.nota === estrelas).length
        return (
          <div key={estrelas} className="linha texto-pequeno">
            <span className="largura-fixa">{estrelas} ★</span>
            <div className="crescer">
              <BarraProgresso valor={total ? (quantidade * 100) / total : 0} cor="ambar" rotulo={`${quantidade} avaliações com nota ${estrelas}`} />
            </div>
            <span className="largura-fixa direita">{quantidade}</span>
          </div>
        )
      })}
    </div>
  )
}

function FormularioAvaliacao({
  usuarioId,
  materialId,
  existente,
  onSalvo,
}: {
  usuarioId: number
  materialId: number
  existente: Avaliacao | undefined
  onSalvo: () => void
}) {
  const [notaEscolhida, setNotaEscolhida] = useState(existente?.nota ?? 0)
  const [comentario, setComentario] = useState(existente?.comentario ?? '')
  const [enviando, setEnviando] = useState(false)
  const [retorno, setRetorno] = useState<{ tipo: 'ok' | 'erro'; texto: string } | null>(null)

  async function enviar(evento: FormEvent) {
    evento.preventDefault()
    if (notaEscolhida < 1) {
      setRetorno({ tipo: 'erro', texto: 'Escolha uma nota de 1 a 5.' })
      return
    }
    setEnviando(true)
    setRetorno(null)
    try {
      if (existente) {
        await api.editarAvaliacao(existente.id, notaEscolhida, comentario)
        setRetorno({ tipo: 'ok', texto: 'Avaliação atualizada.' })
      } else {
        await api.avaliar(usuarioId, materialId, notaEscolhida, comentario)
        setRetorno({ tipo: 'ok', texto: comentario.trim() ? 'Obrigado! Você ganhou 3 pontos.' : 'Obrigado! Você ganhou 2 pontos.' })
      }
      onSalvo()
    } catch (erro) {
      setRetorno({ tipo: 'erro', texto: mensagemDeErro(erro) })
    } finally {
      setEnviando(false)
    }
  }

  return (
    <form onSubmit={enviar} className="cartao pilha">
      <h3 className="titulo-cartao">{existente ? 'Sua avaliação' : 'Avalie este material'}</h3>
      <fieldset className="grupo">
        <legend>Nota</legend>
        <div className="linha">
          {[1, 2, 3, 4, 5].map((valor) => (
            <button
              key={valor}
              type="button"
              className={valor <= notaEscolhida ? 'botao-estrela marcada' : 'botao-estrela'}
              aria-pressed={valor === notaEscolhida}
              aria-label={`Nota ${valor}`}
              onClick={() => setNotaEscolhida(valor)}
            >
              ★
            </button>
          ))}
        </div>
      </fieldset>
      <div className="campo">
        <label htmlFor="comentario">Comentário (opcional)</label>
        <textarea
          id="comentario"
          rows={3}
          maxLength={1000}
          placeholder="O que achou do material?"
          value={comentario}
          onChange={(e) => setComentario(e.target.value)}
        />
      </div>
      <div className="linha-entre">
        {!existente && <span className="etiqueta-pontos">+2 pontos pela avaliação, +1 com comentário</span>}
        <button type="submit" className="botao botao-primario" disabled={enviando}>
          {enviando ? 'Enviando…' : existente ? 'Salvar alterações' : 'Enviar avaliação'}
        </button>
      </div>
      {retorno && <Retorno tipo={retorno.tipo}>{retorno.texto}</Retorno>}
    </form>
  )
}
