package teste;

import idema.curupira.dao.CartaoDao;
import idema.curupira.dominio.Cartao;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;


/**
 *
 * @author Alexandre
 */
public class TesteCartao {

    CartaoDao cartaoDao = new CartaoDao();
    Cartao cartao = new Cartao();

    @Before
    public void configurarCartao() {
        cartao.setCodigo_barra("3189663969");
    }

    @Test
    public void testarCartaoPorCodigoBarra() throws Exception {
        Cartao cartaoTeste = cartaoDao.buscarPorCodigoBarra(cartao);
        assertEquals("3189663969", cartaoTeste.getCodigo_barra());
    }

}

/**
 * @author Alexandre Clemente
 * @version 1.0 Classe criada para testar a classe Armazenamento.
 *
 * public class TestarClasseArmazenamento {
 *
 * Armazenamento armazenamento = new Armazenamento();
 *
 * @Before public void apagarTodosArquivosDoDiretorioAntesDeCadaTeste() { armazenamento.apagarTodosArquivosDoDiretorio(); }
 *
 * @After public void apagarTodosArquivosDoDiretorioDepoisDeCadaTeste() { armazenamento.apagarTodosArquivosDoDiretorio(); }
 *
 * @Test public void testarDiretorioVazio() { assertEquals(true, armazenamento.verificarDiretorioVazio()); }
 *
 * @Test public void testarArmazenarUmPontoTipoEstrela() throws IOException { armazenamento.armazenarPontoDeUsuarioPorTipo("Usuario01", "Estrela01", 10);
 *
 * assertEquals(10, armazenamento.getQuantidadeDePontosDoUsuarioPorTipo("Usuario01", "Estrela01")); }
 *
 * @Test public void testarArmazenarDoisPontosTipoEstrela() throws IOException { armazenamento.armazenarPontoDeUsuarioPorTipo("Usuario02", "Estrela02", 10); armazenamento.armazenarPontoDeUsuarioPorTipo("Usuario02", "Estrela02", 10);
 *
 * assertEquals(20, armazenamento.getQuantidadeDePontosDoUsuarioPorTipo("Usuario02", "Estrela02")); }
 *
 * @Test public void testarArmazenarTresPontosTipoEstrela() throws IOException { armazenamento.armazenarPontoDeUsuarioPorTipo("Usuario03", "Estrela03", 10); armazenamento.armazenarPontoDeUsuarioPorTipo("Usuario03", "Estrela03", 10); armazenamento.armazenarPontoDeUsuarioPorTipo("Usuario03", "Estrela03", 10);
 *
 * assertEquals(30, armazenamento.getQuantidadeDePontosDoUsuarioPorTipo("Usuario03", "Estrela03")); }
 *
 * @Test public void testarArmazenarQuatroPontosTipoEstrela() throws IOException { armazenamento.armazenarPontoDeUsuarioPorTipo("Usuario04", "Estrela04", 10); armazenamento.armazenarPontoDeUsuarioPorTipo("Usuario04", "Estrela04", 20); armazenamento.armazenarPontoDeUsuarioPorTipo("Usuario04", "Estrela04", 30); armazenamento.armazenarPontoDeUsuarioPorTipo("Usuario04", "Estrela04", 40);
 *
 * assertEquals(100, armazenamento.getQuantidadeDePontosDoUsuarioPorTipo("Usuario04", "Estrela04")); }
 *
 * @Test public void testarArmazenarUmPontoTipoMoeda() throws IOException { armazenamento.armazenarPontoDeUsuarioPorTipo("Usuario05", "Moeda05", 10);
 *
 * assertEquals(10, armazenamento.getQuantidadeDePontosDoUsuarioPorTipo("Usuario05", "Moeda05")); }
 *
 * @Test public void testarArmazenarUmPontoTipoEstrelaEUmPontoTipoMoeda() throws IOException { armazenamento.armazenarPontoDeUsuarioPorTipo("Usuario06", "Estrela06", 20); armazenamento.armazenarPontoDeUsuarioPorTipo("Usuario06", "Moeda06", 20);
 *
 * assertEquals(20, armazenamento.getQuantidadeDePontosDoUsuarioPorTipo("Usuario06", "Estrela06")); assertEquals(20, armazenamento.getQuantidadeDePontosDoUsuarioPorTipo("Usuario06", "Moeda06")); }
 *
 * @Test public void testarArmazenarUsuariosComDiferentesTiposDePontuacao() throws IOException { armazenamento.armazenarPontoDeUsuarioPorTipo("Usuario07", "Estrela07", 30); armazenamento.armazenarPontoDeUsuarioPorTipo("Usuario07", "Moeda07", 30);
 *
 * armazenamento.armazenarPontoDeUsuarioPorTipo("Usuaria07", "Estrela07", 30); armazenamento.armazenarPontoDeUsuarioPorTipo("Usuaria07", "Moeda07", 30);
 *
 * assertEquals(30, armazenamento.getQuantidadeDePontosDoUsuarioPorTipo("Usuario07", "Estrela07")); assertEquals(30, armazenamento.getQuantidadeDePontosDoUsuarioPorTipo("Usuario07", "Moeda07"));
 *
 * assertEquals(30, armazenamento.getQuantidadeDePontosDoUsuarioPorTipo("Usuaria07", "Estrela07")); assertEquals(30, armazenamento.getQuantidadeDePontosDoUsuarioPorTipo("Usuaria07", "Moeda07")); }
 *
 * @Test public void testarTodosOsUsuariosComAlgumaPontuacao() throws IOException { armazenamento.armazenarPontoDeUsuarioPorTipo("Usuario08", "Estrela08", 30); armazenamento.armazenarPontoDeUsuarioPorTipo("Usuario08", "Moeda08", 30); armazenamento.armazenarPontoDeUsuarioPorTipo("Usuaria08", "Estrela08", 30); armazenamento.armazenarPontoDeUsuarioPorTipo("Usuaria08", "Moeda08", 30);
 *
 * ArrayList<String> listaUsuarios = armazenamento.getTodosUsuariosComPonto();
 *
 * assertEquals(true, listaUsuarios.contains("Usuario08")); assertEquals(true, listaUsuarios.contains("Usuaria08")); assertEquals(false, listaUsuarios.contains("Usuario09")); }
 *
 * @Test public void testarTodosOsTipoDePontuacaoRegistrados() throws IOException { armazenamento.armazenarPontoDeUsuarioPorTipo("Usuario09", "Estrela09", 30); armazenamento.armazenarPontoDeUsuarioPorTipo("Usuario09", "Moeda09", 30); armazenamento.armazenarPontoDeUsuarioPorTipo("Usuaria09", "Estrela09", 30); armazenamento.armazenarPontoDeUsuarioPorTipo("Usuaria09", "Moeda09", 30);
 *
 * ArrayList<String> listaTipoPontos = armazenamento.getTodosTiposPontuacaoRegistrados();
 *
 * assertEquals(true, listaTipoPontos.contains("Estrela09")); assertEquals(true, listaTipoPontos.contains("Moeda09")); assertEquals(false, listaTipoPontos.contains("Comentario09")); }
 *
 * }
 *
 */
