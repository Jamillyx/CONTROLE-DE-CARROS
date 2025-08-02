package web.controlevacinacao.pagination;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query; // Importar Query para getTotalRegistrosComQuery
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Root;

public class PaginacaoUtil {

    public static void prepararIntervalo(TypedQuery<?> typedQuery, Pageable pageable) {
        int paginaAtual = pageable.getPageNumber();
        int totalRegistrosPorPagina = pageable.getPageSize();
        int primeiroRegistro = paginaAtual * totalRegistrosPorPagina;
        typedQuery.setFirstResult(primeiroRegistro);
        typedQuery.setMaxResults(totalRegistrosPorPagina);
    }

    public static void prepararOrdemCriteria(Root<?> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder builder,
                                             Pageable pageable) {
        String atributo;
        Sort sort = pageable.getSort();
        Order order;
        List<Order> ordenacoes = new ArrayList<>();
        if (sort != null && !sort.isEmpty()) {
            for (Sort.Order o : sort) {
                atributo = o.getProperty();
                order = o.isAscending() ? builder.asc(root.get(atributo)) : builder.desc(root.get(atributo));
                ordenacoes.add(order);
            }
        }
        criteriaQuery.orderBy(ordenacoes);
    }

    public static void prepararOrdemJPQL(StringBuilder query, String alias, Pageable pageable) {
        Sort sort = pageable.getSort();
        if (sort != null && !sort.isEmpty()) {
            query.append(" order by ");
            boolean primeiroOrder = true; // Variável para controlar a vírgula
            for (Sort.Order o : sort) {
                if (!primeiroOrder) {
                    query.append(", ");
                }
                // Adiciona o alias explicitamente para campos da entidade principal
                query.append(alias).append(".").append(o.getProperty()).append(" ").append(o.getDirection().name());
                primeiroOrder = false;
            }
        }
    }

    public static void preencherParametros(Map<String, Object> parametros, TypedQuery<?> typedQuery) {
        for (String chave : parametros.keySet()) {
            typedQuery.setParameter(chave, parametros.get(chave));
        }
    }

    // Seu método original getTotalRegistros - bom para entidades sem joins complexos na contagem
    public static long getTotalRegistros(String entidade, String alias, StringBuilder condicoes,
                                         Map<String, Object> parametros, EntityManager manager) {
        StringBuilder queryTotal = new StringBuilder("select count(");
        queryTotal.append(alias);
        queryTotal.append(") from ");
        queryTotal.append(entidade);
        queryTotal.append(" ");
        queryTotal.append(alias);
        queryTotal.append(condicoes);

        TypedQuery<Long> typedQueryTotal = manager.createQuery(queryTotal.toString(), Long.class);

        preencherParametros(parametros, typedQueryTotal);

        return typedQueryTotal.getSingleResult();
    }

    /**
     * NOVO MÉTODO: Calcula o total de registros para uma query de COUNT JPQL já montada,
     * útil para queries que envolvem joins na contagem.
     *
     * @param fullCountQuery StringBuilder contendo a JPQL completa da query de COUNT (ex: "select count(m.codigo) from Movimentacao m join m.carro c where ...")
     * @param parametros Map de parâmetros a serem preenchidos na query.
     * @param manager O EntityManager.
     * @return O total de registros.
     */
    public static long getTotalRegistrosComQuery(StringBuilder fullCountQuery, Map<String, Object> parametros, EntityManager manager) {
        // Para uma query de COUNT, usamos a interface Query, não TypedQuery<Long> diretamente,
        // pois a projeção pode ser diferente (count(distinct x.id) vs count(x)).
        Query query = manager.createQuery(fullCountQuery.toString());

        // Reutilizamos o método de preencher parâmetros. Como ele aceita TypedQuery<?>,
        // precisamos de um cast ou de um método auxiliar que aceite Query.
        // Vamos criar um auxiliar para Query.
        preencherParametrosParaQueryGenerica(parametros, query);

        return (Long) query.getSingleResult();
    }

    // Método auxiliar para preencher parâmetros em um objeto Query genérico
    private static void preencherParametrosParaQueryGenerica(Map<String, Object> parametros, Query query) {
        for (String chave : parametros.keySet()) {
            query.setParameter(chave, parametros.get(chave));
        }
    }

    public static boolean fazerLigacaoCondicoes(StringBuilder condicoes, boolean isPrimeiraCondicao) {
        if (isPrimeiraCondicao) {
            condicoes.append(" where ");
        } else {
            condicoes.append(" and ");
        }
        return false; // Retorna false, porque a partir de agora já existe pelo menos uma condição.
    }
}





