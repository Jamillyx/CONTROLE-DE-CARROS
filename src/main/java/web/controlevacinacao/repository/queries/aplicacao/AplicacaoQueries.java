package web.controlevacinacao.repository.queries.aplicacao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import web.controlevacinacao.filter.AplicacaoFilter;
import web.controlevacinacao.model.Aplicacao;

public interface AplicacaoQueries {

	Page<Aplicacao> pesquisar(AplicacaoFilter filtro, Pageable pageable);
	
	Aplicacao buscarCompletoCodigo(Long codigo);
}
