package controller;

import dao.UsuarioDAO;
import dao.Conexao;
import view.Cadastro; // Importa a tela de Cadastro
import model.Usuario; // Importa o modelo Usuario

import java.sql.Connection;
import java.sql.SQLException;
import javax.swing.JOptionPane;

public class ControleCadastro {
    private Cadastro telaCadastro; // Referência para a tela de Cadastro

    /**
     * Construtor que recebe a instância da tela de Cadastro.
     * @param telaCadastro A tela de Cadastro associada a este controller.
     */
    public ControleCadastro(Cadastro telaCadastro) {
        this.telaCadastro = telaCadastro;
    }

    /**
     * Pega os dados da tela, cria um objeto Usuario e tenta inseri-lo no banco.
     */
    public void salvarUsuario() {
        String nome = telaCadastro.getTxtNome().getText();
        String email = telaCadastro.getTxtUsuario().getText();
        String senha = telaCadastro.getTxtSenha().getText(); 

        
        if (nome.isEmpty() || email.isEmpty() || senha.isEmpty()) {
            JOptionPane.showMessageDialog(telaCadastro, "Todos os campos são obrigatórios!", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Usuario novoUsuario = new Usuario(nome, email, senha);

        Connection conn = null; 
        try {
            Conexao conexao = new Conexao();
            conn = conexao.getConnection();

            UsuarioDAO dao = new UsuarioDAO(conn);
            dao.inserir(novoUsuario); 

            JOptionPane.showMessageDialog(telaCadastro, "Usuário Cadastrado!", "Aviso", JOptionPane.INFORMATION_MESSAGE);

            // Opcional: Limpar os campos da tela após o cadastro
            telaCadastro.getTxtNome().setText("");
            telaCadastro.getTxtUsuario().setText("");
            telaCadastro.getTxtSenha().setText(""); // Ou setPassword("")

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(telaCadastro, "Erro ao cadastrar usuário!\n" + ex.getMessage(), "Erro de Banco de Dados", JOptionPane.ERROR_MESSAGE);
            // Logger.getLogger(ControleCadastro.class.getName()).log(Level.SEVERE, null, ex); // Logar o erro
        } finally {
            // Garante que a conexão seja fechada, mesmo se ocorrer erro
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Erro ao fechar a conexão: " + e.getMessage());
                }
            }
        }
    }
}