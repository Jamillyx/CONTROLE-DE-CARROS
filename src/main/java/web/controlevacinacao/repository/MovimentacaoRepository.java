package web.controlevacinacao.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import web.controlevacinacao.model.Carro;
import web.controlevacinacao.model.Motorista;
import web.controlevacinacao.model.Movimentacao;
import web.controlevacinacao.model.StatusMovimentacao;
import web.controlevacinacao.repository.queries.movimentacao.MovimentacaoQueries;

@Repository
public interface MovimentacaoRepository extends JpaRepository<Movimentacao, Long>, MovimentacaoQueries {

        Movimentacao findByCodigoAndStatusMovimentacao (Long codigo, StatusMovimentacao statusMovimentacao);

    /**
     * Busca uma movimentação ativa (dataRetorno is NULL) para um determinado carro.
     * @param carro O carro a ser verificado.
     * @return Um Optional contendo a movimentação ativa, se existir.
     */
    Optional<Movimentacao> findByCarroAndDataRetornoIsNull(Carro carro);

    /**
     * Busca uma movimentação ativa (dataRetorno is NULL) para um determinado motorista.
     * @param motorista O motorista a ser verificado.
     * @return Um Optional contendo a movimentação ativa, se existir.
     */
    Optional<Movimentacao> findByMotoristaAndDataRetornoIsNull(Motorista motorista);

    /**
     * Busca a última movimentação finalizada para um determinado carro, ordenada pela data de retorno.
     * Isso é útil para pegar o último KM de retorno.
     * @param carro O carro para buscar a última movimentação finalizada.
     * @return Um Optional contendo a última movimentação finalizada, se existir.
     */
    Optional<Movimentacao> findFirstByCarroAndDataRetornoIsNotNullOrderByDataRetornoDesc(Carro carro);

}