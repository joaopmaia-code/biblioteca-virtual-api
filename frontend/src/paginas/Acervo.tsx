import { useState, type FormEvent } from 'react'
import { Link } from 'react-router-dom'
import { api } from '../api/cliente'
import type { FiltroBusca, OrdenacaoMaterial, SerieRecomendada, TipoMaterial } from '../api/tipos'
import { Capa, Carregando, MensagemErro, Vazio } from '../componentes/Basicos'
import { useCarregar } from '../hooks/useCarregar'
import { nota, plural, ROTULO_SERIE, ROTULO_TIPO } from '../util/formato'

const FILTRO_INICIAL: FiltroBusca = { ordenarPor: 'TITULO', pagina: 0, tamanho: 12 }

/** "" vira undefined (filtro não informado); o resto vira número. */
function numeroOuNada(texto: string): number | undefined {
  return texto.trim() === '' ? undefined : Number(texto)
}

export function Acervo() {
  const [filtro, setFiltro] = useState<FiltroBusca>(FILTRO_INICIAL)
  // Título e anos só valem ao clicar em Buscar (evita uma busca a cada tecla).
  const [titulo, setTitulo] = useState('')
  const [anoMin, setAnoMin] = useState('')
  const [anoMax, setAnoMax] = useState('')

  const categorias = useCarregar(() => api.categorias(), [])
  const resultado = useCarregar(() => api.buscarMateriais(filtro), [JSON.stringify(filtro)])

  /** Muda um filtro e volta para a primeira página. */
  function aplicar(mudanca: Partial<FiltroBusca>) {
    setFiltro((atual) => ({ ...atual, ...mudanca, pagina: 0 }))
  }

  function buscar(evento: FormEvent) {
    evento.preventDefault()
    aplicar({ titulo: titulo.trim() || undefined, anoMin: numeroOuNada(anoMin), anoMax: numeroOuNada(anoMax) })
  }

  function limpar() {
    setTitulo('')
    setAnoMin('')
    setAnoMax('')
    setFiltro(FILTRO_INICIAL)
  }

  const pagina = resultado.dados

  return (
    <main className="conteudo">
      <form onSubmit={buscar} className="pilha">
        <h1 className="titulo-pagina">Acervo</h1>
        <div className="linha-busca" role="search">
          <label htmlFor="busca" className="so-leitor">
            Buscar por título
          </label>
          <input
            id="busca"
            type="search"
            className="campo-busca"
            placeholder="Título, por exemplo: Dom Casmurro"
            value={titulo}
            onChange={(e) => setTitulo(e.target.value)}
          />
          <button type="submit" className="botao botao-primario botao-grande">
            Buscar
          </button>
        </div>

        <div className="colunas colunas-filtro">
          <aside className="cartao filtros" aria-label="Filtros">
            <div className="linha-entre">
              <h2 className="titulo-filtros">Filtros</h2>
              <button type="button" className="botao-link" onClick={limpar}>
                Limpar
              </button>
            </div>

            <fieldset className="grupo">
              <legend>Tipo</legend>
              {(['', 'LIVRO', 'APOSTILA', 'ARTIGO', 'VIDEO'] as const).map((tipo) => (
                <label key={tipo || 'todos'} className="opcao">
                  <input
                    type="radio"
                    name="tipo"
                    checked={(filtro.tipo ?? '') === tipo}
                    onChange={() => aplicar({ tipo: (tipo || undefined) as TipoMaterial | undefined })}
                  />
                  {tipo ? ROTULO_TIPO[tipo] : 'Todos'}
                </label>
              ))}
            </fieldset>

            <div className="campo">
              <label htmlFor="serie">Série</label>
              <select
                id="serie"
                value={filtro.serie ?? ''}
                onChange={(e) => aplicar({ serie: (e.target.value || undefined) as SerieRecomendada | undefined })}
              >
                <option value="">Todas</option>
                {(Object.keys(ROTULO_SERIE) as SerieRecomendada[]).map((serie) => (
                  <option key={serie} value={serie}>
                    {ROTULO_SERIE[serie]}
                  </option>
                ))}
              </select>
            </div>

            <div className="campo">
              <label htmlFor="categoria">Categoria</label>
              <select
                id="categoria"
                value={filtro.categoriaId ?? ''}
                onChange={(e) => aplicar({ categoriaId: numeroOuNada(e.target.value) })}
              >
                <option value="">Todas</option>
                {categorias.dados?.map((categoria) => (
                  <option key={categoria.id} value={categoria.id}>
                    {categoria.nome}
                  </option>
                ))}
              </select>
            </div>

            <fieldset className="grupo">
              <legend>Ano de publicação</legend>
              <div className="linha">
                <label className="campo campo-compacto">
                  De
                  <input type="number" inputMode="numeric" value={anoMin} onChange={(e) => setAnoMin(e.target.value)} />
                </label>
                <label className="campo campo-compacto">
                  Até
                  <input type="number" inputMode="numeric" value={anoMax} onChange={(e) => setAnoMax(e.target.value)} />
                </label>
              </div>
              <button type="submit" className="botao botao-secundario botao-pequeno">
                Aplicar anos
              </button>
            </fieldset>

            <div className="campo">
              <label htmlFor="nota">Avaliação mínima</label>
              <select id="nota" value={filtro.notaMinima ?? ''} onChange={(e) => aplicar({ notaMinima: numeroOuNada(e.target.value) })}>
                <option value="">Qualquer</option>
                <option value="4">★ 4 ou mais</option>
                <option value="3">★ 3 ou mais</option>
              </select>
            </div>

            <label className="opcao">
              <input
                type="checkbox"
                checked={filtro.disponivel === true}
                onChange={(e) => aplicar({ disponivel: e.target.checked ? true : undefined })}
              />
              Só com licença livre agora
            </label>
          </aside>

          <section className="coluna-principal pilha" aria-live="polite">
            <div className="linha-entre">
              <p className="texto-pequeno">
                {pagina ? <strong>{plural(pagina.totalElementos, 'material encontrado', 'materiais encontrados')}</strong> : ' '}
              </p>
              <div className="linha">
                <label htmlFor="ordem" className="texto-pequeno texto-suave">
                  Ordenar por
                </label>
                <select
                  id="ordem"
                  value={filtro.ordenarPor}
                  onChange={(e) => aplicar({ ordenarPor: e.target.value as OrdenacaoMaterial })}
                >
                  <option value="TITULO">Título</option>
                  <option value="ANO">Mais recentes</option>
                  <option value="AVALIACAO">Melhor avaliados</option>
                </select>
              </div>
            </div>

            {resultado.erro !== undefined && <MensagemErro erro={resultado.erro} onTentarDeNovo={resultado.recarregar} />}
            {resultado.carregando && !pagina && <Carregando />}
            {pagina?.totalElementos === 0 && <Vazio>Nenhum material com esses filtros.</Vazio>}

            <div className={resultado.carregando ? 'grade-cartoes atualizando' : 'grade-cartoes'}>
              {pagina?.conteudo.map((material) => (
                <Link key={material.id} to={`/materiais/${material.id}`} className="cartao cartao-material">
                  <Capa
                    id={material.id}
                    titulo={material.titulo}
                    urlCapa={material.urlCapa}
                    rotulo={ROTULO_TIPO[material.tipo].toUpperCase()}
                  />
                  <div>
                    <div className="titulo-cartao">{material.titulo}</div>
                    <div className="texto-suave texto-pequeno">
                      {material.autores.map((a) => a.nome).join(', ')} · {material.anoPublicacao}
                    </div>
                  </div>
                  <div className="linha-entre texto-pequeno">
                    <span>
                      ★ {nota(material.mediaAvaliacoes)} ({material.totalAvaliacoes})
                    </span>
                    {material.licencasDisponiveis > 0 ? (
                      <span className="texto-ok">
                        {material.licencasDisponiveis} de {material.quantidadeLicencas} livres
                      </span>
                    ) : (
                      <span className="texto-perigo">Esgotado</span>
                    )}
                  </div>
                </Link>
              ))}
            </div>

            {pagina && pagina.totalPaginas > 1 && (
              <nav className="paginacao" aria-label="Páginas">
                <button
                  type="button"
                  className="botao botao-secundario"
                  disabled={pagina.pagina === 0}
                  onClick={() => setFiltro((f) => ({ ...f, pagina: (f.pagina ?? 0) - 1 }))}
                >
                  Anterior
                </button>
                <span className="texto-pequeno">
                  Página {pagina.pagina + 1} de {pagina.totalPaginas}
                </span>
                <button
                  type="button"
                  className="botao botao-secundario"
                  disabled={pagina.pagina + 1 >= pagina.totalPaginas}
                  onClick={() => setFiltro((f) => ({ ...f, pagina: (f.pagina ?? 0) + 1 }))}
                >
                  Próxima
                </button>
              </nav>
            )}
          </section>
        </div>
      </form>
    </main>
  )
}
