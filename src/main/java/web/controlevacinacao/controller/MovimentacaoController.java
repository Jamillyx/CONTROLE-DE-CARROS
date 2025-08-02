package web.controlevacinacao.controller;

import java.time.LocalDate;
import java.time.LocalDateTime; // Movimentacao pode usar LocalDateTime para data/hora
import java.time.LocalTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxLocation;
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import web.controlevacinacao.filter.CarroFilter;
import web.controlevacinacao.filter.MotoristaFilter;
import web.controlevacinacao.filter.MovimentacaoFilter;
import web.controlevacinacao.model.Carro;
import web.controlevacinacao.model.Motorista;
import web.controlevacinacao.model.Movimentacao;
import web.controlevacinacao.model.Status; // Assumindo que Status é usado por Carro/Motorista
import web.controlevacinacao.model.StatusCarro;
import web.controlevacinacao.model.StatusMovimentacao; // Se Movimentacao tiver seu próprio enum de status
import web.controlevacinacao.notificacao.NotificacaoSweetAlert2;
import web.controlevacinacao.notificacao.TipoNotificaoSweetAlert2;
import web.controlevacinacao.pagination.PageWrapper;
import web.controlevacinacao.repository.CarroRepository;
import web.controlevacinacao.repository.MotoristaRepository;
import web.controlevacinacao.repository.MovimentacaoRepository;
import web.controlevacinacao.service.CarroService;
import web.controlevacinacao.service.MotoristaService;
import web.controlevacinacao.service.MovimentacaoService;

@Controller
public class MovimentacaoController { // Renomeado de AplicacaoController

    private static final Logger logger = LoggerFactory.getLogger(MovimentacaoController.class); // Log para
                                                                                                // MovimentacaoController
    private CarroRepository carroRepository; // Substitui PessoaRepository
    private MotoristaRepository motoristaRepository; // Substitui LoteRepository
    private MovimentacaoService movimentacaoService; // Substitui AplicacaoService
    private MovimentacaoRepository movimentacaoRepository; // Substitui AplicacaoRepository
    private CarroService carroService; // Adicionado se você precisar alterar Carro
    private MotoristaService motoristaService; // Adicionado se você precisar alterar Motorista

    // Construtor com injeção de dependências
    public MovimentacaoController(CarroRepository carroRepository,
            MotoristaRepository motoristaRepository,
            MovimentacaoService movimentacaoService,
            MovimentacaoRepository movimentacaoRepository,
            CarroService carroService, // Injetar CarroService
            MotoristaService motoristaService) { // Injetar MotoristaService
        this.carroRepository = carroRepository;
        this.motoristaRepository = motoristaRepository;
        this.movimentacaoService = movimentacaoService;
        this.movimentacaoRepository = movimentacaoRepository;
        this.carroService = carroService;
        this.motoristaService = motoristaService;
    }

    // --- CADASTRO DE MOVIMENTAÇÃO: FLUXO DE ESCOLHA DE CARRO E MOTORISTA ---

    @HxRequest
    @GetMapping("/movimentacoes/cadastrar") // URL para iniciar o cadastro
    public String abrirEscolhaCarro() {
        return "movimentacoes/pesquisacarro :: formulario"; // Template para pesquisar carros
    }

    @HxRequest
    @GetMapping("/movimentacoes/pesquisarcarro") // URL para a busca de carros
    public String mostrarCarrosPesquisa(CarroFilter filtro, Model model,
            @PageableDefault(size = 8) @SortDefault(sort = "codigo", direction = Sort.Direction.ASC) Pageable pageable,
            HttpServletRequest request) {
        Page<Carro> pagina = carroRepository.pesquisar(filtro, pageable); // Pesquisa carros
        logger.info("Carros pesquisados: {}", pagina);
        PageWrapper<Carro> paginaWrapper = new PageWrapper<>(pagina, request);

        model.addAttribute("pagina", paginaWrapper);
        return "movimentacoes/escolhercarro :: tabela"; // Template para exibir tabela de carros
    }

