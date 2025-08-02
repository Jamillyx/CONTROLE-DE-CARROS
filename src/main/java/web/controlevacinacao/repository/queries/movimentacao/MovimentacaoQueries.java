package web.controlevacinacao.repository.queries.movimentacao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import web.controlevacinacao.filter.MovimentacaoFilter;
import web.controlevacinacao.model.Movimentacao;

public interface MovimentacaoQueries {
    Page<Movimentacao> pesquisar(MovimentacaoFilter filter, Pageable pageable);
	
	Movimentacao buscarCompletoCodigo(Long codigo);
}