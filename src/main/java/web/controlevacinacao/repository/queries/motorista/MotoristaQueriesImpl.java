package web.controlevacinacao.repository.queries.motorista;

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

import web.controlevacinacao.filter.MotoristaFilter;
import web.controlevacinacao.model.Motorista;
import web.controlevacinacao.pagination.PaginacaoUtil; // Reutilizando sua classe PaginacaoUtil

public class MotoristaQueriesImpl implements MotoristaQueries {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Page<Motorista> pesquisar(MotoristaFilter filtro, Pageable pageable) {

        // Nota: A JPQL para Motorista (subclasse) já inclui o filtro automático pelo
        // DTYPE (tipo_entidade)
        StringBuilder queryMotoristas = new StringBuilder("select distinct m from Motorista m");
        StringBuilder condicoes = new StringBuilder(); // Para as condições 'where'

        Map<String, Object> parametros = new HashMap<>();

        preencherCondicoesEParametros(filtro, condicoes, parametros);

        // O filtro por status 'ATIVO' é adicionado aqui, assim como no
        // PessoaQueriesImpl
        if (condicoes.isEmpty()) {
            condicoes.append(" where m.status = 'ATIVO'");
        } else {
            condicoes.append(" and m.status = 'ATIVO'");
        }

        queryMotoristas.append(condicoes);
        PaginacaoUtil.prepararOrdemJPQL(queryMotoristas, "m", pageable);

        System.out.println("Consulta JPQL Gerada para Motorista: " + queryMotoristas.toString()); // <<< ADICIONE ESTA
                                                                                                  // LINHA
        System.out.println("Parâmetros para Consulta de Motorista: " + parametros); // <<< E ESTA LINHA

        TypedQuery<Motorista> typedQuery = em.createQuery(queryMotoristas.toString(), Motorista.class);

        // ... (restante do código)
        PaginacaoUtil.prepararIntervalo(typedQuery, pageable);
        PaginacaoUtil.preencherParametros(parametros, typedQuery);
        List<Motorista> motoristas = typedQuery.getResultList();

        // Para o total de registros, a entidade também é Motorista
        long totalMotoristas = PaginacaoUtil.getTotalRegistros("Motorista", "m", condicoes, parametros, em);

        return new PageImpl<>(motoristas, pageable, totalMotoristas);
    }

    private void preencherCondicoesEParametros(MotoristaFilter filtro, StringBuilder condicoes,
            Map<String, Object> parametros) {
        boolean primeiraCondicao = true; // Renomeado para maior clareza, funciona como 'condicao' no seu original

        // Código
        if (filtro.getCodigo() != null) {
            primeiraCondicao = PaginacaoUtil.fazerLigacaoCondicoes(condicoes, primeiraCondicao);
            condicoes.append("m.codigo = :codigo");
            parametros.put("codigo", filtro.getCodigo());
        }
        // Nome
        if (StringUtils.hasText(filtro.getNome())) {
            primeiraCondicao = PaginacaoUtil.fazerLigacaoCondicoes(condicoes, primeiraCondicao);
            condicoes.append("lower(m.nome) like :nome");
            parametros.put("nome", "%" + filtro.getNome().toLowerCase() + "%");
        }
        // CPF
        if (StringUtils.hasText(filtro.getCpf())) {
            primeiraCondicao = PaginacaoUtil.fazerLigacaoCondicoes(condicoes, primeiraCondicao);
            condicoes.append("m.cpf like :cpf");
            parametros.put("cpf", "%" + filtro.getCpf() + "%");
        }
        // CNH (ESPECÍFICO DE MOTORISTA)
        if (StringUtils.hasText(filtro.getCnh())) {
            primeiraCondicao = PaginacaoUtil.fazerLigacaoCondicoes(condicoes, primeiraCondicao);
            condicoes.append("lower(m.cnh) like :cnh"); // Ou "m.cnh = :cnh" se quiser correspondência exata
            parametros.put("cnh", "%" + filtro.getCnh().toLowerCase() + "%");
        }
        // Data de Nascimento Inicial
        if (filtro.getDataNascimentoInicial() != null) {
            primeiraCondicao = PaginacaoUtil.fazerLigacaoCondicoes(condicoes, primeiraCondicao);
            condicoes.append("m.dataNascimento >= :dataNascimentoInicial");
            parametros.put("dataNascimentoInicial", filtro.getDataNascimentoInicial());
        }
        // Data de Nascimento Final
        if (filtro.getDataNascimentoFinal() != null) {
            primeiraCondicao = PaginacaoUtil.fazerLigacaoCondicoes(condicoes, primeiraCondicao);
            condicoes.append("m.dataNascimento <= :dataNascimentoFinal");
            parametros.put("dataNascimentoFinal", filtro.getDataNascimentoFinal());
        }
        // Status (já é tratado no final do método pesquisar, mas se precisar de um
        // filtro adicional aqui)
        // Se o status já é sempre ATIVO no final, este bloco pode ser removido se o
        // filtro for apenas para mudar para INATIVO etc.
        // if (filtro.getStatus() != null) {
        // primeiraCondicao = PaginacaoUtil.fazerLigacaoCondicoes(condicoes,
        // primeiraCondicao);
        // condicoes.append("m.status = :status");
        // parametros.put("status", filtro.getStatus());
        // }
    }
}