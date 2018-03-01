package idema.curupira.negocio;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;
import javax.swing.SwingConstants;

import com.topdata.EasyInner;
import com.topdata.easyInner.enumeradores.Enumeradores;

//import idema.cajueiro.dao.EventoDAO;
import idema.curupira.dao.AcessoDao;
import idema.curupira.dao.CartaoDao;
import idema.curupira.dao.InnerDao;
import idema.curupira.dominio.Acesso;
import idema.curupira.dominio.AcessoTipo;
import idema.curupira.dominio.Cartao;
import idema.curupira.dominio.Inner;

public class Monitor {

    private static final long serialVersionUID = 1L;

    //DeclaraÃ§Ã£o de variÃ¡veis
    private boolean parar = false;
    private Inner innerAtual = null;
    private final EasyInner dll;
    private int ret;

    //Catraca
    private boolean liberaEntrada = false;
    private boolean liberaSaida = false;
    private boolean liberaEntradaInvertida = false;
    private boolean liberaSaidaInvertida = false;

    //Teclado
    public static String ultCartao;
    public static int intTentativas;

    //******************************************************
    //MAIS DE UM INNER
    //Array de Inners utilizados na maquina de estados..
    public static Inner[] inners = new Inner[255];

    public static int indiceInner;

    //Quantidade total de Inners na maquina de estados..
    public static int totalInners;

    //tentativas para coleta de bilhetes
    int tentativasColeta;

    //private EventoDAO eventoDAO;
    private AcessoDao acessoDao;
    private CartaoDao cartaoDao;
    private InnerDao innerDAO;
    static List<String> users = null;

    private final int[] bilhete = new int[8];
    private final StringBuffer cartao = new StringBuffer();
    private String numCartao = new String();
    private String bilhetee;

    private HashMap<String, Object> dadosSmartCard = new HashMap<>();

    @SuppressWarnings("unchecked")
    public Monitor() {
        //InicializaÃ§Ã£o dos componentes
        //initComponents();
        innerAtual = new Inner();
        dll = new EasyInner();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents

    private void iniciar() {//GEN-FIRST:event_jBtnIniciarActionPerformed        //BotÃ£o que inicia a configuraÃ§Ã£o e conexÃ£o
        try {
            //UsersBio = new DAOUsuariosBio();
            cartaoDao = new CartaoDao();
            //eventoDAO = new EventoDAO();
            acessoDao = new AcessoDao();
            //Users = 
            new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        iniciarMaquinaEstados();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Monitor.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }).start();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }//GEN-LAST:event_jBtnIniciarActionPerformed

    private void parar() {//GEN-FIRST:event_jBtnPararActionPerformed
        //BotÃ£o que cancela a conexÃ£o

        //Desabilita botÃµes
        parar = true;

        //Mensagem Status
        System.out.println("Interrupção solicitada.");

        //Fecha porta comunicaÃ§Ã£o
        dll.FecharPortaComunicacao();
    }//GEN-LAST:event_jBtnPararActionPerformed

    /**
     * Metodo responsÃ¡vel por realizar um comando simples com o equipamento para detectar se esta conectado.
     *
     * @param Inner
     * @return
     */
    private Integer testarConexaoInner(Integer Inner) {
        int[] DataHora = new int[6];
        Integer ret = dll.ReceberRelogio(Inner, DataHora);
        return ret;
    }

    /**
     * CONECTAR Inicia a conexÃ£o com o Inner PrÃ³ximo passo: ESTADO_ENVIAR_CFG_OFFLINE
     */
    private void PASSO_ESTADO_CONECTAR() {
        try {
            long IniConexao = 0;
            long tempo = 0;

            ret = Enumeradores.Limpar;
            //Inicia tempo ping online
            inners[indiceInner].tempoInicialPingOnLine = (int) System.currentTimeMillis();

            //Mensagem Status
            System.out.println("Inner " + inners[indiceInner].numero + " Conectando...");

            IniConexao = System.currentTimeMillis();
            //Realiza loop enquanto o tempo fim for menor que o tempo atual, e o comando retornado diferente de OK.
            do {
                tempo = System.currentTimeMillis() - IniConexao;
                //Tenta abrir a conexÃ£o 
                Thread.sleep(10l);
                ret = testarConexaoInner(inners[indiceInner].numero);

            } while (ret != Enumeradores.RET_COMANDO_OK && tempo < 10000);

            if (ret == Enumeradores.RET_COMANDO_OK) {
                //caso consiga o Inner vai para o Passo de ConfiguraÃ§Ã£o OFFLINE, posteriormente para coleta de Bilhetes.
                inners[indiceInner].countTentativasEnvioComando = 0;
                inners[indiceInner].estadoAtual = Enumeradores.EstadosInner.ESTADO_ENVIAR_CFG_OFFLINE;

            } else {
                //caso ele nÃ£o consiga, tentarÃ¡ enviar trÃªs vezes, se nÃ£o conseguir volta para o passo Reconectar
                if (inners[indiceInner].countTentativasEnvioComando >= 3) {
                    inners[indiceInner].estadoAtual = Enumeradores.EstadosInner.ESTADO_RECONECTAR;
                }
                inners[indiceInner].countTentativasEnvioComando++;
            }

        } catch (Exception ex) {
            inners[indiceInner].estadoAtual = Enumeradores.EstadosInner.ESTADO_RECONECTAR;
        }
    }

    /**
     * Passo responsÃ¡vel pelo envio das mensagens off-line para o Inner
     */
    private void PASSO_ESTADO_ENVIAR_MSG_OFFLINE() {
        try {
            //Mensagem Entrada e Saida Offline Liberado!
            dll.DefinirMensagemEntradaOffLine(1, "Entrada liberada.");
            dll.DefinirMensagemSaidaOffLine(1, "Saida liberada.");
            dll.DefinirMensagemPadraoOffLine(1, "Modo OffLine");

            ret = dll.EnviarMensagensOffLine(inners[indiceInner].numero);

            if (ret == Enumeradores.RET_COMANDO_OK) {
                inners[indiceInner].countTentativasEnvioComando = 0;
                inners[indiceInner].estadoAtual = Enumeradores.EstadosInner.ESTADO_ENVIAR_CONFIGMUD_ONLINE_OFFLINE;
                inners[indiceInner].tempoColeta = (int) System.currentTimeMillis() + 3000;
            } else {
                if (inners[indiceInner].countTentativasEnvioComando >= 3) {
                    inners[indiceInner].estadoAtual = Enumeradores.EstadosInner.ESTADO_RECONECTAR;
                }
                inners[indiceInner].countTentativasEnvioComando++;
            }
        } catch (Exception ex) {
            inners[indiceInner].estadoAtual = Enumeradores.EstadosInner.ESTADO_CONECTAR;
        }
    }

    /**
     * Configura a mudanÃ§a automÃ¡tica Habilita/Desabilita a mudanÃ§a automÃ¡tica do modo OffLine do Inner para OnLine e vice-versa. Habilita a mudanÃ§a Offline
     */
    private void PASSO_ESTADO_ENVIAR_CONFIGMUD_ONLINE_OFFLINE() {

        try {
            //se for tcp, habilita mudanca com ausencia de confirmacao de ping
            if (inners[indiceInner].tipoConexao == 1 || inners[indiceInner].tipoConexao == 2) {
                //Habilita a mudanÃ§a Offline
                dll.HabilitarMudancaOnLineOffLine(2, 20);
            } else {
                //Habilita a mudanÃ§a Offline
                dll.HabilitarMudancaOnLineOffLine(1, 20);
            }

            //Configura o teclado para quando o Inner voltar para OnLine apÃ³s uma queda
            //para OffLine.
            dll.DefinirConfiguracaoTecladoOnLine(inners[indiceInner].qtdDigitos, 1, 5, 17);

            //Define MudanÃ§as OnLine
            //FunÃ§Ã£o que configura BIT a BIT, Ver no manual Anexo III
            dll.DefinirEntradasMudancaOnLine(configuraEntradasMudancaOnLine(innerAtual));

            if (inners[indiceInner].biometrico) {
                // Configura entradas mudanÃ§a OffLine com Biometria
                dll.DefinirEntradasMudancaOffLineComBiometria((inners[indiceInner].teclado ? Enumeradores.Opcao_SIM : Enumeradores.Opcao_NAO), 3, (byte) (inners[indiceInner].doisLeitores ? 3 : 0), inners[indiceInner].verificacao, inners[indiceInner].identificacao);
            } else {
                // Configura entradas mudanÃ§a OffLine
                dll.DefinirEntradasMudancaOffLine((inners[indiceInner].teclado ? Enumeradores.Opcao_SIM : Enumeradores.Opcao_NAO), (byte) (inners[indiceInner].doisLeitores ? 1 : 3), (byte) (inners[indiceInner].doisLeitores ? 2 : 0), 0);
            }

            //Define mensagem de AlteraÃ§Ã£o Online -> Offline.
            dll.DefinirMensagemPadraoMudancaOffLine(1, " PASSE O CRACHA");

            //Define mensagem de AlteraÃ§Ã£o OffLine -> OnLine.
            dll.DefinirMensagemPadraoMudancaOnLine(1, "Bem vindo ao IDEMA");

            //Envia ConfiguraÃ§Ãµes.
            ret = dll.EnviarConfiguracoesMudancaAutomaticaOnLineOffLine(inners[indiceInner].numero);

            if (ret == Enumeradores.RET_COMANDO_OK) {
                inners[indiceInner].countTentativasEnvioComando = 0;
                inners[indiceInner].estadoAtual = Enumeradores.EstadosInner.ESTADO_COLETAR_BILHETES;
                inners[indiceInner].tempoColeta = (int) System.currentTimeMillis() + 3000;
                inners[indiceInner].tentativasColeta = 0;
            } else {
                if (inners[indiceInner].countTentativasEnvioComando >= 3) {
                    inners[indiceInner].estadoAtual = Enumeradores.EstadosInner.ESTADO_RECONECTAR;
                }
                inners[indiceInner].countTentativasEnvioComando++;
            }
        } catch (Exception ex) {
            inners[indiceInner].estadoAtual = Enumeradores.EstadosInner.ESTADO_CONECTAR;
        }
    }