/*package web.controlevacinacao.pagination;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Root;

public class PaginacaoUtil {

	// private static final Logger logger =
	// LoggerFactory.getLogger(PaginacaoUtil.class);

	public static void prepararIntervalo(TypedQuery<?> typedQuery, Pageable pageable) {
		int paginaAtual = pageable.getPageNumber();
		int totalRegistrosPorPagina = pageable.getPageSize();
		int primeiroRegistro = paginaAtual * totalRegistrosPorPagina;
		// logger.info("Filtrando a página {}, registros entre {} e {}", paginaAtual,
		// primeiroRegistro, primeiroRegistro + totalRegistrosPorPagina);
		typedQuery.setFirstResult(primeiroRegistro);
		typedQuery.setMaxResults(totalRegistrosPorPagina);
	}

	public static void prepararOrdemCriteria(Root<?> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder builder,
			Pageable pageable) {
		String atributo;
		Sort sort = pageable.getSort();
		Order order;
		List<Order> ordenacoes = new ArrayList<>();
		if (sort != null && !sort.isEmpty()) {
			for (Sort.Order o : sort) {
				// logger.info("Ordenando o resultado da pesquisa por {}, {}", o.getProperty(),
				// o.getDirection());
				atributo = o.getProperty();
				order = o.isAscending() ? builder.asc(root.get(atributo)) : builder.desc(root.get(atributo));
				ordenacoes.add(order);
			}
		}
		criteriaQuery.orderBy(ordenacoes);
	}

	public static void prepararOrdemJPQL(StringBuilder query, String alias, Pageable pageable) {
		String atributo;
		Sort sort = pageable.getSort();
		boolean maisDeUm = false;
		if (sort != null && !sort.isEmpty()) {
			query.append(" order by ");
			query.append(alias);
			query.append(".");
			for (Sort.Order o : sort) {
				// logger.info("Ordenando o resultado da pesquisa por {}, {}", o.getProperty(),
				// o.getDirection());
				if (maisDeUm) {
					query.append(", ");
				}
				atributo = o.getProperty();
				query.append(atributo);
				query.append(o.isAscending() ? " asc" : " desc");
				maisDeUm = true;
			}
		}
	}

	public static void preencherParametros(Map<String, Object> parametros, TypedQuery<?> typedQuery) {
		for (String chave : parametros.keySet()) {
			typedQuery.setParameter(chave, parametros.get(chave));
		}
	}

	public static long getTotalRegistros(String entidade, String alias, StringBuilder condicoes,
			Map<String, Object> parametros, EntityManager manager) {
		StringBuilder queryTotal = new StringBuilder("select count(");
		queryTotal.append(alias);
		queryTotal.append(") from ");
		queryTotal.append(entidade);
		queryTotal.append(" ");
		queryTotal.append(alias);
		queryTotal.append(condicoes);

		TypedQuery<Long> typedQueryTotal = manager.createQuery(queryTotal.toString(), Long.class);

		preencherParametros(parametros, typedQueryTotal);

		return typedQueryTotal.getSingleResult();
	}

	/**
	 * Adiciona "where" ou "and" às condições da query.
	 * Retorna true para indicar que uma condição foi adicionada e a próxima ligação
	 * deve ser "and".
	 *
	 * @param condicoes     O StringBuilder das condições.
	 * @param jaTemCondicao Um boolean que indica se já existe alguma condição na
	 *                      string.
	 * @return true, para indicar que a partir de agora já existe pelo menos uma
	 *         condição.
	 */
	/* 
	public static boolean fazerLigacaoCondicoes(StringBuilder condicoes, boolean isPrimeiraCondicao) {
		if (isPrimeiraCondicao) { // Se é a PRIMEIRA condição a ser adicionada, usamos 'WHERE'
			condicoes.append(" where ");
		} else { // Se NÃO é a primeira, já existe 'WHERE' ou 'AND' anterior, então usamos 'AND'
			condicoes.append(" and ");
		}
		return false;

	}// Retorna false, porque a primeira condição já foi tratada
		// (ou uma condição já foi adicionada), então as próximas serão 'AND'.

	// A ideia era boa, mas não funciona com Criteria.
	// O JPA reclama pq a lista de predicados foi criada usando outra Root que não a
	// criada dentro do método.
	// Não encontrei uma maneira de deixar isso genérico, tem que repetir em todo
	// lugar
	// que usar paginação
	// public static <T> long getTotalRegistros(Class<T> clazz, Predicate[]
	// predicateArray, CriteriaBuilder builder, EntityManager manager) {
	// logger.info("Calculando o total de registros que o filtro retornará.");
	// CriteriaQuery<Long> criteriaQuery = builder.createQuery(Long.class);
	// Root<T> root = criteriaQuery.from(clazz);
	// criteriaQuery.select(builder.count(root));
	// criteriaQuery.where(predicateArray);
	// TypedQuery<Long> typedQueryTotal = manager.createQuery(criteriaQuery);
	// long totalRegistros = typedQueryTotal.getSingleResult();
	// logger.info("O filtro retornará {} registros.", totalRegistros);
	// return totalRegistros;
	// }
}
*/