package web.controlevacinacao.repository.queries.carro;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import web.controlevacinacao.filter.CarroFilter;
import web.controlevacinacao.model.Carro;

public interface CarroQueries {

    public Page<Carro> pesquisar(CarroFilter filtro, Pageable pageable);

}