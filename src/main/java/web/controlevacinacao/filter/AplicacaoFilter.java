package web.controlevacinacao.filter;

import java.time.LocalDate;

public class AplicacaoFilter {
    private Long codigo;
    private LocalDate dataInicial;
    private LocalDate dataFinal;
    private String cpf;
    private Long codigoLote;

    public Long getCodigo() {
        return codigo;
    }

    public void setCodigo(Long codigo) {
        this.codigo = codigo;
    }

    public LocalDate getDataInicial() {
        return dataInicial;
    }

    public void setDataInicial(LocalDate dataInicial) {
        this.dataInicial = dataInicial;
    }

    public LocalDate getDataFinal() {
        return dataFinal;
    }

    public void setDataFinal(LocalDate dataFinal) {
        this.dataFinal = dataFinal;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public Long getCodigoLote() {
        return codigoLote;
    }

    public void setCodigoLote(Long codigoLote) {
        this.codigoLote = codigoLote;
    }

    @Override
    public String toString() {
        return "AplicacaoFilter [codigo=" + codigo + ", dataInicial=" + dataInicial + ", dataFinal=" + dataFinal
                + ", cpf=" + cpf + ", codigoLote=" + codigoLote + "]";
    }

}
