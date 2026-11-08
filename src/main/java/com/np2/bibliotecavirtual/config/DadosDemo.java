package com.np2.bibliotecavirtual.config;

import com.np2.bibliotecavirtual.model.Autor;
import com.np2.bibliotecavirtual.model.Avaliacao;
import com.np2.bibliotecavirtual.model.Categoria;
import com.np2.bibliotecavirtual.model.Editora;
import com.np2.bibliotecavirtual.model.Emprestimo;
import com.np2.bibliotecavirtual.model.Material;
import com.np2.bibliotecavirtual.model.ProgressoLeitura;
import com.np2.bibliotecavirtual.model.SerieRecomendada;
import com.np2.bibliotecavirtual.model.StatusEmprestimo;
import com.np2.bibliotecavirtual.model.TipoMaterial;
import com.np2.bibliotecavirtual.model.Usuario;
import com.np2.bibliotecavirtual.repository.AutorRepository;
import com.np2.bibliotecavirtual.repository.AvaliacaoRepository;
import com.np2.bibliotecavirtual.repository.CategoriaRepository;
import com.np2.bibliotecavirtual.repository.EditoraRepository;
import com.np2.bibliotecavirtual.repository.EmprestimoRepository;
import com.np2.bibliotecavirtual.repository.MaterialRepository;
import com.np2.bibliotecavirtual.repository.ProgressoLeituraRepository;
import com.np2.bibliotecavirtual.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;

/**
 * Dados de demonstração (seed), carregados só com o perfil "demo":
 * <pre>./mvnw spring-boot:run -Dspring-boot.run.profiles=demo</pre>
 * <p>
 * Os livros são clássicos em domínio público do Project Gutenberg (dados obtidos pela API Gutendex,
 * gutendex.com), com o link real de leitura em urlArquivo; o número de páginas veio do Open Library.
 * Os três materiais da "Editora Escolar (exemplo)" são fictícios, para mostrar apostila, vídeo e artigo.
 * <p>
 * As datas são relativas a hoje, então sempre há empréstimos ativos, um atrasado e um histórico de
 * devoluções, avaliações e leituras concluídas (o que alimenta recomendação, ranking e conquistas).
 * Se o banco já tiver usuários ou materiais, nada é inserido.
 */
@Slf4j
@Component
@Profile("demo")
@RequiredArgsConstructor
public class DadosDemo implements ApplicationRunner {

    private static final int PRAZO_DIAS = 14;
    private static final String GUTENBERG = "https://www.gutenberg.org/ebooks/";

    private final CategoriaRepository categoriaRepository;
    private final AutorRepository autorRepository;
    private final EditoraRepository editoraRepository;
    private final MaterialRepository materialRepository;
    private final UsuarioRepository usuarioRepository;
    private final EmprestimoRepository emprestimoRepository;
    private final ProgressoLeituraRepository progressoRepository;
    private final AvaliacaoRepository avaliacaoRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (usuarioRepository.count() > 0 || materialRepository.count() > 0) {
            log.warn("Perfil demo: o banco já tem dados, então o seed não foi carregado. "
                    + "Para recomeçar do zero: docker compose down -v");
            return;
        }

        // ----- Categorias -----
        Categoria literaturaBrasileira = categoria("Literatura Brasileira",
                "Romances, contos e poesia de autores brasileiros cobrados no ENEM e nos vestibulares",
                "Linguagens", SerieRecomendada.TERCEIRO_ANO);
        Categoria literaturaPortuguesa = categoria("Literatura Portuguesa",
                "Clássicos de Portugal, do Classicismo ao Realismo", "Linguagens", SerieRecomendada.SEGUNDO_ANO);
        Categoria aventura = categoria("Aventura e Ficção Científica",
                "Clássicos de aventura e ficção científica traduzidos para o português", "Linguagens",
                SerieRecomendada.PRIMEIRO_ANO);
        Categoria matematica = categoria("Matemática", "Apostilas e videoaulas de Matemática", "Exatas",
                SerieRecomendada.PRIMEIRO_ANO);

        // ----- Editora (só para os materiais produzidos pela escola) -----
        Editora editoraEscolar = new Editora();
        editoraEscolar.setNome("Editora Escolar (exemplo)");
        editoraEscolar.setCnpj("11222333000181");
        editoraEscolar.setCidade("São Paulo");
        editoraRepository.save(editoraEscolar);

