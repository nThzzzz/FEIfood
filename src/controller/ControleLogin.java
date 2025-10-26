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
    private Login telaLogin; 

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
        String email = telaLogin.getTxtUsuario().getText();
        String senha = telaLogin.getTxtSenha().getText(); 

       
        if (email.isEmpty() || senha.isEmpty()) {
            JOptionPane.showMessageDialog(telaLogin, "Usuário e Senha são obrigatórios!", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

       
        Usuario usuarioParaAutenticar = new Usuario("", email, senha);

        Connection conn = null;
        ResultSet res = null; 
        try {
            Conexao conexao = new Conexao();
            conn = conexao.getConnection();
            UsuarioDAO dao = new UsuarioDAO(conn);


            res = dao.consultar(usuarioParaAutenticar);

          
            if (res.next()) {
                // Login bem-sucedido
                JOptionPane.showMessageDialog(telaLogin, "Login efetuado com sucesso!", "Aviso", JOptionPane.INFORMATION_MESSAGE);

                
                int idUsuario = res.getInt("id_usuario"); 
                String nomeUsuario = res.getString("nome"); 
              
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