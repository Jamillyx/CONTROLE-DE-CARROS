package web.controlevacinacao.filter;

public class CarroFilter {
    private Long codigo;
    private String modelo;
    private String cor;
    private Long kmInicial;
    private Long kmFinal;
    private String placa;
    
    public Long getKmInicial() {
        return kmInicial;
    }
    public void setKmInicial(Long kmInicial) {
        this.kmInicial = kmInicial;
    }
    public Long getKmFinal() {
        return kmFinal;
    }
    public void setKmFinal(Long kmFinal) {
        this.kmFinal = kmFinal;
    }
    public Long getCodigo() {
        return codigo;
    }
    public void setCodigo(Long codigo) {
        this.codigo = codigo;
    }
    public String getModelo() {
        return modelo;
    }
    public void setModelo(String modelo) {
        this.modelo = modelo;
    }
    public String getCor() {
        return cor;
    }
    public void setCor(String cor) {
        this.cor = cor;
    }

    public String getPlaca() {
        return placa;
    }
    public void setPlaca(String placa) {
        this.placa = placa;
    }
    @Override
    public String toString() {
        return "CarroFilter [codigo=" + codigo + ", modelo=" + modelo + ", cor=" + cor + ", kmInicial=" + kmInicial
                + ", kmFinal=" + kmFinal + ", placa=" + placa + "]";
    }

    

    
}
