import { useState, type FormEvent } from 'react'
import { Link } from 'react-router-dom'
import { api, mensagemDeErro } from '../api/cliente'
import type { ProgressoLeitura } from '../api/tipos'
import { BarraProgresso, Capa, Carregando, MensagemErro, Retorno, SeloStatus, Vazio } from '../componentes/Basicos'
import { useCarregar } from '../hooks/useCarregar'
import { useSessao } from '../sessao/contexto'
import { dataCurta, devolvidoNoPrazo, hojeMais, nota } from '../util/formato'

export function Leituras() {
  const { usuario, recarregarPerfil } = useSessao()
  const usuarioId = usuario!.id

  const leituras = useCarregar(() => api.leituras(usuarioId), [usuarioId])
  const historico = useCarregar(async () => {
    const devolvidos = await api.emprestimos({ usuarioId, status: 'DEVOLVIDO' })
    const progressos = await Promise.all(devolvidos.map((e) => api.progresso(e.id)))
    return devolvidos
      .map((emprestimo, i) => ({ emprestimo, progresso: progressos[i] }))
      .sort((a, b) => (b.emprestimo.dataDevolucao ?? '').localeCompare(a.emprestimo.dataDevolucao ?? ''))
  }, [usuarioId])

  /** Depois de salvar, renovar ou devolver: atualiza a lista, o histórico e os pontos. */
  function atualizarTudo() {
    leituras.recarregar()
    historico.recarregar()
    recarregarPerfil()
  }

  return (
    <main className="conteudo">
      <section>
        <h1 className="titulo-pagina">Minhas leituras</h1>
        <p className="texto-suave">Ler até a última página vale 10 pontos; devolver no prazo vale 3.</p>
      </section>

      <section className="pilha" aria-labelledby="andamento">
        <h2 id="andamento" className="titulo-secao">
          Em andamento
        </h2>
        {leituras.erro !== undefined && <MensagemErro erro={leituras.erro} onTentarDeNovo={leituras.recarregar} />}
        {leituras.carregando && !leituras.dados && <Carregando />}
        {leituras.dados?.length === 0 && (
          <Vazio>
            Nenhum material com você agora. <Link to="/acervo">Escolha um no acervo</Link>.
          </Vazio>
        )}
        {leituras.dados?.map((leitura) => (
          <CartaoLeitura key={leitura.emprestimoId} leitura={leitura} onMudou={atualizarTudo} />
        ))}
      </section>

      <section className="pilha" aria-labelledby="historico">
        <h2 id="historico" className="titulo-secao">
          Histórico
        </h2>
        {historico.erro !== undefined && <MensagemErro erro={historico.erro} onTentarDeNovo={historico.recarregar} />}
        {historico.carregando && !historico.dados && <Carregando />}
        {historico.dados?.length === 0 && <Vazio>Você ainda não devolveu nenhum material.</Vazio>}
        {historico.dados && historico.dados.length > 0 && (
          <div className="tabela-rolavel cartao-sem-padding">
            <table className="tabela">
              <thead>
                <tr>
                  <th scope="col">Material</th>
                  <th scope="col">Devolvido em</th>
                  <th scope="col">Prazo</th>
                  <th scope="col">Leitura</th>
                </tr>
              </thead>
              <tbody>
                {historico.dados.map(({ emprestimo, progresso }) => (
                  <tr key={emprestimo.id}>
                    <td>
                      <Link to={`/materiais/${emprestimo.material.id}`} className="link-forte">
                        {emprestimo.material.nome}
                      </Link>
                    </td>
                    <td>{dataCurta(emprestimo.dataDevolucao)}</td>
                    <td>
                      {devolvidoNoPrazo(emprestimo.dataDevolucao, emprestimo.dataPrevistaDevolucao) ? (
                        <span className="texto-ok">No prazo</span>
                      ) : (
                        <span className="texto-perigo">Com atraso</span>
                      )}
                    </td>
                    <td>
                      {progresso.concluido
                        ? 'Concluída'
                        : progresso.percentual !== null && progresso.paginaAtual > 0
                          ? `${nota(progresso.percentual)}%`
                          : '—'}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </main>
  )
}

function CartaoLeitura({ leitura, onMudou }: { leitura: ProgressoLeitura; onMudou: () => void }) {
  const [pagina, setPagina] = useState(leitura.paginaAtual > 0 ? String(leitura.paginaAtual) : '')
  const [ocupado, setOcupado] = useState(false)
  const [retorno, setRetorno] = useState<{ tipo: 'ok' | 'erro'; texto: string } | null>(null)
  const atrasado = leitura.statusEmprestimo === 'ATRASADO'
  const temPaginas = leitura.numeroPaginas !== null

  // Mostra na barra o que o aluno está digitando, antes mesmo de salvar.
  const paginaDigitada = Math.min(Number(pagina) || 0, leitura.numeroPaginas ?? 0)
  const percentual = temPaginas ? Math.round((paginaDigitada * 1000) / leitura.numeroPaginas!) / 10 : 0

  async function executar(acao: () => Promise<unknown>, sucesso: string) {
    setOcupado(true)
    setRetorno(null)
    try {
      await acao()
      setRetorno({ tipo: 'ok', texto: sucesso })
      onMudou()
    } catch (erro) {
      setRetorno({ tipo: 'erro', texto: mensagemDeErro(erro) })
    } finally {
      setOcupado(false)
    }
  }

  function salvar(evento: FormEvent) {
    evento.preventDefault()
    const numero = Number(pagina)
    if (!Number.isInteger(numero) || numero < 1) {
      setRetorno({ tipo: 'erro', texto: 'Informe uma página de 1 em diante.' })
      return
    }
    const terminou = numero === leitura.numeroPaginas
    executar(
      () => api.registrarProgresso(leitura.emprestimoId, numero),
      terminou ? 'Leitura concluída! Confira seus pontos no perfil de leitor.' : 'Progresso salvo.',
    )
  }

  function devolver() {
    if (!window.confirm(`Devolver "${leitura.material.nome}"?`)) return
    executar(() => api.devolver(leitura.emprestimoId), atrasado ? 'Material devolvido.' : 'Material devolvido no prazo: +3 pontos.')
  }

  return (
    <article className="cartao cartao-leitura">
      <Capa id={leitura.material.id} titulo={leitura.material.nome} urlCapa={leitura.urlCapa} />
      <div className="pilha crescer">
        <div className="linha-entre">
          <Link to={`/materiais/${leitura.material.id}`} className="titulo-cartao titulo-cartao-grande">
            {leitura.material.nome}
          </Link>
          <SeloStatus
            status={leitura.statusEmprestimo}
            prazo={atrasado ? `venceu ${dataCurta(leitura.dataPrevistaDevolucao)}` : `devolver até ${dataCurta(leitura.dataPrevistaDevolucao)}`}
          />
        </div>

        {atrasado && (
          <p className="aviso aviso-perigo">O prazo venceu. Renove ou devolva para liberar a licença; devoluções atrasadas não rendem pontos.</p>
        )}

        {temPaginas ? (
          <form onSubmit={salvar} className="pilha">
            <div className="pilha pilha-justa">
              <BarraProgresso valor={percentual} rotulo={`Progresso de ${leitura.material.nome}`} />
              <div className="linha-entre texto-pequeno">
                <span>
                  Página {paginaDigitada} de {leitura.numeroPaginas} · {nota(percentual)}%
                </span>
                {leitura.concluido && <strong className="texto-ok">Leitura concluída</strong>}
              </div>
            </div>
            <div className="linha linha-quebra alinhar-base">
              <div className="campo">
                <label htmlFor={`pagina-${leitura.emprestimoId}`}>Página atual</label>
                <input
                  id={`pagina-${leitura.emprestimoId}`}
                  type="number"
                  inputMode="numeric"
                  min={1}
                  max={leitura.numeroPaginas ?? undefined}
                  className="campo-pagina"
                  value={pagina}
                  onChange={(e) => setPagina(e.target.value)}
                />
              </div>
              <button type="submit" className="botao botao-primario" disabled={ocupado}>
                Salvar progresso
              </button>
              <span className="crescer" />
              <AcoesEmprestimo ocupado={ocupado} onRenovar={() => executar(() => api.renovar(leitura.emprestimoId, hojeMais(14)), `Prazo renovado até ${dataCurta(hojeMais(14))}.`)} onDevolver={devolver} />
            </div>
          </form>
        ) : (
          <div className="linha linha-quebra">
            <span className="texto-suave texto-pequeno crescer">Material sem contagem de páginas (ex.: vídeo).</span>
            <AcoesEmprestimo ocupado={ocupado} onRenovar={() => executar(() => api.renovar(leitura.emprestimoId, hojeMais(14)), `Prazo renovado até ${dataCurta(hojeMais(14))}.`)} onDevolver={devolver} />
          </div>
        )}
        {retorno && <Retorno tipo={retorno.tipo}>{retorno.texto}</Retorno>}
      </div>
    </article>
  )
}

function AcoesEmprestimo({ ocupado, onRenovar, onDevolver }: { ocupado: boolean; onRenovar: () => void; onDevolver: () => void }) {
  return (
    <div className="linha">
      <button type="button" className="botao botao-secundario" onClick={onRenovar} disabled={ocupado}>
        Renovar prazo
      </button>
      <button type="button" className="botao botao-secundario" onClick={onDevolver} disabled={ocupado}>
        Devolver
      </button>
    </div>
  )
}
