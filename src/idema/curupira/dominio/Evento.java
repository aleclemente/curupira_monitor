package idema.curupira.dominio;

import java.util.Date;

public class Evento {
	
	public Evento(Inner inner, int tipoEvento, Cartao cartao) {
		this.inner = inner;
		this.tipoEvento = tipoEvento;
		this.cartao = cartao;
	}
	
	public int id;
	public Date data;
	public Inner inner;
	public int tipoEvento;
	public Cartao cartao;

}
