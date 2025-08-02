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
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes; // <-- MANTENHA este import para o @HxLocation

import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxLocation;
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import web.controlevacinacao.filter.CarroFilter;
import web.controlevacinacao.model.Carro;
import web.controlevacinacao.model.StatusCarro;
import web.controlevacinacao.notificacao.NotificacaoSweetAlert2;
import web.controlevacinacao.notificacao.TipoNotificaoSweetAlert2;
import web.controlevacinacao.pagination.PageWrapper;
import web.controlevacinacao.repository.CarroRepository;
import web.controlevacinacao.service.CarroService;

@Controller
public class CarroController {

    private static final Logger logger = LoggerFactory.getLogger(CarroController.class); // Ajuste aqui
    private final CarroRepository carroRepository;
    private final CarroService carroService;

    // Construtor com injeção de dependências
    public CarroController(CarroRepository carroRepository, CarroService carroService) {
        this.carroRepository = carroRepository;
        this.carroService = carroService;
    }

    @HxRequest
    @GetMapping("/carros/abrirpesquisa")
    public String abrirPesquisaHTMX() {
        return "carros/pesquisar :: formulario";
    }

    @HxRequest
    @GetMapping("/carros/pesquisar")
    public String mostrarCarrosPesquisaHTMX(CarroFilter filtro, Model model,
            @PageableDefault(size = 8) @SortDefault(sort = "codigo", direction = Sort.Direction.ASC) Pageable pageable,
            HttpServletRequest request) {
        Page<Carro> pagina = carroRepository.pesquisar(filtro, pageable);
        logger.info("Carros pesquisados: {}", pagina);
        PageWrapper<Carro> paginaWrapper = new PageWrapper<>(pagina, request);
        model.addAttribute("pagina", paginaWrapper);
        return "carros/listar :: tabela";
    }

    @HxRequest
    @GetMapping("/carros/cadastrar")
    public String abrirCadastroHTMX(Carro carro) { // Carro carro já cria um objeto vazio se não houver um no model
        return "carros/cadastrar :: formulario";
    }

    // --- MÉTODO CADASTRAR HTMX ATUALIZADO ---
    @HxRequest
    @PostMapping("/carros/cadastrar")
    public String cadastrarHTMX(@Valid Carro carro,
            BindingResult resultado,
            Model model) { // Usamos Model para passar atributos diretamente ao fragmento

        // 1. Verificação inicial de erros de validação (@Valid)
        if (resultado.hasErrors()) {
            logger.info("O Carro recebido para cadastrar não é válido (erros @Valid).");
            // Logar erros detalhados para depuração
            for (FieldError erro : resultado.getFieldErrors()) {
                logger.info("Campo: '{}', Erro: '{}'", erro.getField(), erro.getDefaultMessage());
            }
            for (ObjectError erro : resultado.getGlobalErrors()) {
                logger.info("Erro global: '{}'", erro.getDefaultMessage());
            }
            return "carros/cadastrar :: formulario"; // Retorna o fragmento para exibir os erros
        }

        // 2. Chama o serviço para validação de negócio (placa duplicada) e tentativa de
        // salvar
        try {
            carroService.salvar(carro, resultado); // Passa o BindingResult para o serviço
        } catch (Exception e) { // Captura qualquer exceção inesperada durante o salvamento
            logger.error("Erro inesperado ao salvar carro: {}", e.getMessage(), e);
            // Adiciona um erro genérico ao BindingResult para ser exibido no formulário
            resultado.rejectValue("placa", "erro.inesperado",
                    "Ocorreu um erro inesperado ao salvar o carro. Tente novamente.");
        }

        // 3. Verifica NOVAMENTE se há erros no BindingResult (incluindo os adicionados
        // pelo serviço)
        if (resultado.hasErrors()) {
            logger.info("Erros de validação de negócio encontrados após a execução do serviço.");
            for (FieldError erro : resultado.getFieldErrors()) {
                logger.info("Campo: '{}', Erro: '{}'", erro.getField(), erro.getDefaultMessage());
            }
            return "carros/cadastrar :: formulario"; // Retorna o fragmento com os erros do serviço
        }

        // Se chegou até aqui, significa que não houve erros e o carro foi salvo com
        // sucesso
        model.addAttribute("notificacao",
                new NotificacaoSweetAlert2("Carro cadastrado com sucesso!",
                        TipoNotificaoSweetAlert2.SUCCESS, 4000));

        model.addAttribute("carro", new Carro());

        return "carros/cadastrar :: formulario"; // Retorna o formulário atualizado com mensagem de sucesso e limpo
    }

