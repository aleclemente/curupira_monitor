package idema.curupira.dao;

import java.sql.Connection;
import java.util.Date;
import java.sql.PreparedStatement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import idema.curupira.dominio.Acesso;

public class AcessoDao {

    Connection conexao;

    public AcessoDao() {
        try {
            conexao = ConexaoDao.getInstance();
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    public int inserir(Acesso acesso) throws Exception {

        if (conexao != null) {

            StringBuilder sql = new StringBuilder();
            sql.append("INSERT INTO acesso(");
            sql.append("	data_registro, inner_id, cartao_id, pessoa_id, acesso_tipo_id) ");
            sql.append("VALUES (?, ?, ?, ?, ?)");

            SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
            if (acesso.dataRegistro == null) {
                acesso.dataRegistro = new Date(System.currentTimeMillis());
            }

            PreparedStatement ps = conexao.prepareStatement(sql.toString());
            ps.setString(1, sdf.format(acesso.dataRegistro));
            ps.setInt(2, acesso.innerId);
            ps.setLong(3, acesso.cartaoId);
            ps.setLong(4, acesso.pessoaId);
            ps.setInt(5, acesso.acessoTipoId);

            ps.executeUpdate();

            return ps.getUpdateCount();
        }

        return 0;
    }

}