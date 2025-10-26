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
    public void salvarUsuario() { // Renomeado para salvarUsuario para clareza
        // Pega os dados dos campos de texto da tela
        String nome = telaCadastro.getTxtNome().getText();
        // Usa getTxtUsuario() mas o campo representa o EMAIL no modelo Usuario
        String email = telaCadastro.getTxtUsuario().getText();
        // Assume que o campo txtSenha na verdade é um JPasswordField no design real
        // Se for JTextField, pegue com getText(). Por segurança, deveria ser JPasswordField.
        String senha = telaCadastro.getTxtSenha().getText(); // Ou new String(telaCadastro.getTxtSenha().getPassword()) se for JPasswordField

        // Validação básica (poderia ser mais robusta)
        if (nome.isEmpty() || email.isEmpty() || senha.isEmpty()) {
            JOptionPane.showMessageDialog(telaCadastro, "Todos os campos são obrigatórios!", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Cria o objeto Usuario com os dados da tela
        Usuario novoUsuario = new Usuario(nome, email, senha);

        Connection conn = null; // Declara a conexão fora do try para poder fechar no finally
        try {
            // Obtém a conexão com o banco
            Conexao conexao = new Conexao();
            conn = conexao.getConnection();

            // Cria o DAO e insere o usuário
            UsuarioDAO dao = new UsuarioDAO(conn);
            dao.inserir(novoUsuario); // Chama o método inserir do DAO

            // Mostra mensagem de sucesso
            JOptionPane.showMessageDialog(telaCadastro, "Usuário Cadastrado!", "Aviso", JOptionPane.INFORMATION_MESSAGE);

            // Opcional: Limpar os campos da tela após o cadastro
            telaCadastro.getTxtNome().setText("");
            telaCadastro.getTxtUsuario().setText("");
            telaCadastro.getTxtSenha().setText(""); // Ou setPassword("")

            // Opcional: Fechar a tela de cadastro ou voltar para a de login
            // telaCadastro.dispose();

        } catch (SQLException ex) {
            // Mostra mensagem de erro em caso de falha no banco
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