    @HxRequest
    @GetMapping("/carros/alterar/{codigo}")
    public String abrirAlterarHTMX(@PathVariable("codigo") Long codigo, Model model) {
        Carro carro = carroRepository.findByCodigoAndStatusCarro(codigo, StatusCarro.ATIVO);
        if (carro != null) {
            model.addAttribute("carro", carro);
            return "carros/alterar :: formulario";
        } else {
            model.addAttribute("notificacao",
                    new NotificacaoSweetAlert2("Não existe um carro ativo com esse código para alteração.",
                            TipoNotificaoSweetAlert2.ERROR, 5000));
            // O retorno aqui deve ser para uma página ou fragmento que possa exibir a
            // notificação.
            // Se você quer que exiba na própria página de alteração (vazia ou com outro
            // conteúdo),
            // ou redirecione para a pesquisa.
            return "carros/cadastrar :: formulario"; // Ou o fragmento que você usa para exibir mensagens globais
        }
    }

    // --- MÉTODO ALTERAR HTMX ATUALIZADO ---
    @HxRequest
    @PostMapping("/carros/alterar")
    public String alterarHTMX(@Valid Carro carro,
            BindingResult resultado,
            Model model) { // <-- Volte a usar Model aqui para sucesso no HTMX

        // 1. Verificação inicial de erros de validação (@Valid)
        if (resultado.hasErrors()) {
            logger.info("O Carro recebido para alterar não é válido (erros @Valid).");
            for (FieldError erro : resultado.getFieldErrors()) {
                logger.info("Campo: '{}', Erro: '{}'", erro.getField(), erro.getDefaultMessage());
            }
            for (ObjectError erro : resultado.getGlobalErrors()) {
                logger.info("Erro global: '{}'", erro.getDefaultMessage());
            }
            // Se houver erros, retorna o fragmento com os dados do formulário preenchidos
            // (com erros)
            model.addAttribute("carro", carro); // Re-adiciona o carro ao modelo para manter os dados no formulário
            return "carros/alterar :: formulario"; // Retorna o fragmento para exibir os erros
        }

        // 2. Chama o serviço para validação de negócio (placa duplicada) e tentativa de
        // salvar
        try {
            carroService.alterar(carro, resultado); // <-- CHAME COM BindingResult!
        } catch (Exception e) {
            logger.error("Erro inesperado ao salvar carro: {}", e.getMessage(), e);
            resultado.rejectValue("placa", "erro.inesperado",
                    "Ocorreu um erro inesperado ao alterar o carro. Tente novamente.");
        }

        // 3. Verifica NOVAMENTE se há erros no BindingResult (incluindo os adicionados
        // pelo serviço)
        if (resultado.hasErrors()) {
            logger.info("Erros de validação de negócio encontrados após a execução do serviço.");
            for (FieldError erro : resultado.getFieldErrors()) {
                logger.info("Campo: '{}', Erro: '{}'", erro.getField(), erro.getDefaultMessage());
            }
            // Se houver erros do serviço, retorna o fragmento com os dados do formulário
            // preenchidos (com erros)
            model.addAttribute("carro", carro); // Re-adiciona o carro ao modelo para manter os dados no formulário
            return "carros/alterar :: formulario"; // Retorna o fragmento com os erros do serviço
        }

        // SE SUCESSO:
        logger.info("Carro com código {} alterado com sucesso!", carro.getCodigo());

        // Adiciona a notificação ao Model
        model.addAttribute("notificacao",
                new NotificacaoSweetAlert2("Carro alterado com sucesso!",
                        TipoNotificaoSweetAlert2.SUCCESS, 4000));

        // IMPORTANTE PARA A ALTERAÇÃO: Re-adicione o carro (AGORA ATUALIZADO) ao modelo
        // para que o formulário seja renderizado com os dados mais recentes.
        // Isso é o "recarregamento" que você quer ver, mas sem limpar o formulário.
        model.addAttribute("carro", new Carro());
        return "carros/alterar :: formulario"; // Retorna o fragmento do formulário ATUALIZADO
    }

