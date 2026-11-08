import { BrowserRouter, Route, Routes } from 'react-router-dom'
import { NaoEncontrada } from './componentes/Basicos'
import { AreaAluno, AreaEquipe } from './componentes/Layouts'
import { Acervo } from './paginas/Acervo'
import { Entrada } from './paginas/Entrada'
import { PainelAcervo } from './paginas/equipe/PainelAcervo'
import { PainelEmprestimos } from './paginas/equipe/PainelEmprestimos'
import { Inicio } from './paginas/Inicio'
import { Leituras } from './paginas/Leituras'
import { MaterialDetalhe } from './paginas/MaterialDetalhe'
import { Perfil } from './paginas/Perfil'
import { Ranking } from './paginas/Ranking'
import { SessaoProvider } from './sessao/SessaoProvider'

export default function App() {
  return (
    <SessaoProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<Entrada />} />
          <Route element={<AreaAluno />}>
            <Route path="/inicio" element={<Inicio />} />
            <Route path="/acervo" element={<Acervo />} />
            <Route path="/materiais/:id" element={<MaterialDetalhe />} />
            <Route path="/leituras" element={<Leituras />} />
            <Route path="/perfil" element={<Perfil />} />
            <Route path="/ranking" element={<Ranking />} />
          </Route>
          <Route path="/equipe" element={<AreaEquipe />}>
            <Route index element={<PainelEmprestimos />} />
            <Route path="acervo" element={<PainelAcervo />} />
          </Route>
          <Route path="*" element={<NaoEncontrada />} />
        </Routes>
      </BrowserRouter>
    </SessaoProvider>
  )
}
