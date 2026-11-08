import { useState, type FormEvent } from 'react'
import { Link, Navigate, useNavigate } from 'react-router-dom'
import { api, mensagemDeErro } from '../api/cliente'
import { Retorno } from '../componentes/Basicos'
import { Logo } from '../componentes/Logo'
import { useSessao } from '../sessao/contexto'

export function Entrada() {
  const { usuario, entrar } = useSessao()
  const navegar = useNavigate()
  const [matricula, setMatricula] = useState('')
  const [erro, setErro] = useState<string | null>(null)
  const [enviando, setEnviando] = useState(false)

  if (usuario) {
    return <Navigate to="/inicio" replace />
  }

  async function entrarComoAluno(evento: FormEvent) {
    evento.preventDefault()
    const procurada = matricula.trim().toLowerCase()
    if (!procurada) {
      setErro('Digite a sua matrícula.')
      return
    }
    setEnviando(true)
    setErro(null)
    try {
      // A API não tem busca por matrícula nem login: procuramos na lista de usuários.
      const usuarios = await api.usuarios()
      const encontrado = usuarios.find((u) => u.matricula.toLowerCase() === procurada)
      if (!encontrado) {
        setErro('Matrícula não encontrada. Confira com a biblioteca se o seu cadastro existe.')
        return
      }
      entrar(encontrado)
      navegar('/inicio')
    } catch (falha) {
      setErro(mensagemDeErro(falha))
    } finally {
      setEnviando(false)
    }
  }

  return (
    <div className="entrada">
      <section className="entrada-marca">
        <div className="marca">
          <Logo tamanho={56} claro />
          <span className="marca-nome marca-nome-grande">BIBLIOTECA VIRTUAL</span>
        </div>
        <div className="entrada-chamada">
          <h1>O acervo da escola, a uma página de distância.</h1>
          <p>
            Pegue livros, apostilas e vídeos emprestados, acompanhe sua leitura e suba de nível a cada material
            concluído.
          </p>
        </div>
        <ul className="entrada-lista">
          <li>Busca no acervo por título, autor, série e avaliação</li>
          <li>Progresso de leitura página a página</li>
          <li className="destaque">Pontos, conquistas e ranking de leitores</li>
        </ul>
      </section>

      <section className="entrada-form">
        <div className="entrada-caixa">
          <div>
            <h2>Entrar</h2>
            <p className="texto-suave">Use a matrícula da escola.</p>
          </div>
          <form onSubmit={entrarComoAluno} className="pilha" noValidate>
            <div className="campo">
              <label htmlFor="matricula">Matrícula</label>
              <input
                id="matricula"
                value={matricula}
                onChange={(e) => setMatricula(e.target.value)}
                autoComplete="username"
                aria-invalid={erro !== null}
                aria-describedby={erro ? 'erro-matricula' : undefined}
              />
            </div>
            {erro && (
              <div id="erro-matricula">
                <Retorno tipo="erro">{erro}</Retorno>
              </div>
            )}
            <button type="submit" className="botao botao-primario botao-grande" disabled={enviando}>
              {enviando ? 'Entrando…' : 'Entrar como aluno'}
            </button>
          </form>
          <div className="divisor">ou</div>
          <Link to="/equipe" className="botao botao-secundario botao-grande">
            Sou da equipe da biblioteca
          </Link>
        </div>
      </section>
    </div>
  )
}