    @HxRequest
    @GetMapping("/movimentacoes/carro/{codigo}") // URL para escolher um carro
    public String abrirEscolhaMotorista(@PathVariable("codigo") Long codigo,
            Model model, HttpSession sessao) {
        Carro carro = carroRepository.findByCodigoAndStatusCarro(codigo, StatusCarro.ATIVO); // Buscar carro ativo
        if (carro != null) {

            Movimentacao movimentacao = new Movimentacao();
            movimentacao.setKmSaida(carro.getKmAtual());
            movimentacao.setCarro(carro); // Define o carro na nova movimentação
            sessao.setAttribute("movimentacao", movimentacao); // Armazena na sessão
            // model.addAttribute("movimentacao", movimentacao);
            return "movimentacoes/pesquisamotorista :: formulario"; // Template para pesquisar motoristas
        } else {
            model.addAttribute("mensagem", "Não existe um carro com esse código ou ele não está ativo.");
            return "mensagem :: texto";
        }
    }

    @HxRequest
    @GetMapping("/movimentacoes/pesquisarmotorista") // URL para a busca de motoristas
    public String mostrarMotoristasPesquisa(MotoristaFilter filtro, Model model,
            @PageableDefault(size = 8) @SortDefault(sort = "codigo", direction = Sort.Direction.ASC) Pageable pageable,
            HttpServletRequest request) {
        Page<Motorista> pagina = motoristaRepository.pesquisar(filtro, pageable); // Pesquisa motoristas
        logger.info("Motoristas pesquisados: {}", pagina);
        PageWrapper<Motorista> paginaWrapper = new PageWrapper<>(pagina, request);
        model.addAttribute("pagina", paginaWrapper);
        return "movimentacoes/escolhermotorista :: tabela"; // Template para exibir tabela de motoristas
    }

    @HxRequest
    @GetMapping("/movimentacoes/motorista/{codigo}") // URL para escolher um motorista
    public String abrirCadastro(@PathVariable("codigo") Long codigo,
            Model model, HttpSession sessao) {
        Motorista motorista = motoristaRepository.findByCodigoAndStatus(codigo, Status.ATIVO); // Buscar motorista ativo
        if (motorista != null) {
            Movimentacao movimentacao = (Movimentacao) sessao.getAttribute("movimentacao"); // Pega movimentação da
                                                                                            // sessão

            movimentacao.setMotorista(motorista);
            movimentacao.setDataSaida(LocalDate.now());
            movimentacao.setHoraSaida(LocalTime.now());
            
            sessao.setAttribute("movimentacao", movimentacao); // Atualiza na sessão
            model.addAttribute("movimentacao", movimentacao);

            return "movimentacoes/cadastrar :: formulario"; // Template para o formulário final de cadastro da
                                                            // movimentação
        } else {
            model.addAttribute("mensagem", "Não existe um motorista com esse código ou ele não está ativo.");
            return "mensagem :: texto";
        }
    }

