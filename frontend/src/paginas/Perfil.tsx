import { useEffect } from 'react'
import { Link } from 'react-router-dom'
import { BarraProgresso, Carregando } from '../componentes/Basicos'
import { useSessao } from '../sessao/contexto'
import { iniciais, NIVEIS, percentualNoNivel, plural } from '../util/formato'

export function Perfil() {
  const { usuario, perfil, recarregarPerfil } = useSessao()

  // Ao abrir a tela, busca os pontos de novo (podem ter mudado em outra aba).
  useEffect(() => {
    recarregarPerfil()
  }, [recarregarPerfil])

  if (!perfil) {
    return (
      <main className="conteudo">
        <Carregando />
      </main>
    )
  }

  const indiceAtual = NIVEIS.findIndex((nivel) => nivel.codigo === perfil.nivel)
  const desbloqueadas = perfil.conquistas.filter((c) => c.desbloqueada).length

  return (
    <main className="conteudo">
      <section className="painel-perfil">
        <div className="linha crescer-base">
          <span className="avatar avatar-grande" aria-hidden="true">
            {iniciais(usuario!.nome)}
          </span>
          <div>
            <h1 className="titulo-perfil">{usuario!.nome}</h1>
            <span className="texto-claro-suave">Matrícula {usuario!.matricula}</span>
          </div>
        </div>
        <div className="pilha crescer-dobro">
          <div className="linha-entre linha-base">
            <span className="nivel-perfil">{perfil.nomeNivel}</span>
            <span className="pontos-perfil">
              {perfil.pontos} <span>pontos</span>
            </span>
          </div>
          <ol className="trilha-niveis">
            {NIVEIS.map((nivel, i) => (
              <li key={nivel.codigo}>
                <div className={i < indiceAtual ? 'degrau degrau-cheio' : 'degrau'}>
                  {i === indiceAtual && (
                    <div className="degrau-preenchido" style={{ width: `${perfil.proximoNivel ? percentualNoNivel(perfil.pontos, perfil.nivel) : 100}%` }} />
                  )}
                </div>
                <span>
                  {nivel.curto} · {nivel.minimo}
                </span>
              </li>
            ))}
          </ol>
          <span className="texto-claro">
            {perfil.proximoNivel ? (
              <>
                Faltam <strong>{perfil.pontosParaProximoNivel} pontos</strong> para {perfil.proximoNivel}
              </>
            ) : (
              'Você chegou ao último nível!'
            )}
          </span>
        </div>
      </section>

      <div className="colunas">
        <section className="coluna-principal pilha">
          <div className="linha-secao">
            <h2 className="titulo-secao">Conquistas</h2>
            <span className="texto-suave texto-pequeno">
              {desbloqueadas} de {perfil.conquistas.length} desbloqueadas
            </span>
          </div>
          <div className="grade-conquistas">
            {perfil.conquistas.map((conquista) => (
              <article key={conquista.codigo} className={conquista.desbloqueada ? 'cartao conquista conquista-ok' : 'cartao conquista'}>
                <span className="conquista-icone" aria-hidden="true">
                  {conquista.desbloqueada ? (
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                      <circle cx="12" cy="9" r="6" />
                      <path d="M8.5 14 7 22l5-3 5 3-1.5-8" />
                    </svg>
                  ) : (
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                      <rect x="5" y="11" width="14" height="10" rx="2" />
                      <path d="M8 11V7a4 4 0 0 1 8 0v4" />
                    </svg>
                  )}
                </span>
                <div className="pilha pilha-justa crescer">
                  <h3 className="titulo-cartao">{conquista.nome}</h3>
                  <p className="texto-pequeno">{conquista.descricao}</p>
                  {conquista.desbloqueada ? (
                    <span className="texto-pequeno texto-ambar">Desbloqueada</span>
                  ) : (
                    <>
                      <BarraProgresso
                        valor={(conquista.progresso * 100) / conquista.meta}
                        cor="cinza"
                        rotulo={`Progresso em ${conquista.nome}`}
                      />
                      <span className="texto-pequeno texto-suave">
                        {conquista.progresso} de {conquista.meta}
                      </span>
                    </>
                  )}
                </div>
              </article>
            ))}
          </div>
        </section>

        <aside className="cartao coluna-lateral pilha">
          <h2 className="titulo-secao">De onde vêm seus pontos</h2>
          <table className="tabela-extrato">
            <caption className="so-leitor">Extrato de pontos</caption>
            <tbody>
              {perfil.extrato.map((linha) => (
                <tr key={linha.motivo}>
                  <th scope="row">
                    {linha.motivo}
                    <span>
                      {plural(linha.quantidade, 'vez', 'vezes')} × {linha.pontosCada} pts
                    </span>
                  </th>
                  <td>{linha.total}</td>
                </tr>
              ))}
              <tr className="total">
                <th scope="row">Total</th>
                <td>{perfil.pontos}</td>
              </tr>
            </tbody>
          </table>
          <Link to="/ranking" className="link-forte">
            Ver o ranking de leitores
          </Link>
        </aside>
      </div>
    </main>
  )
}
