package teste;

import idema.curupira.dao.AcessoDao;
import idema.curupira.dao.CartaoDao;
import idema.curupira.dominio.Cartao;
import idema.curupira.dominio.Evento;
import idema.curupira.dominio.Inner;
import idema.curupira.dominio.Acesso;
import idema.curupira.dominio.AcessoTipo;

public class TesteAcessoDao {
	public static void main(String[] args) {
		
		AcessoDao eDAO = new AcessoDao();
		
		try {
			
			//eDAO.inserir(new Acesso(0, 1, 2, 1));
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		

	}
}
