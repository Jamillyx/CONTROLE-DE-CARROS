package web.controlevacinacao.controller;

import java.time.LocalDate;

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
import web.controlevacinacao.filter.AplicacaoFilter;
import web.controlevacinacao.filter.LoteFilter;
import web.controlevacinacao.filter.PessoaFilter;
import web.controlevacinacao.model.Aplicacao;
import web.controlevacinacao.model.Lote;
import web.controlevacinacao.model.Pessoa;
import web.controlevacinacao.model.Status;
import web.controlevacinacao.notificacao.NotificacaoSweetAlert2;
import web.controlevacinacao.notificacao.TipoNotificaoSweetAlert2;
import web.controlevacinacao.pagination.PageWrapper;
import web.controlevacinacao.repository.AplicacaoRepository;
import web.controlevacinacao.repository.LoteRepository;
import web.controlevacinacao.repository.PessoaRepository;
import web.controlevacinacao.service.AplicacaoService;
import web.controlevacinacao.service.LoteService;

@Controller
public class AplicacaoController {

    private static final Logger logger = LoggerFactory.getLogger(AplicacaoController.class);
    private PessoaRepository pessoaRepository;
    private LoteRepository loteRepository;
    private LoteService loteService;
    private AplicacaoService aplicacaoService;
    private AplicacaoRepository aplicacaoRepository;

    public AplicacaoController(PessoaRepository pessoaRepository,
            LoteRepository loteRepository, 
            LoteService loteService,
            AplicacaoService aplicacaoService,
            AplicacaoRepository aplicacaoRepository) {
        this.pessoaRepository = pessoaRepository;
        this.loteRepository = loteRepository;
        this.loteService = loteService;
        this.aplicacaoService = aplicacaoService;
        this.aplicacaoRepository = aplicacaoRepository;
    }

    @HxRequest
    @GetMapping("/aplicacoes/cadastrar")
    public String abrirEscolhaPessoa() {
        return "aplicacoes/pesquisapessoa :: formulario";
    }

    @HxRequest
    @GetMapping("/aplicacoes/pesquisarpessoa")
    public String mostrarPessoasPesquisa(PessoaFilter filtro, Model model,
            @PageableDefault(size = 8) @SortDefault(sort = "codigo", direction = Sort.Direction.ASC) Pageable pageable,
            HttpServletRequest request) {
        Page<Pessoa> pagina = pessoaRepository.pesquisar(filtro, pageable);
        logger.info("Pessoas pesquisadas: {}", pagina);
        PageWrapper<Pessoa> paginaWrapper = new PageWrapper<>(pagina, request);
        model.addAttribute("pagina", paginaWrapper);
        return "aplicacoes/escolherpessoa :: tabela";
    }

    @HxRequest
    @GetMapping("/aplicacoes/pessoa/{codigo}")
    public String abrirEscolhaLote(@PathVariable("codigo") Long codigo,
            Model model, HttpSession sessao) {
        Pessoa pessoa = pessoaRepository.findByCodigoAndStatus(codigo, Status.ATIVO);
        if (pessoa != null) {
            Aplicacao aplicacao = new Aplicacao();
            aplicacao.setPessoa(pessoa);
            sessao.setAttribute("aplicacao", aplicacao);
            return "aplicacoes/pesquisalote :: formulario";
        } else {
            model.addAttribute("mensagem", "Não existe uma vacina com esse código");
            return "mensagem :: texto";
        }
    }

    @HxRequest
    @GetMapping("/aplicacoes/pesquisarlote")
    public String mostrarLotesPesquisa(LoteFilter filtro, Model model,
            @PageableDefault(size = 8) @SortDefault(sort = "codigo", direction = Sort.Direction.ASC) Pageable pageable,
            HttpServletRequest request) {
        Page<Lote> pagina = loteRepository.pesquisar(filtro, pageable, true);
        logger.info("Lotes pesquisados: {}", pagina);
        PageWrapper<Lote> paginaWrapper = new PageWrapper<>(pagina, request);
        model.addAttribute("pagina", paginaWrapper);
        return "aplicacoes/escolherlote :: tabela";
    }

    @HxRequest
    @GetMapping("/aplicacoes/lote/{codigo}")
    public String abrirCadastro(@PathVariable("codigo") Long codigo,
            Model model, HttpSession sessao) {
        Lote lote = loteRepository.findByCodigoAndStatus(codigo, Status.ATIVO);
        if (lote != null) {
            Aplicacao aplicacao = (Aplicacao) sessao.getAttribute("aplicacao");
            aplicacao.setLote(lote);
            aplicacao.setData(LocalDate.now());
            sessao.setAttribute("aplicacao", aplicacao);
            model.addAttribute("aplicacao", aplicacao);
            return "aplicacoes/cadastrar :: formulario";
        } else {
            model.addAttribute("mensagem", "Não existe um lote com esse código");
            return "mensagem :: texto";
        }
    }

