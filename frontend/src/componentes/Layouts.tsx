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

/** Área do aluno: cabeçalho, abas logo abaixo e a tela. Sem aluno identificado, volta para a entrada. */
export function AreaAluno() {
  const { usuario, perfil, sair } = useSessao()
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

/** Área da equipe da biblioteca. A API não tem perfis de acesso, então não há bloqueio aqui. */
export function AreaEquipe() {
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
        <Link to="/" className="botao botao-fantasma botao-fantasma-claro">
          Sair
        </Link>
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
