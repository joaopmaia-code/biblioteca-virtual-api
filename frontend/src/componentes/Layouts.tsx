import { Link, Navigate, NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useSessao } from '../sessao/contexto'
import { iniciais } from '../util/formato'
import { Estrela } from './Basicos'
import { Logo } from './Logo'

const ABAS_ALUNO = [
  { para: '/inicio', rotulo: 'Início' },
  { para: '/acervo', rotulo: 'Acervo' },
  { para: '/leituras', rotulo: 'Minhas leituras' },
  { para: '/ranking', rotulo: 'Ranking' },
  { para: '/perfil', rotulo: 'Perfil de leitor' },
]

const ABAS_EQUIPE = [
  { para: '/equipe', rotulo: 'Empréstimos', fim: true },
  { para: '/equipe/acervo', rotulo: 'Acervo', fim: false },
]

function classeDaAba({ isActive }: { isActive: boolean }) {
  return isActive ? 'aba aba-ativa' : 'aba'
}

/** Área do leitor (aluno, e também professor e bibliotecário quando pegam materiais). Sem login, volta para a entrada. */
export function AreaAluno() {
  const { usuario, perfil, ehEquipe, sair } = useSessao()
  const navegar = useNavigate()

  if (!usuario) {
    return <Navigate to="/" replace />
  }

  return (
    <>
      <header className="cabecalho">
        <Link to="/inicio" className="marca">
          <Logo />
          <span className="marca-nome">BIBLIOTECA VIRTUAL</span>
        </Link>
        <div className="cabecalho-acoes">
          {ehEquipe && (
            <Link to="/equipe" className="botao botao-secundario botao-pequeno">
              Painel da equipe
            </Link>
          )}
          {perfil && (
            <Link to="/perfil" className="chip-pontos">
              <Estrela />
              {perfil.pontos} pts · {perfil.nomeNivel}
            </Link>
          )}
          <span className="avatar" title={usuario.nome} aria-hidden="true">
            {iniciais(usuario.nome)}
          </span>
          <button
            type="button"
            className="botao botao-fantasma"
            onClick={() => {
              sair()
              navegar('/')
            }}
          >
            Sair
          </button>
        </div>
      </header>
      <nav className="abas" aria-label="Principal">
        {ABAS_ALUNO.map((aba) => (
          <NavLink key={aba.para} to={aba.para} className={classeDaAba}>
            {aba.rotulo}
          </NavLink>
        ))}
      </nav>
      <Outlet />
    </>
  )
}

/**
 * Painel da equipe: só professor e bibliotecário. A API também recusa (403) o que o perfil não pode,
 * então este bloqueio é só para não mostrar telas inúteis.
 */
export function AreaEquipe() {
  const { usuario, ehEquipe, sair } = useSessao()
  const navegar = useNavigate()

  if (!usuario) {
    return <Navigate to="/" replace />
  }
  if (!ehEquipe) {
    return <Navigate to="/inicio" replace />
  }

  return (
    <>
      <header className="cabecalho cabecalho-escuro">
        <Link to="/equipe" className="marca">
          <Logo claro />
          <span className="marca-pilha">
            <span className="marca-nome">BIBLIOTECA VIRTUAL</span>
            <span className="marca-sub">Painel da equipe</span>
          </span>
        </Link>
        <div className="cabecalho-acoes">
          <span className="texto-claro-suave">
            {usuario.nome} · {usuario.perfil === 'BIBLIOTECARIO' ? 'Bibliotecário(a)' : 'Professor(a)'}
          </span>
          <Link to="/inicio" className="botao botao-fantasma botao-fantasma-claro">
            Área do leitor
          </Link>
          <button
            type="button"
            className="botao botao-fantasma botao-fantasma-claro"
            onClick={() => {
              sair()
              navegar('/')
            }}
          >
            Sair
          </button>
        </div>
      </header>
      <nav className="abas" aria-label="Gestão">
        {ABAS_EQUIPE.map((aba) => (
          <NavLink key={aba.para} to={aba.para} end={aba.fim} className={classeDaAba}>
            {aba.rotulo}
          </NavLink>
        ))}
      </nav>
      <Outlet />
    </>
  )
}
