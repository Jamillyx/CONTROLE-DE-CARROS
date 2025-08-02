package web.controlevacinacao.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn; // Adicione esta importação
import jakarta.persistence.DiscriminatorType; // Adicione esta importação
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance; // Adicione esta importação
import jakarta.persistence.InheritanceType; // Adicione esta importação
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@Entity
@Table(name = "pessoa")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE) // **NOVO: Define a estratégia de herança**
@DiscriminatorValue("PESSOA")
@DiscriminatorColumn(name = "tipo_entidade", // **NOVO: Coluna para identificar o tipo de entidade**
                     discriminatorType = DiscriminatorType.STRING)
public class Pessoa implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @SequenceGenerator(name="gerador2", sequenceName="pessoa_codigo_seq", allocationSize=1)
    @GeneratedValue(generator="gerador2", strategy=GenerationType.SEQUENCE)
    private Long codigo;

    @NotBlank(message = "O nome da pessoa é obrigatório")
    private String nome;

    @NotBlank(message = "O CPF da pessoa é obrigatório")
    @Pattern(regexp = "\\d{11}", message = "O CPF deve conter 11 dígitos") // Exemplo de validação para CNH
    @Column(unique = true)
    private String cpf;


    @NotNull(message = "A data de nascimento da pessoa é obrigatória")
    @Column(name = "data_nascimento")
    private LocalDate dataNascimento;

    @Enumerated(EnumType.STRING)
    private Status status = Status.ATIVO;

    // **NOVO: Construtor padrão (sem argumentos) é necessário para o JPA**
    public Pessoa() {
    }

    // **NOVO: Construtor para uso pelas subclasses ou para criar Pessoa diretamente**
    public Pessoa(String nome, String cpf, LocalDate dataNascimento) {
        this.nome = nome;
        this.cpf = cpf;
        this.dataNascimento = dataNascimento;
        this.status = Status.ATIVO; // Define o status padrão
    }


    public Long getCodigo() {
        return codigo;
    }

    public void setCodigo(Long codigo) {
        this.codigo = codigo;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public Status getStatus() {
        return status;
    }

     public boolean isAtivo() {
        return Status.ATIVO.equals(this.status);
    }

    public void setStatus(Status status) {
        this.status = status;
    }
  
    @Override
    public String toString() {
        return "Pessoa [codigo=" + codigo + ", nome=" + nome + ", cpf=" + cpf + ", dataNascimento=" + dataNascimento
                + ", status=" + status + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(codigo);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) // Se for a mesma instância, são iguais
            return true;
        if (obj == null) // Se o outro objeto for nulo, não são iguais
            return false;
        // Permite que uma subclasse seja igual a outra subclasse ou à classe pai,
        // desde que ambas sejam instâncias de Pessoa e tenham o mesmo código.
        if (!(obj instanceof Pessoa)) // Se o objeto não for uma instância de Pessoa (ou subclasse de Pessoa), não são iguais
            return false;
        Pessoa other = (Pessoa) obj;
        // A igualdade é baseada no código (ID)
        return Objects.equals(codigo, other.codigo);
    }
}