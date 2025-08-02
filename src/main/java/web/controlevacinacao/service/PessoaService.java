package web.controlevacinacao.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import web.controlevacinacao.model.Pessoa;
import web.controlevacinacao.model.Status;
import web.controlevacinacao.repository.PessoaRepository;

@Service
@Transactional
public class PessoaService {

    private PessoaRepository pessoaRepository;

    public PessoaService(PessoaRepository pessoaRepository) {
        this.pessoaRepository = pessoaRepository;
    }

    public void salvar(Pessoa pessoa, BindingResult result) {

        // Verificação de CPF duplicado (novo cadastro)
        Pessoa pessoaExistente = pessoaRepository.findByCpf(pessoa.getCpf());
        if (pessoaExistente != null && !pessoaExistente.getCodigo().equals(pessoa.getCodigo())) {
            // Se o CPF já existe E não é a mesma pessoa sendo alterada, adicione um erro
            result.rejectValue("cpf", "cpf.duplicado", "CPF já cadastrado no sistema.");
            
        }

          // Somente salva se não houver erros no BindingResult (incluindo os adicionados acima)
        if (!result.hasErrors()) {
            pessoaRepository.save(pessoa);
        }
    }

    public void alterar(Pessoa pessoa) {
        pessoaRepository.save(pessoa);
    }

    public void remover(Long codigo) {
        Pessoa pessoa = pessoaRepository.findByCodigoAndStatus(codigo, Status.ATIVO);
        if (pessoa == null) {
            throw new RuntimeException("Remoção de Pessoa com codigo inválido");
        } else {
            pessoa.setStatus(Status.INATIVO);
            pessoaRepository.save(pessoa);
        }
    }

    public void alterar(Pessoa pessoa, BindingResult resultado) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'alterar'");
    }

}
