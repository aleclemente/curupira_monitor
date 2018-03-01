package idema.curupira.dominio;

import java.util.Date;

public class Acesso {
	
	public Acesso() {
	}
	
	public Acesso(int inner, int tipoAcesso, long cartao, long pessoa) {
		this.innerId = inner;
		this.acessoTipoId = tipoAcesso;
		this.pessoaId = pessoa;
		this.cartaoId = cartao;
	}
	
	public int id;
	public int innerId;
	public int acessoTipoId;
	public long pessoaId;
	public long cartaoId;
	public Date dataRegistro; 
}
