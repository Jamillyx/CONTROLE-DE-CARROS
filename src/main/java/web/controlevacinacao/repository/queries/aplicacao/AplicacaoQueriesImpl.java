package web.controlevacinacao.repository.queries.aplicacao;

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
import web.controlevacinacao.filter.AplicacaoFilter;
import web.controlevacinacao.model.Aplicacao;
import web.controlevacinacao.pagination.PaginacaoUtil;

public class AplicacaoQueriesImpl implements AplicacaoQueries {

	@PersistenceContext
	private EntityManager em;

	@Override
	public Aplicacao buscarCompletoCodigo(Long codigo) {
		String query = "select distinct a from Aplicacao a inner join fetch a.pessoa inner join fetch a.lote inner join fetch a.lote.vacina where a.status = 'ATIVO' and a.codigo = :codigo";
		TypedQuery<Aplicacao> typedQuery = em.createQuery(query, Aplicacao.class);
		typedQuery.setParameter("codigo", codigo);
		return typedQuery.getSingleResult();
	}

	@Override
	public Page<Aplicacao> pesquisar(AplicacaoFilter filtro, Pageable pageable) {

		StringBuilder queryAplicacoes = new StringBuilder(
				"select distinct a from Aplicacao a inner join fetch a.pessoa inner join fetch a.lote inner join fetch a.lote.vacina");
		StringBuilder condicoes = new StringBuilder();
		Map<String, Object> parametros = new HashMap<>();

		preencherCondicoesEParametros(filtro, condicoes, parametros);

		if (condicoes.isEmpty()) {
			condicoes.append(" where a.status = 'ATIVO'");
		} else {
			condicoes.append(" and a.status = 'ATIVO'");
		}

		queryAplicacoes.append(condicoes);
		PaginacaoUtil.prepararOrdemJPQL(queryAplicacoes, "a", pageable);
		TypedQuery<Aplicacao> typedQuery = em.createQuery(queryAplicacoes.toString(), Aplicacao.class);
		PaginacaoUtil.prepararIntervalo(typedQuery, pageable);
		PaginacaoUtil.preencherParametros(parametros, typedQuery);
		List<Aplicacao> aplicacoes = typedQuery.getResultList();

		long totalAplicacoes = PaginacaoUtil.getTotalRegistros("Aplicacao", "a", condicoes, parametros, em);

		return new PageImpl<>(aplicacoes, pageable, totalAplicacoes);
	}

	private void preencherCondicoesEParametros(AplicacaoFilter filtro, StringBuilder condicoes,
			Map<String, Object> parametros) {
		boolean condicao = false;

		if (filtro.getCodigo() != null) {
			PaginacaoUtil.fazerLigacaoCondicoes(condicoes, condicao);
			condicoes.append("a.codigo = :codigo");
			parametros.put("codigo", filtro.getCodigo());
			condicao = true;
		}
		if (filtro.getDataInicial() != null) {
			PaginacaoUtil.fazerLigacaoCondicoes(condicoes, condicao);
			condicoes.append("a.data >= :inicioValidade");
			parametros.put("inicioValidade", filtro.getDataInicial());
			condicao = true;
		}
		if (filtro.getDataFinal() != null) {
			PaginacaoUtil.fazerLigacaoCondicoes(condicoes, condicao);
			condicoes.append("a.data <= :fimValidade");
			parametros.put("fimValidade", filtro.getDataFinal());
			condicao = true;
		}
		if (StringUtils.hasText(filtro.getCpf())) {
			PaginacaoUtil.fazerLigacaoCondicoes(condicoes, condicao);
			condicoes.append("a.pessoa.cpf like :cpf");
			parametros.put("cpf", "%" + filtro.getCpf() + "%");
			condicao = true;
		}
		if (filtro.getCodigoLote() != null) {
			PaginacaoUtil.fazerLigacaoCondicoes(condicoes, condicao);
			condicoes.append("a.lote.codigo = :codigoLote");
			parametros.put("codigoLote", filtro.getCodigoLote());
		}
	}

}
