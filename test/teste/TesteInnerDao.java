package teste;

import java.util.List;

import idema.curupira.dao.InnerDao;
import idema.curupira.dominio.Inner;

public class TesteInnerDao {

	public static void main(String[] args) {
		
		InnerDao iDAO = new InnerDao();
		try {
			List<Inner> inners = iDAO.listaTodos();
			
			for (Inner inner : inners) {
				System.out.println(inner.numero);
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		
	}
	
}