    /**
     * Passo responsÃ¡vel pela criaÃ§Ã£o e envio da lista de horÃ¡rios de acesso.
     *
     */
    private void PASSO_ESTADO_ENVIAR_HORARIOS() {
        try {
            if (inners[indiceInner].listaHorario) {

                montarHorarios();

                ret = dll.EnviarHorariosAcesso(inners[indiceInner].numero);

                if (ret == Enumeradores.RET_COMANDO_OK) {
                    inners[indiceInner].countTentativasEnvioComando = 0;
                    inners[indiceInner].estadoAtual = Enumeradores.EstadosInner.ESTADO_ENVIAR_USUARIOS_LISTAS;
                    inners[indiceInner].tempoColeta = (int) System.currentTimeMillis() + 3000;
                } else {
                    if (inners[indiceInner].countTentativasEnvioComando >= 3) {
                        inners[indiceInner].estadoAtual = Enumeradores.EstadosInner.ESTADO_RECONECTAR;
                    }
                    inners[indiceInner].countTentativasEnvioComando++;
                }
            } else {
                inners[indiceInner].estadoAtual = Enumeradores.EstadosInner.ESTADO_ENVIAR_USUARIOS_LISTAS;
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            inners[indiceInner].estadoAtual = Enumeradores.EstadosInner.ESTADO_CONECTAR;
        }
    }

    /**
     * Passo responsÃ¡vel pelo envio da lista usuÃ¡rio com acesso off-line
     */
    private void PASSO_ESTADO_ENVIAR_USUARIOS_LISTAS() {
        try {
            if (inners[indiceInner].lista) {

                //Define a Lista de verificaÃ§Ã£o
                if (inners[indiceInner].padraoCartao == 1) {
                    montarListaLivre();
                } else {
                    montarListaTopdata();
                }

                //Define qual tipo de lista(controle) de acesso o Inner vai utilizar.
                //Utilizar lista branca (cartÃµes fora da lista tem o acesso negado).
                dll.DefinirTipoListaAcesso(1);
            } else {
                //NÃ£o utilizar a lista de acesso.
                dll.DefinirTipoListaAcesso(0);
            }

            if (inners[indiceInner].listaBio) {
                //Chama rotina que monta o buffer de cartoes que nao irao precisar da digital
                montarBufferListaSemDigital();
                //Envia o buffer com a lista de usuarios sem digital
                ret = dll.EnviarListaUsuariosSemDigitalBio(inners[indiceInner].numero);
            }

            if (ret == Enumeradores.RET_COMANDO_OK) {
                inners[indiceInner].countTentativasEnvioComando = 0;
                inners[indiceInner].estadoAtual = Enumeradores.EstadosInner.ESTADO_ENVIAR_MENSAGEM;
                inners[indiceInner].tempoColeta = (int) System.currentTimeMillis() + 3000;
            } else {
                if (inners[indiceInner].countTentativasEnvioComando >= 3) {
                    inners[indiceInner].estadoAtual = Enumeradores.EstadosInner.ESTADO_RECONECTAR;
                }
                inners[indiceInner].countTentativasEnvioComando++;
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            inners[indiceInner].estadoAtual = Enumeradores.EstadosInner.ESTADO_CONECTAR;
        }
    }

    /**
     * Passo responsÃ¡vel pelo envio das configuraÃ§Ãµes off-line do Inner
     */
    private void PASSO_ESTADO_ENVIAR_CFG_OFFLINE() {
        try {
            //Mensagem Status
            System.out.println("Inner " + inners[indiceInner].numero + " Enviado configuracoes OFF-LINE...");

            //Preenche os campos de configuraÃ§Ã£o do Inner
            montaConfiguracaoInner(Enumeradores.MODO_OFF_LINE);

            //Envia o comando de configuraÃ§Ã£o
            ret = dll.EnviarConfiguracoes(inners[indiceInner].numero);

            //Testa o retorno do envio das configuraÃ§Ãµes Off Line
            if (ret == Enumeradores.RET_COMANDO_OK) {
                defineVersao();
                inners[indiceInner].countTentativasEnvioComando = 0;
                //verifica se o enviar lista esta selecionado
                inners[indiceInner].estadoAtual = Enumeradores.EstadosInner.ESTADO_ENVIAR_HORARIOS;
                inners[indiceInner].tempoColeta = (int) System.currentTimeMillis() + 3000;

            } else {
                //caso ele nÃ£o consiga, tentarÃ¡ enviar trÃªs vezes, se nÃ£o conseguir volta para o passo Reconectar
                if (inners[indiceInner].countTentativasEnvioComando >= 3) {
                    inners[indiceInner].estadoAtual = Enumeradores.EstadosInner.ESTADO_RECONECTAR;
                }
                inners[indiceInner].countTentativasEnvioComando++;
            }
        } catch (Exception ex) {
            inners[indiceInner].estadoAtual = Enumeradores.EstadosInner.ESTADO_CONECTAR;
        }
    }

    /**
     * Passo agurada 2 segundos o tempo para apresentaÃ§Ã£o da mensagem no visor do Inner
     */
    private void PASSO_AGUARDA_TEMPO_MENSAGEM() {
        try {
            //ApÃ³s passar os 2 segundos volta para o passo enviar mensagem padrÃ£o
            long tempo = System.currentTimeMillis() - inners[indiceInner].tempoInicialMensagem;
            if (tempo > 2000) {
                inners[indiceInner].estadoAtual = Enumeradores.EstadosInner.ESTADO_ENVIAR_MSG_PADRAO;
            }
        } catch (Exception ex) {
            inners[indiceInner].estadoAtual = Enumeradores.EstadosInner.ESTADO_CONECTAR;
        }
    }

    /**
     * Passo resposÃ¡vel pela coleta dos bilhetes registrados em modo off-line
     */
    private void PASSO_ESTADO_COLETAR_BILHETES() throws InterruptedException {

        if (inners[indiceInner].innerNetAcesso) {
            coletarBilhetesInnerAcesso();
        } else {
            coletarBilhetesInnerNet();
        }
        inners[indiceInner].estadoAtual = Enumeradores.EstadosInner.ESTADO_ENVIAR_CFG_ONLINE;

    }

    /**
     * Realiza a coleta dos bilhetes off line do equipamentos da linha Inner Acesso
     *
     * @throws InterruptedException
     */
    private void coletarBilhetesInnerAcesso() throws InterruptedException {
        int[] Bilhete = new int[8];
        StringBuffer Cartao;
        int nBilhetes;
        int i = Enumeradores.Limpar;
        int QtdeBilhetes;
        int receber[] = new int[2];

        //Verifica conexao
        nBilhetes = 0;
        QtdeBilhetes = 0;
        ret = dll.ReceberQuantidadeBilhetes(inners[indiceInner].numero, receber);
        QtdeBilhetes = receber[0];

        do {
            if (QtdeBilhetes > 0) {
                do {
                    Thread.sleep(100l);

                    Cartao = new StringBuffer();
                    //Coleta um bilhete Off-Line que estÃ¡ armazenado na memÃ³ria do Inner
                    ret = dll.ColetarBilhete(inners[indiceInner].numero, Bilhete, Cartao);
                    if (ret == Enumeradores.RET_COMANDO_OK) {

                        //Armazena os dados do bilhete no list, pode ser utilizado com
                        //banco de dados ou outro meio de armazenamento compatÃ­vel
                        System.out.println("Tipo:" + String.valueOf(Bilhete[0]) + " Cartao:"
                                + Cartao.toString() + " Data:"
                                + (String.valueOf(Bilhete[1]).length() == 1 ? "0" + String.valueOf(Bilhete[1]) : String.valueOf(Bilhete[1])) + "/"
                                + (String.valueOf(Bilhete[2]).length() == 1 ? "0" + String.valueOf(Bilhete[2]) : String.valueOf(Bilhete[2])) + "/"
                                + String.valueOf(Bilhete[3]) + " Hora:"
                                + (String.valueOf(Bilhete[4]).length() == 1 ? "0" + String.valueOf(Bilhete[4]) : String.valueOf(Bilhete[4])) + ":"
                                + (String.valueOf(Bilhete[5]).length() == 1 ? "0" + String.valueOf(Bilhete[5]) : String.valueOf(Bilhete[5])) + ":"
                                + (String.valueOf(Bilhete[6]).length() == 1 ? "0" + String.valueOf(Bilhete[6]) : String.valueOf(Bilhete[6])) + "\n");

                        try {
                            registrarAcessoOffLine(Cartao.toString(), Bilhete);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        nBilhetes++;
                        QtdeBilhetes--;
                    }

                } while (QtdeBilhetes > 0);

                System.out.println("Foram coletados " + nBilhetes + " bilhete(s) offline !");
                ret = dll.ReceberQuantidadeBilhetes(inners[indiceInner].numero, receber);
                QtdeBilhetes = receber[0];
            }
        } while (QtdeBilhetes > 0);
    }

    /**
     * Realiza a coleta dos bilhetes off line do equipamentos da linha Inner Net
     */
    private void coletarBilhetesInnerNet() {
        try {
            int[] Bilhete = new int[8];
            StringBuffer Cartao;
            Long tempo;

            //Exibe no rodapÃ© da janela o estado da maquina.
            System.out.println("Inner " + Integer.toString(inners[indiceInner].numero) + " Coletando Bilhetes...");

            tempo = System.currentTimeMillis() + 200;
            do {
                Cartao = new StringBuffer();
                //Envia o Comando de Coleta de Bilhetes..
                ret = dll.ColetarBilhete(inners[indiceInner].numero, Bilhete, Cartao);

                //Zera a contagem de Ping Online.
                inners[indiceInner].cntDoEvents = 0;
                inners[indiceInner].countPingFail = 0;
                inners[indiceInner].countRepeatPingOnline = 0;

                //Caso exista bilhete a coletar..
                if (ret == Enumeradores.RET_COMANDO_OK) {

                    //Recebe hora atual para inicio do PingOnline
                    inners[indiceInner].tempoInicialPingOnLine = (int) System.currentTimeMillis();

                    //Adiciona a lista de bilhetes o Nro bilhete coletado..
                    System.out.println("Marcacoes Offline. Inner: " + inners[indiceInner].numero + " Complemento:"
                            + String.valueOf(Bilhete[0]) + " Data:"
                            + (String.valueOf(Bilhete[1]).length() == 1 ? "0" + String.valueOf(Bilhete[1]) : String.valueOf(Bilhete[1])) + "/"
                            + (String.valueOf(Bilhete[2]).length() == 1 ? "0" + String.valueOf(Bilhete[2]) : String.valueOf(Bilhete[2])) + "/"
                            + String.valueOf(Bilhete[3]) + " Hora:"
                            + (String.valueOf(Bilhete[4]).length() == 1 ? "0" + String.valueOf(Bilhete[4]) : String.valueOf(Bilhete[4])) + ":"
                            + (String.valueOf(Bilhete[5]).length() == 1 ? "0" + String.valueOf(Bilhete[5]) : String.valueOf(Bilhete[5])) + ":"
                            + (String.valueOf(Bilhete[6]).length() == 1 ? "0" + String.valueOf(Bilhete[6]) : String.valueOf(Bilhete[6])) + " "
                            + " Cartao: " + Cartao.toString() + "\n");

                    try {
                        registrarAcessoOffLine(Cartao.toString(), Bilhete);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            } while (System.currentTimeMillis() < tempo);
            inners[indiceInner].tentativasColeta += 1;
        } catch (Exception ex) {
            inners[indiceInner].estadoAtual = Enumeradores.EstadosInner.ESTADO_CONECTAR;
        }

    }

    private void registrarAcessoOffLine(String cartao, int[] bilhete) throws Exception {
        //registrando entradas offline
        Acesso a = new Acesso();
        a.innerId = inners[indiceInner].numero;

        String data = "20" + String.valueOf(bilhete[3]) + "-"
                + (String.valueOf(bilhete[2]).length() == 1 ? "0" + String.valueOf(bilhete[2]) : String.valueOf(bilhete[2])) + "-"
                + (String.valueOf(bilhete[1]).length() == 1 ? "0" + String.valueOf(bilhete[1]) : String.valueOf(bilhete[1])) + " "
                + (String.valueOf(bilhete[4]).length() == 1 ? "0" + String.valueOf(bilhete[4]) : String.valueOf(bilhete[4])) + ":"
                + (String.valueOf(bilhete[5]).length() == 1 ? "0" + String.valueOf(bilhete[5]) : String.valueOf(bilhete[5])) + ":"
                + (String.valueOf(bilhete[6]).length() == 1 ? "0" + String.valueOf(bilhete[6]) : String.valueOf(bilhete[6])) + "\n";

        SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");

        switch (bilhete[0]) {
            case 10:
                a.acessoTipoId = AcessoTipo.ENTRADA_LIBERADA;
                break;
            case 11:
                a.acessoTipoId = AcessoTipo.SAIDA_LIBERADA;
                break;
            case 110:
                a.acessoTipoId = AcessoTipo.ENTRADA_LIBERADA;
                break;
            case 111:
                a.acessoTipoId = AcessoTipo.SAIDA_LIBERADA;
                break;
            default:
                a.acessoTipoId = AcessoTipo.SEM_MOVIMENTO;
                break;
        }

        //recupera o id do cartao
        Cartao c = new Cartao();
        c.codigoBarra = cartao;
        c = cartaoDao.buscarPorCodigoBarra(c);
        a.cartaoId = c.id;

        //recupera o id da pessoa
        a.pessoaId = c.pessoaId;

        a.dataRegistro = sdf.parse(data);

        acessoDao.inserir(a);
    }

    /**
     * Configura modo On-line PrÃ³ximo passo: ESTADO_ENVIAR_DATA_HORA
     */
    private void PASSO_ESTADO_ENVIAR_CFG_ONLINE() {
        try {

            //Monta configuraÃ§Ã£o modo Online
            montaConfiguracaoInner(Enumeradores.MODO_ON_LINE);

            //Envia as configuraÃ§Ãµes ao Inner Atual.
            ret = dll.EnviarConfiguracoes(inners[indiceInner].numero);

            if (ret == Enumeradores.RET_COMANDO_OK) {
                //caso consiga enviar as configuraÃ§Ãµes, passa para o passo Enviar Data Hora
                inners[indiceInner].countTentativasEnvioComando = 0;
                inners[indiceInner].estadoAtual = Enumeradores.EstadosInner.ESTADO_ENVIAR_DATA_HORA;
            } else {
                //caso ele nÃ£o consiga, tentarÃ¡ enviar trÃªs vezes, se nÃ£o conseguir volta para o passo Reconectar
                if (inners[indiceInner].countTentativasEnvioComando >= 3) {
                    inners[indiceInner].estadoAtual = Enumeradores.EstadosInner.ESTADO_RECONECTAR;
                }
                inners[indiceInner].countTentativasEnvioComando++;
            }
        } catch (Exception ex) {
            inners[indiceInner].estadoAtual = Enumeradores.EstadosInner.ESTADO_CONECTAR;
        }
    }

    /**
     * Envia ao Inner data e hora atual PrÃ³ximo passo: ESTADO_ENVIAR_MSG_PADRAO
     */
    private void PASSO_ESTADO_ENVIAR_DATA_HORA() {
        try {
            //Exibe estado do Inner no RodapÃ© da Janela
            System.out.println("Inner " + inners[indiceInner].numero + " Enviando data e hora...");

            //DeclaraÃ§Ã£o de VariÃ¡veis..
            ret = Enumeradores.Limpar;

            Date Data = new Date();
            SimpleDateFormat formatter = new SimpleDateFormat("yy");
            int Ano = Integer.parseInt(formatter.format(Data));
            formatter = new SimpleDateFormat("MM");
            int Mes = Integer.parseInt(formatter.format(Data));
            formatter = new SimpleDateFormat("dd");
            int Dia = Integer.parseInt(formatter.format(Data));
            formatter = new SimpleDateFormat("HH");
            int Hora = Integer.parseInt(formatter.format(Data));
            formatter = new SimpleDateFormat("mm");
            int Minuto = Integer.parseInt(formatter.format(Data));
            formatter = new SimpleDateFormat("ss");
            int Segundo = Integer.parseInt(formatter.format(Data));
            //Envia Comando de RelÃ³gio ao Inner Atual..
            //          RelogioInner relogioInner = new RelogioInner();
            ret = dll.EnviarRelogio(inners[indiceInner].numero, Dia, Mes, Ano, Hora, Minuto, Segundo);
            //Testa o Retorno do comando de Envio de RelÃ³gio..
            if (ret == Enumeradores.RET_COMANDO_OK) {
                //Vai para o passo de Envio de Msg PadrÃ£o..
                inners[indiceInner].countTentativasEnvioComando = 0;
                inners[indiceInner].estadoAtual = Enumeradores.EstadosInner.ESTADO_ENVIAR_MSG_PADRAO;
            } else {
                //caso ele nÃ£o consiga, tentarÃ¡ enviar trÃªs vezes, se nÃ£o conseguir volta para o passo Reconectar
                if (inners[indiceInner].countTentativasEnvioComando >= 3) {
                    inners[indiceInner].estadoAtual = Enumeradores.EstadosInner.ESTADO_RECONECTAR;
                }
                inners[indiceInner].countTentativasEnvioComando++;
            }
        } catch (Exception ex) {
            inners[indiceInner].estadoAtual = Enumeradores.EstadosInner.ESTADO_CONECTAR;
        }
    }

    /**
     * Envia mensagem padrÃ£o modo Online PrÃ³ximo passo: ESTADO_CONFIGURAR_ENTRADAS_ONLINE
     */
    private void PASSO_ENVIAR_MENSAGEM_PADRAO() {
        try {
            //Exibe estado do Inner no RodapÃ© da Janela
            System.out.println("Inner " + inners[indiceInner].numero + " Enviando Mensagem Padrão...");

            //DeclaraÃ§Ã£o de VariÃ¡veis..
            ret = Enumeradores.Limpar;

            //Envia comando definindo a mensagem PadrÃ£o Online para o Inner.
            ret = dll.EnviarMensagemPadraoOnLine(inners[indiceInner].numero, 0, " Passe o cracha");

            //Testa o retorno da mensagem enviada..
            if (ret == Enumeradores.RET_COMANDO_OK) {
                //Muda o passo para configuraÃ§Ã£o de entradas Online.
                inners[indiceInner].countTentativasEnvioComando = 0;
                inners[indiceInner].estadoAtual = Enumeradores.EstadosInner.ESTADO_CONFIGURAR_ENTRADAS_ONLINE;
            } else {
                //caso ele nÃ£o consiga, tentarÃ¡ enviar trÃªs vezes, se nÃ£o conseguir volta para o passo Reconectar
                if (inners[indiceInner].countTentativasEnvioComando >= 3) {
                    inners[indiceInner].estadoAtual = Enumeradores.EstadosInner.ESTADO_RECONECTAR;
                }
                //Adiciona 1 ao contador de tentativas
                inners[indiceInner].countTentativasEnvioComando++;
            }
        } catch (Exception ex) {
            inners[indiceInner].estadoAtual = Enumeradores.EstadosInner.ESTADO_CONECTAR;
        }
    }

    /**
     * PreparaÃ§Ã£o configuraÃ§Ã£o online para entrar em modo Polling PrÃ³ximo passo: ESTADO_POLLING
     */
    private void PASSO_ESTADO_CONFIGURAR_ENTRADAS_ONLINE() {
        try {
            //Exibe estado do Inner no RodapÃ© da Janela
            System.out.println("Inner " + inners[indiceInner].numero + " Configurando Entradas Online...");

            //DeclaraÃ§Ã£o de variÃ¡veis..
            ret = Enumeradores.Limpar;

            //Converte BinÃ¡rio para Decimal
            int ValorDecimal = configuraEntradasMudancaOnLine(innerAtual); //Ver no manual Anexo III

            ret = dll.EnviarFormasEntradasOnLine(inners[indiceInner].numero, (byte) inners[indiceInner].qtdDigitos, 1, (byte) ValorDecimal, 15, 17);
            //Testa o retorno do comando..
            if (ret == Enumeradores.RET_COMANDO_OK) {
                //Vai para o Estado De Polling.
                inners[indiceInner].tempoInicialPingOnLine = (int) System.currentTimeMillis();
                inners[indiceInner].countTentativasEnvioComando = 0;
                inners[indiceInner].estadoAtual = Enumeradores.EstadosInner.ESTADO_POLLING;

                /*				if (typInnersCadastrados[lngInnerAtual].catraca) {
					jBtnEntrada.setText("Entrada");
					jBtnSaida.setText("SaÃ­da");
					jBtnEntrada.setEnabled(true);
					jBtnSaida.setEnabled(true);
				} else {
					jBtnEntrada.setText("Porta 1");
					jBtnSaida.setText("Porta 2");
					jBtnEntrada.setEnabled(true);
					jBtnSaida.setEnabled(true);
				}
                 */
            } else {
                //caso ele nÃ£o consiga, tentarÃ¡ enviar trÃªs vezes, se nÃ£o conseguir volta para o passo Reconectar
                if (inners[indiceInner].countTentativasEnvioComando >= 3) {
                    inners[indiceInner].estadoAtual = Enumeradores.EstadosInner.ESTADO_RECONECTAR;
                }
                //Adiciona 1 ao contador de tentativas
                inners[indiceInner].countTentativasEnvioComando++;
            }
        } catch (Exception ex) {
            inners[indiceInner].estadoAtual = Enumeradores.EstadosInner.ESTADO_CONECTAR;
        }
    }

    /**
     * Verifica se a quantidade maxima de tentativas de um envio de comando Ocorreu, caso tenha ocorrido retorna TRUE, senÃ£o FALSE..
     *
     * @return
     */
    private static boolean maximoNumeroTentativas() {
        //Incrementa o nÃºmero de tentativas..
        intTentativas = intTentativas + 1;

        //Verifica se o nÃºmero de tentativas Ã© maior do que 3..
        //MAXIMO_TENTATIVAS_COMUNICACAO
        if (intTentativas > 2) {
            return true; //Retorna TRUE
        } else {
            return false; //Retorna FALSE
        }
    }

    /**
     * Mostra mensagem para que seja informado se Ã© entrada ou saÃ­da Este estado configura a mensagem padrÃ£o que serÃ¡ exibida no dispositivo em seu funcionamento Online utilizando o mÃ©todo EnviarMensagemPadraoOnline. O passo posterior a este estado Ã© o passo de configuraÃ§Ã£o de entradas online, ou em caso de erro pode retornar para o estado de conexÃ£o apÃ³s alcanÃ§ar o nÃºmero mÃ¡ximo de tentativas. PrÃ³ximo passo: ESTADO_POLLING
     */
    private void PASSO_ESTADO_DEFINICAO_TECLADO() {
        int Ret = Enumeradores.Limpar;

        //Envia mensagem PadrÃ£o Online..
        dll.EnviarMensagemPadraoOnLine(inners[indiceInner].numero, 0, "ENTRADA OU SAIDA?");
        Ret = dll.EnviarFormasEntradasOnLine(inners[indiceInner].numero,
                0, //Quantidade de Digitos do Teclado.. (NÃ£o aceita digitaÃ§Ã£o numÃ©rica)
                0, //0 â€“ nÃ£o ecoa
                Enumeradores.ACEITA_TECLADO,
                10, // Tempo de entrada do Teclado (10s).
                32);//PosiÃ§Ã£o do Cursor (32 fica fora..)

        //Se Retorno OK, vai para proximo estado..
        if (Ret == Enumeradores.RET_COMANDO_OK) {
            intTentativas = 0;
            inners[indiceInner].estadoTeclado = Enumeradores.EstadosTeclado.AGUARDANDO_TECLADO;
            inners[indiceInner].estadoAtual = Enumeradores.EstadosInner.ESTADO_AGUARDA_DEFINICAO_TECLADO;
        } else {
            //Caso o retorno nÃ£o for OK, tenta novamente atÃ© 3x..
            if (maximoNumeroTentativas() == true) {
                inners[indiceInner].estadoAtual = Enumeradores.EstadosInner.ESTADO_CONECTAR;
            }
        }
    }

    /**
     * PASSO_ESTADO_AGUARDAR_DEFINICAO_TECLADO Aguarda a resposta do teclado (Entrada, Saida, anula ou confirma) Proximo estado: ESTADO_POLLING
     */
    private void PASSO_ESTADO_AGUARDA_DEFINICAO_TECLADO() {
        try {
            int[] Bilhete = new int[8];
            StringBuffer Cartao;
            String NumCartao;
            String Bilhetee;

            System.out.println("Inner " + inners[indiceInner].numero + " Estado aguardar definiÃ§Ã£o teclado");

            Cartao = new StringBuffer();
            NumCartao = new String();

            //Envia o Comando de Coleta de Bilhetes..
            ret = dll.ReceberDadosOnLine(inners[indiceInner].numero, Bilhete, Cartao);

            //Atribui Temporizador
            inners[indiceInner].temporizador = (int) System.currentTimeMillis();

            if (ret == Enumeradores.RET_COMANDO_OK) {
                if (inners[indiceInner].estadoTeclado == Enumeradores.EstadosTeclado.AGUARDANDO_TECLADO) {
                    //****************************************************
                    //Entrada, saÃ­da liberada, confirma, anula ou funÃ§Ã£o tratar mensagem
                    //66 - "Entrada" via teclado
                    //67 - "SaÃ­da" via teclado
                    //35 - "Confirma" via teclado
                    //42 - "Anula" via teclado
                    //65 - "FunÃ§Ã£o" via teclado
                    if (Integer.parseInt(String.valueOf(Bilhete[1])) == Enumeradores.TECLA_ENTRADA) //entrada
                    {
                        dll.AcionarBipCurto(inners[indiceInner].numero);
                        verificaLadoCatraca("Entrada");
                        inners[indiceInner].estadoAtual = Enumeradores.EstadosInner.ESTADO_LIBERAR_CATRACA;
                    } else if (Integer.parseInt(String.valueOf(Bilhete[1])) == Enumeradores.TECLA_SAIDA) //saida
                    {
                        dll.AcionarBipCurto(inners[indiceInner].numero);
                        verificaLadoCatraca("Saida");
                        inners[indiceInner].estadoAtual = Enumeradores.EstadosInner.ESTADO_LIBERAR_CATRACA;
                    } else if (Integer.parseInt(String.valueOf(Bilhete[1])) == Enumeradores.TECLA_CONFIRMA) //confirma
                    {
                        dll.AcionarBipCurto(inners[indiceInner].numero);
                        ret = dll.EnviarMensagemPadraoOnLine(inners[indiceInner].numero, 0, "LIBERADO DOIS   SENTIDOS.");
                        inners[indiceInner].estadoAtual = Enumeradores.EstadosInner.ESTADO_LIBERAR_CATRACA;
                    } else if (Integer.parseInt(String.valueOf(Bilhete[1])) == Enumeradores.TECLA_ANULA) //anula
                    {
                        dll.LigarBackLite(inners[indiceInner].numero);
                        inners[indiceInner].tempoInicialMensagem = (int) System.currentTimeMillis();
                        inners[indiceInner].countTentativasEnvioComando = 0;
                        inners[indiceInner].estadoAtual = Enumeradores.EstadosInner.ESTADO_ENVIAR_MSG_PADRAO;
                    } else if (Integer.parseInt(String.valueOf(Bilhete[1])) == Enumeradores.TECLA_FUNCAO) //funÃ§Ã£o
                    {
                        inners[indiceInner].estadoAtual = Enumeradores.EstadosInner.ESTADO_DEFINICAO_TECLADO;
                    }
                    inners[indiceInner].estadoTeclado = Enumeradores.EstadosTeclado.TECLADO_EM_BRANCO;
                }
            } else {
                long temp = System.currentTimeMillis() - inners[indiceInner].tempoInicialPingOnLine;
                //Se passar 3 segundos sem receber nada, passa para o estado enviar ping on line, para manter o equipamento em on line.
                if ((int) temp > 3000) {
                    inners[indiceInner].estadoSolicitacaoPingOnLine = inners[indiceInner].estadoAtual;
                    inners[indiceInner].countTentativasEnvioComando = 0;
                    inners[indiceInner].tempoInicialPingOnLine = (int) System.currentTimeMillis();
                    inners[indiceInner].estadoAtual = Enumeradores.EstadosInner.PING_ONLINE;
                }
            }
        } catch (Exception ex) {
            inners[indiceInner].estadoAtual = Enumeradores.EstadosInner.ESTADO_CONECTAR;
        }
    }

    /**
     * MENSAGEM_PADRAO Envia mensagem acesso negado PrÃ³ximo passo: AGUARDA_TEMPO_MENSAGEM
     */
    private void PASSO_ENVIAR_MENSAGEM_ACESSO_NEGADO() {
        try {
            //Testa o Retorno do comando de Envio de Mensagem PadrÃ£o On Line
            if (dll.EnviarMensagemPadraoOnLine(inners[indiceInner].numero, 0, " Acesso Negado!  NAO CADASTRADO \r\n") == Enumeradores.RET_COMANDO_OK) {
                inners[indiceInner].tempoInicialMensagem = System.currentTimeMillis();
                dll.AcionarBipLongo(inners[indiceInner].numero);
                if (inners[indiceInner].innerNetAcesso) {
                    dll.LigarLedVermelho(inners[indiceInner].numero);
                }
                //Muda o passo para configuraÃ§Ã£o de entradas Online.
                inners[indiceInner].countTentativasEnvioComando = 0;
                inners[indiceInner].estadoAtual = Enumeradores.EstadosInner.AGUARDA_TEMPO_MENSAGEM;
            } else {
                //caso ele nÃ£o consiga, tentarÃ¡ enviar trÃªs vezes, se nÃ£o conseguir volta para o passo Reconectar
                if (inners[indiceInner].countTentativasEnvioComando >= 3) {
                    inners[indiceInner].estadoAtual = Enumeradores.EstadosInner.ESTADO_RECONECTAR;
                }
                //Adiciona 1 ao contador de tentativas
                inners[indiceInner].countTentativasEnvioComando++;
            }
        } catch (Exception ex) {
            inners[indiceInner].estadoAtual = Enumeradores.EstadosInner.ESTADO_CONECTAR;
        }
    }

    /**
     * Envia mensagem padrÃ£o estado Urna PrÃ³ximo passo: ESTADO_MONITORA_URNA
     */
    private void PASSO_ESTADO_ENVIA_MSG_URNA() {
        try {
            //Testa o Retorno do comando de Envio de Mensagem PadrÃ£o On Line
            if (dll.EnviarMensagemPadraoOnLine(inners[indiceInner].numero, 0, " DEPOSITE O       CARTAO") == Enumeradores.RET_COMANDO_OK) {
                dll.AcionarRele2(inners[indiceInner].numero);
                inners[indiceInner].countTentativasEnvioComando = 0;
                inners[indiceInner].estadoAtual = Enumeradores.EstadosInner.ESTADO_MONITORA_URNA;
            } else {
                //caso ele nÃ£o consiga, tentarÃ¡ enviar trÃªs vezes, se nÃ£o conseguir volta para o passo Reconectar
                if (inners[indiceInner].countTentativasEnvioComando >= 3) {
                    inners[indiceInner].estadoAtual = Enumeradores.EstadosInner.ESTADO_RECONECTAR;
                }
                inners[indiceInner].countTentativasEnvioComando++;
            }
        } catch (Exception ex) {
            inners[indiceInner].estadoAtual = Enumeradores.EstadosInner.ESTADO_CONECTAR;
        }
    }

    /**
     * Monitora o depÃ³sito do cartÃ£o na Urna PrÃ³ximo passo: ESTADO_LIBERAR_CATRACA
     */
    private void PASSO_ESTADO_MONITORA_URNA() {
        try {
            //Exibe estado do giro
            System.out.println("Monitorando Giro de Catraca!");

            //Exibe estado do Inner no RodapÃ© da Janela
            System.out.println("Inner " + inners[indiceInner].numero + " Monitora Giro da Catraca...");

            //DeclaraÃ§Ã£o de VariÃ¡veis..
            int[] Bilhete = new int[8];
            StringBuffer Cartao;
            String NumCartao;
            int Ret = Enumeradores.Limpar;

            Cartao = new StringBuffer();
            NumCartao = new String();

            //Monitora o giro da catraca..
            Ret = dll.ReceberDadosOnLine(inners[indiceInner].numero, Bilhete, Cartao);

            //Testa o retorno do comando..
            if (Ret == Enumeradores.RET_COMANDO_OK) {
                //Testa se girou o nÃ£o a catraca..
                if (Bilhete[0] == Enumeradores.URNA) {
                    System.out.println("URNA RECOLHEU CARTÃƒO");
                    //Vai para o estado de Envio de Msg PadrÃ£o..
                    liberaSaida = true;
                    inners[indiceInner].estadoAtual = Enumeradores.EstadosInner.ESTADO_LIBERAR_CATRACA;
                } else if (Bilhete[0] == Enumeradores.FIM_TEMPO_ACIONAMENTO) {
                    System.out.println("NÃƒO DEPOSITOU CARTÃƒO");
                    //easyInner.AcionarBipLongo(typInnersCadastrados[lngInnerAtual].Numero);
                    dll.EnviarMensagemPadraoOnLine(inners[indiceInner].numero, 0, "     ACESSO          NEGADO");
                    //Vai para o estado de Envio de Msg PadrÃ£o..
                    inners[indiceInner].tempoInicialMensagem = (int) System.currentTimeMillis();
                    inners[indiceInner].estadoAtual = Enumeradores.EstadosInner.AGUARDA_TEMPO_MENSAGEM;
                }
            } else {
                //Caso o tempo que estiver monitorando o giro chegue a 3 segundos,
                long tempo = (System.currentTimeMillis() - inners[indiceInner].tempoInicialPingOnLine);
                //deverÃ¡ enviar o ping on line para manter o equipamento em modo on line
                if (tempo > 3000) {
                    inners[indiceInner].estadoSolicitacaoPingOnLine = inners[indiceInner].estadoAtual;
                    inners[indiceInner].countTentativasEnvioComando = 0;
                    inners[indiceInner].tempoInicialPingOnLine = (int) System.currentTimeMillis();
                    inners[indiceInner].estadoAtual = Enumeradores.EstadosInner.PING_ONLINE;
                }
            }
        } catch (Exception ex) {
            inners[indiceInner].estadoAtual = Enumeradores.EstadosInner.ESTADO_CONECTAR;
        }
    }

    /**
     * De acordo com o que foi informado (Esquerda ou Direita)F
     *
     * @param lado
     */
    private void verificaLadoCatraca(String lado) {
        if (lado.equals("Entrada")) {
            //entrada
            if (inners[indiceInner].entradaCatracaDireita) {
                liberaEntrada = true;
                liberaEntradaInvertida = false;
            } else {
                liberaEntradaInvertida = true;
                liberaEntrada = false;
            }
        }

        if (lado.equals("Saida")) {
            //saÃ­da
            if (inners[indiceInner].entradaCatracaDireita) {
                liberaSaida = true;
                liberaSaidaInvertida = false;
            } else {
                liberaSaidaInvertida = true;
                liberaSaida = false;
            }
        }
    }

    /**
     * MÃ©todo responsÃ¡vel pela liberaÃ§Ã£o de acesso. Somente usuarios listado serÃ£o liberados. Esta consulta deverÃ¡ ser feita em sua base de dados.
     *
     * @param NumCartao
     * @return
     */
    private boolean liberaAcesso(String numCartao) {
        boolean ret = false;

        /*List<String> cartoes = new ArrayList<>();
		cartoes.add("1");
		cartoes.add("187");
		cartoes.add("123456");
		cartoes.add("27105070");
		cartoes.add("103086639459");
		cartoes.add("00000000000052");
		
		for (String s : cartoes) {
			if (s.equals(numCartao)) {
				ret = true;
			}
		}
		return ret;
         */
        try {
            Cartao cartao = new Cartao();

            //cartao.codigoBarra = numCartao;
            //cartao = cartaoDao.buscarPorCodigoBarra(cartao);
            if (numCartao.length() == 12) {
                numCartao = numCartao.substring(1);
                cartao.codigoBarra = numCartao;
            }
            cartao = cartaoDao.buscarPorCpf(cartao);

            if (cartao != null && cartao.ativo && cartao.pessoaId != 0) {
                //eventoDAO.inserir(new Evento(inners[indiceInner], TipoEvento.CARTAO_COM_CREDITO, cartao));
                System.out.println("Cartao " + cartao.codigoBarra + " Ativado");
                ret = true;
            } else if (cartao != null && !cartao.ativo) {
                System.out.println("Cartao " + cartao.codigoBarra + " Desativado");
                ret = false;
            } /* else if (cartao != null) {
				eventoDAO.inserir(new Evento(inners[indiceInner], TipoEvento.CARTAO_SEM_CREDITO, cartao));
				System.out.println("Cartao "+ cartao.codigo +" sem credito");
				ret = false;
			}*/ else {
                //eventoDAO.inserir(new Evento(inners[indiceInner], TipoEvento.CARTAO_NAO_CADASTRADO, cartao));
                System.out.println("Cartao " + numCartao + " nao cadastrado");
                ret = false;
            }
            inners[indiceInner].cartao = cartao;
        } catch (Exception e) {
            System.out.println("Erro de comunicacao com banco de dados");
            e.printStackTrace();
        }

        return ret;
        /*Cartao cartao = new Cartao();
		cartao.codigoBarra = numCartao;
		inners[indiceInner].cartao = cartao;
		
		return true;*/
    }

    /**
     * Ã‰ onde funciona todo o processo do modo online Passagem de cartÃ£o, catraca, urna, mensagens...
     *
     */
    private void PASSO_ESTADO_POLLING() {
        try {

            //Exibe estado do Inner no RodapÃ© da Janela
            //System.out.println("Inner " + inners[indiceInner].numero + " Estado de Polling...");
            //ssSystem.out.println("Catraca:["+ inners[indiceInner].catraca + "]\t Equipamento:["+ inners[indiceInner].equipamento +"] qt digitos["+ inners[indiceInner].qtdDigitos +"]");
            cartao.delete(0, cartao.length());

            //dll.InserirQuantidadeDigitoVariavel(6);
            //dll.InserirQuantidadeDigitoVariavel(12);
            //Thread.sleep(10l);
            //Bilhete = new int[8];
            //Envia o Comando de Coleta de Bilhetes..
            ret = dll.ReceberDadosOnLine(inners[indiceInner].numero, bilhete, cartao);

            //Atribui Temporizador
            inners[indiceInner].temporizador = (int) System.currentTimeMillis();

            if (ret == Enumeradores.RET_COMANDO_OK) {
                if (bilhete[0] == Enumeradores.FIM_TEMPO_ACIONAMENTO
                        || bilhete[0] == Enumeradores.GIRO_DA_CATRACA_TOPDATA
                        || bilhete[0] == Enumeradores.TECLA_FUNCAO
                        || bilhete[0] == Enumeradores.TECLA_ANULA
                        || ((cartao.length() == 0)
                        && !(inners[indiceInner].estadoTeclado == Enumeradores.EstadosTeclado.AGUARDANDO_TECLADO))) {
                    inners[indiceInner].countTentativasEnvioComando = 0;
                    inners[indiceInner].estadoAtual = Enumeradores.EstadosInner.ESTADO_ENVIAR_MSG_PADRAO;
                    return;
                }

                //Se o cartÃ£o padrÃ£o for topdata, configura os dÃ­gitos do cartÃ£o como padrÃ£o topdata
                numCartao = "";
                if (inners[indiceInner].padraoCartao == 0) {
                    //PadrÃ£o Topdata --> CartÃ£o Topdata deve ter sempre 14 dÃ­gitos.
                    //5 dÃ­gitos5
                    numCartao = cartao.toString();
                    numCartao = numCartao.substring(13, 14) + "" + numCartao.substring(4, 8);
                } else {
                    //PadrÃ£o Livre
                    numCartao = cartao.toString();
                }
                bilhetee = "";
                bilhetee = "Marcacoes Online. Inner: " + inners[indiceInner].numero + " Complemento:"
                        + String.valueOf(bilhete[1]);

                //Se Quantidade de dÃ­gitos informado for maior que 14 nÃ£o deve mostrar data e hora
                if (inners[indiceInner].qtdDigitos <= 14) {
                    bilhetee = bilhetee
                            + " Data:"
                            + (String.valueOf(bilhete[2]).length() == 1 ? "0" + String.valueOf(bilhete[2]) : String.valueOf(bilhete[2])) + "/"
                            + (String.valueOf(bilhete[3]).length() == 1 ? "0" + String.valueOf(bilhete[3]) : String.valueOf(bilhete[3])) + "/"
                            + String.valueOf(bilhete[4]) + " Hora:"
                            + (String.valueOf(bilhete[5]).length() == 1 ? "0" + String.valueOf(bilhete[5]) : String.valueOf(bilhete[5])) + ":"
                            + (String.valueOf(bilhete[6]).length() == 1 ? "0" + String.valueOf(bilhete[6]) : String.valueOf(bilhete[6])) + ":"
                            + (String.valueOf(bilhete[7]).length() == 1 ? "0" + String.valueOf(bilhete[7]) : String.valueOf(bilhete[7])) + " "
                            + " Cartao: " + numCartao + "\n";
                } else {
                    bilhetee = bilhetee + " Cartao: " + numCartao + "\r\n";
                }

                //Adiciona bilhete coletado na Lista
                System.out.println(bilhetee);

                if (!liberaAcesso(numCartao)) {
                    inners[indiceInner].estadoAtual = Enumeradores.EstadosInner.ESTADO_ENVIAR_MENSAGEM_ACESSO_NEGADO;
                } //Se 1 leitor
                //E Urna ou entrada e saÃ­da ou liberada 2 sentidos ou sentido giro
                //E cartÃ£o = proximidade
                else if (((inners[indiceInner].doisLeitores == false)
                        && ((inners[indiceInner].equipamento == Enumeradores.Acionamento_Catraca_Urna)
                        || (inners[indiceInner].equipamento == Enumeradores.Acionamento_Catraca_Entrada_E_Saida)
                        || (inners[indiceInner].equipamento == Enumeradores.Acionamento_Catraca_Liberada_2_Sentidos)
                        || (inners[indiceInner].equipamento == Enumeradores.Acionamento_Catraca_Sentido_Giro))
                        && ((inners[indiceInner].tipoLeitor == 2) || (inners[indiceInner].tipoLeitor == 3)
                        || (inners[indiceInner].tipoLeitor == 4)))) {

                    if (inners[indiceInner].estadoTeclado == Enumeradores.EstadosTeclado.TECLADO_EM_BRANCO) {
                        inners[indiceInner].estadoAtual = Enumeradores.EstadosInner.ESTADO_DEFINICAO_TECLADO;
                    }

                    //Se estamos trabalhando com Urna e 1 leitor
                    if ((inners[indiceInner].catraca) && (inners[indiceInner].equipamento == Enumeradores.Acionamento_Catraca_Urna)) {
                        inners[indiceInner].estadoAtual = Enumeradores.EstadosInner.ESTADO_ENVIA_MSG_URNA;
                    }
                } else {
                    if (inners[indiceInner].catraca) {
                        if (inners[indiceInner].equipamento == Enumeradores.Acionamento_Catraca_Entrada) {
                            verificaLadoCatraca("Entrada");
                            inners[indiceInner].estadoAtual = Enumeradores.EstadosInner.ESTADO_LIBERAR_CATRACA;
                        } else if (inners[indiceInner].equipamento == Enumeradores.Acionamento_Catraca_Saida) {
                            verificaLadoCatraca("Saida");
                            inners[indiceInner].estadoAtual = Enumeradores.EstadosInner.ESTADO_LIBERAR_CATRACA;
                        } //Se Urna e 2 leitores
                        else if (inners[indiceInner].equipamento == Enumeradores.Acionamento_Catraca_Urna & bilhete[0] == Enumeradores.VIA_LEITOR2) {
                            inners[indiceInner].estadoAtual = Enumeradores.EstadosInner.ESTADO_ENVIA_MSG_URNA;
                        } else {
                            /*
							//@TODO verificar funcionamento
							if (inners[indiceInner].cartao.ultimoAcessoTipoId == AcessoTipo.ENTRADA_LIBERADA) {
								verificaLadoCatraca("Saida");
							} else if (inners[indiceInner].cartao.ultimoAcessoTipoId == AcessoTipo.SAIDA_LIBERADA) {
								verificaLadoCatraca("Entrada");
							}
                             */
                            inners[indiceInner].estadoAtual = Enumeradores.EstadosInner.ESTADO_LIBERAR_CATRACA;
                        }
                    } else {
                        //Aciona Bip Curto..
                        dll.AcionarBipCurto(inners[indiceInner].numero);
                        //Desliga Led Verde
                        dll.LigarBackLite(inners[indiceInner].numero);
                        inners[indiceInner].tempoInicialMensagem = (int) System.currentTimeMillis();
                        inners[indiceInner].countTentativasEnvioComando = 0;
                        dll.EnviarFormasEntradasOnLine(0, 0, 0, 0, 0, 0);
                        dll.EnviarMensagemPadraoOnLine(inners[indiceInner].numero, 0, "Acesso Liberado!");
                        dll.AcionarRele1(inners[indiceInner].numero);
                        inners[indiceInner].estadoAtual = Enumeradores.EstadosInner.AGUARDA_TEMPO_MENSAGEM;
                    }
                }
            } else {
                long temp = System.currentTimeMillis() - inners[indiceInner].tempoInicialPingOnLine;
                //Se passar 3 segundos sem receber nada, passa para o estado enviar ping on line, para manter o equipamento em on line.
                if ((int) temp > 3000) {
                    inners[indiceInner].estadoSolicitacaoPingOnLine = inners[indiceInner].estadoAtual;
                    inners[indiceInner].countTentativasEnvioComando = 0;
                    inners[indiceInner].tempoInicialPingOnLine = (int) System.currentTimeMillis();
                    inners[indiceInner].estadoAtual = Enumeradores.EstadosInner.PING_ONLINE;
                }
            }

        } catch (Exception ex) {
            System.err.println(ex);
            inners[indiceInner].estadoAtual = Enumeradores.EstadosInner.ESTADO_CONECTAR;
        }
    }

    /**
     * Aciona o rele do Inner 1
     */
    private void ACIONAR_RELE() {
        dll.AcionarBipCurto(1);
        if (Inner.rele) {
            dll.AcionarRele1(1);
        } else {
            dll.AcionarRele2(1);
        }
        inners[indiceInner].estadoAtual = Enumeradores.EstadosInner.ESTADO_POLLING;
    }

    /**
     * Libera a catraca de acordo com o lado informado PrÃ³ximo Passo: ESTADO_MONITORA_GIRO_CATRACA
     */
    private void PASSO_LIBERA_GIRO_CATRACA() {
        try {
            //Exibe estado do Inner no RodapÃ© da Janela
            System.out.println("Inner " + inners[indiceInner].numero + " Libera Giro da Catraca...");
            //DeclaraÃ§Ã£o de VariÃ¡veis..
            ret = Enumeradores.Limpar;
            //Envia comando de liberar a catraca para Entrada.
            if (liberaEntrada) {
                dll.EnviarMensagemPadraoOnLine(inners[indiceInner].numero, 0, "                ENTRADA LIBERADA");
                liberaEntrada = false;
                ret = dll.LiberarCatracaEntrada(inners[indiceInner].numero);
            } else if (liberaEntradaInvertida) {
                dll.EnviarMensagemPadraoOnLine(inners[indiceInner].numero, 0, "                ENTRADA LIBERADA");
                liberaEntradaInvertida = false;
                ret = dll.LiberarCatracaEntradaInvertida(inners[indiceInner].numero);
            } else if (liberaSaida) {
                //Envia comando de liberar a catraca para SaÃ­da.
                dll.EnviarMensagemPadraoOnLine(inners[indiceInner].numero, 0, "                SAIDA LIBERADA");
                liberaSaida = false;
                ret = dll.LiberarCatracaSaida(inners[indiceInner].numero);
            } else if (liberaSaidaInvertida) {
                dll.EnviarMensagemPadraoOnLine(inners[indiceInner].numero, 0, "                SAIDA LIBERADA");
                liberaSaidaInvertida = false;
                ret = dll.LiberarCatracaSaidaInvertida(inners[indiceInner].numero);
            } else {
                dll.EnviarMensagemPadraoOnLine(inners[indiceInner].numero, 0, "LIBERADO DOIS SENTIDOS");
                ret = dll.LiberarCatracaDoisSentidos(inners[indiceInner].numero);
            }

            //Testa Retorno do comando..
            if (ret == Enumeradores.RET_COMANDO_OK) {
                dll.AcionarBipCurto(inners[indiceInner].numero);
                inners[indiceInner].countPingFail = 0;
                inners[indiceInner].countTentativasEnvioComando = 0;
                inners[indiceInner].tempoInicialPingOnLine = (int) System.currentTimeMillis();
                inners[indiceInner].estadoAtual = Enumeradores.EstadosInner.ESTADO_MONITORA_GIRO_CATRACA;

            } else {
                //Se o retorno for diferente de 0 tenta liberar a catraca 3 vezes, caso nÃ£o consiga enviar o comando volta para o passo reconectar.
                if (inners[indiceInner].countTentativasEnvioComando >= 3) {
                    inners[indiceInner].countTentativasEnvioComando = 0;
                    inners[indiceInner].estadoAtual = Enumeradores.EstadosInner.ESTADO_RECONECTAR;
                }
                inners[indiceInner].countTentativasEnvioComando++;
            }
        } catch (Exception ex) {
            inners[indiceInner].estadoAtual = Enumeradores.EstadosInner.ESTADO_CONECTAR;
        }
    }

    /**
     * Verifica se a catraca foi girada ou nÃ£o e caso sim para qual lado. PrÃ³ximo Passo: ESTADO_ENVIAR_MSG_PADRAO
     */
    private void PASSO_MONITORA_GIRO_CATRACA() {
        try {
            int[] Bilhete = new int[8];
            StringBuffer Cartao;

            Cartao = new StringBuffer();

            //Exibe estado do giro
            System.out.println("Monitorando Giro de Catraca!");

            //Exibe estado do Inner no RodapÃ© da Janela
            System.out.println("Inner " + inners[indiceInner].numero + " Monitora Giro da Catraca...");

            //Monitora o giro da catraca..
            ret = dll.ReceberDadosOnLine(inners[indiceInner].numero, Bilhete, Cartao);

            //Testa o retorno do comando..
            if (ret == Enumeradores.RET_COMANDO_OK) {
                //Testa se girou o nÃ£o a catraca..
                if (Bilhete[0] == Enumeradores.FIM_TEMPO_ACIONAMENTO) {
                    System.out.println("Nao girou a catraca!");
                    //eventoDAO.inserir(new Evento(inners[indiceInner], TipoEvento.TEMPO_EXCEDIDO_PARA_GIRO, inners[indiceInner].cartao));
                    acessoDao.inserir(new Acesso(inners[indiceInner].numero, AcessoTipo.SEM_MOVIMENTO, inners[indiceInner].cartao.id, inners[indiceInner].cartao.pessoaId));
                } else if (Bilhete[0] == (int) Enumeradores.GIRO_DA_CATRACA_TOPDATA) {
                    if (!inners[indiceInner].entradaCatracaDireita) {
                        if (Integer.parseInt(String.valueOf(Bilhete[1])) == 0) {
                            System.out.println("Girou a catraca para saída.");
                            //eventoDAO.inserir(new Evento(inners[indiceInner], TipoEvento.SAIDA_LIBERADA, inners[indiceInner].cartao));
                            acessoDao.inserir(new Acesso(inners[indiceInner].numero, AcessoTipo.SAIDA_LIBERADA, inners[indiceInner].cartao.id, inners[indiceInner].cartao.pessoaId));
                            cartaoDao.desassociar(inners[indiceInner].cartao);

                        } else {
                            System.out.println("Girou a catraca para entrada.");
                            //eventoDAO.inserir(new Evento(inners[indiceInner], TipoEvento.ENTRADA_LIBERADA, inners[indiceInner].cartao));
                            //acessoDAO.inserir(new InnerAcesso(inners[indiceInner], "ENTRADA", inners[indiceInner].cartao.codigo));
                            acessoDao.inserir(new Acesso(inners[indiceInner].numero, AcessoTipo.ENTRADA_LIBERADA, inners[indiceInner].cartao.id, inners[indiceInner].cartao.pessoaId));
                        }
                    } else {
                        if (Integer.parseInt(String.valueOf(Bilhete[1])) == 0) {
                            System.out.println("Girou a catraca para entrada.");
                            //eventoDAO.inserir(new Evento(inners[indiceInner], TipoEvento.ENTRADA_LIBERADA, inners[indiceInner].cartao));
                            //acessoDAO.inserir(new InnerAcesso(inners[indiceInner], "ENTRADA", inners[indiceInner].cartao.codigo));
                            acessoDao.inserir(new Acesso(inners[indiceInner].numero, AcessoTipo.ENTRADA_LIBERADA, inners[indiceInner].cartao.id, inners[indiceInner].cartao.pessoaId));
                        } else {
                            System.out.println("Girou a catraca para saída.");
                            //eventoDAO.inserir(new Evento(inners[indiceInner], TipoEvento.SAIDA_LIBERADA, inners[indiceInner].cartao));
                            //acessoDAO.inserir(new InnerAcesso(inners[indiceInner], "SAIDA", inners[indiceInner].cartao.codigo));
                            acessoDao.inserir(new Acesso(inners[indiceInner].numero, AcessoTipo.SAIDA_LIBERADA, inners[indiceInner].cartao.id, inners[indiceInner].cartao.pessoaId));
                            cartaoDao.desassociar(inners[indiceInner].cartao);
                        }
                    }

                    //RETIRA O CREDITO DO CARTAO
                    //cartaoDAO.retirarCredito(inners[indiceInner].cartao);
                    inners[indiceInner].cartao = null;
                }

                //Vai para o estado de Envio de Msg PadrÃ£o..
                inners[indiceInner].estadoAtual = Enumeradores.EstadosInner.ESTADO_ENVIAR_MSG_PADRAO;
            } else {
                //Caso o tempo que estiver monitorando o giro chegue a 3 segundos,
                //deverÃ¡ enviar o ping on line para manter o equipamento em modo on line
                long tempo = (System.currentTimeMillis() - inners[indiceInner].tempoInicialPingOnLine);
                //Se passar 3 segundos sem receber nada, passa para o estado enviar ping on line, para manter o equipamento em on line.
                if (tempo >= 3000) {
                    inners[indiceInner].estadoSolicitacaoPingOnLine = inners[indiceInner].estadoAtual;
                    inners[indiceInner].countTentativasEnvioComando = 0;
                    inners[indiceInner].tempoInicialPingOnLine = System.currentTimeMillis();
                    inners[indiceInner].estadoAtual = Enumeradores.EstadosInner.PING_ONLINE;
                }
            }
        } catch (Exception ex) {
            inners[indiceInner].estadoAtual = Enumeradores.EstadosInner.ESTADO_CONECTAR;
        }
    }

    /**
     * Testa comunicaÃ§Ã£o com o Inner e mantÃ©m o Inner em OnLine quando a mudanÃ§a automÃ¡tica estÃ¡ configurada. Especialmente indicada para a verificaÃ§Ã£o da conexÃ£o em comunicaÃ§Ã£o TCP/IP. PrÃ³ximo Passo: RETORNA MÃ‰TODO QUE O ACIONOU
     */
    private void PASSO_ESTADO_ENVIA_PING_ONLINE() {
        try {
            //Exibe estado do Inner no RodapÃ© da Janela
            System.out.println("Inner " + inners[indiceInner].numero + " PING ONLINE...");

            //Envia o comando de PING ON LINE, se o retorno for OK volta para o estado onde chamou o mÃ©todo
            int retorno = dll.PingOnLine(inners[indiceInner].numero);
            if (retorno == dll.RET_COMANDO_OK) {
                inners[indiceInner].estadoAtual = inners[indiceInner].estadoSolicitacaoPingOnLine;
            } else {
                //caso ele nÃ£o consiga, tentarÃ¡ enviar trÃªs vezes, se nÃ£o conseguir volta para o passo Reconectar
                if (inners[indiceInner].countTentativasEnvioComando >= 3) {
                    inners[indiceInner].estadoAtual = Enumeradores.EstadosInner.ESTADO_RECONECTAR;
                }
                inners[indiceInner].countTentativasEnvioComando++;
            }
            inners[indiceInner].tempoInicialPingOnLine = System.currentTimeMillis();
        } catch (Exception ex) {
            inners[indiceInner].estadoAtual = Enumeradores.EstadosInner.ESTADO_RECONECTAR;
        }
    }

    /**
     * Se a conexÃ£o cair tenta conectar novamente PrÃ³ximo Passo: ESTADO_ENVIAR_CFG_OFFLINE
     */
    private void PASSO_ESTADO_RECONECTAR() {
        try {
            long IniConexao = 0;

            long tempo = System.currentTimeMillis() - inners[indiceInner].tempoInicialPingOnLine;
            if (tempo < 10000) {
                return;
            }
            inners[indiceInner].tempoInicialPingOnLine = (int) System.currentTimeMillis();

            System.out.println("Inner " + inners[indiceInner].numero + " Reconectando...");
            ret = Enumeradores.Limpar;

            IniConexao = System.currentTimeMillis();
            //Realiza loop enquanto o tempo fim for menor que o tempo atual, e o comando retornado diferente de OK.
            do {
                tempo = System.currentTimeMillis() - IniConexao;

                ret = testarConexaoInner(inners[indiceInner].numero);

                Thread.sleep(10l);

            } while (ret != Enumeradores.RET_COMANDO_OK && tempo < 15000);

            //Testa o comando de envio de relÃ³gio para o Inner
            if (ret == Enumeradores.RET_COMANDO_OK) {
                //Zera as variÃ¡veis de controle da maquina de estados.
                inners[indiceInner].countTentativasEnvioComando = 0;
                inners[indiceInner].estadoAtual = Enumeradores.EstadosInner.ESTADO_ENVIAR_CFG_OFFLINE;
            } else {
                //caso ele nÃ£o consiga, tentarÃ¡ enviar trÃªs vezes, se nÃ£o conseguir volta para o passo Reconectar
                if (inners[indiceInner].countTentativasEnvioComando >= 3) {
                    inners[indiceInner].estadoAtual = Enumeradores.EstadosInner.ESTADO_RECONECTAR;
                }
                inners[indiceInner].countTentativasEnvioComando++;
            }

            inners[indiceInner].countRepeatPingOnline = 0;
        } catch (Exception ex) {
            System.out.println("Passo Reconectar :  " + ex);
            inners[indiceInner].estadoAtual = Enumeradores.EstadosInner.ESTADO_CONECTAR;
        }
    }

    /**
     * Realiza a criÃ§Ã£o do array de equipamentos para serem controlados.
     *
     * @throws InterruptedException
     */
    private void iniciarMaquinaEstados() throws InterruptedException {

        //Catraca
/*		if ((typInnersCadastrados[lngInnerAtual].equipamento != Enumeradores.Acionamento_Coletor) && (!(jOptDireita.isSelected()) && (!(jOptEsquerda.isSelected())))) {
			JOptionPane.showMessageDialog(null, "Favor informar o lado de instalaÃ§Ã£o da catraca !", "AtenÃ§Ã£o", JOptionPane.INFORMATION_MESSAGE);
			return;
		}

		jBtnIniciar.setEnabled(false);
		jBtnParar.setEnabled(true);
		jBtnLimpar.setEnabled(true);
         */
        System.out.println("");
        System.out.println("");
//		jTxaVersao.setText("");
//		jBtnIniciar.setEnabled(false);

        //******************************************************
        //MAIS DE UM INNER
        //Define a quantidade de Inners que o sistema terÃ¡..
        try {
            innerDAO = new InnerDao();
            List<Inner> innerss = innerDAO.listaTodos();

            totalInners = innerss.size();
            indiceInner = 0;

            //Atribui o vetor com os nÃºmeros dos Inners, sempre de 1 a N
            for (int i = 0; i < totalInners; i++) {
                System.out.println(innerss.get(i).equipamento);
                inners[i] = innerss.get(i);
                inners[i].cntDoEvents = 0;
                inners[i].countPingFail = 0;
                inners[i].countTentativasEnvioComando = 0;
                inners[i].estadoAtual = Enumeradores.EstadosInner.ESTADO_CONECTAR;
                inners[i].tempoInicialPingOnLine = (int) System.currentTimeMillis();
                inners[i].estadoTeclado = Enumeradores.EstadosTeclado.TECLADO_EM_BRANCO;
                /*			inners[i].numero = (int) jTblInners.getValueAt(i, 0);
				inners[i].qtdDigitos = (int) jTblInners.getValueAt(i, 1);
				inners[i].teclado = (boolean) jTblInners.getValueAt(i, 2);
				inners[i].lista = (boolean) jTblInners.getValueAt(i, 3);
				inners[i].listaBio = (boolean) jTblInners.getValueAt(i, 4);
				inners[i].tipoLeitor = (int) jTblInners.getValueAt(i, 5);
				inners[i].identificacao = ((boolean) jTblInners.getValueAt(i, 6) ? 1 : 0);
				inners[i].verificacao = ((boolean) jTblInners.getValueAt(i, 7) ? 1 : 0);
				inners[i].doisLeitores = (boolean) jTblInners.getValueAt(i, 8);
				inners[i].catraca = (boolean) jTblInners.getValueAt(i, 9);
				inners[i].biometrico = (boolean) jTblInners.getValueAt(i, 10);
				inners[i].cntDoEvents = 0;
				inners[i].countPingFail = 0;
				inners[i].countTentativasEnvioComando = 0;
				inners[i].estadoAtual = Enumeradores.EstadosInner.ESTADO_CONECTAR;
				inners[i].tempoInicialPingOnLine = (int) System.currentTimeMillis();
				inners[i].estadoTeclado = Enumeradores.EstadosTeclado.TECLADO_EM_BRANCO;
                 */
            }

            //Fecha qualquer conexÃ£o que estivesse aberta..
            dll.FecharPortaComunicacao();
            //Define o tipo de conexÃ£o conforme o selecionado no combo (serial, TCP porta Variavel, TCP Porta Fixa..etc)
            dll.DefinirTipoConexao(inners[indiceInner].tipoConexao);

            //Abre a porta de ComunicaÃ§Ã£o com os Inners..
            ret = dll.AbrirPortaComunicacao(inners[indiceInner].porta);

            //Caso o retorno seja OK, abre a maquina de Estados..
            if (ret == Enumeradores.RET_COMANDO_OK) {
                parar = false;
                maquinaEstados();
            } else {
                //JOptionPane.showMessageDialog(null, "Erro ao tentar abrir a porta de comunicação.", "Mensagem", JOptionPane.INFORMATION_MESSAGE);
                System.out.println("Erro ao tentar abrir a porta de comunicação.");
//				jBtnIniciar.setEnabled(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Monta as configuraÃ§Ãµes necessÃ¡ria para o funcionamento do Inner. Esta funÃ§Ã£o Ã© utilizada on-line ou off-line. modo = 0 off line/modo = 1 on line
     *
     * @param modo
     */
    private void montaConfiguracaoInner(int modo) {
        try {
            // ANTES de realizar a configuraÃ§Ã£o precisa definir o PadrÃ£o do cartÃ£o
            if (inners[indiceInner].padraoCartao == 1) {
                dll.DefinirPadraoCartao(Enumeradores.PADRAO_LIVRE);
            } else {
                dll.DefinirPadraoCartao(Enumeradores.PADRAO_TOPDATA);
            }

            //Define Modo de comunicaÃ§Ã£o
            if (modo == Enumeradores.MODO_OFF_LINE) {
                //ConfiguraÃ§Ãµes para Modo Offline.
                //Prepara o Inner para trabalhar no modo Off-Line, porÃ©m essa funÃ§Ã£o
                //ainda nÃ£o envia essa informaÃ§Ã£o para o equipamento.
                dll.ConfigurarInnerOffLine();

                /*	dll.ConfigurarAcionamento1(Enumeradores.CATRACA_LIBERADA_DOIS_SENTIDOS, 5);
				dll.ConfigurarAcionamento2(Enumeradores.NAO_UTILIZADO, 0);
				
				dll.ConfigurarTipoLeitor(Enumeradores.CODIGO_DE_BARRAS);
				
				dll.InserirQuantidadeDigitoVariavel(6);
				dll.InserirQuantidadeDigitoVariavel(12);
				
				dll.HabilitarTeclado((inners[indiceInner].teclado ? Enumeradores.Opcao_SIM : Enumeradores.Opcao_NAO), 0);

				//Define os valores para configurar os leitores de acordo com o tipo de inner
				defineValoresParaConfigurarLeitores();
				dll.ConfigurarLeitor1(inners[indiceInner].valorLeitor1);
				dll.ConfigurarLeitor2(inners[indiceInner].valorLeitor2);

				//Box = Configura equipamentos com dois leitores
				if (inners[indiceInner].doisLeitores) {
					// exibe mensagens do segundo leitor
					dll.ConfigurarWiegandDoisLeitores(1, Enumeradores.Opcao_SIM);
				}

				// Registra acesso negado
				dll.RegistrarAcessoNegado(1);
				
				dll.DefinirFuncaoDefaultLeitoresProximidade(12);
				
				dll.DefinirFuncaoDefaultSensorBiometria(0);
				
				dll.ReceberDataHoraDadosOnLine(Enumeradores.Opcao_SIM); */
            } else {
                //ConfiguraÃ§Ãµes para Modo Online.
                //Prepara o Inner para trabalhar no modo On-Line, porÃ©m essa funÃ§Ã£o
                //ainda nÃ£o envia essa informaÃ§Ã£o para o equipamento.
                dll.ConfigurarInnerOnLine();

            }

            //Verificar
            //Acionamentos 1 e 2
            //Configura como irÃ¡ funcionar o acionamento(rele) 1 e 2 do Inner, e por
            //quanto tempo ele serÃ¡ acionado.
            switch (inners[indiceInner].equipamento) {
                //Coletor
                case Enumeradores.Acionamento_Coletor:
                    dll.ConfigurarAcionamento1(Enumeradores.ACIONA_REGISTRO_ENTRADA_OU_SAIDA, 3);
                    dll.ConfigurarAcionamento2(Enumeradores.ACIONA_REGISTRO_ENTRADA_OU_SAIDA, 2);
                    break;

                //Catraca
                case Enumeradores.Acionamento_Catraca_Entrada_E_Saida:
                    dll.ConfigurarAcionamento1(Enumeradores.ACIONA_REGISTRO_ENTRADA_OU_SAIDA, 5);
                    dll.ConfigurarAcionamento2(Enumeradores.NAO_UTILIZADO, 0);
                    break;

                case Enumeradores.Acionamento_Catraca_Entrada:
                    dll.ConfigurarAcionamento1(Enumeradores.ACIONA_REGISTRO_ENTRADA, 5);
                    dll.ConfigurarAcionamento2(Enumeradores.NAO_UTILIZADO, 0);
                    break;

                case Enumeradores.Acionamento_Catraca_Saida:
                    dll.ConfigurarAcionamento1(Enumeradores.ACIONA_REGISTRO_SAIDA, 5);
                    dll.ConfigurarAcionamento2(Enumeradores.NAO_UTILIZADO, 0);
                    break;

                case Enumeradores.Acionamento_Catraca_Urna:
                    dll.ConfigurarAcionamento1(Enumeradores.ACIONA_REGISTRO_ENTRADA_OU_SAIDA, 5);
                    dll.ConfigurarAcionamento2(Enumeradores.ACIONA_REGISTRO_SAIDA, 5);
                    break;

                case Enumeradores.Acionamento_Catraca_Saida_Liberada:
                    //Se Esquerda Selecionado - Inverte cÃ³digo
                    if ((inners[indiceInner].equipamento != Enumeradores.Acionamento_Coletor) && (!inners[indiceInner].entradaCatracaDireita)) {
                        dll.ConfigurarAcionamento1(Enumeradores.CATRACA_ENTRADA_LIBERADA, 5);
                    } else {
                        dll.ConfigurarAcionamento1(Enumeradores.CATRACA_SAIDA_LIBERADA, 5);
                    }
                    dll.ConfigurarAcionamento2(Enumeradores.NAO_UTILIZADO, 0);
                    break;

                case Enumeradores.Acionamento_Catraca_Entrada_Liberada:
                    //Se Esquerda Selecionado - Inverte cÃ³digo
                    if ((inners[indiceInner].equipamento != Enumeradores.Acionamento_Coletor) && (!inners[indiceInner].entradaCatracaDireita)) {
                        dll.ConfigurarAcionamento1(Enumeradores.CATRACA_SAIDA_LIBERADA, 5);
                    } else {
                        dll.ConfigurarAcionamento1(Enumeradores.CATRACA_ENTRADA_LIBERADA, 5);
                    }
                    dll.ConfigurarAcionamento2(Enumeradores.NAO_UTILIZADO, 0);
                    break;

                case Enumeradores.Acionamento_Catraca_Liberada_2_Sentidos:
                    dll.ConfigurarAcionamento1(Enumeradores.CATRACA_LIBERADA_DOIS_SENTIDOS, 5);
                    dll.ConfigurarAcionamento2(Enumeradores.NAO_UTILIZADO, 0);
                    break;

                case Enumeradores.Acionamento_Catraca_Sentido_Giro:
                    dll.ConfigurarAcionamento1(Enumeradores.CATRACA_LIBERADA_DOIS_SENTIDOS_MARCACAO_REGISTRO, 5);
                    dll.ConfigurarAcionamento2(Enumeradores.NAO_UTILIZADO, 0);
                    break;
            }

            //Configurar tipo do leitor
            switch (inners[indiceInner].tipoLeitor) {
                case Enumeradores.CODIGO_DE_BARRAS:
                    dll.ConfigurarTipoLeitor(Enumeradores.CODIGO_DE_BARRAS);
                    break;
                case Enumeradores.MAGNETICO:
                    dll.ConfigurarTipoLeitor(Enumeradores.MAGNETICO);
                    break;
                case Enumeradores.PROXIMIDADE_ABATRACK2:
                    dll.ConfigurarTipoLeitor(Enumeradores.PROXIMIDADE_ABATRACK2);
                    break;
                case Enumeradores.WIEGAND:
                    dll.ConfigurarTipoLeitor(Enumeradores.WIEGAND);
                    break;
                case Enumeradores.PROXIMIDADE_SMART_CARD:
                    dll.ConfigurarTipoLeitor(Enumeradores.PROXIMIDADE_SMART_CARD);
                    break;
                case Enumeradores.CODIGO_BARRAS_SERIAL:
                    dll.ConfigurarTipoLeitor(Enumeradores.CODIGO_BARRAS_SERIAL);
                    break;
                case Enumeradores.WIEGAND_FC_SEM_ZERO:
                    dll.ConfigurarTipoLeitor(Enumeradores.WIEGAND_FC_SEM_ZERO);
                    break;
            }

            dll.DefinirQuantidadeDigitosCartao(inners[indiceInner].qtdDigitos);
            //dll.DefinirQuantidadeDigitosCartao(12);

            dll.InserirQuantidadeDigitoVariavel(6);
            dll.InserirQuantidadeDigitoVariavel(12);

            //Habilitar teclado
            dll.HabilitarTeclado((inners[indiceInner].teclado ? Enumeradores.Opcao_SIM : Enumeradores.Opcao_NAO), 0);

            //Define os valores para configurar os leitores de acordo com o tipo de inner
            defineValoresParaConfigurarLeitores();
            dll.ConfigurarLeitor1(inners[indiceInner].valorLeitor1);
            dll.ConfigurarLeitor2(inners[indiceInner].valorLeitor2);

            //Box = Configura equipamentos com dois leitores
            if (inners[indiceInner].doisLeitores) {
                // exibe mensagens do segundo leitor
                dll.ConfigurarWiegandDoisLeitores(1, Enumeradores.Opcao_SIM);
            }

            // Registra acesso negado
            dll.RegistrarAcessoNegado(1);

            //Catraca
            //Define qual serÃ¡ o tipo do registro realizado pelo Inner ao aproximar um
            //cartÃ£o do tipo proximidade no leitor do Inner, sem que o usuÃ¡rio tenha
            //pressionado a tecla entrada, saÃ­da ou funÃ§Ã£o.
            if ((inners[indiceInner].equipamento == Enumeradores.Acionamento_Catraca_Entrada_E_Saida) || (inners[indiceInner].equipamento == Enumeradores.Acionamento_Catraca_Liberada_2_Sentidos) || (inners[indiceInner].equipamento == Enumeradores.Acionamento_Catraca_Sentido_Giro)) {
                dll.DefinirFuncaoDefaultLeitoresProximidade(12); // 12 â€“ Libera a catraca nos dois sentidos e registra o bilhete conforme o sentido giro.
            } else {
                if ((inners[indiceInner].equipamento == Enumeradores.Acionamento_Catraca_Entrada) || (inners[indiceInner].equipamento == Enumeradores.Acionamento_Catraca_Saida_Liberada)) {
                    if (inners[indiceInner].entradaCatracaDireita) {
                        dll.DefinirFuncaoDefaultLeitoresProximidade(10);  // 10 â€“ Registrar sempre como entrada.
                    } else {
                        dll.DefinirFuncaoDefaultLeitoresProximidade(11);  // 11 â€“ Registrar sempre como saÃ­da.
                    }
                } else {
                    if (inners[indiceInner].entradaCatracaDireita) {
                        dll.DefinirFuncaoDefaultLeitoresProximidade(11);  // 11 â€“ Registrar sempre como saÃ­da.
                    } else {
                        dll.DefinirFuncaoDefaultLeitoresProximidade(10);  // 10 â€“ Registrar sempre como entrada.
                    }
                }
            }

            //Configura o tipo de registro que serÃ¡ associado a uma marcaÃ§Ã£o
            if (inners[indiceInner].biometrico) {
                dll.DefinirFuncaoDefaultSensorBiometria(10);
            } else {
                dll.DefinirFuncaoDefaultSensorBiometria(0);
            }

            //Configura para receber o horario dos dados qdo Online.
            if (inners[indiceInner].qtdDigitos <= 14) {
                dll.ReceberDataHoraDadosOnLine(Enumeradores.Opcao_SIM);
            }

            // easyInner.InserirQuantidadeDigitoVariavel(8);
            // easyInner.InserirQuantidadeDigitoVariavel(10);
            // easyInner.InserirQuantidadeDigitoVariavel(14);
            //dll.InserirQuantidadeDigitoVariavel(6);
            //dll.InserirQuantidadeDigitoVariavel(12);
        } catch (Exception ex) {
            //JOptionPane.showMessageDialog(rootPane, ex);
            System.out.println(ex.getMessage());
        }
    }

    /**
     * FUNCIONAMENTO DA MÃ�QUINA DE ESTADOS MÃ‰TODO RESPONSÃ�VEL EM EXECUTAR OS PROCEDIMENTOS DO MODO ONLINE A MÃ¡quina de Estados nada mais Ã© do que uma rotina que fica em loop testando uma variÃ¡vel que chamamos de Estado. Dependendo do estado atual, executamos alguns procedimentos e em seguida alteramos o estado que serÃ¡ verificado pela mÃ¡quina de estados novamente no prÃ³ximo passo do loop.
     *
     * @throws InterruptedException
     */
    private void maquinaEstados() throws InterruptedException {

        //Enquanto Parar = false prosseguir a maquina...
        while (!parar) {
            //Verifica o Estado do Inner Atual..
            switch (inners[indiceInner].estadoAtual) {
                case ESTADO_CONECTAR:
                    PASSO_ESTADO_CONECTAR();
                    break;

                case ESTADO_ENVIAR_CFG_OFFLINE:
                    PASSO_ESTADO_ENVIAR_CFG_OFFLINE();
                    break;

                case ESTADO_COLETAR_BILHETES:
                    PASSO_ESTADO_COLETAR_BILHETES();
                    break;

                case ESTADO_ENVIAR_CFG_ONLINE:
                    PASSO_ESTADO_ENVIAR_CFG_ONLINE();
                    break;

                case ESTADO_ENVIAR_DATA_HORA:
                    PASSO_ESTADO_ENVIAR_DATA_HORA();
                    break;

                case ESTADO_ENVIAR_MSG_PADRAO:
                    PASSO_ENVIAR_MENSAGEM_PADRAO();
                    break;

                case ESTADO_CONFIGURAR_ENTRADAS_ONLINE:
                    PASSO_ESTADO_CONFIGURAR_ENTRADAS_ONLINE();
                    break;

                case ESTADO_POLLING:
                    PASSO_ESTADO_POLLING();
                    break;

                case ESTADO_LIBERAR_CATRACA:
                    PASSO_LIBERA_GIRO_CATRACA();
                    break;

                case ESTADO_MONITORA_GIRO_CATRACA:
                    PASSO_MONITORA_GIRO_CATRACA();
                    break;

                case PING_ONLINE:
                    PASSO_ESTADO_ENVIA_PING_ONLINE();
                    break;

                case ESTADO_RECONECTAR:
                    PASSO_ESTADO_RECONECTAR();
                    break;

                case AGUARDA_TEMPO_MENSAGEM:
                    PASSO_AGUARDA_TEMPO_MENSAGEM();
                    break;

                case ESTADO_DEFINICAO_TECLADO:
                    PASSO_ESTADO_DEFINICAO_TECLADO();
                    break;

                case ESTADO_AGUARDA_DEFINICAO_TECLADO:
                    PASSO_ESTADO_AGUARDA_DEFINICAO_TECLADO();
                    break;

                case ESTADO_ENVIA_MSG_URNA:
                    PASSO_ESTADO_ENVIA_MSG_URNA();
                    break;

                case ESTADO_MONITORA_URNA:
                    PASSO_ESTADO_MONITORA_URNA();
                    break;

                case ACIONAR_RELE:
                    ACIONAR_RELE();
                    break;

                case ESTADO_ENVIAR_CONFIGMUD_ONLINE_OFFLINE:
                    PASSO_ESTADO_ENVIAR_CONFIGMUD_ONLINE_OFFLINE();
                    break;

                case ESTADO_ENVIAR_MENSAGEM:
                    PASSO_ESTADO_ENVIAR_MSG_OFFLINE();
                    break;

                case ESTADO_ENVIAR_HORARIOS:
                    PASSO_ESTADO_ENVIAR_HORARIOS();
                    break;

                case ESTADO_ENVIAR_MENSAGEM_ACESSO_NEGADO:
                    PASSO_ENVIAR_MENSAGEM_ACESSO_NEGADO();
                    break;

                case ESTADO_ENVIAR_USUARIOS_LISTAS:
                    PASSO_ESTADO_ENVIAR_USUARIOS_LISTAS();
                    break;
            }

            if (inners[indiceInner].cntDoEvents++ > 10) {
                inners[indiceInner].cntDoEvents = 0;

                Thread.sleep(1000l);

            }

            //INCREMENTA A VARIÃ�VEL QUE FAZ A CONTAGEM DE INNERS..
            indiceInner = indiceInner + 1;

            //CASO O VALOR INCREMENTAL FOR MAIOR QUE A QUANTIDADE TOTAL, REATRIBUI 0
            //PARA VARIÃ�VEL..
            if (indiceInner > totalInners - 1) {
                indiceInner = 0;
            }
        }

        //Fecha a porta de ComunicaÃ§Ã£o quando sai da maquina de estados..
        dll.FecharPortaComunicacao();
    }

    /**
     * CONFIGURAÃ‡ÃƒO LEITORES De acordo com o lado da catraca, coletor ou se Ã© dois leitores
     */
    private void defineValoresParaConfigurarLeitores() {

        //ConfiguraÃ§Ã£o Catraca Esquerda ou Direita
        //define os valores para configurar os leitores de acordo com o tipo de inner
        if (inners[indiceInner].doisLeitores) {
            if (inners[indiceInner].entradaCatracaDireita) {
                //Direita Selecionado
                inners[indiceInner].valorLeitor1 = Enumeradores.SOMENTE_ENTRADA;
                inners[indiceInner].valorLeitor2 = Enumeradores.SOMENTE_SAIDA;
            } else {
                //Esquerda Selecionado
                inners[indiceInner].valorLeitor1 = Enumeradores.SOMENTE_SAIDA;
                inners[indiceInner].valorLeitor2 = Enumeradores.SOMENTE_ENTRADA;
            }
        } else {
            if (inners[indiceInner].entradaCatracaDireita) {
                //Direita Selecionado
                inners[indiceInner].valorLeitor1 = Enumeradores.ENTRADA_E_SAIDA;
            } else {
                //Esquerda Selecionado
                inners[indiceInner].valorLeitor1 = Enumeradores.ENTRADA_E_SAIDA_INVERTIDAS;
            }

            inners[indiceInner].valorLeitor2 = Enumeradores.DESATIVADO;

        }
    }

    /**
     * Define MudanÃ§as OnLine FunÃ§Ã£o que configura BIT a BIT, Ver no manual Anexo III
     *
     * @param InnerAtual
     * @return
     */
    private static int configuraEntradasMudancaOnLine(Inner InnerAtual) {
        String Configuracao;

        //Habilita Teclado
        Configuracao = (inners[indiceInner].teclado ? "1" : "0");

        if (!inners[indiceInner].biometrico) {
            //CÃ³digo de Barras e Proximidade

            //Dois leitores
            if (inners[indiceInner].doisLeitores) {
                Configuracao = "010"
                        + //Leitor 2 sÃ³ saida
                        "001"
                        + //Leitor 1 sÃ³ entrada
                        Configuracao;
            } else { //Apenas um leitores
                Configuracao = "000"
                        + //Leitor 2 Desativado
                        "011"
                        + //Leitor 1 configurado para Entrada e SaÃ­da
                        Configuracao;
            }

            Configuracao = "1"
                    + // Habilitado
                    Configuracao;

            /*
             --------------------------------------------------------------------------------------------------
             |       7        |     6      |   5    |   4    |   3    |    2    |      1     |        0       |
             --------------------------------------------------------------------------------------------------
             | Seta/Reseta    |  Bit 2     |  Bit 1 |  Bit 0 | Bit 2  |  Bit 1  |   Bit 0    |  Teclado       |
             |   config.      | Leitor 2   |        |        |        |         |            |                |
             |   bit-a-bit    |            |        |        |        |         |            |                |
             --------------------------------------------------------------------------------------------------
             | 1 â€“ Habilita   | 000 â€“ Desativa leitor        |  000 - Desativa leitor        | 1 â€“ Habilita   |
             | 0 â€“ Desabilita | 001 - Leitor sÃ³ entrada      |  001 - Leitor sÃ³ entrada      | 0 â€“ Desabilita |
             |                | 010 - Leitor sÃ³ saÃ­da        |  010 - Leitor sÃ³ saÃ­da        |                |
             |                | 011 - Leitor Entrada e saÃ­da |  011 - Leitor Entrada e saÃ­da |                |
             |                | 100 - Leitor Entrada e SaÃ­da |  100 - Leitor Entrada e       |                |
             |                |   Invertido                  |   SaÃ­da Invertido             |                |
             --------------------------------------------------------------------------------------------------
             */
        } else { //Com Biometria

            Configuracao = "0"
                    + //Bit Fixo
                    "1"
                    + //Habilitado
                    inners[indiceInner].identificacao
                    + //IdentificaÃ§Ã£o
                    inners[indiceInner].verificacao
                    + //VerificaÃ§Ã£o
                    "0"
                    + //Bit fixo
                    (inners[indiceInner].doisLeitores ? "11" : "10")
                    + // 11 -> habilita leitor 1 e 2, 10 -> habilita apenas leitor 1
                    Configuracao;

            /*
             ------------------------------------------------------------------------------------------------------------------------
             |    7     |       6       |       5       |       4       |      3       |       2      |      1       |      0       |
             ------------------------------------------------------------------------------------------------------------------------
             | Bit fixo | Seta/Reseta   | IdentificaÃ§Ã£o |  VerificaÃ§Ã£o  |   Bit fixo   |   Leitor 1   | Leitor 2     |  Teclado     |
             |   '0'    |    config.    |      Bio      |      Bio      |    Config    |              |              |              |
             |          | bit-a-bit bio |               |               |      L2      |              |              |              |
             |          |               |               |               |     '0'      |              |              |              |
             ------------------------------------------------------------------------------------------------------------------------
             |    0     |  1-Habilita   | 1-Habilita    | 1-Habilita    | 1-Habilita   | 1-Habilita   | 1-Habilita   | 1-Habilita   |
             |          |  0-Desabilita | 0-Desabilita  | 0-Desabilita  | 0-Desabilita | 0-Desabilita | 0-Desabilita | 0-Desabilita |
             ------------------------------------------------------------------------------------------------------------------------
             */
        }

        //Converte BinÃ¡rio para Decimal
        return binarioParaDecimal(Configuracao);
    }

    /**
     * Realiza a conversÃ£o BinÃ¡rio para Decimal
     *
     * @param valorBinario
     * @return
     */
    private static int binarioParaDecimal(String valorBinario) {
        int length_bin = 0, aux = 0, retorno = 0, i;
        length_bin = valorBinario.length();

        for (i = 0; i < length_bin; i++) {
            aux = Integer.parseInt(valorBinario.substring(i, i + 1));
            retorno += aux * (int) Math.pow(2, (length_bin - i)) / 2;
        }
        return (retorno);
    }

    /**
     * Esta rotina Ã© responsÃ¡vel por identificar a versÃ£o do inner e modelo.
     *
     * @throws InterruptedException
     */
    private void defineVersao() throws InterruptedException {
        int Versao[] = new int[30];
        long Variacao = 0;
        int Modelo[] = new int[3];

        //Solicita a versÃ£o do firmware do Inner e dados como o Idioma, se Ã©
        //uma versÃ£o especial.
        ret = dll.ReceberVersaoFirmware(inners[indiceInner].numero, Versao);

        inners[indiceInner].innerAcessoBio = Versao[6];
        //Se selecionado Biometria, valida se o equipamento Ã© compatÃ­vel
        if (inners[indiceInner].biometrico) {
            if ((((Versao[0] != 6) && (Versao[0] != 14)) || ((Versao[0] == 14) && (inners[indiceInner].innerAcessoBio == 0))) && inners[indiceInner].biometrico) {
                //JOptionPane.showMessageDialog(null, "Equipamento " + String.valueOf(inners[indiceInner].numero) + " nÃ£o compatÃ­vel com Biometria.", "AtenÃ§Ã£o", JOptionPane.INFORMATION_MESSAGE);
                System.out.println("Equipamento " + String.valueOf(inners[indiceInner].numero) + " não compatível com Biometria.");
            }
        }

        if (ret == Enumeradores.RET_COMANDO_OK) {
            //Define a linha do Inner
            switch (Versao[0]) {
                case 1:
                    inners[indiceInner].linhaInner = "Inner Plus";
                    break;

                case 2:
                    inners[indiceInner].linhaInner = "Inner Disk";
                    break;

                case 3:
                    inners[indiceInner].linhaInner = "Inner Verid";
                    break;

                case 6:
                    inners[indiceInner].linhaInner = "Inner Bio";
                    break;

                case 7:
                    inners[indiceInner].linhaInner = "Inner NET";
                    break;

                case 14:
                    inners[indiceInner].linhaInner = "Inner Acesso";
                    inners[indiceInner].innerNetAcesso = true;
                    break;
            }

            inners[indiceInner].variacaoInner = Variacao;
            inners[indiceInner].versaoInner = Integer.toString(Versao[3]) + '.' + Integer.toString(Versao[4]) + '.' + Integer.toString(Versao[5]);

            //Se for biometria
            if ((Versao[0] == 6) || (Versao[0] == 14 && inners[indiceInner].innerAcessoBio == 1)) {

                //Solicita o modelo do Inner bio.
                ret = dll.SolicitarModeloBio(inners[indiceInner].numero);

                do {
                    //Retorna o resultado do comando SolicitarModeloBio, o modelo
                    //do Inner Bio Ã© retornado por referÃªncia no parÃ¢metro da funÃ§Ã£o.
                    ret = dll.ReceberModeloBio(inners[indiceInner].numero, 0, Modelo);
                    Thread.sleep(100);
                } while (ret == 128);

                //Define o modelo do Inner Bio
                switch (Modelo[0]) {
                    case 1:
                        inners[indiceInner].modeloBioInner = "Modelo do bio: Light 100 usuários FIM10";
                        break;
                    case 4:
                        inners[indiceInner].modeloBioInner = "Modelo do bio: 1000/4000 usuários FIM01";
                        break;
                    case 51:
                        inners[indiceInner].modeloBioInner = "Modelo do bio: 1000/4000 usuários FIM2030";
                        break;
                    case 52:
                        inners[indiceInner].modeloBioInner = "Modelo do bio: 1000/4000 usuários FIM2040";
                        break;
                    case 48:
                        inners[indiceInner].modeloBioInner = "Modelo do bio: Light 100 usuários FIM3030";
                        break;
                    case 64:
                        inners[indiceInner].modeloBioInner = "Modelo do bio: Light 100 usuários FIM3040";
                        break;
                    case 80:
                        inners[indiceInner].modeloBioInner = "Modelo do bio: FIM5060";
                        break;
                    case 82:
                        inners[indiceInner].modeloBioInner = "Modelo do bio: FIM5260";
                        break;
                    case 83:
                        inners[indiceInner].modeloBioInner = "Modelo do bio: FIM5360";
                        break;
                    case 96:
                        inners[indiceInner].modeloBioInner = "Modelo do bio: FIM6060";
                        break;
                    case 255:
                        inners[indiceInner].modeloBioInner = "Modelo do bio: Desconhecido";
                        break;
                }

                //Solicita a versÃ£o do Inner bio.
                ret = dll.SolicitarVersaoBio(inners[indiceInner].numero);

                do {
                    //Retorna o resultado do comando SolicitarVersaoBio, a versÃ£o
                    //do Inner Bio Ã© retornado por referÃªncia nos parÃ¢metros da
                    //funÃ§Ã£o.
                    Thread.sleep(100);
                    ret = dll.ReceberVersaoBio(inners[indiceInner].numero, 0, Versao);
                } while (ret == 128);
                inners[indiceInner].versaoBio = Integer.toString(Versao[0]) + "." + Integer.toString(Versao[1]);
                System.out.println(inners[indiceInner].linhaInner + " - Versão: " + inners[indiceInner].versaoInner + " ");
                System.out.println(inners[indiceInner].modeloBioInner + " -> " + inners[indiceInner].versaoBio + "\r\n");
            }

        }

        innerDAO = new InnerDao();
        try {
            innerDAO.atualizar(inners[indiceInner]);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * MONTAR HORARIOS Insere no buffer da dll um horÃ¡rio de acesso. O Inner possui uma tabela de 100 horÃ¡rios de acesso, para cada horÃ¡rio Ã© possÃ­vel definir 4 faixas de acesso para cada dia da semana. Tabela de horÃ¡rios numero 1
     *
     * @throws Exception
     */
    private void montarHorarios() throws Exception {

        //Insere no buffer da DLL horario de acesso    
        byte Horario = 1;
        byte Dia = 1;
        byte faixa = 1;
        byte Hora = 8;
        byte Minuto = 0;

        dll.InserirHorarioAcesso(Horario, Dia, faixa, Hora, Minuto);
    }

    /**
     * MONTAR LISTA TOPDATA //Monta o buffer para enviar a lista nos inners da linha Inner, cartÃ£o padrÃ£o Topdata
     *
     */
    private void montarListaTopdata() {
        dll.DefinirPadraoCartao(0);

        dll.DefinirQuantidadeDigitosCartao(14);

        for (int i = 0; i < 5; i++) {
            dll.InserirUsuarioListaAcesso(Integer.toString(i), 101);
        }
        dll.EnviarListaAcesso(inners[indiceInner].numero);
    }

    /**
     * MONTAR LISTA LIVRE Monta o buffer para enviar a lista nos inners da linha Inner, cartÃ£o padrÃ£o livre 14 dÃ­gitos
     *
     * @throws Exception
     */
    private void montarListaLivre() throws Exception {
        dll.DefinirPadraoCartao(1);

        dll.DefinirQuantidadeDigitosCartao(inners[indiceInner].qtdDigitos);
        //dll.DefinirQuantidadeDigitosCartao(0);

        //insere usuÃ¡rio na lista de acesso
        dll.InserirUsuarioListaAcesso("1", 101);
        dll.InserirUsuarioListaAcesso("187", 101);
        dll.InserirUsuarioListaAcesso("123456", 101);
        dll.InserirUsuarioListaAcesso("27105070", 101);
        dll.InserirUsuarioListaAcesso("103086639459", 101);

        dll.EnviarListaAcesso(inners[indiceInner].numero);
    }

    /**
     * Monta lista de usuÃ¡rios que nÃ£o possuem digitais cadastradas, caso a Biometria esteja habilitada para verificaÃ§Ã£o nÃ£o sera solicitado.
     *
     * @throws Exception
     */
    private void montarBufferListaSemDigital() throws Exception {
        List<Cartao> cartoes = cartaoDao.listaModoOffLine();
        for (Cartao cartao : cartoes) {
            dll.IncluirUsuarioSemDigitalBio(cartao.codigoBarra);
        }
    }

    public static void main(String[] args) {
        Monitor m = new Monitor();
        m.iniciar();
    }

}
