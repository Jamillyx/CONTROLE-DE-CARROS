package web.controlevacinacao.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import web.controlevacinacao.model.Carro;
import web.controlevacinacao.model.StatusCarro;
import web.controlevacinacao.repository.queries.carro.CarroQueries; // Importe a interface CarroQueries

@Repository
public interface CarroRepository extends JpaRepository<Carro, Long>, CarroQueries {

    // Método de busca simples por código e status (igual ao Pessoa)
    Carro findByCodigoAndStatusCarro(Long codigo, StatusCarro ativo);
    // --- NOVOS MÉTODOS PARA A VALIDAÇÃO DA PLACA ---
    // 1. Para verificar se a placa já existe para QUALQUER carro (útil para cadastro)
    Optional<Carro> findByPlaca(String placa);

    // 2. Para verificar se a placa já existe para outro carro, EXCLUINDO o carro que está sendo alterado
    // (útil para alteração, onde o próprio carro pode ter a mesma placa)
    Optional<Carro> findByPlacaAndCodigoNot(String placa, Long codigo);
    Object findByStatusCarro(StatusCarro disponivel);


}