        // ----- Autores -----
        Autor machado = autor("Machado de Assis", "Brasileira", LocalDate.of(1839, 6, 21),
                "Fundador da Academia Brasileira de Letras e principal nome do Realismo no Brasil.");
        Autor aluisio = autor("Aluísio Azevedo", "Brasileira", LocalDate.of(1857, 4, 14),
                "Principal autor do Naturalismo brasileiro.");
        Autor limaBarreto = autor("Lima Barreto", "Brasileira", LocalDate.of(1881, 5, 13),
                "Romancista do Pré-Modernismo, crítico da sociedade da Primeira República.");
        Autor alencar = autor("José de Alencar", "Brasileira", LocalDate.of(1829, 5, 1),
                "Romancista do Romantismo, autor de romances indianistas e urbanos.");
        Autor oswald = autor("Oswald de Andrade", "Brasileira", LocalDate.of(1890, 1, 11),
                "Um dos líderes da Semana de Arte Moderna de 1922.");
        Autor camoes = autor("Luís de Camões", "Portuguesa", null,
                "Poeta do Classicismo português, autor de Os Lusíadas.");
        Autor eca = autor("Eça de Queirós", "Portuguesa", LocalDate.of(1845, 11, 25),
                "Principal romancista do Realismo português.");
        Autor camilo = autor("Camilo Castelo Branco", "Portuguesa", LocalDate.of(1825, 3, 16),
                "Romancista do Romantismo português.");
        Autor verne = autor("Jules Verne", "Francesa", LocalDate.of(1828, 2, 8),
                "Pioneiro da ficção científica.");
        Autor haggard = autor("H. Rider Haggard", "Inglesa", LocalDate.of(1856, 6, 22),
                "Autor de romances de aventura ambientados na África.");
        Autor equipeMatematica = autor("Equipe de Matemática", "Brasileira", null,
                "Professores de Matemática da escola.");
        Autor equipePortugues = autor("Equipe de Língua Portuguesa", "Brasileira", null,
                "Professores de Língua Portuguesa da escola.");

