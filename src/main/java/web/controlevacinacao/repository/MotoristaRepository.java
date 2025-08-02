package web.controlevacinacao.repository;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import web.controlevacinacao.model.Motorista;
import web.controlevacinacao.model.Status;
import web.controlevacinacao.repository.queries.motorista.MotoristaQueries;

@Repository
public interface MotoristaRepository extends JpaRepository<Motorista, Long>, MotoristaQueries {
    Motorista findByCodigoAndStatus(Long codigo, Status status);
    
    Motorista findByCnh(String cnh); 
    
    List<Motorista> findByStatus(Status status);

    Optional<Motorista> findByCodigo(Long codigo);
}