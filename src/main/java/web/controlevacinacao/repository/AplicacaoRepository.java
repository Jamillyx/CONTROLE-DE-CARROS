package web.controlevacinacao.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import web.controlevacinacao.model.Aplicacao;
import web.controlevacinacao.model.Status;
import web.controlevacinacao.repository.queries.aplicacao.AplicacaoQueries;

public interface AplicacaoRepository extends JpaRepository<Aplicacao, Long>, AplicacaoQueries {

    Aplicacao findByCodigoAndStatus (Long codigo, Status status);

}
