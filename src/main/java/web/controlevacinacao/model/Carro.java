package web.controlevacinacao.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "carro")
public class Carro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long codigo;

    @NotBlank(message = "A placa é obrigatória")
    @Size(min = 7, max = 8, message = "A placa deve ter 7 ou 8 caracteres (padrão Mercosul)")
    @Column(unique = true, nullable = false, length = 8)
    private String placa;

    @NotBlank(message = "O modelo é obrigatório")
    @Size(max = 50, message = "O modelo deve ter no máximo 50 caracteres")
    @Column(nullable = false, length = 50)
    private String modelo;

    @NotBlank(message = "A cor é obrigatória")
    @Size(max = 30, message = "A cor deve ter no máximo 30 caracteres")
    @Column(nullable = false, length = 30)
    private String cor;

    @NotNull(message = "A quilometragem atual é obrigatória")
    @Min(value = 0, message = "A quilometragem não pode ser negativa")
    @Column(nullable = false)
    private Long kmAtual; // Use Long para KM, pode ser alto

    // Status do Carro: DISPONIVEL, EM_USO, MANUTENCAO, INATIVO
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusCarro statusCarro = StatusCarro.ATIVO; // Status inicial

    // Construtores
    public Carro() {
    }

    // Getters e Setters
    public Long getCodigo() {
        return codigo;
    }

    public void setCodigo(Long codigo) {
        this.codigo = codigo;
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo; // Corrigindo para 'this.modelo = modelo;'
    }

    public String getCor() {
        return cor;
    }

    public void setCor(String cor) {
        this.cor = cor;
    }

    public Long getKmAtual() {
        return kmAtual;
    }

    public void setKmAtual(Long kmAtual) {
        this.kmAtual = kmAtual;
    }

    public StatusCarro getStatusCarro() {
        return statusCarro;
    }

    public void setStatusCarro(StatusCarro statusCarro) {
        this.statusCarro = statusCarro;
    }

      public boolean isAtivo() {
        return StatusCarro.ATIVO.equals(this.statusCarro);
    }
    

    @Override
    public String toString() {
        return "Carro [codigo=" + codigo + ", placa=" + placa + ", modelo=" + modelo + ", cor=" + cor + ", kmAtual="
                + kmAtual + ", statusCarro=" + statusCarro + "]";
    }

    // Equals e HashCode (importante para coleções e comparações)
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Carro carro = (Carro) o;
        return codigo != null ? codigo.equals(carro.codigo) : carro.codigo == null;
    }

    @Override
    public int hashCode() {
        return codigo != null ? codigo.hashCode() : 0;
    }

}