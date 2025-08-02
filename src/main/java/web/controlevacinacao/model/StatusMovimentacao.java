
package web.controlevacinacao.model;

public enum StatusMovimentacao {
    FINALIZADA("Finalizada"),
    ATIVA("Ativa");

    private final String descricao;

    StatusMovimentacao(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
