import { useState, type FormEvent } from 'react'
import { Navigate, useNavigate } from 'react-router-dom'
import { api, mensagemDeErro } from '../api/cliente'
import { Retorno } from '../componentes/Basicos'
import { Logo } from '../componentes/Logo'
import { useSessao } from '../sessao/contexto'

export function Entrada() {
  const { usuario, entrar } = useSessao()
  const navegar = useNavigate()
  const [matricula, setMatricula] = useState('')
  const [senha, setSenha] = useState('')
  const [erro, setErro] = useState<string | null>(null)
  const [enviando, setEnviando] = useState(false)

  if (usuario) {
    return <Navigate to={usuario.perfil === 'BIBLIOTECARIO' ? '/equipe' : '/inicio'} replace />
  }

  async function fazerLogin(evento: FormEvent) {
    evento.preventDefault()
    if (!matricula.trim() || !senha) {
      setErro('Preencha a matrícula e a senha.')
      return
    }
    setEnviando(true)
    setErro(null)
    try {
      const resposta = await api.login(matricula.trim(), senha)
      entrar(resposta.token, resposta.usuario)
      navegar(resposta.usuario.perfil === 'BIBLIOTECARIO' ? '/equipe' : '/inicio')
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
            <p className="texto-suave">Alunos, professores e equipe da biblioteca entram com a matrícula da escola.</p>
          </div>
          <form onSubmit={fazerLogin} className="pilha" noValidate>
            <div className="campo">
              <label htmlFor="matricula">Matrícula</label>
              <input
                id="matricula"
                value={matricula}
                onChange={(e) => setMatricula(e.target.value)}
                autoComplete="username"
                aria-invalid={erro !== null}
              />
            </div>
            <div className="campo">
              <label htmlFor="senha">Senha</label>
              <input
                id="senha"
                type="password"
                value={senha}
                onChange={(e) => setSenha(e.target.value)}
                autoComplete="current-password"
                aria-invalid={erro !== null}
                aria-describedby={erro ? 'erro-login' : undefined}
              />
            </div>
            {erro && (
              <div id="erro-login">
                <Retorno tipo="erro">{erro}</Retorno>
              </div>
            )}
            <button type="submit" className="botao botao-primario botao-grande" disabled={enviando}>
              {enviando ? 'Entrando…' : 'Entrar'}
            </button>
          </form>
          <p className="texto-pequeno texto-suave">Esqueceu a senha? Procure a equipe da biblioteca.</p>
        </div>
      </section>
    </div>
  )
}