    @HxRequest
    @PostMapping("/aplicacoes/cadastrar")
    public String cadastrar(@Valid Aplicacao aplicacao,
            BindingResult resultado,
            RedirectAttributes attributes, HttpSession sessao) {
        if (resultado.hasErrors()) {
            logger.debug("{}", aplicacao);
            logger.debug("{}", aplicacao.getLote());
            logger.debug("{}", aplicacao.getPessoa());
            logger.info("A aplicacao recebida para cadastrar não é válida.");
            logger.info("Erros encontrados:");
            for (FieldError erro : resultado.getFieldErrors()) {
                logger.info("{}", erro);
            }
            for (ObjectError erro : resultado.getGlobalErrors()) {
                logger.info("{}", erro);
            }
            return "aplicacoes/cadastrar :: formulario";
        } else {
            Aplicacao aplicacaoSalva = (Aplicacao) sessao.getAttribute("aplicacao");
            aplicacaoSalva.setData(aplicacao.getData());

            Lote lote = aplicacaoSalva.getLote();
            lote.setNroDosesAtual(lote.getNroDosesAtual() - 1);
            loteService.alterar(lote);

            aplicacaoService.salvar(aplicacaoSalva);

            sessao.removeAttribute("aplicacao");

            attributes.addFlashAttribute("notificacao",
                    new NotificacaoSweetAlert2("Aplicação cadastrada com sucesso!",
                            TipoNotificaoSweetAlert2.SUCCESS, 4000));

            return "redirect:/aplicacoes/cadastrar";
        }
    }

    @HxRequest
    @GetMapping("/aplicacoes/abrirpesquisa")
    public String abrirPesquisaString() {
        return "aplicacoes/pesquisar :: formulario";
    }

    @HxRequest
    @GetMapping("/aplicacoes/pesquisar")
    public String pesquisar(AplicacaoFilter filtro, Model model,
            @PageableDefault(size = 8) @SortDefault(sort = "codigo", direction = Sort.Direction.ASC) Pageable pageable,
            HttpServletRequest request) {
        Page<Aplicacao> pagina = aplicacaoRepository.pesquisar(filtro, pageable);
        logger.info("Aplicacoes pesquisadas: {}", pagina);
        PageWrapper<Aplicacao> paginaWrapper = new PageWrapper<>(pagina, request);
        model.addAttribute("pagina", paginaWrapper);
        return "aplicacoes/listar :: tabela";
    }

    @HxRequest
    @HxLocation(path = "/mensagem", target = "#main", swap = "outerHTML")
    @GetMapping("/aplicacoes/remover/{codigo}")
    public String remover(@PathVariable("codigo") Long codigo, RedirectAttributes attributes) {
        aplicacaoService.remover(codigo);
        attributes.addFlashAttribute("notificacao", new NotificacaoSweetAlert2("Aplicação removida com sucesso!", TipoNotificaoSweetAlert2.SUCCESS, 4000));
        return "redirect:/aplicacoes/abrirpesquisa";
    }

    @HxRequest
    @GetMapping("/aplicacoes/alterar/{codigo}")
    public String abrirAlterar(@PathVariable("codigo") Long codigo, Model model,
             HttpSession sessao) {
        Aplicacao aplicacao = aplicacaoRepository.buscarCompletoCodigo(codigo);
        if (aplicacao != null) {
            sessao.setAttribute("aplicacao", aplicacao);
            model.addAttribute("aplicacao", aplicacao);
            return "aplicacoes/alterar :: formulario";
        } else {
            model.addAttribute("mensagem", "Não existe uma aplicação com esse código");
            return "mensagem :: texto";
        }
    }

    @HxRequest
    @PostMapping("/aplicacoes/alterar")
    public String alterar(@Valid Aplicacao aplicacao, BindingResult resultado,
            RedirectAttributes redirectAttributes, HttpSession sessao) {
        if (resultado.hasErrors()) {
            logger.info("A Aplicação recebida para alterar não é válida.");
            logger.info("Erros encontrados:");
            for (FieldError erro : resultado.getFieldErrors()) {
                logger.info("{}", erro);
            }
            for (ObjectError erro : resultado.getGlobalErrors()) {
                logger.info("{}", erro);
            }
            return "aplicacoes/alterar :: formulario";
        } else {
            Aplicacao salva = (Aplicacao) sessao.getAttribute("aplicacao");
            salva.setData(aplicacao.getData());
            aplicacaoService.alterar(salva);
            sessao.removeAttribute("aplicacao");

            redirectAttributes.addFlashAttribute("notificacao",
                    new NotificacaoSweetAlert2("Aplicação alterada com sucesso!",
                            TipoNotificaoSweetAlert2.SUCCESS, 4000));

            return "redirect:/aplicacoes/abrirpesquisa";
        }
    }
}