        // ----- Materiais (livros do Project Gutenberg: domínio público) -----
        Material domCasmurro = livro("Dom Casmurro", 1899, 269, 3, literaturaBrasileira, 55752,
                "Já velho, Bento Santiago conta a própria história: a infância com Capitu, o seminário, "
                        + "o casamento e o ciúme que o faz duvidar da mulher.", machado);
        Material memorias = livro("Memórias Póstumas de Brás Cubas", 1881, 242, 2, literaturaBrasileira, 54829,
                "Brás Cubas, um defunto autor, narra a própria vida com ironia, do nascimento à morte.", machado);
        Material quincas = livro("Quincas Borba", 1891, 270, 2, literaturaBrasileira, 55682,
                "Rubião herda a fortuna do filósofo Quincas Borba e se muda para o Rio de Janeiro, onde é "
                        + "explorado por falsos amigos.", machado);
        Material cortico = livro("O Cortiço", 1890, 254, 1, literaturaBrasileira, 69187,
                "A vida dos moradores de um cortiço no Rio de Janeiro do século XIX e a ascensão do "
                        + "português João Romão.", aluisio);
        Material policarpo = livro("Triste Fim de Policarpo Quaresma", 1915, 248, 2, literaturaBrasileira, 67535,
                "O major Policarpo Quaresma, patriota ingênuo, tenta reformar o Brasil e acaba vítima do "
                        + "próprio idealismo.", limaBarreto);
        Material iracema = livro("Iracema", 1865, 180, 3, literaturaBrasileira, 67740,
                "A lenda da índia tabajara Iracema e do português Martim, que simboliza a origem do povo "
                        + "cearense.", alencar);
        Material guarani = livro("O Guarani", 1857, 380, 2, literaturaBrasileira, 67724,
                "O índio Peri dedica a vida a proteger Cecília, filha de um fidalgo português, no Brasil do "
                        + "século XVII.", alencar);
        Material cincoMinutos = livro("Cinco Minutos", 1856, 216, 2, literaturaBrasileira, 44540,
                "Um atraso de cinco minutos muda a vida do narrador, que se apaixona por uma passageira "
                        + "misteriosa.", alencar);
        Material pauBrasil = livro("Pau-Brasil", 1925, 145, 2, literaturaBrasileira, 79326,
                "Poemas curtos e bem-humorados em busca de uma poesia brasileira, marco do Modernismo.", oswald);
        Material lusiadas = livro("Os Lusíadas", 1572, 382, 3, literaturaPortuguesa, 3333,
                "Epopeia em dez cantos sobre a viagem de Vasco da Gama às Índias e a história de Portugal.",
                camoes);
        Material osMaias = livro("Os Maias", 1888, 634, 2, literaturaPortuguesa, 40409,
                "A decadência da família Maia, em Lisboa, através do amor trágico de Carlos da Maia e "
                        + "Maria Eduarda.", eca);
        Material primoBasilio = livro("O Primo Basílio", 1878, 500, 1, literaturaPortuguesa, 42942,
                "Luísa, casada e entediada, envolve-se com o primo Basílio e passa a ser chantageada pela "
                        + "empregada Juliana.", eca);
        Material amorPerdicao = livro("Amor de Perdição", 1862, 221, 2, literaturaPortuguesa, 16425,
                "O amor proibido de Simão e Teresa, filhos de famílias inimigas.", camilo);
        Material cidadeSerras = livro("A Cidade e as Serras", 1901, 290, 2, literaturaPortuguesa, 18220,
                "Jacinto troca a vida cheia de tecnologia em Paris pela simplicidade das serras portuguesas.",
                eca);
        Material terraLua = livro("Da Terra à Lua", 1865, 308, 3, aventura, 28341,
                "Os sócios do Clube do Canhão, de Baltimore, decidem lançar um projétil tripulado até a Lua.",
                verne);
        Material minasSalomao = livro("As Minas de Salomão", 1885, 222, 2, aventura, 22015,
                "Allan Quatermain guia uma expedição pela África em busca de um homem desaparecido e das "
                        + "lendárias minas do rei Salomão.", haggard);

        // ----- Materiais da escola (fictícios, para mostrar os outros tipos) -----
        Material funcoes = material("Funções do 1º grau", TipoMaterial.APOSTILA, 2025, 40, 10, matematica,
                editoraEscolar, "https://escola.example.com/acervo/funcoes-do-1-grau.pdf",
                "Teoria e exercícios resolvidos de função afim para o 1º ano.", equipeMatematica);
        Material geometria = material("Geometria plana: áreas e perímetros", TipoMaterial.VIDEO, 2025, null, 30,
                matematica, editoraEscolar, "https://escola.example.com/acervo/geometria-plana.mp4",
                "Videoaula sobre áreas e perímetros de figuras planas.", equipeMatematica);
        material("Como ler um romance realista", TipoMaterial.ARTIGO, 2024, 12, 20, literaturaBrasileira,
                editoraEscolar, "https://escola.example.com/acervo/romance-realista.pdf",
                "Roteiro para reconhecer as marcas do Realismo nos romances cobrados no vestibular.",
                equipePortugues);
        material("Os Sertões em sala de aula", TipoMaterial.ARTIGO, 2023, 8, 20, literaturaBrasileira,
                editoraEscolar, "https://escola.example.com/acervo/os-sertoes-em-sala.pdf",
                "Guia de leitura com contexto histórico da Guerra de Canudos.", equipePortugues);

        // ----- Alunos -----
        Usuario ana = aluno("Ana Souza", "2026A0142");
        Usuario bruno = aluno("Bruno Lima", "2026A0150");
        Usuario carla = aluno("Carla Mendes", "2026A0101");
        Usuario lucas = aluno("Lucas Almeida", "2026A0117");
        Usuario julia = aluno("Júlia Rocha", "2026A0123");
        Usuario rafael = aluno("Rafael Torres", "2026A0131");
        Usuario marina = aluno("Marina Costa", "2026A0138");
        aluno("Diego Fernandes", "2026A0160"); // sem histórico: mostra as telas vazias e fica fora do ranking

