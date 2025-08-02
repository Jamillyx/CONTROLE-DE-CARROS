// web.controlevacinacao.service/MotoristaService.java
package web.controlevacinacao.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import web.controlevacinacao.model.Motorista;
import web.controlevacinacao.model.Status;
import web.controlevacinacao.repository.MotoristaRepository;

@Service
@Transactional
public class MotoristaService {

    private final MotoristaRepository motoristaRepository;
    private final PessoaService pessoaService;

    @Autowired // Você pode usar injeção por construtor também, como no PessoaController

    public MotoristaService(MotoristaRepository motoristaRepository, PessoaService pessoaService) {
        this.motoristaRepository = motoristaRepository;
        this.pessoaService = pessoaService; // Inicialize
    }

    public void salvar(Motorista motorista, BindingResult result) {
        // Chama a validação de CPF no PessoaService (que Motorista herda)
        // O PessoaService AGORA NÃO RETORNA E APENAS ADICIONA ERROS AO 'result'
        // A condição para salvar/alterar é decidida pelo 'codigo' dentro do
        // PessoaService

        // Validação de CNH duplicada (específica de Motorista)
        Motorista motoristaExistenteCnh = motoristaRepository.findByCnh(motorista.getCnh());
        if (motoristaExistenteCnh != null && (motorista.getCodigo() == null
                || !motoristaExistenteCnh.getCodigo().equals(motorista.getCodigo()))) {
            result.rejectValue("cnh", "cnh.duplicada", "CNH já cadastrada no sistema.");
        }
        pessoaService.salvar(motorista, result);

        // CRÍTICO: SOMENTE SALVA NO BANCO DE DADOS SE NÃO HOUVER ERROS
        // (incluindo os adicionados pelo PessoaService e pela validação de CNH)
        if (!result.hasErrors()) {
            motoristaRepository.save(motorista);
        }
    }

    public void alterar(Motorista motorista, BindingResult result) {

        // Validação de CNH duplicada (específica de Motorista)
        Motorista motoristaExistenteCnh = motoristaRepository.findByCnh(motorista.getCnh());
        if (motoristaExistenteCnh != null && (motorista.getCodigo() == null
                || !motoristaExistenteCnh.getCodigo().equals(motorista.getCodigo()))) {
            result.rejectValue("cnh", "cnh.duplicada", "CNH já cadastrada no sistema.");
        }

        // Chamar a validação de CPF do PessoaService
        pessoaService.salvar(motorista, result);

        // CRÍTICO: SOMENTE SALVA NO BANCO DE DADOS SE NÃO HOUVER ERROS
        // (incluindo os adicionados pelo PessoaService e pela validação de CNH)
        if (!result.hasErrors()) {
            motoristaRepository.save(motorista);
        }
    }

    public void remover(Long codigo) {
        Motorista motorista = motoristaRepository.findByCodigoAndStatus(codigo, Status.ATIVO);
        if (motorista == null) {
            throw new RuntimeException("Remoção de Motorista com codigo inválido");
        } else {
            motorista.setStatus(Status.INATIVO); // Altera o status do motorista para INATIVO
            motoristaRepository.save(motorista); // Salva a entidade atualizada
        }
    }

    // Você pode adicionar outros métodos de serviço aqui, como busca, listagem,
    // etc.
}