// web.controlevacinacao.controller/MotoristaController.java
package web.controlevacinacao.controller;

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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxLocation;
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import web.controlevacinacao.filter.MotoristaFilter;
import web.controlevacinacao.model.Motorista;
import web.controlevacinacao.notificacao.NotificacaoSweetAlert2;
import web.controlevacinacao.notificacao.TipoNotificaoSweetAlert2;
import web.controlevacinacao.pagination.PageWrapper;
import web.controlevacinacao.repository.MotoristaRepository;
import web.controlevacinacao.repository.queries.motorista.MotoristaQueries;
import web.controlevacinacao.service.MotoristaService;

@Controller
@RequestMapping("/motoristas")
public class MotoristaController {

    private static final Logger logger = LoggerFactory.getLogger(MotoristaController.class);
    private final MotoristaRepository motoristaRepository;
    private final MotoristaService motoristaService;

    public MotoristaController(MotoristaRepository motoristaRepository, MotoristaService motoristaService) {
        this.motoristaRepository = motoristaRepository;
        this.motoristaService = motoristaService;
    }

    @HxRequest
    @GetMapping("/abrirpesquisa")
    public String abrirPesquisaHTMX(MotoristaFilter filtro, Model model) {
        model.addAttribute("filtro", filtro);
        return "motoristas/pesquisar :: formulario";
    }

    @HxRequest
    @GetMapping("/pesquisar")
    public String mostrarMotoristasPesquisaHTMX(MotoristaFilter filtro, Model model,
            @PageableDefault(size = 8) @SortDefault(sort = "codigo", direction = Sort.Direction.ASC) Pageable pageable,
            HttpServletRequest request) {
        logger.info("Pesquisando motoristas com filtro: {}", filtro);

        Page<Motorista> pagina = ((MotoristaQueries) motoristaRepository).pesquisar(filtro, pageable);

        logger.info("Motoristas encontrados na página: {}", pagina.getNumberOfElements());
        PageWrapper<Motorista> paginaWrapper = new PageWrapper<>(pagina, request);

        model.addAttribute("pagina", paginaWrapper);
        model.addAttribute("filtro", filtro);

        return "motoristas/listar :: tabela";
    }

    @HxRequest
    @GetMapping("/cadastrar")
    public String abrirCadastroHTMX(Motorista motorista) {
        return "motoristas/cadastrar :: formulario";
    }

    @HxRequest
    @PostMapping("/cadastrar")
    public String cadastrarHTMX(@Valid Motorista motorista,
            BindingResult resultado,
            Model model) { // Remova RedirectAttributes, use Model para notificações HTMX

        // 1. Validação inicial @Valid (anotações de validação na entidade)
        if (resultado.hasErrors()) {
            logger.warn("Erros de validação @Valid no cadastro de motorista.");
            resultado.getAllErrors().forEach(error -> logger.warn("  - {}", error));
            return "motoristas/cadastrar :: formulario";
        }

        // 2. Chamar o serviço para validação de negócio (CNH e CPF duplicados)
        // Não use try-catch AQUI. O serviço adicionará os erros ao 'resultado'.
        motoristaService.salvar(motorista, resultado);

        // 3. Verifica NOVAMENTE se há erros no BindingResult (incluindo os adicionados
        // pelo serviço)
        if (resultado.hasErrors()) {
            logger.warn("Erros de validação de negócio (CNH/CPF duplicado) no cadastro de motorista.");
            resultado.getAllErrors().forEach(error -> logger.warn("  - {}", error));
            // Adiciona uma notificação de erro geral, caso haja múltiplos erros de campo
            model.addAttribute("notificacao",
                    new NotificacaoSweetAlert2("Verifique os campos com erro.", TipoNotificaoSweetAlert2.ERROR, 5000));
            return "motoristas/cadastrar :: formulario"; // Retorna o fragmento com os erros específicos
        }

        // Se chegou até aqui, significa que não houve erros e o motorista foi salvo com
        // sucesso
        model.addAttribute("notificacao",
                new NotificacaoSweetAlert2("Motorista cadastrado com sucesso!",
                        TipoNotificaoSweetAlert2.SUCCESS, 4000));

        // Limpa o formulário criando um novo objeto Motorista vazio no modelo
        model.addAttribute("motorista", new Motorista());

        return "motoristas/cadastrar :: formulario";
    }

