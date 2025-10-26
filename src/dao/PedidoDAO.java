package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement; // Para retornar a chave gerada
import java.sql.Timestamp; // Para LocalDateTime e ResultSet
import java.time.LocalDateTime;
import model.Pedido;
import model.Alimento;
import model.Usuario; // Necessário para listarPedidosPorUsuario implicitamente
import java.util.ArrayList; // Necessário para futuros métodos de leitura
import java.util.HashMap;
import java.util.LinkedHashMap; // Para manter a ordem de inserção dos itens
import java.util.List; // Necessário para futuros métodos de leitura
import java.util.Map; // Para iterar sobre os itens do pedido e retornar itens

public class PedidoDAO {

    private Connection conn;

    public PedidoDAO(Connection conn) {
        this.conn = conn;
    }

    // --- Métodos existentes (criarPedido, adicionarOuAtualizarItemPedido, removerItemPedido, excluirPedido) ---
    // Mantenha os métodos criarPedido, adicionarOuAtualizarItemPedido, removerItemPedido, excluirPedido aqui...
    /**
     * Cria um novo pedido no banco de dados, incluindo seus itens.
     * Primeiro insere na tabela Pedido e depois na tabela Pedido_Alimento.
     * IMPORTANTE: Idealmente, chame este método dentro de uma transação no Controller.
     *
     * @param pedido O objeto Pedido a ser inserido (deve conter o Usuario e os Itens).
     * @return O ID do pedido criado.
     * @throws SQLException Se ocorrer um erro no banco.
     * @throws IllegalArgumentException Se o pedido não tiver um usuário associado.
     */
    public int criarPedido(Pedido pedido) throws SQLException {
        if (pedido.getUsuario() == null || pedido.getUsuario().getId() <= 0) {
            throw new IllegalArgumentException("Pedido deve estar associado a um usuário válido.");
        }

        // 1. Inserir na tabela Pedido
        String sqlPedido = "INSERT INTO Pedido (data_hora, avaliacao, id_usuario) VALUES (?, ?, ?)";
        int idPedidoCriado = -1;

        // Usamos RETURN_GENERATED_KEYS para obter o id_pedido criado
        try (PreparedStatement statementPedido = conn.prepareStatement(sqlPedido, Statement.RETURN_GENERATED_KEYS)) {

            statementPedido.setTimestamp(1, Timestamp.valueOf(pedido.getDataHora() != null ? pedido.getDataHora() : LocalDateTime.now()));
            // Trata avaliacao nula
            if (pedido.getAvaliacao() != null) {
                statementPedido.setInt(2, pedido.getAvaliacao());
            } else {
                statementPedido.setNull(2, java.sql.Types.INTEGER);
            }
            statementPedido.setInt(3, pedido.getUsuario().getId());

            int affectedRows = statementPedido.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Falha ao criar o pedido, nenhuma linha afetada.");
            }

            // Obter o ID gerado para o pedido
            try (ResultSet generatedKeys = statementPedido.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    idPedidoCriado = generatedKeys.getInt(1);
                    pedido.setId(idPedidoCriado); // Atualiza o ID no objeto Pedido
                } else {
                    throw new SQLException("Falha ao criar o pedido, nenhum ID obtido.");
                }
            }
        } // try-with-resources fecha statementPedido

        // 2. Inserir os itens na tabela Pedido_Alimento
        if (idPedidoCriado > 0 && pedido.getItens() != null && !pedido.getItens().isEmpty()) {
            String sqlItem = "INSERT INTO Pedido_Alimento (id_pedido, id_alimento, quantidade) VALUES (?, ?, ?)";
            // Usar try-with-resources garante que o PreparedStatement seja fechado
            try (PreparedStatement statementItem = conn.prepareStatement(sqlItem)) {
                for (Map.Entry<Alimento, Integer> entry : pedido.getItens().entrySet()) {
                    Alimento alimento = entry.getKey();
                    Integer quantidade = entry.getValue();

                    statementItem.setInt(1, idPedidoCriado);
                    statementItem.setInt(2, alimento.getId());
                    statementItem.setInt(3, quantidade);
                    statementItem.addBatch(); // Adiciona o comando ao batch
                }
                statementItem.executeBatch(); // Executa todos os inserts de itens de uma vez
            } // try-with-resources fecha statementItem
        }
        // Não feche a conexão conn aqui

        return idPedidoCriado;
    }

    /**
     * Adiciona um alimento específico a um pedido existente ou atualiza sua quantidade
     * na tabela Pedido_Alimento.
     *
     * @param idPedido O ID do pedido a ser modificado.
     * @param idAlimento O ID do alimento a ser adicionado/atualizado.
     * @param quantidade A quantidade do alimento (se já existir, esta será a nova quantidade).
     * @throws SQLException Se ocorrer um erro no banco.
     */
    public void adicionarOuAtualizarItemPedido(int idPedido, int idAlimento, int quantidade) throws SQLException {
        if (quantidade <= 0) {
            // Se a quantidade for zero ou negativa, remove o item
            removerItemPedido(idPedido, idAlimento);
            return;
        }
        // Tenta inserir, se houver conflito (chave primária id_pedido, id_alimento já existe), atualiza a quantidade
        String sql = "INSERT INTO Pedido_Alimento (id_pedido, id_alimento, quantidade) VALUES (?, ?, ?) " +
                     "ON CONFLICT (id_pedido, id_alimento) DO UPDATE SET quantidade = EXCLUDED.quantidade";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, idPedido);
            statement.setInt(2, idAlimento);
            statement.setInt(3, quantidade);
            statement.executeUpdate();
        } // try-with-resources fecha statement
        // Não feche a conexão conn aqui
    }

     /**
     * Remove um alimento específico de um pedido na tabela Pedido_Alimento.
     *
     * @param idPedido O ID do pedido.
     * @param idAlimento O ID do alimento a ser removido.
     * @throws SQLException Se ocorrer um erro no banco.
     */
    public void removerItemPedido(int idPedido, int idAlimento) throws SQLException {
        String sql = "DELETE FROM Pedido_Alimento WHERE id_pedido = ? AND id_alimento = ?";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, idPedido);
            statement.setInt(2, idAlimento);
            statement.executeUpdate();
        } // try-with-resources fecha statement
        // Não feche a conexão conn aqui
    }

        /**
     * Exclui um pedido da tabela Pedido.
     * Graças ao "ON DELETE CASCADE" na definição da tabela Pedido_Alimento,
     * os itens associados serão removidos automaticamente.
     *
     * @param idPedido O ID do pedido a ser excluído.
     * @return true se o pedido foi excluído com sucesso (pelo menos 1 linha afetada),
     * false se nenhum pedido com o ID foi encontrado.
     * @throws SQLException Se ocorrer um erro no banco durante a execução do delete.
     */
    public boolean excluirPedido(int idPedido) throws SQLException {
        String sql = "DELETE FROM Pedido WHERE id_pedido = ?";
        int affectedRows = 0;
        // try-with-resources garante que o PreparedStatement seja fechado
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, idPedido);
            affectedRows = statement.executeUpdate(); 
        }
        return affectedRows > 0;
    }

    /**
     * Atualiza a avaliação de um pedido existente na tabela Pedido.
     * Garante que a avaliação esteja entre 0 e 5.
     *
     * @param idPedido O ID do pedido a ser avaliado.
     * @param novaAvaliacao A nova avaliação (deve ser um valor entre 0 e 5).
     * @throws SQLException Se ocorrer um erro no banco ou se o pedido não for encontrado.
     * @throws IllegalArgumentException Se a avaliação estiver fora do intervalo permitido (0-5).
     */
    public void avaliarPedido(int idPedido, int novaAvaliacao) throws SQLException {
         if (novaAvaliacao < 0 || novaAvaliacao > 5) {
            throw new IllegalArgumentException("A avaliação deve ser um valor inteiro entre 0 e 5.");
         }

        String sql = "UPDATE Pedido SET avaliacao = ? WHERE id_pedido = ?";
         try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, novaAvaliacao);
            statement.setInt(2, idPedido);

            int affectedRows = statement.executeUpdate();

            // *** MODIFICAÇÃO: Verifica se alguma linha foi afetada ***
            if (affectedRows == 0) {
                 // Lança uma exceção se o pedido com o ID fornecido não existir
                 throw new SQLException("Pedido com ID " + idPedido + " não encontrado para avaliação.");
            }
         }
         // Não feche a conexão conn aqui
    }

    /**
     * Lista todos os pedidos de um usuário específico.
     * Retorna informações básicas de cada pedido (ID, Data/Hora, Avaliação).
     * IMPORTANTE: O ResultSet retornado DEVE ser fechado pelo chamador (Controller).
     *
     * @param idUsuario O ID do usuário cujos pedidos serão listados.
     * @return Um ResultSet aberto contendo id_pedido, data_hora, avaliacao. O chamador DEVE fechar este ResultSet.
     * @throws SQLException Se ocorrer um erro no banco.
     */
    public ResultSet listarPedidosPorUsuario(int idUsuario) throws SQLException {
        String sql = "SELECT id_pedido, data_hora, avaliacao FROM Pedido WHERE id_usuario = ? ORDER BY data_hora DESC";
        PreparedStatement statement = conn.prepareStatement(sql);
        try {
            statement.setInt(1, idUsuario);
            ResultSet resultado = statement.executeQuery();
            return resultado; // Retorna ResultSet aberto
        } catch (SQLException e) {
            // Garante o fechamento do statement em caso de erro na execução
            if (statement != null) {
                try { statement.close(); } catch (SQLException closeEx) { e.addSuppressed(closeEx); }
            }
            throw e; // Relança a exceção original
        }
        // Não feche a conexão conn aqui
    }

    // --- NOVO MÉTODO ---
    /**
     * Lista os itens (nome do alimento e quantidade) de um pedido específico.
     *
     * @param idPedido O ID do pedido cujos itens serão listados.
     * @return Um Map onde a chave é o nome do Alimento (String) e o valor é a quantidade (Integer).
     * @throws SQLException Se ocorrer um erro no banco.
     */
    public Map<String, Integer> listarItensPorPedido(int idPedido) throws SQLException {
        Map<String, Integer> itens = new LinkedHashMap<>(); // LinkedHashMap mantém a ordem
        String sql = "SELECT a.nome, pa.quantidade " +
                     "FROM Pedido_Alimento pa " +
                     "JOIN Alimento a ON pa.id_alimento = a.id_alimento " +
                     "WHERE pa.id_pedido = ? " +
                     "ORDER BY a.nome"; // Opcional: ordenar itens alfabeticamente

        // try-with-resources garante que PreparedStatement e ResultSet sejam fechados
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, idPedido);
            try (ResultSet resultado = statement.executeQuery()) {
                while (resultado.next()) {
                    String nomeAlimento = resultado.getString("nome");
                    int quantidade = resultado.getInt("quantidade");
                    itens.put(nomeAlimento, quantidade);
                }
            }
        }
        // Não feche a conexão conn aqui
        return itens;
    }
    
    /**
     * Lista os itens (ID do alimento, nome do alimento e quantidade) de um pedido específico.
     *
     * @param idPedido O ID do pedido cujos itens serão listados.
     * @return Um Map onde a chave principal é o ID do Alimento (Integer),
     * e o valor é outro Map contendo "nome" (String) e "quantidade" (Integer).
     * @throws SQLException Se ocorrer um erro no banco.
     */
    public Map<Integer, Map<String, Object>> listarItensPorPedidoComId(int idPedido) throws SQLException {
        // Usamos Map<String, Object> para guardar nome (String) e quantidade (Integer)
        Map<Integer, Map<String, Object>> itens = new LinkedHashMap<>();
        // *** SQL MODIFICADO para incluir a.id_alimento ***
        String sql = "SELECT a.id_alimento, a.nome, pa.quantidade " +
                     "FROM Pedido_Alimento pa " +
                     "JOIN Alimento a ON pa.id_alimento = a.id_alimento " +
                     "WHERE pa.id_pedido = ? " +
                     "ORDER BY a.nome"; // Opcional: ordenar itens alfabeticamente

        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, idPedido);
            try (ResultSet resultado = statement.executeQuery()) {
                while (resultado.next()) {
                    int idAlimento = resultado.getInt("id_alimento"); // *** Pega o ID ***
                    String nomeAlimento = resultado.getString("nome");
                    int quantidade = resultado.getInt("quantidade");

                    // Cria o map interno com nome e quantidade
                    Map<String, Object> detalhesItem = new HashMap<>();
                    detalhesItem.put("nome", nomeAlimento);
                    detalhesItem.put("quantidade", quantidade);

                    // Adiciona ao map principal usando o idAlimento como chave
                    itens.put(idAlimento, detalhesItem);
                }
            }
        }
        // Não feche a conexão conn aqui
        return itens;
    }
    
    /**
     * Verifica se um item (alimento) específico existe em um determinado pedido.
     *
     * @param idPedido O ID do pedido.
     * @param idAlimento O ID do alimento.
     * @return true se o item existe no pedido, false caso contrário.
     * @throws SQLException Se ocorrer um erro no banco.
     */
    public boolean verificarItemExisteNoPedido(int idPedido, int idAlimento) throws SQLException {
        String sql = "SELECT 1 FROM Pedido_Alimento WHERE id_pedido = ? AND id_alimento = ?";
        boolean existe = false;
        // try-with-resources garante fechamento
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, idPedido);
            statement.setInt(2, idAlimento);
            try (ResultSet resultado = statement.executeQuery()) {
                existe = resultado.next(); // Retorna true se encontrar alguma linha
            }
        }
        // Não feche a conexão conn aqui
        return existe;
    }
}