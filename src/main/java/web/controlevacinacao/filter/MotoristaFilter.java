package web.controlevacinacao.filter;

import java.time.LocalDate;
import web.controlevacinacao.model.Status;

public class MotoristaFilter {

    private Long codigo; // Adicionado para pesquisa por código
    private String nome;
    private String cpf;
    private String cnh; // Campo específico de Motorista
    private LocalDate dataNascimentoInicial; // Para pesquisa por intervalo
    private LocalDate dataNascimentoFinal;   // Para pesquisa por intervalo
    private Status status;

    // Construtor padrão
    public MotoristaFilter() {
    }

    // Getters e Setters para todos os campos
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

    public String getCnh() {
        return cnh;
    }

    public void setCnh(String cnh) { // Corrigido o nome do setter aqui
        this.cnh = cnh;
    }

    public LocalDate getDataNascimentoInicial() {
        return dataNascimentoInicial;
    }

    public void setDataNascimentoInicial(LocalDate dataNascimentoInicial) {
        this.dataNascimentoInicial = dataNascimentoInicial;
    }

    public LocalDate getDataNascimentoFinal() {
        return dataNascimentoFinal;
    }

    public void setDataNascimentoFinal(LocalDate dataNascimentoFinal) {
        this.dataNascimentoFinal = dataNascimentoFinal;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "MotoristaFilter{" +
               "codigo=" + codigo +
               ", nome='" + nome + '\'' +
               ", cpf='" + cpf + '\'' +
               ", cnh='" + cnh + '\'' +
               ", dataNascimentoInicial=" + dataNascimentoInicial +
               ", dataNascimentoFinal=" + dataNascimentoFinal +
               ", status=" + status +
               '}';
    }
}