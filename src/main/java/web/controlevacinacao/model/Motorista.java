package web.controlevacinacao.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue; // Adicione esta importação
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

@Entity
@DiscriminatorValue("MOTORISTA") // **NOVO: Valor para a coluna 'tipo_entidade' quando for um Motorista**
public class Motorista extends Pessoa {

    @NotBlank(message = "A CNH do motorista é obrigatória")
    @Pattern(regexp = "\\d{11}", message = "A CNH deve conter 11 dígitos") // Exemplo de validação para CNH
    @Column(name = "cnh", unique = true) // CNH deve ser única
    private String cnh;

    // Construtor padrão obrigatório para o JPA
    public Motorista() {
        super(); // Chama o construtor padrão de Pessoa
    }

    // Construtor com todos os campos, incluindo os da superclasse
    public Motorista(String nome, String cpf, LocalDate dataNascimento, String cnh) {
        super(nome, cpf, dataNascimento); // Chama o construtor de Pessoa
        this.cnh = cnh;
    }

    public String getCnh() {
        return cnh;
    }

    public void setCnh(String cnh) {
        this.cnh = cnh;
    }
    

    @Override
    public String toString() {
        // Inclua os campos da superclasse para um toString completo
        return "Motorista [codigo=" + getCodigo() + ", nome=" + getNome() + ", cpf=" + getCpf() +
               ", dataNascimento=" + getDataNascimento() + ", status=" + getStatus() +
               ", cnh=" + cnh + "]";
    }
}