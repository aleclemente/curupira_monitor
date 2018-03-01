package idema.curupira.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import idema.curupira.dominio.Cartao;

public class CartaoDao {

    Connection conexao;
    Cartao cartao;

    public CartaoDao() {
        try {
            conexao = ConexaoDao.getInstance();
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    public List<Cartao> listaModoOffLine() throws Exception {

        List<Cartao> cartoes = new ArrayList<>();
        if (conexao != null) {
            
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT cartao_id, codigo_barra, ativo, cartao_tipo_id, pessoa_id, created_at, updated_at");
            sql.append("FROM cartao c");
            sql.append("	INNER JOIN pessoa p ON (p.pessoa_id = c.pessoa_id)");
            sql.append("WHERE ativo = 1 ");

            ResultSet rs = conexao.prepareStatement(sql.toString()).executeQuery();
            while (rs.next()) {
                this.cartao = new Cartao();

                this.cartao.setCartao_id(rs.getInt("cartao_id"));
                this.cartao.setCodigo_barra(rs.getString("codigo_barra"));
                this.cartao.setCreditos(0);
                //this.cartao.setData_cadastro();
                //this.cartao.setResponsavel_cadastro_id(0);
                //this.cartao.setVenda_entrada_id(0);

                cartoes.add(this.cartao);
            }
        }
        return cartoes;
    }

    public Cartao buscarPorCodigoBarra(Cartao cartao) throws Exception {
        this.cartao = null;
        if (conexao != null) {

            //System.out.println("\n\n\n\n\n\n\n"+ cartao.getCodigo_barra());
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT c.cartao_id, c.codigo_barra, c.creditos, c.data_cadastro, c.responsavel_cadastro_id, c.venda_entrada_id ");
            sql.append("FROM cartoes c ");
            //sql.append("LEFT JOIN acesso a ON (a.cartao_id = c.cartao_id AND a.acesso_tipo_id < 3) ");
            sql.append("WHERE c.creditos > 0 AND c.codigo_barra = ?");
            //sql.append("ORDER BY a.acesso_id DESC ");

            PreparedStatement ps = conexao.prepareStatement(sql.toString());
            ps.setString(1, cartao.getCodigo_barra());
            
            
            //System.out.println(sql.toString());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                this.cartao = new Cartao();

                this.cartao.setCartao_id(rs.getInt("cartao_id"));
                this.cartao.setCodigo_barra(rs.getString("codigo_barra"));
                this.cartao.setCreditos(0);
                //this.cartao.setData_cadastro();
                //this.cartao.setResponsavel_cadastro_id(0);
                //this.cartao.setVenda_entrada_id(0);
            }
        }
        return this.cartao;
    }

    public Cartao buscarPorCpf(Cartao cartao) throws Exception {
        this.cartao = null;
        if (conexao != null) {

            //System.out.println("\n\n\n\n\n\n\n"+ cartao.codigoBarra);
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT c.cartao_id, c.codigo_barra, c.ativo, c.cartao_tipo_id, c.pessoa_id, a.acesso_tipo_id ");
            sql.append("FROM cartao c  ");
            sql.append("	INNER JOIN pessoa p ON (p.pessoa_id = c.pessoa_id) ");
            sql.append("	LEFT JOIN acesso a ON (a.cartao_id = c.cartao_id AND p.pessoa_id = a.pessoa_id AND a.acesso_tipo_id < 3) ");
            sql.append("WHERE p.cpf LIKE ? ");
//			sql.append("	AND c.ativo = 1 ");
            sql.append("ORDER BY a.acesso_id DESC");

            PreparedStatement ps = conexao.prepareStatement(sql.toString());
            ps.setString(1, "%" +  cartao.getCodigo_barra());

            //System.out.println(sql.toString());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                this.cartao = new Cartao();
            }
        }
        return this.cartao;
    }

    public int desassociar(Cartao cartao) throws Exception {
        this.cartao = null;
        if (conexao != null) {

            //System.out.println("\n\n\n\n\n\n\n"+ cartao.codigoBarra);
            StringBuilder sql = new StringBuilder();
            sql.append("UPDATE Cartao ");
            sql.append("SET pessoa_id = NULL ");
            sql.append("WHERE cartao_tipo_id = 2 AND codigo_barra = ? ");

            PreparedStatement ps = conexao.prepareStatement(sql.toString());
            ps.setString(1,  cartao.getCodigo_barra());

            //System.out.println(sql.toString());
            return ps.executeUpdate();
        }
        return -1;
    }

}
