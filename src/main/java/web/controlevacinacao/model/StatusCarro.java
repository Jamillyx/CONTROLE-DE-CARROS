package web.controlevacinacao.model;

public enum StatusCarro {
    DISPONIVEL("Disponível"),
    EM_USO("Em Uso"),
    MANUTENCAO("Em Manutenção"),
    INATIVO("Inativo"), // Para exclusão lógica, se aplicável ao carro
    ATIVO("Ativo");

    private final String descricao;

    StatusCarro(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}