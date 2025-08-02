package web.controlevacinacao.repository.queries.movimentacao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import web.controlevacinacao.filter.MovimentacaoFilter;
import web.controlevacinacao.model.Movimentacao;
import web.controlevacinacao.model.StatusMovimentacao;
import web.controlevacinacao.pagination.PaginacaoUtil;

public class MovimentacaoQueriesImpl implements MovimentacaoQueries {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Movimentacao buscarCompletoCodigo(Long codigo) {
        String query = "select m from Movimentacao m " +
                "inner join fetch m.carro c " +
                "inner join fetch m.motorista mt " +
                "where m.statusMovimentacao = :statusAtivo " + // Consideramos filtrar por status, como no exemplo
                "and m.codigo = :codigo";

        TypedQuery<Movimentacao> typedQuery = em.createQuery(query, Movimentacao.class);
        typedQuery.setParameter("codigo", codigo);
        typedQuery.setParameter("statusAtivo", StatusMovimentacao.ATIVA); // Usando o enum StatusMovimentacao

        try {
            return typedQuery.getSingleResult();
        } catch (NoResultException e) {
            return null; // Ou lance uma exceção específica, ou Optional.empty() se o retorno for
                         // Optional
        }
    }

    @Override
    public Page<Movimentacao> pesquisar(MovimentacaoFilter filtro, Pageable pageable) {
        StringBuilder queryMovimentacoes = new StringBuilder(
                "select distinct m from Movimentacao m " +
                        "inner join fetch m.carro c " +
                        "inner join fetch m.motorista mt " 
        );
        StringBuilder condicoes = new StringBuilder();
        Map<String, Object> parametros = new HashMap<>();

        // Preenche as condições e parâmetros a partir do filtro
        preencherCondicoesEParametros(filtro, condicoes, parametros);

        // Lógica para adicionar o status padrão 'ATIVA' se não houver outras condições
        // ou status definido no filtro
        // ATENÇÃO: Adaptei a lógica aqui para lidar com o filtro.getStatus()
        if (filtro.getStatusMovimentacao() == null) { // Se o status não foi explicitamente setado no filtro
            if (condicoes.isEmpty()) {
                condicoes.append(" where m.statusMovimentacao = :defaultStatus");
            } else {
                condicoes.append(" and m.statusMovimentacao = :defaultStatus");
            }
            parametros.put("defaultStatus", StatusMovimentacao.ATIVA);
        }

        queryMovimentacoes.append(condicoes);

        PaginacaoUtil.prepararOrdemJPQL(queryMovimentacoes, "m", pageable);

        TypedQuery<Movimentacao> typedQuery = em.createQuery(queryMovimentacoes.toString(), Movimentacao.class);
        PaginacaoUtil.prepararIntervalo(typedQuery, pageable);
        PaginacaoUtil.preencherParametros(parametros, typedQuery);
        List<Movimentacao> movimentacoes = typedQuery.getResultList();

        // Para a contagem total, você precisa de uma query de count separada,
        // mas sem os JOIN FETCH e com a função COUNT.
        // O PaginacaoUtil.getTotalRegistros precisa ser adaptado ou você pode usar um
        // CriteriaBuilder.
        // Se PaginacaoUtil.getTotalRegistros funciona com "Movimentacao", "m",
        // condicoes, parametros, em
        // então deve estar tudo bem.
        long totalMovimentacoes = PaginacaoUtil.getTotalRegistros("Movimentacao", "m", condicoes, parametros, em);

        return new PageImpl<>(movimentacoes, pageable, totalMovimentacoes);
    }
    
    private void preencherCondicoesEParametros(MovimentacaoFilter filtro, StringBuilder condicoes,
            Map<String, Object> parametros) {
        boolean condicao = false; // Usando a sua lógica de flag 'condicao'

        // 1. Filtro por Código da Movimentação
        if (filtro.getCodigo() != null) {
            PaginacaoUtil.fazerLigacaoCondicoes(condicoes, condicao);
            condicoes.append("m.codigo = :codigo");
            parametros.put("codigo", filtro.getCodigo());
            condicao = true;
        }

        // 2. Filtro por Placa do Carro (a.carro.placa -> c.placa)
        if (StringUtils.hasText(filtro.getPlacaCarro())) {
            PaginacaoUtil.fazerLigacaoCondicoes(condicoes, condicao);
            condicoes.append("lower(c.placa) like :placa"); // 'c' é o alias para o Carro
            parametros.put("placa", "%" + filtro.getPlacaCarro().toLowerCase() + "%");
            condicao = true;
        }

        // 3. Filtro por Nome do Motorista (a.motorista.nome -> mt.nome)
       if (filtro.getCodigoMotorista() != null) {
            condicao = PaginacaoUtil.fazerLigacaoCondicoes(condicoes, condicao);
            condicoes.append("mt.codigo = :codigoMotorista"); // 'mt' é o alias para o Motorista
            parametros.put("codigoMotorista", filtro.getCodigoMotorista());
        }

        // 4. Filtro por Data de Saída Inicial (a.dataSaidaInicial -> m.dataSaida)
        if (filtro.getDataSaidaInicial() != null) {
            PaginacaoUtil.fazerLigacaoCondicoes(condicoes, condicao);
            condicoes.append("m.dataSaida >= :dataSaidaInicial");
            parametros.put("dataSaidaInicial", filtro.getDataSaidaInicial());
            condicao = true;
        }

        // 5. Filtro por Data de Saída Final (a.dataSaidaFinal -> m.dataSaida)
        if (filtro.getDataSaidaFinal() != null) {
            PaginacaoUtil.fazerLigacaoCondicoes(condicoes, condicao);
            condicoes.append("m.dataSaida <= :dataSaidaFinal");
            parametros.put("dataSaidaFinal", filtro.getDataSaidaFinal());
            condicao = true;
        }

        // 6. Filtro por Data de Retorno Inicial (a.dataRetornoInicial -> m.dataRetorno)
        if (filtro.getDataRetornoInicial() != null) {
            PaginacaoUtil.fazerLigacaoCondicoes(condicoes, condicao);
            condicoes.append("m.dataRetorno >= :dataRetornoInicial");
            parametros.put("dataRetornoInicial", filtro.getDataRetornoInicial());
            condicao = true;
        }

        // 7. Filtro por Data de Retorno Final (a.dataRetornoFinal -> m.dataRetorno)
        if (filtro.getDataRetornoFinal() != null) {
            PaginacaoUtil.fazerLigacaoCondicoes(condicoes, condicao);
            condicoes.append("m.dataRetorno <= :dataRetornoFinal");
            parametros.put("dataRetornoFinal", filtro.getDataRetornoFinal());
            condicao = true;
        }

        // 8. Filtro por Status da Movimentação (a.status -> m.status)
        // Este filtro já está sendo tratado na lógica principal do método 'pesquisar'
        // onde você adiciona 'm.status = :defaultStatus' se filtro.getStatus() for
        // nulo.
        // Se você quiser que o usuário possa filtrar por status na tela, adicione aqui:
        if (filtro.getStatusMovimentacao() != null) { // Permite filtrar explicitamente por ATIVA, FINALIZADA, etc.
            PaginacaoUtil.fazerLigacaoCondicoes(condicoes, condicao);
            condicoes.append("m.statusMovimentacao = :statusMovimentacao");
            parametros.put("statusMovimentacao", filtro.getStatusMovimentacao());
            condicao = true; // Necessário para a próxima condição
        }
    }

    
}

