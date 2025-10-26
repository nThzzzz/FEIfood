package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.Alimento;
import model.Bebida; 
import model.Comida; 
import model.Estabelecimento; 

public class AlimentoDAO {

    private Connection conn;

    public AlimentoDAO(Connection conn) {
        this.conn = conn;
    }

    /**
     * Lista o ID e o Nome de todos os alimentos cadastrados.
     * IMPORTANTE: O ResultSet retornado DEVE ser fechado pelo chamador (Controller).
     * @return ResultSet aberto contendo id_alimento e nome.
     * @throws SQLException Se ocorrer um erro no banco.
     */
    public ResultSet listarTodosResumido() throws SQLException {
        // Prepara o SQL mas não usa try-with-resources aqui
        String sql = "SELECT id_alimento, nome FROM Alimento ORDER BY nome";
        PreparedStatement statement = conn.prepareStatement(sql);
        try {
            ResultSet resultado = statement.executeQuery(); // Use executeQuery para SELECT
            // Retorna o ResultSet aberto. O PreparedStatement será fechado com ele.
            return resultado;
        } catch (SQLException e) {
             // Garante o fechamento do statement em caso de erro na execução
            if (statement != null) {
                try { statement.close(); } catch (SQLException closeEx) { e.addSuppressed(closeEx); }
            }
            throw e; // Relança a exceção original
        }
        // Não feche a conexão conn aqui
    }

    /**
     * Busca todos os detalhes de um alimento específico pelo ID para exibição.
     * Inclui o nome do estabelecimento associado.
     * IMPORTANTE: O ResultSet retornado DEVE ser fechado pelo chamador (Controller).
     * @param id O ID do alimento a ser buscado.
     * @return ResultSet aberto contendo os detalhes do alimento e o nome do estabelecimento.
     * @throws SQLException Se ocorrer um erro no banco.
     */
    public ResultSet buscarPorIdDetalhado(int id) throws SQLException {
        // SQL com JOIN para buscar o nome do estabelecimento
        String sql = "SELECT a.id_alimento, a.nome, a.descricao, a.preco, a.tipo_alimento, a.percentual_imposto, e.nome AS nome_estabelecimento " +
                     "FROM Alimento a " +
                     "JOIN Estabelecimento e ON a.id_estabelecimento = e.id_estabelecimento " +
                     "WHERE a.id_alimento = ?";

        PreparedStatement statement = conn.prepareStatement(sql);
        try {
            statement.setInt(1, id); // Define o parâmetro ID
            ResultSet resultado = statement.executeQuery();
            // Retorna o ResultSet aberto. O PreparedStatement será fechado com ele.
            return resultado;
        } catch (SQLException e) {
             // Garante o fechamento do statement em caso de erro na execução
            if (statement != null) {
                try { statement.close(); } catch (SQLException closeEx) { e.addSuppressed(closeEx); }
            }
            throw e; // Relança a exceção original
        }
        // Não feche a conexão conn aqui
    }

    /**
     * Busca um Alimento pelo seu ID e retorna o objeto correspondente (Comida ou Bebida).
     * Inclui o Estabelecimento associado.
     *
     * @param id O ID do alimento a ser buscado.
     * @return O objeto Alimento (Comida ou Bebida) se encontrado, ou null caso contrário.
     * @throws SQLException Se ocorrer um erro no banco.
     */
    public Alimento buscarAlimentoPorId(int id) throws SQLException {
        Alimento alimentoEncontrado = null;
        String sql = "SELECT a.id_alimento, a.nome, a.descricao, a.preco, a.tipo_alimento, a.percentual_imposto, " +
                     "       e.id_estabelecimento, e.nome AS nome_estabelecimento, e.endereco " +
                     "FROM Alimento a " +
                     "JOIN Estabelecimento e ON a.id_estabelecimento = e.id_estabelecimento " +
                     "WHERE a.id_alimento = ?";

        // try-with-resources garante o fechamento do PreparedStatement e ResultSet
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, id);

            try (ResultSet resultado = statement.executeQuery()) {
                if (resultado.next()) {
                    // Estabelecimento
                    int idEstabelecimento = resultado.getInt("id_estabelecimento");
                    String nomeEstabelecimento = resultado.getString("nome_estabelecimento");
                    String enderecoEstabelecimento = resultado.getString("endereco");
                    Estabelecimento estabelecimento = new Estabelecimento(idEstabelecimento, nomeEstabelecimento, enderecoEstabelecimento);

                    // Alimento
                    int idAlimento = resultado.getInt("id_alimento");
                    String nomeAlimento = resultado.getString("nome");
                    String descricao = resultado.getString("descricao");
                    double preco = resultado.getDouble("preco");
                    String tipoAlimento = resultado.getString("tipo_alimento");
                    double percentualImposto = resultado.getDouble("percentual_imposto"); // Pode ser 0 se NULL

                    // Cria Comida ou Bebida
                    if ("COMIDA".equals(tipoAlimento)) {
                        alimentoEncontrado = new Comida(idAlimento, nomeAlimento, descricao, preco, estabelecimento);
                    } else if ("BEBIDA".equals(tipoAlimento)) {
                        // Assume um percentual de álcool se houver imposto (exemplo)
                        double percentualAlcool = (percentualImposto > 0) ? 5.0 : 0.0;
                        alimentoEncontrado = new Bebida(idAlimento, nomeAlimento, descricao, preco, estabelecimento, percentualAlcool);
                    }
                }
            }
        }
        return alimentoEncontrado;
    }
}