    @HxRequest
    @GetMapping("/alterar/{codigo}")
    public String abrirAlterarHTMX(@PathVariable("codigo") Long codigo, Model model) {
        Motorista motorista = motoristaRepository.findById(codigo).orElse(null);

        if (motorista != null) {
            model.addAttribute("motorista", motorista);
            return "motoristas/alterar :: formulario";
        } else {
            logger.warn("Motorista com código {} não encontrado ou inativo para alteração.", codigo);
            // Notificação direta via Model para HTMX
            model.addAttribute("notificacao",
                    new NotificacaoSweetAlert2("Motorista não encontrado ou inativo.", TipoNotificaoSweetAlert2.WARNING,
                            4000));
            // Retorna um fragmento vazio ou um redirecionamento HTMX se for para outra tela
            return "motoristas/pesquisar :: formulario"; // Ou redirecionar HTMX para a tela de pesquisa
        }
    }

    @HxRequest
    @PostMapping("/alterar")
    public String alterarHTMX(@Valid Motorista motorista, BindingResult resultado,
            Model model) { // Remova RedirectAttributes, use Model para notificações HTMX

        if (resultado.hasErrors()) {
            logger.warn("Erros de validação @Valid na alteração de motorista.");
            resultado.getAllErrors().forEach(error -> logger.warn("  - {}", error));
            return "motoristas/alterar :: formulario";
        }

        // Chamar o serviço para validação de negócio
        // Não use try-catch AQUI. O serviço adicionará os erros ao 'resultado'.
        motoristaService.alterar(motorista, resultado);

        if (resultado.hasErrors()) {
            logger.warn("Erros de validação de negócio (CNH/CPF duplicado) na alteração de motorista.");
            resultado.getAllErrors().forEach(error -> logger.warn("  - {}", error));
            // Adiciona uma notificação de erro geral
            model.addAttribute("notificacao",
                    new NotificacaoSweetAlert2("Verifique os campos com erro.", TipoNotificaoSweetAlert2.ERROR, 5000));
            return "motoristas/alterar :: formulario";
        }

        model.addAttribute("notificacao",
                new NotificacaoSweetAlert2("Motorista alterado com sucesso!",
                        TipoNotificaoSweetAlert2.SUCCESS, 4000));

        // Na alteração, geralmente você não "limpa" o formulário, mas o mantém com os
        // dados alterados
        // Se a intenção é redirecionar, use HxLocation.
        model.addAttribute("motorista", new Motorista());
        return "motoristas/alterar :: formulario";
    }

    @HxRequest
    @HxLocation(path = "/motoristas/abrirpesquisa", target = "#main", swap = "outerHTML")
    @GetMapping("/remover/{codigo}")
    public String removerHTMX(@PathVariable("codigo") Long codigo, RedirectAttributes attributes) {
        try {
            motoristaService.remover(codigo);
            // Para HTMX, adicionar notificação ao Model é geralmente preferível,
            // ou deixar o HxLocation lidar com o redirecionamento.
            // Se o HxLocation for para outra tela, o FlashAttribute funciona.
            attributes.addFlashAttribute("notificacao",
                    new NotificacaoSweetAlert2("Motorista removido com sucesso!", TipoNotificaoSweetAlert2.SUCCESS,
                            4000));
        } catch (Exception e) {
            logger.error("Erro ao remover motorista com código {}: {}", codigo, e.getMessage(), e);
            attributes.addFlashAttribute("notificacao",
                    new NotificacaoSweetAlert2("Erro ao remover motorista: " + e.getMessage(),
                            TipoNotificaoSweetAlert2.ERROR, 5000));
        }
        return ""; // HTMX irá processar o HxLocation, ou o redirect normal funcionaria
    }
}