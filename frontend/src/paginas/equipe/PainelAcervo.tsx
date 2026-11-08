import { useState } from 'react'
import { api } from '../../api/cliente'
import { Carregando, MensagemErro, Vazio } from '../../componentes/Basicos'
import { useCarregar } from '../../hooks/useCarregar'
import { nota, ROTULO_TIPO } from '../../util/formato'

/** Visão do acervo para a equipe: licenças em uso e avaliações de cada material. */
export function PainelAcervo() {
  const materiais = useCarregar(() => api.materiais(), [])
  const [termo, setTermo] = useState('')

  const filtrados = (materiais.dados ?? []).filter((m) => m.titulo.toLowerCase().includes(termo.trim().toLowerCase()))

  return (
    <main className="conteudo">
      <section className="linha-titulo">
        <div>
          <h1 className="titulo-pagina">Acervo</h1>
          <p className="texto-suave">Cadastros e edições continuam pelo Swagger (/swagger-ui.html).</p>
        </div>
        <div className="campo">
          <label htmlFor="filtro-titulo">Filtrar por título</label>
          <input id="filtro-titulo" type="search" value={termo} onChange={(e) => setTermo(e.target.value)} />
        </div>
      </section>

      {materiais.erro !== undefined && <MensagemErro erro={materiais.erro} onTentarDeNovo={materiais.recarregar} />}
      {materiais.carregando && !materiais.dados && <Carregando />}
      {materiais.dados && filtrados.length === 0 && <Vazio>Nenhum material encontrado.</Vazio>}

      {filtrados.length > 0 && (
        <div className="tabela-rolavel cartao-sem-padding">
          <table className="tabela">
            <thead>
              <tr>
                <th scope="col">Título</th>
                <th scope="col">Tipo</th>
                <th scope="col">Categoria</th>
                <th scope="col">Autores</th>
                <th scope="col">Licenças livres</th>
                <th scope="col">Avaliação</th>
              </tr>
            </thead>
            <tbody>
              {filtrados.map((m) => (
                <tr key={m.id}>
                  <td>
                    <strong>{m.titulo}</strong> <span className="texto-suave">({m.anoPublicacao})</span>
                  </td>
                  <td>{ROTULO_TIPO[m.tipo]}</td>
                  <td>{m.categoria.nome}</td>
                  <td>{m.autores.map((a) => a.nome).join(', ')}</td>
                  <td className={m.licencasDisponiveis === 0 ? 'texto-perigo' : undefined}>
                    {m.licencasDisponiveis} de {m.quantidadeLicencas}
                  </td>
                  <td>
                    ★ {nota(m.mediaAvaliacoes)} <span className="texto-suave">({m.totalAvaliacoes})</span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </main>
  )
}
