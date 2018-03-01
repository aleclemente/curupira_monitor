package idema.curupira.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import idema.curupira.dominio.Acesso;
import idema.curupira.dominio.Inner;

public class InnerDao {

	Connection conexao;

	public InnerDao () {
		try {
			conexao = ConexaoDao.getInstance();
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	public List<Inner> listaTodos() throws Exception {

		List<Inner> inners = new ArrayList<>();
		if (conexao != null) {

			StringBuilder sql = new StringBuilder();
			sql.append("SELECT inner_id, i.padrao_cartao_id, nome, count_tentativas_envio_comando, ");
			sql.append("count_repeat_ping_online, verificacao, identificacao, count_ping_fail, ");
			sql.append("numero, qtd_digitos, cnt_do_events, i.tipo_leitor_id, valor_leitor1, valor_leitor2,");
			sql.append("tentativas_coleta, inner_acesso_bio, i.equipamento_id, porta, i.tipo_conexao_id, ");
			sql.append("tempo_inicial_ping_on_line, tempo_coleta, temporizador, tempo_inicial_mensagem, ");
			sql.append("variacao_inner, estado_teclado, estado_atual, estadosolicitacao_ping_on_line, dois_leitores,");
			sql.append("catraca, biometrico, teclado, lista, lista_bio, inner_net_acesso, rele, lista_horario,");
			sql.append("entrada_catraca_direita, linha_inner, versao_inner, modelo_bio_inner, versao_bio,");
			sql.append("caminho_dados, e.codigo as equipamento, pc.codigo as padrao_cartao, tc.codigo as tipo_conexao, ");
			sql.append("tl.codigo as tipo_leitor ");
			sql.append("FROM \"inner\" i");
			sql.append("	INNER JOIN equipamento e ON (i.equipamento_id = e.equipamento_id) ");
			sql.append("	INNER JOIN padrao_cartao pc ON (i.padrao_cartao_id = pc.padrao_cartao_id) ");
			sql.append("	INNER JOIN tipo_conexao tc ON (i.tipo_conexao_id = tc.tipo_conexao_id) ");
			sql.append("	INNER JOIN tipo_leitor tl ON (i.tipo_leitor_id = tl.tipo_leitor_id) ");
			


			ResultSet rs = conexao.prepareStatement(sql.toString()).executeQuery();
			while (rs.next()) {
				Inner inner = new Inner();

				inner.id = rs.getLong("inner_id");
				inner.padraoCartao = rs.getInt("padrao_cartao");
				inner.countTentativasEnvioComando = rs.getInt("count_tentativas_envio_comando");
				inner.countRepeatPingOnline = rs.getInt("count_repeat_ping_online");
				inner.verificacao = rs.getInt("verificacao");
				inner.identificacao = rs.getInt("identificacao");
				inner.countPingFail = rs.getInt("count_ping_fail");
				inner.numero = rs.getInt("numero");
				inner.qtdDigitos = rs.getInt("qtd_digitos");
				inner.cntDoEvents = rs.getInt("cnt_do_events");
				inner.tipoLeitor = rs.getInt("tipo_leitor");
				inner.valorLeitor1 = rs.getInt("valor_leitor1");
				inner.valorLeitor2 = rs.getInt("valor_leitor2");
				inner.tentativasColeta = rs.getInt("tentativas_coleta");
				inner.innerAcessoBio = rs.getInt("inner_acesso_bio");
				inner.equipamento = rs.getInt("equipamento");
				inner.porta = rs.getInt("porta");
				inner.tipoConexao = rs.getInt("tipo_conexao");

				inner.tempoInicialPingOnLine = rs.getLong("tempo_inicial_ping_on_line");
				inner.tempoColeta = rs.getLong("tempo_coleta");
				inner.temporizador = rs.getLong("temporizador");
				inner.tempoInicialMensagem = rs.getLong("tempo_inicial_mensagem");
				inner.variacaoInner = rs.getLong("variacao_inner");
				/*
					inner.estadoTeclado= rs.getInt("estado_teclado");
					inner.estadoAtual = rs.getInt("estado_atual");
					inner.estadoSolicitacaoPingOnLine = rs.getInt("estadosolicitacao_ping_on_line");
				 */
				inner.doisLeitores = rs.getBoolean("dois_leitores");
				inner.catraca = rs.getBoolean("catraca");
				inner.biometrico = rs.getBoolean("biometrico");
				inner.teclado = rs.getBoolean("teclado");
				inner.lista = rs.getBoolean("lista");
				inner.listaBio = rs.getBoolean("lista_bio");
				inner.innerNetAcesso = rs.getBoolean("inner_net_acesso");
				inner.rele = rs.getBoolean("rele");
				inner.listaHorario = rs.getBoolean("lista_horario");
				inner.entradaCatracaDireita = rs.getBoolean("entrada_catraca_direita");

				inner.linhaInner = rs.getString("linha_inner");
				inner.versaoInner = rs.getString("versao_inner");
				inner.modeloBioInner = rs.getString("modelo_bio_inner");
				inner.versaoBio = rs.getString("versao_bio");
				inner.caminhoDados = rs.getString("caminho_dados");

				inners.add(inner);
			}
		}
		return inners; 
	}
	
	
	public int atualizar(Inner inner) throws Exception {

		if (conexao != null) {

			StringBuilder sql = new StringBuilder();
			sql.append("UPDATE \"inner\" ");
			sql.append("SET	linha_inner = ?, variacao_inner = ?, versao_inner = ?, modelo_bio_inner = ?, versao_bio = ?, inner_net_acesso = ? ");
			sql.append("WHERE inner_id = ?");
			
			PreparedStatement ps = conexao.prepareStatement(sql.toString());
			ps.setString(1, inner.linhaInner);
			ps.setLong(2, inner.variacaoInner);
			ps.setString(3, inner.versaoInner);
			ps.setString(4, inner.modeloBioInner);
			ps.setString(5, inner.versaoBio);
			ps.setBoolean(6, inner.innerNetAcesso);
			ps.setLong(7, inner.id);
			
			ps.executeUpdate();

			return ps.getUpdateCount();
		}
		
		return 0;
	}
	
	
	public static void main(String[] args) {
		//System.out.println("Teste");

		InnerDao id = new InnerDao();
		try {
			List<Inner> inners = id.listaTodos();
			//System.out.println("Teste");
			for (Inner inner : inners) {
				//System.out.println(inner.equipamento);
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

}