        // ----- Histórico: emprestimo(aluno, material, retirado há N dias, devolvido após N dias ou null, página) -----
        // Carla: 6 leituras concluídas em 3 categorias, todas devolvidas no prazo, 5 avaliações com comentário.
        avaliar(emprestimo(carla, domCasmurro, 80, 10, 269), 5, "A dúvida sobre Capitu rende ótimas discussões em aula.");
        avaliar(emprestimo(carla, memorias, 70, 9, 242), 5, "O narrador morto é genial, muito irônico.");
        avaliar(emprestimo(carla, iracema, 60, 7, 180), 4, "Linguagem poética, vale ler em voz alta.");
        avaliar(emprestimo(carla, lusiadas, 50, 12, 382), 4, "Difícil no começo, mas ajuda muito com a história de Portugal.");
        avaliar(emprestimo(carla, terraLua, 40, 8, 308), 5, "Divertido e cheio de ciência.");
        emprestimo(carla, guarani, 30, 11, 380);
        emprestimo(carla, osMaias, 5, null, 300);

        // Lucas: 4 leituras concluídas, 3 avaliações (2 com comentário).
        avaliar(emprestimo(lucas, quincas, 75, 10, 270), 4, "Melhor ler depois de Memórias Póstumas.");
        avaliar(emprestimo(lucas, policarpo, 55, 12, 248), 5, "O major é o personagem mais marcante que já li.");
        emprestimo(lucas, cincoMinutos, 42, 6, 216);
        avaliar(emprestimo(lucas, minasSalomao, 28, 9, 222), 4, null);
        emprestimo(lucas, pauBrasil, 3, null, 60);

        // Júlia: 3 leituras concluídas, 3 avaliações com comentário.
        avaliar(emprestimo(julia, domCasmurro, 65, 12, 269), 5, "Releria fácil.");
        avaliar(emprestimo(julia, amorPerdicao, 45, 10, 221), 4, "Trágico do começo ao fim.");
        avaliar(emprestimo(julia, primoBasilio, 33, 13, 500), 3, "A empregada Juliana rouba a cena.");
        emprestimo(julia, memorias, 6, null, 100);

        // Ana: 2 leituras concluídas, uma devolução com atraso, um empréstimo ATRASADO (O Cortiço, que fica esgotado).
        avaliar(emprestimo(ana, iracema, 50, 9, 180), 5, "Linda descrição da natureza.");
        avaliar(emprestimo(ana, memorias, 38, 13, 242), 4, "Capítulos curtos, leitura rápida.");
        emprestimo(ana, lusiadas, 45, 20, 150);
        emprestimo(ana, cortico, 20, null, 210);
        emprestimo(ana, domCasmurro, 4, null, 120);

        // Bruno: 1 leitura concluída e avaliada; dois empréstimos ativos.
        avaliar(emprestimo(bruno, domCasmurro, 35, 11, 269), 5, null);
        emprestimo(bruno, quincas, 7, null, 40);
        emprestimo(bruno, funcoes, 2, null, 12);

        // Marina: 1 leitura concluída (empata com o Bruno no ranking).
        avaliar(emprestimo(marina, funcoes, 25, 8, 40), 4, null);

        // Rafael: leitura incompleta, um empréstimo ATRASADO e um vídeo.
        avaliar(emprestimo(rafael, terraLua, 30, 10, 150), 3, "Muita conta no meio da história.");
        emprestimo(rafael, policarpo, 16, null, 80);
        emprestimo(rafael, geometria, 1, null, null);

