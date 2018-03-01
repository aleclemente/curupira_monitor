package idema.curupira.dominio;

import java.util.*;

public class Cartao {

    private int cartao_id;
    private String codigo_barra;
    private int creditos;
    private Date data_cadastro;
    private int responsavel_cadastro_id;
    private int venda_entrada_id;

    public int getCartao_id() {
        return cartao_id;
    }

    public String getCodigo_barra() {
        return codigo_barra;
    }

    public int getCreditos() {
        return creditos;
    }

    public Date getData_cadastro() {
        return data_cadastro;
    }

    public int getResponsavel_cadastro_id() {
        return responsavel_cadastro_id;
    }

    public int getVenda_entrada_id() {
        return venda_entrada_id;
    }

    public void setCartao_id(int cartao_id) {
        this.cartao_id = cartao_id;
    }

    public void setCodigo_barra(String codigo_barra) {
        this.codigo_barra = codigo_barra;
    }

    public void setCreditos(int creditos) {
        this.creditos = creditos;
    }

    public void setData_cadastro(Date data_cadastro) {
        this.data_cadastro = data_cadastro;
    }

    public void setResponsavel_cadastro_id(int responsavel_cadastro_id) {
        this.responsavel_cadastro_id = responsavel_cadastro_id;
    }

    public void setVenda_entrada_id(int venda_entrada_id) {
        this.venda_entrada_id = venda_entrada_id;
    }

}
