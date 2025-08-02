package web.controlevacinacao.model;

public enum Status {

	ATIVO("ativo"),
	EM_MOVIMENTACAO("Em Movimentação"),
	INATIVO("inativo");
	
	private String descricao;
	
	private Status(String descricao) {
		this.descricao = descricao;
	}
	
	public String getDescricao() {
		return descricao;
	}
}