    // --- MÉTODO REMOVER ATUALIZADO PARA HTMX (COM RedirectAttributes para
    // @HxLocation) ---
    @HxRequest
    @HxLocation(path = "/carros/abrirpesquisa", target = "#main", swap = "outerHTML")
    @GetMapping("/carros/remover/{codigo}")
    public String removerHTMX(@PathVariable("codigo") Long codigo, RedirectAttributes attributes) { // Usa
                                                                                                    // RedirectAttributes
                                                                                                    // para
                                                                                                    // FlashAttribute
        try {
            carroService.remover(codigo);
            attributes.addFlashAttribute("notificacao",
                    new NotificacaoSweetAlert2("Carro removido com sucesso!", TipoNotificaoSweetAlert2.SUCCESS, 4000));
        } catch (RuntimeException e) {
            logger.error("Erro ao remover carro com código {}: {}", codigo, e.getMessage(), e);
            attributes.addFlashAttribute("notificacao",
                    new NotificacaoSweetAlert2("Erro ao remover carro: " + e.getMessage(),
                            TipoNotificaoSweetAlert2.ERROR, 5000));
        }
        // O `HxLocation` acima instruirá o cliente a fazer uma requisição GET para
        // "/carros/abrirpesquisa".
        // O `RedirectAttributes` garantirá que a "notificacao" esteja disponível no
        // Model dessa requisição GET subsequente.
        // O retorno do método em si não importa tanto aqui, pois o HxLocation vai
        // "assumir o controle".
        // Podemos retornar a própria URL do redirecionamento para maior clareza, mesmo
        // que não seja estritamente necessário.

        return "redirect:/carros/abrirpesquisa";
    }
}