    @HxRequest
    @PostMapping("/movimentacoes/cadastrar") // URL para processar o cadastro
    public String cadastrar(@Valid Movimentacao movimentacao, // @Valid para validação
            BindingResult resultado,
            RedirectAttributes attributes, HttpSession sessao) {

        if (resultado.hasErrors()) {
            logger.debug("{}", movimentacao);
            logger.debug("{}", movimentacao.getMotorista());
            logger.debug("{}", movimentacao.getCarro());
            logger.info("A movimentacao recebida para cadastrar não é válida.");
            logger.info("Erros encontrados:");
            for (FieldError erro : resultado.getFieldErrors()) {
                logger.info("{}", erro);
            }
            for (ObjectError erro : resultado.getGlobalErrors()) {
                logger.info("{}", erro);
            }
            return "movimentacoes/cadastrar :: formulario"; // Retorna o formulário com erros
        } else {
            Movimentacao movimentacaoSalva = (Movimentacao) sessao.getAttribute("movimentacao");
            movimentacao.setDataSaida(LocalDate.now());
            movimentacao.setHoraSaida(LocalTime.now());
            movimentacaoSalva.setStatusMovimentacao(StatusMovimentacao.ATIVA);
            Motorista motorista = movimentacaoSalva.getMotorista();
            motorista.setStatus(Status.EM_MOVIMENTACAO);
            motoristaService.alterar(motorista, resultado);
            Carro carro = movimentacaoSalva.getCarro();
            carro.setStatusCarro(StatusCarro.EM_USO);
            carroService.alterar(carro, resultado);
            movimentacaoService.salvar(movimentacaoSalva); // Salva a movimentação

            sessao.removeAttribute("movimentacao"); // Limpa a sessão após o cadastro

            attributes.addFlashAttribute("notificacao",
                    new NotificacaoSweetAlert2("Movimentação cadastrada com sucesso!",
                            TipoNotificaoSweetAlert2.SUCCESS, 4000));

            return "redirect:/movimentacoes/cadastrar"; // Redireciona para iniciar um novo cadastro
        }
    }

    // --- PESQUISA E LISTAGEM DE MOVIMENTAÇÕES ---

    @HxRequest
    @GetMapping("/movimentacoes/abrirpesquisa") // URL para abrir a tela de pesquisa
    public String abrirPesquisaMovimentacao() {
        return "movimentacoes/pesquisar :: formulario"; // Template para formulário de pesquisa
    }

    @HxRequest
    @GetMapping("/movimentacoes/pesquisar") // URL para a busca paginada
    public String pesquisar(MovimentacaoFilter filtro, Model model,
            @PageableDefault(size = 8) @SortDefault(sort = "codigo", direction = Sort.Direction.ASC) Pageable pageable,
            HttpServletRequest request) {
        Page<Movimentacao> pagina = movimentacaoRepository.pesquisar(filtro, pageable); // Executa a pesquisa
        logger.info("Movimentações pesquisadas: {}", pagina);
        PageWrapper<Movimentacao> paginaWrapper = new PageWrapper<>(pagina, request);
        model.addAttribute("pagina", paginaWrapper);
        return "movimentacoes/listar :: tabela"; // Template para listar resultados
    }

    // --- REMOÇÃO DE MOVIMENTAÇÃO ---

    @HxRequest
    @HxLocation(path = "/mensagem", target = "#main", swap = "outerHTML") // HTMX para redirecionar e mostrar mensagem
    @GetMapping("/movimentacoes/remover/{codigo}") // URL para remover
    public String remover(@PathVariable("codigo") Long codigo, RedirectAttributes attributes) {
        movimentacaoService.remover(codigo); // Remove a movimentação
        attributes.addFlashAttribute("notificacao", new NotificacaoSweetAlert2("Movimentação removida com sucesso!",
                TipoNotificaoSweetAlert2.SUCCESS, 4000));
        return "redirect:/movimentacoes/abrirpesquisa"; // Redireciona após remover
    }

    // --- ALTERAÇÃO DE MOVIMENTAÇÃO ---

    @HxRequest
    @GetMapping("/movimentacoes/alterar/{codigo}") // URL para abrir formulário de alteração
    public String abrirAlterar(@PathVariable("codigo") Long codigo, Model model,
            HttpSession sessao) {
        Movimentacao movimentacao = movimentacaoRepository.buscarCompletoCodigo(codigo); // Busca a movimentação
                                                                                         // completa
        if (movimentacao != null) {
            sessao.setAttribute("movimentacao", movimentacao); // Armazena na sessão
            model.addAttribute("movimentacao", movimentacao);
            return "movimentacoes/alterar :: formulario"; // Template para formulário de alteração
        } else {
            model.addAttribute("mensagem", "Não existe uma movimentação com esse código.");
            return "mensagem :: texto";
        }
    }

