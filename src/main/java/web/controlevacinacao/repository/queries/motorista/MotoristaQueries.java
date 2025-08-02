package web.controlevacinacao.repository.queries.motorista;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import web.controlevacinacao.filter.MotoristaFilter;
import web.controlevacinacao.model.Motorista;

public interface MotoristaQueries {
    public Page<Motorista> pesquisar(MotoristaFilter filtro, Pageable pageable);
}