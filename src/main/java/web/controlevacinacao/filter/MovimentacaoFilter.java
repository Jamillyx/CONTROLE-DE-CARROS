package web.controlevacinacao.filter;

import java.time.LocalDateTime;
import web.controlevacinacao.model.StatusMovimentacao;

public class MovimentacaoFilter {
    private Long codigo;
    private String placaCarro;
    private Long codigoMotorista;
    private LocalDateTime dataSaidaInicial;
    private LocalDateTime dataSaidaFinal;
    private Long kmSaida;
    private LocalDateTime dataRetornoInicial;
    private LocalDateTime dataRetornoFinal; // Pode ser nulo se a movimentação estiver ativa
    private Long kmRetorno; // Pode ser nulo se a movimentação estiver ativa
    private StatusMovimentacao statusMovimentacao;

    
    public Long getCodigo() {
        return codigo;
    }


    public void setCodigo(Long codigo) {
        this.codigo = codigo;
    }


    public String getPlacaCarro() {
        return placaCarro;
    }


    public void setPlacaCarro(String placaCarro) {
        this.placaCarro = placaCarro;
    }


    public Long getCodigoMotorista() {
        return codigoMotorista;
    }


    public void setCodigoMotorista(Long codigoMotorista) {
        this.codigoMotorista = codigoMotorista;
    }


    public LocalDateTime getDataSaidaInicial() {
        return dataSaidaInicial;
    }

    public void setDataSaidaInicial(LocalDateTime dataSaidaInicial) {
        this.dataSaidaInicial = dataSaidaInicial;
    }
     public LocalDateTime getDataSaidaFinal() {
        return dataSaidaFinal;
    }

    public void setDataSaidaFinal(LocalDateTime dataSaidaFinal) {
        this.dataSaidaFinal = dataSaidaFinal;
    }

    public Long getKmSaida() {
        return kmSaida;
    }

    public void setKmSaida(Long kmSaida) {
        this.kmSaida = kmSaida;
    }

    public LocalDateTime getDataRetornoInicial() {
        return dataRetornoInicial;
    }

    public void setDataRetornoInicial(LocalDateTime dataRetornoInicial) {
        this.dataRetornoInicial = dataRetornoInicial;
    }

     public LocalDateTime getDataRetornoFinal() {
        return dataRetornoFinal;
    }

    public void setDataRetornoFinal(LocalDateTime dataRetornoFinal) {
        this.dataRetornoFinal = dataRetornoFinal;
    }
    public Long getKmRetorno() {
        return kmRetorno;
    }

    public void setKmRetorno(Long kmRetorno) {
        this.kmRetorno = kmRetorno;
    }

    public StatusMovimentacao getStatusMovimentacao() {
        return statusMovimentacao;
    }

    public void setStatusMovimentacao(StatusMovimentacao statusMovimentacao) {
        this.statusMovimentacao = statusMovimentacao;
    }


    @Override
    public String toString() {
        return "MovimentacaoFilter [codigo=" + codigo + ", placaCarro=" + placaCarro + ", codigoMotorista="
                + codigoMotorista + ", dataSaidaInicial=" + dataSaidaInicial + ", dataSaidaFinal=" + dataSaidaFinal
                + ", kmSaida=" + kmSaida + ", dataRetornoInicial=" + dataRetornoInicial + ", dataRetornoFinal="
                + dataRetornoFinal + ", kmRetorno=" + kmRetorno + ", statusMovimentacao=" + statusMovimentacao + "]";
    }

    


   

    

}