/*
 * package web.controlevacinacao.controller;
 * 
 * import org.slf4j.Logger;
 * import org.slf4j.LoggerFactory;
 * import org.springframework.data.domain.Page;
 * import org.springframework.data.domain.Pageable;
 * import org.springframework.data.domain.Sort;
 * import org.springframework.data.web.PageableDefault;
 * import org.springframework.data.web.SortDefault;
 * import org.springframework.stereotype.Controller;
 * import org.springframework.ui.Model;
 * import org.springframework.validation.BindingResult;
 * import org.springframework.validation.FieldError;
 * import org.springframework.validation.ObjectError;
 * import org.springframework.web.bind.annotation.GetMapping;
 * import org.springframework.web.bind.annotation.PathVariable;
 * import org.springframework.web.bind.annotation.PostMapping;
 * import org.springframework.web.servlet.mvc.support.RedirectAttributes;
 * 
 * import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxLocation;
 * import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest;
 * import jakarta.servlet.http.HttpServletRequest;
 * import jakarta.validation.Valid;
 * import web.controlevacinacao.filter.CarroFilter;
 * import web.controlevacinacao.model.Carro;
 * import web.controlevacinacao.model.StatusCarro;
 * import web.controlevacinacao.notificacao.NotificacaoSweetAlert2;
 * import web.controlevacinacao.notificacao.TipoNotificaoSweetAlert2;
 * import web.controlevacinacao.pagination.PageWrapper;
 * import web.controlevacinacao.repository.CarroRepository;
 * import web.controlevacinacao.service.CarroService;
 * 
 * @Controller
 * public class CarroController {
 * 
 * private static final Logger logger =
 * LoggerFactory.getLogger(Controller.class);
 * private final CarroRepository carroRepository;
 * private final CarroService carroService;
 * 
 * public CarroController(CarroRepository carroRepository, CarroService
 * carroService) {
 * this.carroRepository = carroRepository;
 * this.carroService = carroService;
 * }
 * 
 * @HxRequest
 * 
 * @GetMapping("/carros/abrirpesquisa")
 * public String abrirPesquisaHTMX() {
 * return "carros/pesquisar :: formulario";
 * }
 * 
 * @HxRequest
 * 
 * @GetMapping("/carros/pesquisar")
 * public String mostrarCarrosPesquisaHTMX(CarroFilter filtro, Model model,
 * 
 * @PageableDefault(size = 8) @SortDefault(sort = "codigo", direction =
 * Sort.Direction.ASC) Pageable pageable,
 * HttpServletRequest request) {
 * Page<Carro> pagina = carroRepository.pesquisar(filtro, pageable);
 * logger.info("Carros pesquisados: {}", pagina);
 * PageWrapper<Carro> paginaWrapper = new PageWrapper<>(pagina, request);
 * model.addAttribute("pagina", paginaWrapper);
 * return "carros/listar :: tabela";
 * }
 * 
 * @HxRequest
 * 
 * @GetMapping("/carros/cadastrar")
 * public String abrirCadastroHTMX(Carro carro) {
 * return "carros/cadastrar :: formulario";
 * }
 * 
 * @HxRequest
 * 
 * @PostMapping("/carros/cadastrar")
 * public String cadastrarHTMX(@Valid Carro carro,
 * BindingResult resultado,
 * RedirectAttributes attributes) {
 * if (resultado.hasErrors()) {
 * logger.info("O Carro recebida para cadastrar não é válido.");
 * logger.info("Erros encontrados:");
 * for (FieldError erro : resultado.getFieldErrors()) {
 * logger.info("{}", erro);
 * }
 * for (ObjectError erro : resultado.getGlobalErrors()) {
 * logger.info("{}", erro);
 * }
 * return "carros/cadastrar :: formulario";
 * } else {
 * carroService.salvar(carro);
 * 
 * attributes.addFlashAttribute("notificacao",
 * new NotificacaoSweetAlert2("Carro cadastrada com sucesso!",
 * TipoNotificaoSweetAlert2.SUCCESS, 4000));
 * 
 * return "redirect:/carros/cadastrar";
 * }
 * }
 * 
 * // @HxRequest
 * // @GetMapping("/mensagem")
 * // public String mostrarMensagemHTMX(String mensagem, Model model) {
 * // if (mensagem != null && !mensagem.isEmpty()) {
 * // model.addAttribute("mensagem", mensagem);
 * // }
 * // return "mensagem :: texto";
 * // }
 * 
 * @HxRequest
 * 
 * @GetMapping("/carros/alterar/{codigo}")
 * public String abrirAlterarHTMX(@PathVariable("codigo") Long codigo, Model
 * model) {
 * Carro carro = carroRepository.findByCodigoAndStatusCarro(codigo,
 * StatusCarro.ATIVO);
 * if (carro != null) {
 * model.addAttribute("carro", carro);
 * return "carros/alterar :: formulario";
 * } else {
 * model.addAttribute("mensagem", "Não existe um carro com esse código");
 * return "mensagem :: texto";
 * }
 * }
 * 
 * @HxRequest
 * 
 * @PostMapping("/carros/alterar")
 * public String alterarHTMX(@Valid Carro carro, BindingResult resultado,
 * RedirectAttributes redirectAttributes) {
 * if (resultado.hasErrors()) {
 * logger.info("O carro recebido para alterar não é válido.");
 * logger.info("Erros encontrados:");
 * for (FieldError erro : resultado.getFieldErrors()) {
 * logger.info("{}", erro);
 * }
 * for (ObjectError erro : resultado.getGlobalErrors()) {
 * logger.info("{}", erro);
 * }
 * return "carros/alterar :: formulario";
 * } else {
 * carroService.alterar(carro);
 * redirectAttributes.addFlashAttribute("notificacao",
 * new NotificacaoSweetAlert2("Carro alterado com sucesso!",
 * TipoNotificaoSweetAlert2.SUCCESS, 4000));
 * return "redirect:/carros/abrirpesquisa";
 * }
 * }
 * 
 * @HxRequest
 * 
 * @HxLocation(path = "/mensagem", target = "#main", swap = "outerHTML")
 * 
 * @GetMapping("/carros/remover/{codigo}")
 * public String removerHTMX(@PathVariable("codigo") Long codigo,
 * RedirectAttributes attributes) {
 * carroService.remover(codigo);
 * attributes.addFlashAttribute("notificacao",
 * new NotificacaoSweetAlert2("Carro removido com sucesso!",
 * TipoNotificaoSweetAlert2.SUCCESS, 4000));
 * return "redirect:/carross/abrirpesquisa";
 * }
 * }
 */
