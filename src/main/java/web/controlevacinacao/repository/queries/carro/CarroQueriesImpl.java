package web.controlevacinacao.repository.queries.carro;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils; // Important for checking if string is null or empty

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import web.controlevacinacao.filter.CarroFilter;
import web.controlevacinacao.model.Carro;
import web.controlevacinacao.pagination.PaginacaoUtil; // Your utility for pagination

public class CarroQueriesImpl implements CarroQueries {

    @PersistenceContext
    private EntityManager em;

    /**
     * Fills the WHERE conditions and parameters for the JPQL query based on the
     * CarroFilter.
     * 
     * @param filtro     The CarroFilter object with search criteria.
     * @param condicoes  StringBuilder to append the WHERE/AND clauses.
     * @param parametros Map to store the named parameters and their values.
     */
    private void preencherCondicoesEParametros(CarroFilter filtro, StringBuilder condicoes,
            Map<String, Object> parametros) {
        boolean primeiraCondicao = true; // Flag to determine if "WHERE" or "AND" is needed

        // Filter by Código
        if (filtro.getCodigo() != null) {
            primeiraCondicao = PaginacaoUtil.fazerLigacaoCondicoes(condicoes, primeiraCondicao);
            condicoes.append("c.codigo = :codigo");
            parametros.put("codigo", filtro.getCodigo());
        }

        // Filter by Modelo
        if (StringUtils.hasText(filtro.getModelo())) {
            primeiraCondicao = PaginacaoUtil.fazerLigacaoCondicoes(condicoes, primeiraCondicao);
            // Using lower() for case-insensitive search
            condicoes.append("lower(c.modelo) like :modelo");
            parametros.put("modelo", "%" + filtro.getModelo().toLowerCase() + "%");
        }

        // Filter by Cor
        if (StringUtils.hasText(filtro.getCor())) {
            primeiraCondicao = PaginacaoUtil.fazerLigacaoCondicoes(condicoes, primeiraCondicao);
            // Using lower() for case-insensitive search
            condicoes.append("lower(c.cor) like :cor");
            parametros.put("cor", "%" + filtro.getCor().toLowerCase() + "%");
        }

        // Filter by KM Inicial (min kmAtual)
        if (filtro.getKmInicial() != null) {
            primeiraCondicao = PaginacaoUtil.fazerLigacaoCondicoes(condicoes, primeiraCondicao);
            condicoes.append("c.kmAtual >= :kmInicial");
            parametros.put("kmInicial", filtro.getKmInicial());
        }

        // Filter by KM Final (max kmAtual)
        if (filtro.getKmFinal() != null) {
            primeiraCondicao = PaginacaoUtil.fazerLigacaoCondicoes(condicoes, primeiraCondicao);
            condicoes.append("c.kmAtual <= :kmFinal");
            parametros.put("kmFinal", filtro.getKmFinal());
        }

        // Filter by Placa
        if (StringUtils.hasText(filtro.getPlaca())) {
            primeiraCondicao = PaginacaoUtil.fazerLigacaoCondicoes(condicoes, primeiraCondicao);
            // Exact match or 'like' depending on your business rule for placa
            // For general search, 'like' is often preferred.
            condicoes.append("lower(c.placa) like :placa");
            parametros.put("placa", "%" + filtro.getPlaca().toLowerCase() + "%");
        }
    }

    @Override
    public Page<Carro> pesquisar(CarroFilter filtro, Pageable pageable) {

        StringBuilder queryCarros = new StringBuilder("select distinct c from Carro c");
        StringBuilder condicoes = new StringBuilder(); // Will hold the WHERE/AND clauses

        Map<String, Object> parametros = new HashMap<>(); // Holds the query parameters

        // Populate conditions and parameters based on the filter
        preencherCondicoesEParametros(filtro, condicoes, parametros);

        // Always filter by ATIVO status, unless explicitly allowed otherwise by the
        // filter
        if (condicoes.isEmpty()) {
            condicoes.append(" where (c.statusCarro = 'ATIVO' or c.statusCarro = 'EM_USO' or c.statusCarro = 'DISPONIVEL' or c.statusCarro = 'EM_MANUTENCAO')");
        } else {
            condicoes.append(" and c.statusCarro = 'ATIVO'");
        }

        queryCarros.append(condicoes); // Append the conditions to the main query

        // Prepare the ordering (if specified in Pageable)
        PaginacaoUtil.prepararOrdemJPQL(queryCarros, "c", pageable);

        // Create the TypedQuery
        TypedQuery<Carro> typedQuery = em.createQuery(queryCarros.toString(), Carro.class);

        // Apply pagination (offset and limit)
        PaginacaoUtil.prepararIntervalo(typedQuery, pageable);

        // Set the query parameters
        PaginacaoUtil.preencherParametros(parametros, typedQuery);

        // Execute the query and get the result list
        List<Carro> carros = typedQuery.getResultList();

        // Get the total number of records for pagination metadata
        long totalCarros = PaginacaoUtil.getTotalRegistros("Carro", "c", condicoes, parametros, em);

        // Return the Page object
        return new PageImpl<>(carros, pageable, totalCarros);
    }
}