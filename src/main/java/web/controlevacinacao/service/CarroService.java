package web.controlevacinacao.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult; // <<== NOVO IMPORT

import web.controlevacinacao.model.Carro;
import web.controlevacinacao.model.StatusCarro;
import web.controlevacinacao.repository.CarroRepository;

import java.util.Optional; // <<== NOVO IMPORT

@Service
@Transactional // Aplica transações a todos os métodos públicos desta classe
public class CarroService {

    private final CarroRepository carroRepository;

    public CarroService(CarroRepository carroRepository) {
        this.carroRepository = carroRepository;
    }

    // --- MÉTODO SALVAR ATUALIZADO COM VALIDAÇÃO DE PLACA ---
    public void salvar(Carro carro, BindingResult resultado) { // <<== AGORA RECEBE BindingResult

        // Lógica para verificar se a placa já existe no banco de dados

        Optional<Carro> carroExistente;

        // Se o carro não tem código, significa que é um NOVO CADASTRO
        if (carro.getCodigo() == null) {
            // Buscamos qualquer carro com a placa informada
            carroExistente = carroRepository.findByPlaca(carro.getPlaca());
        } else { // Se o carro JÁ TEM CÓDIGO, significa que é uma ALTERAÇÃO
            // Buscamos um carro com a placa informada, mas que NÃO seja o carro que estamos editando
            carroExistente = carroRepository.findByPlacaAndCodigoNot(carro.getPlaca(), carro.getCodigo());
        }

        // Se um carro com a mesma placa (e diferente código, se for alteração) foi encontrado
        if (carroExistente.isPresent()) {
            // Adicionamos um erro ao BindingResult, associado ao campo "placa"
            resultado.rejectValue("placa", "placa.ja.existe", "Já existe um carro cadastrado com esta placa.");
            // Não vamos lançar uma exceção aqui, apenas adicionar o erro.
            // O controller verificará 'resultado.hasErrors()' depois de chamar este método.
            return; // Sai do método sem tentar salvar no banco
        }

        // Se a validação da placa passou (ou não era uma placa duplicada)
        carroRepository.save(carro); // Salva o carro no banco de dados
    }

    // --- MÉTODO ALTERAR ATUALIZADO (se quiser aplicar a mesma validação) ---
    // É uma boa prática ter a mesma validação de placa para evitar duplicidade na alteração
    public void alterar(Carro carro, BindingResult resultado) { // <<== AGORA RECEBE BindingResult
       Optional<Carro> carroExistente;

        // Se o carro não tem código, significa que é um NOVO CADASTRO
        if (carro.getCodigo() == null) {
            // Buscamos qualquer carro com a placa informada
            carroExistente = carroRepository.findByPlaca(carro.getPlaca());
        } else { // Se o carro JÁ TEM CÓDIGO, significa que é uma ALTERAÇÃO
            // Buscamos um carro com a placa informada, mas que NÃO seja o carro que estamos editando
            carroExistente = carroRepository.findByPlacaAndCodigoNot(carro.getPlaca(), carro.getCodigo());
        }

        // Se um carro com a mesma placa (e diferente código, se for alteração) foi encontrado
        if (carroExistente.isPresent()) {
            // Adicionamos um erro ao BindingResult, associado ao campo "placa"
            resultado.rejectValue("placa", "placa.ja.existe", "Já existe um carro cadastrado com esta placa.");
            // Não vamos lançar uma exceção aqui, apenas adicionar o erro.
            // O controller verificará 'resultado.hasErrors()' depois de chamar este método.
            return; // Sai do método sem tentar salvar no banco
        }

        // Se a validação da placa passou (ou não era uma placa duplicada)
        carroRepository.save(carro); // Salva o carro no banco de dados
    }

    public void remover(Long codigo) {
        // Busca o carro pelo código e status ATIVO
        Carro carro = carroRepository.findByCodigoAndStatusCarro(codigo, StatusCarro.ATIVO);
        if (carro == null) {
            // Lança uma exceção se o carro não for encontrado ou já estiver inativo
            throw new RuntimeException("Remoção de Carro com código inválido ou já inativo.");
        } else {
            // Altera o status para INATIVO (remoção lógica)
            carro.setStatusCarro(StatusCarro.INATIVO);
            carroRepository.save(carro);
        }
    }
}