import { useState, type FormEvent } from 'react'
import { api, mensagemDeErro } from '../../api/cliente'
import type { Emprestimo, StatusEmprestimo } from '../../api/tipos'
import { Carregando, MensagemErro, Retorno, SeloStatus, Vazio } from '../../componentes/Basicos'
import { useCarregar } from '../../hooks/useCarregar'
import { dataCurta, hojeMais, plural } from '../../util/formato'

type Filtro = 'TODOS' | StatusEmprestimo

const FILTROS: { id: Filtro; rotulo: string }[] = [
  { id: 'TODOS', rotulo: 'Todos' },
  { id: 'ATIVO', rotulo: 'Ativos' },
  { id: 'ATRASADO', rotulo: 'Atrasados' },
  { id: 'DEVOLVIDO', rotulo: 'Devolvidos' },
]

export function PainelEmprestimos() {
  const emprestimos = useCarregar(() => api.emprestimos(), [])
  const materiais = useCarregar(() => api.materiais(), [])
  const [filtro, setFiltro] = useState<Filtro>('TODOS')
  const [mostrarNovo, setMostrarNovo] = useState(false)
  const [retorno, setRetorno] = useState<{ tipo: 'ok' | 'erro'; texto: string } | null>(null)
  const [ocupado, setOcupado] = useState(false)

  const todos = emprestimos.dados ?? []
  const contar = (status: StatusEmprestimo) => todos.filter((e) => e.status === status).length
  const licencasLivres = materiais.dados?.reduce((soma, m) => soma + m.licencasDisponiveis, 0)
  const visiveis = [...todos].reverse().filter((e) => filtro === 'TODOS' || e.status === filtro)

  function atualizar() {
    emprestimos.recarregar()
    materiais.recarregar()
  }

  async function executar(acao: () => Promise<unknown>, sucesso: (resultado: unknown) => string) {
    setOcupado(true)
    setRetorno(null)
    try {
      const resultado = await acao()
      setRetorno({ tipo: 'ok', texto: sucesso(resultado) })
      atualizar()
    } catch (erro) {
      setRetorno({ tipo: 'erro', texto: mensagemDeErro(erro) })
    } finally {
      setOcupado(false)
    }
  }

  function devolver(e: Emprestimo) {
    if (!window.confirm(`Registrar a devolução de "${e.material.nome}" (${e.usuario.nome})?`)) return
    executar(() => api.devolver(e.id), () => `Devolução de "${e.material.nome}" registrada.`)
  }

  function renovar(e: Emprestimo) {
    executar(() => api.renovar(e.id, hojeMais(14)), () => `Prazo de "${e.material.nome}" renovado até ${dataCurta(hojeMais(14))}.`)
  }

  return (
    <main className="conteudo">
      <section className="linha-titulo">
        <div>
          <h1 className="titulo-pagina">Empréstimos</h1>
          <p className="texto-suave">Os atrasados também são marcados sozinhos a cada hora.</p>
        </div>
        <div className="linha linha-quebra">
          <button
            type="button"
            className="botao botao-secundario"
            disabled={ocupado}
            onClick={() =>
              executar(
                () => api.atualizarAtrasados(),
                (r) => {
                  const total = (r as { atualizados: number }).atualizados
                  return total === 0 ? 'Nenhum empréstimo novo em atraso.' : `${plural(total, 'empréstimo marcado', 'empréstimos marcados')} como atrasado.`
                },
              )
            }
          >
            Verificar atrasados agora
          </button>
          <button type="button" className="botao botao-primario" onClick={() => setMostrarNovo((v) => !v)} aria-expanded={mostrarNovo}>
            {mostrarNovo ? 'Fechar' : 'Novo empréstimo'}
          </button>
        </div>
      </section>

      {mostrarNovo && (
        <NovoEmprestimo
          onCriado={(texto) => {
            setRetorno({ tipo: 'ok', texto })
            setMostrarNovo(false)
            atualizar()
          }}
        />
      )}
      {retorno && <Retorno tipo={retorno.tipo}>{retorno.texto}</Retorno>}

      <section aria-label="Resumo" className="grade-numeros">
        <div className="cartao numero">
          <span>Ativos</span>
          <strong>{emprestimos.dados ? contar('ATIVO') : '…'}</strong>
        </div>
        <div className="cartao numero">
          <span>Atrasados</span>
          <strong className="texto-perigo">{emprestimos.dados ? contar('ATRASADO') : '…'}</strong>
        </div>
        <div className="cartao numero">
          <span>Devolvidos</span>
          <strong>{emprestimos.dados ? contar('DEVOLVIDO') : '…'}</strong>
        </div>
        <div className="cartao numero">
          <span>Licenças livres no acervo</span>
          <strong>{licencasLivres ?? '…'}</strong>
        </div>
      </section>

      <section className="pilha">
        <div className="linha linha-quebra" role="group" aria-label="Filtrar por status">
          {FILTROS.map((f) => (
            <button
              key={f.id}
              type="button"
              className={filtro === f.id ? 'pilula pilula-ativa' : 'pilula'}
              aria-pressed={filtro === f.id}
              onClick={() => setFiltro(f.id)}
            >
              {f.rotulo}
            </button>
          ))}
        </div>

        {emprestimos.erro !== undefined && <MensagemErro erro={emprestimos.erro} onTentarDeNovo={emprestimos.recarregar} />}
        {emprestimos.carregando && !emprestimos.dados && <Carregando />}
        {emprestimos.dados && visiveis.length === 0 && <Vazio>Nenhum empréstimo com esse status.</Vazio>}

        {visiveis.length > 0 && (
          <div className="tabela-rolavel cartao-sem-padding">
            <table className="tabela">
              <thead>
                <tr>
                  <th scope="col">#</th>
                  <th scope="col">Aluno</th>
                  <th scope="col">Material</th>
                  <th scope="col">Retirada</th>
                  <th scope="col">Prazo</th>
                  <th scope="col">Status</th>
                  <th scope="col">
                    <span className="so-leitor">Ações</span>
                  </th>
                </tr>
              </thead>
              <tbody>
                {visiveis.map((e) => (
                  <tr key={e.id}>
                    <td className="texto-suave">{e.id}</td>
                    <td>
                      <strong>{e.usuario.nome}</strong>
                    </td>
                    <td>{e.material.nome}</td>
                    <td>{dataCurta(e.dataRetirada)}</td>
                    <td>{dataCurta(e.dataPrevistaDevolucao)}</td>
                    <td>
                      <SeloStatus status={e.status} />
                    </td>
                    <td className="direita sem-quebra">
                      {e.status !== 'DEVOLVIDO' && (
                        <>
                          <button type="button" className="botao botao-secundario botao-pequeno" disabled={ocupado} onClick={() => renovar(e)}>
                            Renovar
                          </button>{' '}
                          <button type="button" className="botao botao-primario botao-pequeno" disabled={ocupado} onClick={() => devolver(e)}>
                            Devolver
                          </button>
                        </>
                      )}
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

function NovoEmprestimo({ onCriado }: { onCriado: (texto: string) => void }) {
  const usuarios = useCarregar(() => api.usuarios(), [])
  const materiais = useCarregar(() => api.materiais(), [])
  const [usuarioId, setUsuarioId] = useState('')
  const [materialId, setMaterialId] = useState('')
  const [erro, setErro] = useState<string | null>(null)
  const [enviando, setEnviando] = useState(false)

  async function registrar(evento: FormEvent) {
    evento.preventDefault()
    if (!usuarioId || !materialId) {
      setErro('Escolha o aluno e o material.')
      return
    }
    setEnviando(true)
    setErro(null)
    try {
      const criado = await api.pegarEmprestado(Number(usuarioId), Number(materialId))
      onCriado(`Empréstimo de "${criado.material.nome}" para ${criado.usuario.nome}, até ${dataCurta(criado.dataPrevistaDevolucao)}.`)
    } catch (falha) {
      setErro(mensagemDeErro(falha))
    } finally {
      setEnviando(false)
    }
  }

  return (
    <form onSubmit={registrar} className="cartao pilha">
      <h2 className="titulo-secao-pequeno">Novo empréstimo</h2>
      {(usuarios.erro !== undefined || materiais.erro !== undefined) && (
        <MensagemErro erro={usuarios.erro ?? materiais.erro} />
      )}
      <div className="linha linha-quebra alinhar-base">
        <div className="campo crescer">
          <label htmlFor="novo-aluno">Aluno</label>
          <select id="novo-aluno" value={usuarioId} onChange={(e) => setUsuarioId(e.target.value)}>
            <option value="">Escolha…</option>
            {usuarios.dados?.map((u) => (
              <option key={u.id} value={u.id}>
                {u.nome} ({u.matricula})
              </option>
            ))}
          </select>
        </div>
        <div className="campo crescer">
          <label htmlFor="novo-material">Material</label>
          <select id="novo-material" value={materialId} onChange={(e) => setMaterialId(e.target.value)}>
            <option value="">Escolha…</option>
            {materiais.dados?.map((m) => (
              <option key={m.id} value={m.id} disabled={m.licencasDisponiveis === 0}>
                {m.titulo} ({m.licencasDisponiveis === 0 ? 'esgotado' : `${m.licencasDisponiveis} livres`})
              </option>
            ))}
          </select>
        </div>
        <button type="submit" className="botao botao-primario" disabled={enviando}>
          {enviando ? 'Registrando…' : 'Registrar'}
        </button>
      </div>
      <span className="texto-pequeno texto-suave">Prazo padrão de 14 dias. A API recusa se o aluno já tiver 3 empréstimos ou já estiver com o material.</span>
      {erro && <Retorno tipo="erro">{erro}</Retorno>}
    </form>
  )
}
