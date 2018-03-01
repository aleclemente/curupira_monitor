package idema.curupira.dominio;

import com.topdata.easyInner.enumeradores.Enumeradores;

public class Inner {
	public long id;
	
	public int padraoCartao;
	public int countTentativasEnvioComando;
	public int countRepeatPingOnline;
	public int verificacao;
	public int identificacao;
	public int countPingFail;
	public int numero;
	public int qtdDigitos = 10;
	public int cntDoEvents;
	public int tipoLeitor = 0;
	public int valorLeitor1;
	public int valorLeitor2;
	public int tentativasColeta;
	public int innerAcessoBio;
	public int equipamento;
	public int porta = 3570;
	public int tipoConexao = 2;

	public long tempoInicialPingOnLine;
	public long tempoColeta;
	public long temporizador;
	public long tempoInicialMensagem;
	public long variacaoInner;

	public Enumeradores.EstadosTeclado estadoTeclado;
	public Enumeradores.EstadosInner estadoAtual;
	public Enumeradores.EstadosInner estadoSolicitacaoPingOnLine;

	public boolean doisLeitores = false;
	public boolean catraca;
	public boolean biometrico = false;
	public boolean teclado;
	public boolean lista;
	public boolean listaBio;
	public boolean innerNetAcesso;
	public static boolean rele;
	public boolean listaHorario;
	public boolean entradaCatracaDireita;
	
	public String linhaInner;
	public String versaoInner;
	public String modeloBioInner;
	public String versaoBio;
	public static String caminhoDados;
	
	//dados temporarios
	public Cartao cartao;
	
}
