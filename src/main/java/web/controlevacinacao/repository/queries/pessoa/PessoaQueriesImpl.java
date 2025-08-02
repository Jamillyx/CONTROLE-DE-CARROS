package web.controlevacinacao.repository.queries.pessoa;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import web.controlevacinacao.filter.PessoaFilter;
import web.controlevacinacao.model.Pessoa;
import web.controlevacinacao.pagination.PaginacaoUtil;

public class PessoaQueriesImpl implements PessoaQueries {

    @PersistenceContext
    private EntityManager em;

    private void preencherCondicoesEParametros(PessoaFilter filtro, StringBuilder condicoes, Map<String, Object> parametros) {
        boolean primeiraCondicao = true; // <-- CORREÇÃO AQUI! DEVE SER TRUE.

        if (filtro.getCodigo() != null) {
            primeiraCondicao = PaginacaoUtil.fazerLigacaoCondicoes(condicoes, primeiraCondicao);
            condicoes.append("p.codigo = :codigo");
            parametros.put("codigo", filtro.getCodigo());
        }
        if (StringUtils.hasText(filtro.getNome())) {
            primeiraCondicao = PaginacaoUtil.fazerLigacaoCondicoes(condicoes, primeiraCondicao);
            condicoes.append("lower(p.nome) like :nome");
            parametros.put("nome", "%" + filtro.getNome().toLowerCase() + "%");
        }
        if (StringUtils.hasText(filtro.getCpf())) {
            primeiraCondicao = PaginacaoUtil.fazerLigacaoCondicoes(condicoes, primeiraCondicao);
            condicoes.append("p.cpf like :cpf");
            parametros.put("cpf", "%" + filtro.getCpf() + "%");
        }
        if (filtro.getDataNascimentoInicial() != null) {
            primeiraCondicao = PaginacaoUtil.fazerLigacaoCondicoes(condicoes, primeiraCondicao);
            condicoes.append("p.dataNascimento >= :dataNascimentoInicial");
            parametros.put("dataNascimentoInicial", filtro.getDataNascimentoInicial());
        }
        if (filtro.getDataNascimentoFinal() != null) {
            primeiraCondicao = PaginacaoUtil.fazerLigacaoCondicoes(condicoes, primeiraCondicao);
            condicoes.append("p.dataNascimento <= :dataNascimentoFinal");
            parametros.put("dataNascimentoFinal", filtro.getDataNascimentoFinal());
        }
      
    }

    @Override // Não se esqueça da anotação @Override se for uma interface
    public Page<Pessoa> pesquisar(PessoaFilter filtro, Pageable pageable) {

        StringBuilder queryPessoas = new StringBuilder("select distinct p from Pessoa p");
        StringBuilder condicoes = new StringBuilder();

        Map<String, Object> parametros = new HashMap<>();

        preencherCondicoesEParametros(filtro, condicoes, parametros);

        if (condicoes.isEmpty()) {
            condicoes.append(" where p.status = 'ATIVO'");
        } else {
            condicoes.append(" and p.status = 'ATIVO'");
        }

        queryPessoas.append(condicoes);
        PaginacaoUtil.prepararOrdemJPQL(queryPessoas, "p", pageable);
        TypedQuery<Pessoa> typedQuery = em.createQuery(queryPessoas.toString(), Pessoa.class);
        PaginacaoUtil.prepararIntervalo(typedQuery, pageable);
        PaginacaoUtil.preencherParametros(parametros, typedQuery);
        List<Pessoa> pessoas = typedQuery.getResultList();

        long totalPessoas = PaginacaoUtil.getTotalRegistros("Pessoa", "p", condicoes, parametros, em);

        return new PageImpl<>(pessoas, pageable, totalPessoas);
    }
}