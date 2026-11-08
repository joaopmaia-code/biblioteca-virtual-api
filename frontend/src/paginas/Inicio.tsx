import { Link } from 'react-router-dom'
import { api } from '../api/cliente'
import { BarraProgresso, Capa, Carregando, Estrela, MensagemErro, SeloStatus, Vazio } from '../componentes/Basicos'
import { useCarregar } from '../hooks/useCarregar'
import { useSessao } from '../sessao/contexto'
import { dataCurta, nota, percentualNoNivel, plural, primeiroNome } from '../util/formato'

export function Inicio() {
  const { usuario, perfil } = useSessao()
  const id = usuario!.id
  const leituras = useCarregar(() => api.leituras(id), [id])
  const recomendacoes = useCarregar(() => api.recomendacoes(id), [id])

  const atrasadas = leituras.dados?.filter((l) => l.statusEmprestimo === 'ATRASADO').length ?? 0
  const ultimaConquista = perfil?.conquistas.filter((c) => c.desbloqueada).at(-1)

  return (
    <main className="conteudo">
      <section className="linha-titulo">
        <div>
          <h1 className="titulo-pagina">Olá, {primeiroNome(usuario!.nome)}</h1>
          {leituras.dados && (
            <p className="texto-suave">
              Você tem {plural(leituras.dados.length, 'leitura em andamento', 'leituras em andamento')}
              {atrasadas > 0 ? ` e ${plural(atrasadas, 'empréstimo com prazo vencido', 'empréstimos com prazo vencido')}` : ''}.
            </p>
          )}
        </div>
        <Link to="/acervo" className="botao botao-primario">
          Explorar o acervo
        </Link>
      </section>

      <div className="colunas">
        <section className="coluna-principal pilha">
          <div className="linha-secao">
            <h2 className="titulo-secao">Continue lendo</h2>
            <Link to="/leituras" className="link-forte">
              Ver todas
            </Link>
          </div>
          {leituras.erro !== undefined && <MensagemErro erro={leituras.erro} onTentarDeNovo={leituras.recarregar} />}
          {leituras.carregando && !leituras.dados && <Carregando />}
          {leituras.dados?.length === 0 && (
            <Vazio>
              Nenhum material com você agora. <Link to="/acervo">Escolha um no acervo</Link>.
            </Vazio>
          )}
          {leituras.dados?.map((leitura) => (
            <article key={leitura.emprestimoId} className="cartao cartao-linha">
              <Capa id={leitura.material.id} titulo={leitura.material.nome} />
              <div className="pilha pilha-justa crescer">
                <div className="linha-entre">
                  <Link to={`/materiais/${leitura.material.id}`} className="titulo-cartao">
                    {leitura.material.nome}
                  </Link>
                  <SeloStatus
                    status={leitura.statusEmprestimo}
                    prazo={leitura.statusEmprestimo === 'ATRASADO' ? `venceu ${dataCurta(leitura.dataPrevistaDevolucao)}` : `até ${dataCurta(leitura.dataPrevistaDevolucao)}`}
                  />
                </div>
                {leitura.numeroPaginas !== null ? (
                  <>
                    <BarraProgresso valor={leitura.percentual ?? 0} rotulo={`Progresso de ${leitura.material.nome}`} />
                    <div className="linha-entre texto-pequeno">
                      <span>
                        Página {leitura.paginaAtual} de {leitura.numeroPaginas} · {nota(leitura.percentual)}%
                      </span>
                      <Link to="/leituras" className="link-forte">
                        Atualizar progresso
                      </Link>
                    </div>
                  </>
                ) : (
                  <span className="texto-pequeno texto-suave">Material sem contagem de páginas (ex.: vídeo).</span>
                )}
              </div>
            </article>
          ))}
        </section>

        <aside className="cartao coluna-lateral pilha">
          <h2 className="titulo-secao">Seu nível</h2>
          {!perfil ? (
            <Carregando />
          ) : (
            <>
              <div className="linha">
                <span className="icone-nivel">
                  <Estrela tamanho={28} />
                </span>
                <div>
                  <div className="nivel-nome">{perfil.nomeNivel}</div>
                  <div className="texto-suave">{plural(perfil.pontos, 'ponto', 'pontos')}</div>
                </div>
              </div>
              {perfil.proximoNivel ? (
                <div className="pilha pilha-justa">
                  <BarraProgresso valor={percentualNoNivel(perfil.pontos, perfil.nivel)} cor="ambar" rotulo="Progresso até o próximo nível" />
                  <span className="texto-pequeno">
                    Faltam {perfil.pontosParaProximoNivel} pontos para <strong>{perfil.proximoNivel}</strong>
                  </span>
                </div>
              ) : (
                <span className="texto-pequeno">Você chegou ao último nível!</span>
              )}
              {ultimaConquista && (
                <div className="caixa-cinza">
                  <span className="texto-pequeno texto-suave">Conquista desbloqueada</span>
                  <strong>
                    {ultimaConquista.nome}: {ultimaConquista.descricao.toLowerCase()}
                  </strong>
                </div>
              )}
              <Link to="/perfil" className="link-forte">
                Ver perfil de leitor
              </Link>
            </>
          )}
        </aside>
      </div>

      <section className="pilha">
        <div>
          <h2 className="titulo-secao">Recomendados para você</h2>
          <p className="texto-suave texto-pequeno">Com base nos materiais que você já pegou emprestado.</p>
        </div>
        {recomendacoes.erro !== undefined && <MensagemErro erro={recomendacoes.erro} onTentarDeNovo={recomendacoes.recarregar} />}
        {recomendacoes.carregando && !recomendacoes.dados && <Carregando />}
        {recomendacoes.dados?.length === 0 && (
          <Vazio>As recomendações aparecem depois do seu primeiro empréstimo.</Vazio>
        )}
        <div className="grade-cartoes">
          {recomendacoes.dados?.map(({ material, motivo }) => (
            <Link key={material.id} to={`/materiais/${material.id}`} className="cartao cartao-material">
              <Capa id={material.id} titulo={material.titulo} />
              <div>
                <div className="titulo-cartao">{material.titulo}</div>
                <div className="texto-suave texto-pequeno">
                  {material.autores.map((a) => a.nome).join(', ')} · {material.anoPublicacao}
                </div>
              </div>
              <span className="etiqueta-motivo">{motivo}</span>
              <span className="texto-pequeno">
                ★ {nota(material.mediaAvaliacoes)} · {plural(material.totalAvaliacoes, 'avaliação', 'avaliações')}
              </span>
            </Link>
          ))}
        </div>
      </section>
    </main>
  )
}
