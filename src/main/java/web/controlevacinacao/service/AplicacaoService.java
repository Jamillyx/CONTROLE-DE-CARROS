package web.controlevacinacao.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import web.controlevacinacao.model.Aplicacao;
import web.controlevacinacao.model.Status;
import web.controlevacinacao.repository.AplicacaoRepository;

@Service
@Transactional
public class AplicacaoService {

    private AplicacaoRepository aplicacaoRepository;

    public AplicacaoService(AplicacaoRepository aplicacaoRepository) {
        this.aplicacaoRepository = aplicacaoRepository;
    }

    public void salvar(Aplicacao aplicacao) {
        aplicacaoRepository.save(aplicacao);
    }

    public void alterar(Aplicacao aplicacao) {
        aplicacaoRepository.save(aplicacao);
    }

    public void remover(Long codigo) {
        Aplicacao aplicacao = aplicacaoRepository.findByCodigoAndStatus(codigo, Status.ATIVO);
        if (aplicacao == null) {
            throw new RuntimeException("Remoção da aplicaçao com codigo inválido");
        } else {
            aplicacao.setStatus(Status.INATIVO);
            aplicacaoRepository.save(aplicacao);
        }
    }

}