    @HxRequest
    @PostMapping("/movimentacoes/alterar") // URL para processar a alteração
    public String alterar(@Valid Movimentacao movimentacao, BindingResult resultado,
            RedirectAttributes redirectAttributes, HttpSession sessao) {
        if (resultado.hasErrors()) {
            logger.info("A Movimentação recebida para alterar não é válida.");
            for (FieldError erro : resultado.getFieldErrors()) {
                logger.info("Erro de campo: {}", erro);
            }
            for (ObjectError erro : resultado.getGlobalErrors()) {
                logger.info("Erro global: {}", erro);
            }
            return "movimentacoes/alterar :: formulario"; // Retorna o formulário com erros
        } else {
            Movimentacao salva = (Movimentacao) sessao.getAttribute("movimentacao"); // Pega da sessão
            if (salva == null) {
                redirectAttributes.addFlashAttribute("notificacao", new NotificacaoSweetAlert2(
                        "Erro: Sessão de alteração expirada.", TipoNotificaoSweetAlert2.ERROR, 4000));
                return "redirect:/movimentacoes/abrirpesquisa"; // Redireciona para pesquisa
            }
            // Atualiza apenas os campos que podem ser alterados pelo formulário.
            // Cuidado para não sobrescrever o Carro/Motorista se eles não forem alterados
            // no formulário de alteração.
            salva.setDataSaida(movimentacao.getDataSaida());
            salva.setDataRetorno(movimentacao.getDataRetorno());
            // Atualize outros campos se necessário

            movimentacaoService.alterar(salva); // Salva as alterações
            sessao.removeAttribute("movimentacao"); // Limpa a sessão

            redirectAttributes.addFlashAttribute("notificacao",
                    new NotificacaoSweetAlert2("Movimentação alterada com sucesso!",
                            TipoNotificaoSweetAlert2.SUCCESS, 4000));

            return "redirect:/movimentacoes/abrirpesquisa"; // Redireciona para a tela de pesquisa
        }
    }

    // --- MÉTODOS ADICIONAIS (Se precisar de um para "finalizar" movimentação, por
    // exemplo) ---
  
    @HxRequest
    @HxLocation(path = "/mensagem", target = "#main", swap = "outerHTML")
    @GetMapping("/movimentacoes/finalizar/{codigo}")
    public String finalizarMovimentacao(@PathVariable("codigo") Long codigo, RedirectAttributes attributes) {
        try {
            Movimentacao movimentacao = movimentacaoRepository.buscarCompletoCodigo(codigo);
            if (movimentacao == null) {
                attributes.addFlashAttribute("notificacao", new NotificacaoSweetAlert2("Movimentação não encontrada!",
                        TipoNotificaoSweetAlert2.ERROR, 4000));
            } else {
                movimentacao.setStatusMovimentacao(StatusMovimentacao.FINALIZADA); // Altere para o status de finalizada
                movimentacao.setDataRetorno(LocalDate.now()); // Defina a data/hora de retorno
                movimentacao.setHoraRetorno(LocalTime.now());
                movimentacaoService.alterar(movimentacao); // Salva a alteração de status e data de retorno

                attributes.addFlashAttribute("notificacao", new NotificacaoSweetAlert2(
                        "Movimentação finalizada com sucesso!", TipoNotificaoSweetAlert2.SUCCESS, 4000));
            }
        } catch (Exception e) {
            logger.error("Erro ao finalizar movimentação {}: {}", codigo, e.getMessage());
            attributes.addFlashAttribute("notificacao", new NotificacaoSweetAlert2("Erro ao finalizar movimentação!",
                    TipoNotificaoSweetAlert2.ERROR, 4000));
        }
        return "redirect:/movimentacoes/abrirpesquisa";
    }
}