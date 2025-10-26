package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import model.Usuario; // Importa o modelo Usuario

public class UsuarioDAO {
    private Connection conn; // A conexão é recebida no construtor

    /**
     * Construtor que recebe a conexão com o banco de dados.
     * @param conn A conexão JDBC.
     */
    public UsuarioDAO(Connection conn) {
        this.conn = conn;
    }

    /**
     * Consulta um usuário no banco de dados pelo email e senha.
     * Usado para a funcionalidade de login.
     * @param usu Objeto Usuario contendo o email e a senha para consulta.
     * @return ResultSet contendo os dados do usuário se encontrado, ou vazio caso contrário.
     * @throws SQLException Se ocorrer um erro no banco de dados.
     */
    public ResultSet consultar(Usuario usu) throws SQLException {
        // SQL ajustado para usar a tabela 'Usuario' e a coluna 'email'
        String sql = "SELECT id_usuario, nome, email, senha FROM Usuario WHERE email = ? AND senha = ?";
        PreparedStatement statement = conn.prepareStatement(sql);

        // Define os parâmetros usando os métodos do objeto Usuario
        statement.setString(1, usu.getEmail()); // Usa getEmail()
        statement.setString(2, usu.getSenha());

        // Executa a consulta
        statement.execute(); // execute() pode ser usado para SELECT também

        // Retorna o ResultSet
        ResultSet resultado = statement.getResultSet();
        return resultado;
        // Não feche o statement ou a conexão aqui, quem chamou deve gerenciar isso
    }

    /**
     * Insere um novo usuário no banco de dados.
     * Usado para a funcionalidade de cadastro.
     * @param usu Objeto Usuario contendo nome, email e senha.
     * @throws SQLException Se ocorrer um erro no banco de dados.
     */
    public void inserir(Usuario usu) throws SQLException {
        // SQL ajustado para 'Usuario', 'email' e usando PreparedStatement para segurança
        String sql = "INSERT INTO Usuario (nome, email, senha) VALUES (?, ?, ?)";
        PreparedStatement statement = conn.prepareStatement(sql);

        // Define os parâmetros
        statement.setString(1, usu.getNome());
        statement.setString(2, usu.getEmail()); // Usa getEmail()
        statement.setString(3, usu.getSenha()); // Idealmente, deveria ser um hash da senha

        // Executa a inserção
        statement.executeUpdate(); // executeUpdate() é mais apropriado para INSERT/UPDATE/DELETE

        // Fecha o PreparedStatement (boa prática dentro do método DAO)
        statement.close();
        // Não feche a conexão aqui (conn.close())
    }

    /**
     * Atualiza a senha de um usuário existente, identificado pelo email.
     * (Pode ser adaptado para atualizar outros campos se necessário).
     * @param usu Objeto Usuario contendo o email (para identificar o usuário) e a nova senha.
     * @throws SQLException Se ocorrer um erro no banco de dados.
     */
    public void atualizar(Usuario usu) throws SQLException {
        // SQL ajustado para 'Usuario', 'email' e usando PreparedStatement
        String sql = "UPDATE Usuario SET senha = ? WHERE email = ?";
        PreparedStatement statement = conn.prepareStatement(sql);

        // Define os parâmetros (nova senha, email para o WHERE)
        statement.setString(1, usu.getSenha()); // Nova senha
        statement.setString(2, usu.getEmail()); // Email para identificar o usuário

        // Executa a atualização
        statement.executeUpdate();

        // Fecha o PreparedStatement
        statement.close();
        // Não feche a conexão aqui
    }

    /**
     * Remove um usuário do banco de dados, identificado pelo email.
     * @param usu Objeto Usuario contendo o email do usuário a ser removido.
     * @throws SQLException Se ocorrer um erro no banco de dados.
     */
    public void remover(Usuario usu) throws SQLException {
        // SQL ajustado para 'Usuario', 'email' e usando PreparedStatement
        String sql = "DELETE FROM Usuario WHERE email = ?";
        PreparedStatement statement = conn.prepareStatement(sql);

        // Define o parâmetro (email para o WHERE)
        statement.setString(1, usu.getEmail()); // Email para identificar o usuário

        // Executa a remoção
        statement.executeUpdate();

        // Fecha o PreparedStatement
        statement.close();
        // Não feche a conexão aqui
    }
}