        log.info("Perfil demo: seed carregado ({} materiais, {} alunos, {} empréstimos, {} avaliações). "
                        + "Entre no front com a matrícula 2026A0142 (Ana Souza).",
                materialRepository.count(), usuarioRepository.count(), emprestimoRepository.count(),
                avaliacaoRepository.count());
    }

    private Categoria categoria(String nome, String descricao, String area, SerieRecomendada serie) {
        Categoria categoria = new Categoria();
        categoria.setNome(nome);
        categoria.setDescricao(descricao);
        categoria.setAreaConhecimento(area);
        categoria.setSerieRecomendada(serie);
        return categoriaRepository.save(categoria);
    }

    private Autor autor(String nome, String nacionalidade, LocalDate nascimento, String biografia) {
        Autor autor = new Autor();
        autor.setNome(nome);
        autor.setNacionalidade(nacionalidade);
        autor.setDataNascimento(nascimento);
        autor.setBiografia(biografia);
        return autorRepository.save(autor);
    }

    private Material livro(String titulo, int ano, int paginas, int licencas, Categoria categoria, int idGutenberg,
                           String sinopse, Autor autor) {
        return material(titulo, TipoMaterial.LIVRO, ano, paginas, licencas, categoria, null,
                GUTENBERG + idGutenberg, sinopse, autor);
    }

    private Material material(String titulo, TipoMaterial tipo, int ano, Integer paginas, int licencas,
                              Categoria categoria, Editora editora, String url, String sinopse, Autor... autores) {
        Material material = new Material();
        material.setTitulo(titulo);
        material.setTipo(tipo);
        material.setAnoPublicacao(ano);
        material.setNumeroPaginas(paginas);
        material.setQuantidadeLicencas(licencas);
        material.setCategoria(categoria);
        material.setEditora(editora);
        material.setUrlArquivo(url);
        material.setSinopse(sinopse);
        material.setAutores(Set.of(autores));
        return materialRepository.save(material);
    }

    private Usuario aluno(String nome, String matricula) {
        String primeiro = nome.split(" ")[0].toLowerCase()
                .replace("ú", "u").replace("á", "a").replace("é", "e").replace("í", "i");
        String ultimo = nome.substring(nome.lastIndexOf(' ') + 1).toLowerCase();
        Usuario usuario = new Usuario();
        usuario.setNome(nome);
        usuario.setEmail(primeiro + "." + ultimo + "@escola.example.com");
        usuario.setMatricula(matricula);
        return usuarioRepository.save(usuario);
    }

    /**
     * Cria um empréstimo com prazo de 14 dias a partir da retirada. Sem devolução, fica ATIVO ou,
     * se o prazo já passou, ATRASADO (como a RN05 faria). Com página, grava o progresso de leitura.
     */
    private Emprestimo emprestimo(Usuario usuario, Material material, int retiradoHaDias, Integer devolvidoAposDias,
                                  Integer pagina) {
        LocalDateTime retirada = LocalDate.now().minusDays(retiradoHaDias).atTime(LocalTime.of(10, 0));
        LocalDate prevista = retirada.toLocalDate().plusDays(PRAZO_DIAS);

        Emprestimo emprestimo = new Emprestimo();
        emprestimo.setUsuario(usuario);
        emprestimo.setMaterial(material);
        emprestimo.setDataRetirada(retirada);
        emprestimo.setDataPrevistaDevolucao(prevista);
        if (devolvidoAposDias != null) {
            emprestimo.setDataDevolucao(retirada.plusDays(devolvidoAposDias).withHour(16));
            emprestimo.setStatus(StatusEmprestimo.DEVOLVIDO);
        } else {
            emprestimo.setStatus(prevista.isBefore(LocalDate.now()) ? StatusEmprestimo.ATRASADO : StatusEmprestimo.ATIVO);
        }
        emprestimoRepository.save(emprestimo);

        if (pagina != null) {
            ProgressoLeitura progresso = new ProgressoLeitura();
            progresso.setEmprestimo(emprestimo);
            progresso.setPaginaAtual(pagina);
            progresso.setAtualizadoEm(emprestimo.getDataDevolucao() != null
                    ? emprestimo.getDataDevolucao().minusHours(2)
                    : LocalDateTime.now().minusHours(3));
            progressoRepository.save(progresso);
        }
        return emprestimo;
    }

    /** Avaliação feita um dia depois da devolução (ou hoje, se o material ainda está com o aluno). */
    private void avaliar(Emprestimo emprestimo, int nota, String comentario) {
        Avaliacao avaliacao = new Avaliacao();
        avaliacao.setUsuario(emprestimo.getUsuario());
        avaliacao.setMaterial(emprestimo.getMaterial());
        avaliacao.setNota(nota);
        avaliacao.setComentario(comentario);
        avaliacao.setDataAvaliacao(emprestimo.getDataDevolucao() != null
                ? emprestimo.getDataDevolucao().plusDays(1)
                : LocalDateTime.now());
        avaliacaoRepository.save(avaliacao);
    }
}
