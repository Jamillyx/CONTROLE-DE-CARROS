package web.controlevacinacao.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "movimentacao")
public class Movimentacao implements Serializable {

    @Id
    @SequenceGenerator(name="gerador6", sequenceName="movimentacao_codigo_seq", allocationSize=1)
    @GeneratedValue(generator="gerador6", strategy=GenerationType.SEQUENCE)
    private Long codigo;

    @ManyToOne
    @JoinColumn(name = "codigo_carro", nullable = false)
    @NotNull(message = "O carro é obrigatório")
    private Carro carro;

    @ManyToOne
    @JoinColumn(name = "codigo_motorista", nullable = false)
    @NotNull(message = "O motorista é obrigatório")
    private Motorista motorista;

    @NotNull(message = "A data de saída é obrigatória.")
    @Column(name = "data_saida")
    @DateTimeFormat(pattern = "dd/MM/yyyy") // Formato que o MCDatepicker entrega
    private LocalDate dataSaida;

    @NotNull(message = "A hora de saída é obrigatória.")
    @Column(name = "hora_saida")
    @DateTimeFormat(pattern = "HH:mm") // Formato que o MDDateTimePicker entrega
    private LocalTime horaSaida;

    @Column(name = "data_retorno", nullable = true)
    @DateTimeFormat(pattern = "dd/MM/yyyy") // Formato que o MCDatepicker entrega
    private LocalDate dataRetorno;

    @Column(name = "hora_retorno", nullable = true)
    @DateTimeFormat(pattern = "HH:mm") // Formato que o MDDateTimePicker entrega
    private LocalTime horaRetorno;


    @Column(name = "km_saida", nullable = false)
    //@NotNull(message = "A quilometragem de saída é obrigatória")
    @Min(value = 0, message = "A quilometragem de saída deve ser maior ou igual a zero")
    private Long kmSaida;


    @Column(name = "km_retorno", nullable = true)
    @Min(value = 0, message = "A quilometragem de retorno deve ser maior ou igual a zero")
    private Long kmRetorno; // Pode ser nulo se a movimentação estiver ativa

    @Enumerated(EnumType.STRING) // <<--- Remova "org.hibernate.type.EnumType." se estiver lá
    @Column(name = "status")
    private StatusMovimentacao statusMovimentacao;

    // --- Métodos de ciclo de vida JPA para definir o status ---
    
    @PreUpdate // Chamado antes da entidade ser atualizada
    public void preUpdate() {
        // Lógica para atualizar status (ex: se dataRetorno e kmRetorno forem preenchidos, muda para FINALIZADA)
      /*   if (this.dataRetorno != null && this.kmRetorno != null && this.statusMovimentacao != StatusMovimentacao.FINALIZADA) {
            this.statusMovimentacao = StatusMovimentacao.FINALIZADA;
        } else if (this.dataRetorno == null && this.kmRetorno == null && this.statusMovimentacao != StatusMovimentacao.ATIVA) {
            this.statusMovimentacao = StatusMovimentacao.ATIVA;
        }
        */
    }

    public Movimentacao() {
    }

    public Long getCodigo() {
        return codigo;
    }

    public void setCodigo(Long codigo) {
        this.codigo = codigo;
    }

    public Carro getCarro() {
        return carro;
    }

    public void setCarro(Carro carro) {
        this.carro = carro;
    }

    public Motorista getMotorista() {
        return motorista;
    }

    public void setMotorista(Motorista motorista) {
        this.motorista = motorista;
    }

   

    public LocalDate getDataSaida() {
        return dataSaida;
    }

    public void setDataSaida(LocalDate dataSaida) {
        this.dataSaida = dataSaida;
    }

    public LocalTime getHoraSaida() {
        return horaSaida;
    }

    public void setHoraSaida(LocalTime horaSaida) {
        this.horaSaida = horaSaida;
    }

    public LocalDate getDataRetorno() {
        return dataRetorno;
    }

    public void setDataRetorno(LocalDate dataRetorno) {
        this.dataRetorno = dataRetorno;
    }

    public LocalTime getHoraRetorno() {
        return horaRetorno;
    }

    public void setHoraRetorno(LocalTime horaRetorno) {
        this.horaRetorno = horaRetorno;
    }

    public Long getKmSaida() {
        return kmSaida;
    }

    public void setKmSaida(Long kmSaida) {
        this.kmSaida = kmSaida;
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
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((codigo == null) ? 0 : codigo.hashCode());
        result = prime * result + ((carro == null) ? 0 : carro.hashCode());
        result = prime * result + ((motorista == null) ? 0 : motorista.hashCode());
        result = prime * result + ((dataSaida == null) ? 0 : dataSaida.hashCode());
        result = prime * result + ((horaSaida == null) ? 0 : horaSaida.hashCode());
        result = prime * result + ((dataRetorno == null) ? 0 : dataRetorno.hashCode());
        result = prime * result + ((horaRetorno == null) ? 0 : horaRetorno.hashCode());
        result = prime * result + ((kmSaida == null) ? 0 : kmSaida.hashCode());
        result = prime * result + ((kmRetorno == null) ? 0 : kmRetorno.hashCode());
        result = prime * result + ((statusMovimentacao == null) ? 0 : statusMovimentacao.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Movimentacao other = (Movimentacao) obj;
        if (codigo == null) {
            if (other.codigo != null)
                return false;
        } else if (!codigo.equals(other.codigo))
            return false;
        if (carro == null) {
            if (other.carro != null)
                return false;
        } else if (!carro.equals(other.carro))
            return false;
        if (motorista == null) {
            if (other.motorista != null)
                return false;
        } else if (!motorista.equals(other.motorista))
            return false;
        if (dataSaida == null) {
            if (other.dataSaida != null)
                return false;
        } else if (!dataSaida.equals(other.dataSaida))
            return false;
        if (horaSaida == null) {
            if (other.horaSaida != null)
                return false;
        } else if (!horaSaida.equals(other.horaSaida))
            return false;
        if (dataRetorno == null) {
            if (other.dataRetorno != null)
                return false;
        } else if (!dataRetorno.equals(other.dataRetorno))
            return false;
        if (horaRetorno == null) {
            if (other.horaRetorno != null)
                return false;
        } else if (!horaRetorno.equals(other.horaRetorno))
            return false;
        if (kmSaida == null) {
            if (other.kmSaida != null)
                return false;
        } else if (!kmSaida.equals(other.kmSaida))
            return false;
        if (kmRetorno == null) {
            if (other.kmRetorno != null)
                return false;
        } else if (!kmRetorno.equals(other.kmRetorno))
            return false;
        if (statusMovimentacao != other.statusMovimentacao)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Movimentacao [codigo=" + codigo + ", carro=" + carro + ", motorista=" + motorista + ", dataSaida="
                + dataSaida + ", horaSaida=" + horaSaida + ", dataRetorno=" + dataRetorno + ", horaRetorno="
                + horaRetorno + ", kmSaida=" + kmSaida + ", kmRetorno=" + kmRetorno + ", statusMovimentacao="
                + statusMovimentacao + "]";
    }


    
    
   
    

    
}
