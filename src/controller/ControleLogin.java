package controller;

import dao.UsuarioDAO;
import dao.Conexao;
import model.Usuario; // Importa o modelo Usuario
import view.Login;   // Importa a tela de Login
import view.Menu;    // Importa a tela de Menu (próxima tela após login)

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JOptionPane;

public class ControleLogin {
    private Login telaLogin; // Referência para a tela de Login

    /**
     * Construtor que recebe a instância da tela de Login.
     * @param telaLogin A tela de Login associada a este controller.
     */
    public ControleLogin(Login telaLogin) {
        this.telaLogin = telaLogin;
    }

    /**
     * Pega os dados da tela, consulta o banco e tenta autenticar o usuário.
     */
    public void autenticarUsuario() { // Renomeado para autenticarUsuario para clareza
        // Pega os dados dos campos de texto da tela
        // Usa getTxtUsuario() mas o campo representa o EMAIL no modelo Usuario
        String email = telaLogin.getTxtUsuario().getText();
        // Assume JPasswordField para senha. Se for JTextField, use getText().
        String senha = telaLogin.getTxtSenha().getText(); // Ou new String(telaLogin.getTxtSenha().getPassword())

        // Validação básica
        if (email.isEmpty() || senha.isEmpty()) {
            JOptionPane.showMessageDialog(telaLogin, "Usuário e Senha são obrigatórios!", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Cria um objeto Usuario apenas com email e senha para consulta
        // O nome não é necessário para o login, podemos passar null ou string vazia
        Usuario usuarioParaAutenticar = new Usuario("", email, senha); // Nome vazio, só email e senha importam

        Connection conn = null;
        ResultSet res = null; // Para guardar o resultado da consulta
        try {
            Conexao conexao = new Conexao();
            conn = conexao.getConnection();
            UsuarioDAO dao = new UsuarioDAO(conn);

            // Consulta o usuário no banco
            res = dao.consultar(usuarioParaAutenticar);

            // Verifica se o ResultSet retornou alguma linha (usuário encontrado)
            if (res.next()) {
                // Login bem-sucedido
                JOptionPane.showMessageDialog(telaLogin, "Login efetuado com sucesso!", "Aviso", JOptionPane.INFORMATION_MESSAGE);

                // Recupera os dados completos do usuário logado do ResultSet
                // Assumindo que a tabela Usuario tem 'id_usuario' e 'nome'
                // Se os nomes das colunas forem diferentes no seu script SQL, ajuste aqui.
                int idUsuario = res.getInt("id_usuario"); // Pega o ID do banco (AJUSTE O NOME DA COLUNA SE NECESSÁRIO)
                String nomeUsuario = res.getString("nome"); // Pega o nome do banco
                // Poderíamos criar um objeto Usuario completo aqui se necessário para passar para a próxima tela
                Usuario usuarioLogado = new Usuario(idUsuario, nomeUsuario, email, senha); // Recria com ID e nome

                // Abre a tela principal (Menu) e fecha a de login
                Menu telaMenu = new Menu(usuarioLogado);
                telaMenu.setVisible(true); //
                telaLogin.dispose(); // Fecha a tela de login (melhor que setVisible(false))

            } else {
                // Login falhou (usuário ou senha incorretos)
                JOptionPane.showMessageDialog(telaLogin, "Usuário ou Senha inválidos!", "Erro de Login", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            //
            JOptionPane.showMessageDialog(telaLogin, "Erro de conexão com o banco de dados:\n" + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            // Logger.getLogger(ControleLogin.class.getName()).log(Level.SEVERE, null, e); // Logar o erro
        } finally {
            // Garante que ResultSet e Connection sejam fechados
             try {
                if (res != null) res.close();
                if (conn != null) conn.close(); //
            } catch (SQLException e) {
                 System.err.println("Erro ao fechar recursos do banco: " + e.getMessage());
            }
        }
    }
}