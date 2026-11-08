import { useState } from 'react'
import { api } from '../api/cliente'
import { Carregando, MensagemErro, Vazio } from '../componentes/Basicos'
import { useCarregar } from '../hooks/useCarregar'
import { useSessao } from '../sessao/contexto'
import { nomeDoNivel, plural } from '../util/formato'

const CORES_PODIO = ['#F2B23A', '#D1D5DB', '#E7B98A']

export function Ranking() {
  const { usuario } = useSessao()
  const [limite, setLimite] = useState(10)
  const ranking = useCarregar(() => api.ranking(limite), [limite])

  const podio = ranking.dados?.slice(0, 3) ?? []
  const demais = ranking.dados?.slice(3) ?? []

  return (
    <main className="conteudo conteudo-estreito">
      <section className="linha-titulo">
        <div>
          <h1 className="titulo-pagina">Ranking de leitores</h1>
          <p className="texto-suave">Pontos por leituras concluídas, devoluções no prazo e avaliações.</p>
        </div>
        <div className="linha">
          <label htmlFor="limite" className="texto-pequeno texto-suave">
            Mostrar
          </label>
          <select id="limite" value={limite} onChange={(e) => setLimite(Number(e.target.value))}>
            <option value={10}>Top 10</option>
            <option value={20}>Top 20</option>
            <option value={50}>Top 50</option>
          </select>
        </div>
      </section>

      {ranking.erro !== undefined && <MensagemErro erro={ranking.erro} onTentarDeNovo={ranking.recarregar} />}
      {ranking.carregando && !ranking.dados && <Carregando />}
      {ranking.dados?.length === 0 && <Vazio>Ninguém pontuou ainda. Termine uma leitura para aparecer aqui!</Vazio>}

      {podio.length > 0 && (
        <section aria-label="Pódio" className="podio">
          {podio.map((linha, i) => (
            <article key={linha.usuario.id} className={linha.usuario.id === usuario!.id ? 'cartao podio-item voce' : 'cartao podio-item'}>
              <span className="podio-posicao" style={{ background: CORES_PODIO[i] }}>
                {linha.posicao}º
              </span>
              <h2 className="titulo-cartao titulo-cartao-grande">
                {linha.usuario.nome}
                {linha.usuario.id === usuario!.id && <span className="selo-voce">VOCÊ</span>}
              </h2>
              <span className="texto-suave texto-pequeno">{nomeDoNivel(linha.nivel)}</span>
              <span className="numero-destaque">
                {linha.pontos} <small>pts</small>
              </span>
              <span className="etiqueta-pontos">{plural(linha.conquistasDesbloqueadas, 'conquista', 'conquistas')}</span>
            </article>
          ))}
        </section>
      )}

      {demais.length > 0 && (
        <div className="tabela-rolavel cartao-sem-padding">
          <table className="tabela">
            <caption className="so-leitor">Demais posições do ranking</caption>
            <thead>
              <tr>
                <th scope="col">Posição</th>
                <th scope="col">Aluno</th>
                <th scope="col">Nível</th>
                <th scope="col">Conquistas</th>
                <th scope="col" className="direita">
                  Pontos
                </th>
              </tr>
            </thead>
            <tbody>
              {demais.map((linha) => {
                const voce = linha.usuario.id === usuario!.id
                return (
                  <tr key={linha.usuario.id} className={voce ? 'linha-voce' : undefined}>
                    <td>{linha.posicao}º</td>
                    <td>
                      <strong>{linha.usuario.nome}</strong>
                      {voce && <span className="selo-voce">VOCÊ</span>}
                    </td>
                    <td>{nomeDoNivel(linha.nivel)}</td>
                    <td>{linha.conquistasDesbloqueadas}</td>
                    <td className="direita">
                      <strong>{linha.pontos}</strong>
                    </td>
                  </tr>
                )
              })}
            </tbody>
          </table>
        </div>
      )}
      <p className="texto-pequeno texto-suave">
        Empatados dividem a posição. Só aparecem alunos com pelo menos 1 ponto, com o nome abreviado.
      </p>
    </main>
  )
}
