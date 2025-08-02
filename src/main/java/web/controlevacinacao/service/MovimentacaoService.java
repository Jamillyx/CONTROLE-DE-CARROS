package web.controlevacinacao.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import web.controlevacinacao.model.Movimentacao;
import web.controlevacinacao.model.StatusMovimentacao;
import web.controlevacinacao.repository.MovimentacaoRepository;

@Transactional
@Service
public class MovimentacaoService {

    private MovimentacaoRepository movimentacaoRepository;

    public MovimentacaoService(MovimentacaoRepository movimentacaoRepository) {
        this.movimentacaoRepository = movimentacaoRepository;
    }

    public void salvar(Movimentacao movimentacao) {
        movimentacaoRepository.save(movimentacao);
    }

    public void alterar(Movimentacao movimentacao) {
        movimentacaoRepository.save(movimentacao);
    }

    public void remover(Long codigo) {
        Movimentacao movimentacao = movimentacaoRepository.findByCodigoAndStatusMovimentacao(codigo, StatusMovimentacao.ATIVA);
        if (movimentacao == null) {
            throw new RuntimeException("Remoção da movimentação com codigo inválido");
        } else {
            movimentacao.setStatusMovimentacao(StatusMovimentacao.FINALIZADA);
            movimentacaoRepository.save(movimentacao);
        }
    }
    
}