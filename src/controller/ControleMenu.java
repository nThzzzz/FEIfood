package controller;

import view.Menu;
import model.*;
import dao.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map; // Para usar o Map retornado pelo DAO
import javax.swing.JOptionPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
// Imports de ActionListener removidos

public class ControleMenu {
    private Menu telaMenu;
    private Usuario usuarioLogado;
    private Pedido pedidoAtual; // Guarda o pedido em construção
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public ControleMenu(Menu mn, Usuario usuarioLogado) {
        this.telaMenu = mn;
        this.usuarioLogado = usuarioLogado;
        // Inicializa um novo pedido vazio associado ao usuário logado
        this.pedidoAtual = new Pedido(this.usuarioLogado);

        // Carrega dados iniciais
        carregarListaAlimentos();
        carregarPedidosUsuario();
        atualizarOutputPedidoAtual(); // Exibe o estado inicial (vazio) do pedido atual

        // Adiciona listeners
        adicionarListenerSliderNota();
        // Os listeners dos botões Adicionar/Remover/Avaliar/Buscar são chamados pela View (Menu.java)
    }

    // --- Métodos da Aba Alimentos ---

    /**
     * Busca a lista resumida de alimentos (ID e Nome) no banco
     * e preenche o JTextArea txtOutputAlimentos.
     */
    public void carregarListaAlimentos() {
        Connection conn = null;
        ResultSet res = null;
        PreparedStatement stmt = null;

        try {
            Conexao conexao = new Conexao();
            conn = conexao.getConnection();
            String sql = "SELECT id_alimento, nome FROM Alimento ORDER BY nome";
            stmt = conn.prepareStatement(sql);
            res = stmt.executeQuery();

            telaMenu.getTxtOutputAlimentos().setText("");
            StringBuilder listaTexto = new StringBuilder();
            while (res.next()) {
                int id = res.getInt("id_alimento");
                String nome = res.getString("nome");
                listaTexto.append(id).append(" - ").append(nome).append("\n");
            }
            telaMenu.getTxtOutputAlimentos().setText(listaTexto.toString());

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(telaMenu, "Erro ao carregar lista de alimentos:\n" + e.getMessage(), "Erro de Banco de Dados", JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                if (res != null) res.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("Erro ao fechar recursos do banco (Alimentos): " + e.getMessage());
            }
        }
    }

    /**
     * Busca os detalhes de um alimento pelo ID informado na tela
     * e exibe no JTextArea txtOutputInfoAlimento.
     */
    public void buscarDetalhesAlimento() {
        String idTexto = telaMenu.getTxtInputIDalimento().getText();
        int idAlimento;

        try {
            idAlimento = Integer.parseInt(idTexto);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(telaMenu, "Por favor, insira um ID numérico válido.", "Erro de Entrada", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Connection conn = null;
        ResultSet res = null;
        PreparedStatement stmt = null;
        AlimentoDAO dao = null; // Usaremos o DAO aqui

        try {
            Conexao conexao = new Conexao();
            conn = conexao.getConnection();
            dao = new AlimentoDAO(conn); // Cria o DAO

            // Chama o método que retorna ResultSet aberto
            res = dao.buscarPorIdDetalhado(idAlimento);
            // Pega o PreparedStatement associado para poder fechar
            stmt = (PreparedStatement) res.getStatement();


            if (res.next()) {
                String nome = res.getString("nome");
                String descricao = res.getString("descricao");
                double preco = res.getDouble("preco");
                String tipo = res.getString("tipo_alimento");
                double imposto = res.getDouble("percentual_imposto");
                boolean impostoEraNull = res.wasNull();
                String estabelecimento = res.getString("nome_estabelecimento");

                StringBuilder infoTexto = new StringBuilder();
                infoTexto.append("Nome: ").append(nome).append("\n");
                infoTexto.append("Descrição: ").append(descricao != null ? descricao : "N/A").append("\n");
                infoTexto.append("Preço: R$ ").append(String.format("%.2f", preco)).append("\n");
                infoTexto.append("Tipo: ").append(tipo).append("\n");
                if (tipo.equals("BEBIDA") && !impostoEraNull) {
                    infoTexto.append("Imposto (aprox): ").append(String.format("%.1f", imposto)).append("%\n");
                }
                infoTexto.append("Estabelecimento: ").append(estabelecimento).append("\n");
                telaMenu.getTxtOutputInfoAlimento().setText(infoTexto.toString());
            } else {
                telaMenu.getTxtOutputInfoAlimento().setText("Alimento com ID " + idAlimento + " não encontrado.");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(telaMenu, "Erro ao buscar detalhes do alimento:\n" + e.getMessage(), "Erro de Banco de Dados", JOptionPane.ERROR_MESSAGE);
            telaMenu.getTxtOutputInfoAlimento().setText("Erro ao buscar dados.");
        } finally {
            try {
                if (res != null) res.close(); // Fecha ResultSet
                if (stmt != null) stmt.close(); // Fecha PreparedStatement associado
                if (conn != null) conn.close(); // Fecha Connection
            } catch (SQLException e) {
                System.err.println("Erro ao fechar recursos do banco (Detalhes Alimento): " + e.getMessage());
            }
        }
    }


    // --- Métodos para a Aba Avaliar Pedidos ---

    /**
     * Busca os pedidos do usuário logado, incluindo seus itens,
     * e preenche o JTextArea txtOutputTodosPedidos.
     */
    /**
     * Busca os pedidos do usuário logado, incluindo seus itens (com ID),
     * e preenche o JTextArea txtOutputTodosPedidos.
     */
    public void carregarPedidosUsuario() {
        Connection conn = null;
        ResultSet resPedidos = null;
        PreparedStatement stmtPedidos = null;
        PedidoDAO dao = null;

        if (usuarioLogado == null) {
            telaMenu.getTxtOutputTodosPedidos().setText("Nenhum usuário logado.");
            // *** Atualiza também o outro campo de texto ***
            if (telaMenu.getTxtOutputTodosPedidos1() != null) {
                telaMenu.getTxtOutputTodosPedidos1().setText("Nenhum usuário logado.");
            }
            return;
        }

        // *** Pega o nome do usuário para o cabeçalho ***
        String nomeUsuario = (usuarioLogado.getNome() != null && !usuarioLogado.getNome().isEmpty())
                             ? usuarioLogado.getNome()
                             : "Usuário"; // Fallback se o nome for nulo ou vazio
        StringBuilder pedidosTexto = new StringBuilder("--- SEUS PEDIDOS, " + nomeUsuario + " ---\n");


        try {
            Conexao conexao = new Conexao();
            conn = conexao.getConnection();
            dao = new PedidoDAO(conn);

            resPedidos = dao.listarPedidosPorUsuario(usuarioLogado.getId());
            if (resPedidos != null) {
                 stmtPedidos = (PreparedStatement) resPedidos.getStatement(); // Cast explícito
            } else {
                 throw new SQLException("Falha ao obter ResultSet de listarPedidosPorUsuario.");
            }

            boolean encontrouPedidos = false;
            while (resPedidos.next()) {
                encontrouPedidos = true;
                int idPedido = resPedidos.getInt("id_pedido");
                Timestamp ts = resPedidos.getTimestamp("data_hora");
                LocalDateTime dataHora = (ts != null) ? ts.toLocalDateTime() : null;
                int avaliacaoInt = resPedidos.getInt("avaliacao");
                boolean avaliacaoEraNull = resPedidos.wasNull();
                String avaliacaoStr = avaliacaoEraNull ? "N/A" : String.valueOf(avaliacaoInt);

                pedidosTexto.append("\n------------------------------------------\n");
                pedidosTexto.append(String.format("Pedido ID: %d | Data: %s | Avaliação: %s\n",
                        idPedido,
                        (dataHora != null ? dataHora.format(formatter) : "N/A"),
                        avaliacaoStr));
                pedidosTexto.append("Itens:\n");

                try {
                     // *** Chama o novo método DAO ***
                     Map<Integer, Map<String, Object>> itens = dao.listarItensPorPedidoComId(idPedido);
                     if (itens.isEmpty()) {
                         pedidosTexto.append("  (Nenhum item encontrado)\n");
                     } else {
                         // *** Itera sobre o novo Map e formata a string com ID ***
                         for (Map.Entry<Integer, Map<String, Object>> entry : itens.entrySet()) {
                             int idAlimento = entry.getKey();
                             Map<String, Object> detalhes = entry.getValue();
                             String nomeAlimento = (String) detalhes.get("nome");
                             int quantidade = (Integer) detalhes.get("quantidade");
                             pedidosTexto.append(String.format("  - ID %d: %dx %s\n", idAlimento, quantidade, nomeAlimento));
                         }
                     }
                } catch (SQLException eItens) {
                     System.err.println("Erro ao buscar itens para o pedido ID " + idPedido + ": " + eItens.getMessage());
                     pedidosTexto.append("  (Erro ao buscar itens)\n");
                }
            }

            if (!encontrouPedidos) {
                pedidosTexto.append("(Nenhum pedido encontrado para este usuário)\n");
            }

            // *** Define o texto em AMBOS os JTextAreas ***
            telaMenu.getTxtOutputTodosPedidos().setText(pedidosTexto.toString());
            if (telaMenu.getTxtOutputTodosPedidos1() != null) { // Verifica se o getter existe
                telaMenu.getTxtOutputTodosPedidos1().setText(pedidosTexto.toString());
            } else {
                System.err.println("AVISO: JTextArea txtOutputTodosPedidos1 não encontrado ou sem getter na tela Menu.");
            }


        } catch (SQLException e) {
            JOptionPane.showMessageDialog(telaMenu, "Erro ao carregar pedidos:\n" + e.getMessage(), "Erro de Banco de Dados", JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                if (resPedidos != null) resPedidos.close();
                if (stmtPedidos != null) stmtPedidos.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("Erro ao fechar recursos do banco (Pedidos): " + e.getMessage());
            }
        }
    }

     /**
     * Pega o ID do pedido e a nota do slider e chama o DAO para avaliar.
     * Mostra pop-up de erro se o ID for inválido.
     * Atualiza a lista de pedidos após a avaliação bem-sucedida.
     */
    public void avaliarPedidoSelecionado() {
        String idPedidoTexto = telaMenu.getTxtInputIDpedido().getText();
        int idPedido;
        int nota = telaMenu.getSliderInputNota().getValue();

        try {
            idPedido = Integer.parseInt(idPedidoTexto);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(telaMenu, "Por favor, insira um ID de pedido numérico válido.", "Erro de Entrada", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Connection conn = null;
        try {
            Conexao conexao = new Conexao();
            conn = conexao.getConnection();
            conn.setAutoCommit(false); // Inicia transação
            PedidoDAO pedidoDAO = new PedidoDAO(conn);

            pedidoDAO.avaliarPedido(idPedido, nota); // Tenta avaliar

            conn.commit(); // Confirma a transação

            JOptionPane.showMessageDialog(telaMenu, "Pedido ID " + idPedido + " avaliado com nota " + nota + "!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);

            telaMenu.getTxtInputIDpedido().setText("");
            telaMenu.getTxtOutputNota().setText("Nota: " + telaMenu.getSliderInputNota().getValue());
            carregarPedidosUsuario(); // Recarrega a lista

        } catch (SQLException ex) {
            // Trata o erro específico de ID não encontrado
            if (ex.getMessage().contains("não encontrado para avaliação")) {
                 JOptionPane.showMessageDialog(telaMenu, "ID de Pedido inválido ou não pertence a você.", "Erro de Avaliação", JOptionPane.ERROR_MESSAGE);
            } else {
                // Outros erros SQL
                JOptionPane.showMessageDialog(telaMenu, "Erro ao avaliar pedido no banco:\n" + ex.getMessage(), "Erro de Banco de Dados", JOptionPane.ERROR_MESSAGE);
            }
            // Reverte a transação
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException rbEx) { System.err.println("Erro ao reverter transação: " + rbEx.getMessage()); }
            }
        } catch (IllegalArgumentException ex) { // Erro de nota fora do intervalo (0-5)
            JOptionPane.showMessageDialog(telaMenu, ex.getMessage(), "Erro de Avaliação", JOptionPane.ERROR_MESSAGE);
             if (conn != null) { // Reverte
                try { conn.rollback(); } catch (SQLException rbEx) { System.err.println("Erro ao reverter transação: " + rbEx.getMessage()); }
            }
        } finally {
             // Fecha conexão e restaura autoCommit
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Erro ao fechar conexão (Avaliação): " + e.getMessage());
                }
            }
        }
    }


    // --- Métodos para a Aba Fazer Pedido ---

    /**
     * Adiciona um alimento (buscado pelo ID) com a quantidade especificada
     * ao pedidoAtual e atualiza a exibição.
     */
    public void adicionarAlimentoAoPedidoAtual() {
        String idAlimentoTexto = telaMenu.getTxtInputIDalimentoPedido().getText();
        String quantidadeTexto = telaMenu.getTxtInputQuantidadeAlimento().getText();
        int idAlimento;
        int quantidade;

        try {
            idAlimento = Integer.parseInt(idAlimentoTexto);
            quantidade = Integer.parseInt(quantidadeTexto);
            if (quantidade <= 0) {
                JOptionPane.showMessageDialog(telaMenu, "A quantidade deve ser maior que zero.", "Erro de Entrada", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(telaMenu, "ID do Alimento e Quantidade devem ser números válidos.", "Erro de Entrada", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Connection conn = null;
        try {
            Conexao conexao = new Conexao();
            conn = conexao.getConnection();
            AlimentoDAO alimentoDAO = new AlimentoDAO(conn);

            Alimento alimentoParaAdicionar = alimentoDAO.buscarAlimentoPorId(idAlimento);

            if (alimentoParaAdicionar != null) {
                pedidoAtual.adicionarItem(alimentoParaAdicionar, quantidade);
                atualizarOutputPedidoAtual();
                telaMenu.getTxtInputIDalimentoPedido().setText("");
                telaMenu.getTxtInputQuantidadeAlimento().setText("");
            } else {
                JOptionPane.showMessageDialog(telaMenu, "Alimento com ID " + idAlimento + " não encontrado.", "Erro", JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(telaMenu, "Erro ao buscar alimento no banco:\n" + ex.getMessage(), "Erro de Banco de Dados", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException ex) {
             JOptionPane.showMessageDialog(telaMenu, ex.getMessage(), "Erro ao Adicionar Item", JOptionPane.ERROR_MESSAGE);
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (SQLException e) { System.err.println("Erro ao fechar conexão (Adicionar Item): " + e.getMessage()); }
            }
        }
    }

    /**
     * Remove um alimento (identificado pelo ID) do pedidoAtual e atualiza a exibição.
     */
    public void removerAlimentoDoPedidoAtual() {
        String idAlimentoTexto = telaMenu.getTxtInputIDalimentoPedido().getText();
        int idAlimento;

        try {
            idAlimento = Integer.parseInt(idAlimentoTexto);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(telaMenu, "Por favor, insira um ID de alimento numérico válido para remover.", "Erro de Entrada", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Alimento alimentoParaRemover = null;
        for (Alimento alim : pedidoAtual.getItens().keySet()) {
            if (alim.getId() == idAlimento) {
                alimentoParaRemover = alim;
                break;
            }
        }

        if (alimentoParaRemover != null) {
            pedidoAtual.removerItem(alimentoParaRemover);
            atualizarOutputPedidoAtual();
            telaMenu.getTxtInputIDalimentoPedido().setText("");
            telaMenu.getTxtInputQuantidadeAlimento().setText("");
        } else {
            JOptionPane.showMessageDialog(telaMenu, "Alimento com ID " + idAlimento + " não encontrado no pedido atual.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
        }
    }

     /**
     * Atualiza o JTextArea txtOutputItensPedido com os itens do pedidoAtual.
     * (Assume getter getTxtOutputItensPedido() na view Menu.java)
     */
    private void atualizarOutputPedidoAtual() {
        StringBuilder itensTexto = new StringBuilder("--- Pedido Atual ---\n");
        Map<Alimento, Integer> itens = pedidoAtual.getItens();

        if (itens.isEmpty()) {
            itensTexto.append("(Vazio)\n");
        } else {
            for (Map.Entry<Alimento, Integer> entry : itens.entrySet()) {
                Alimento alim = entry.getKey();
                int qtd = entry.getValue();
                itensTexto.append(String.format("%dx %s (R$ %.2f)\n", qtd, alim.getNome(), alim.getPreco()));
            }
        }

        itensTexto.append("--------------------\n");
        itensTexto.append(String.format("Total: R$ %.2f\n", pedidoAtual.calcularTotal()));

        // Usa o getter correspondente ao JTextArea da aba "Fazer Pedido"
        if (telaMenu.getTxtOutputItensPedido() != null) {
            telaMenu.getTxtOutputItensPedido().setText(itensTexto.toString());
        } else {
             System.err.println("AVISO: JTextArea para itens do pedido atual (getTxtOutputItensPedido) não encontrado ou sem getter na tela Menu.");
        }
    }
    
    /**
     * Pega o pedidoAtual, verifica se tem itens, e o salva no banco de dados.
     * Após salvar, reinicia o pedidoAtual para um novo pedido vazio.
     * Atualiza a lista de pedidos do usuário.
     * Este método será chamado pelo ActionListener do botão "FAZER PEDIDO".
     */
    public void fazerPedido() {
        // Verifica se há itens no pedido atual
        if (pedidoAtual == null || pedidoAtual.getItens().isEmpty()) {
            JOptionPane.showMessageDialog(telaMenu, "Adicione pelo menos um item ao pedido antes de finalizá-lo.", "Pedido Vazio", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Verifica se o usuário logado ainda é válido (embora improvável de mudar)
        if (usuarioLogado == null || usuarioLogado.getId() <= 0) {
             JOptionPane.showMessageDialog(telaMenu, "Erro: Usuário não está logado corretamente.", "Erro de Usuário", JOptionPane.ERROR_MESSAGE);
             return;
        }
        // Garante que o pedidoAtual está associado ao usuário correto
        pedidoAtual.setUsuario(usuarioLogado);

        Connection conn = null;
        try {
            Conexao conexao = new Conexao();
            conn = conexao.getConnection();
            // *** Iniciar Transação ***
            conn.setAutoCommit(false);

            PedidoDAO pedidoDAO = new PedidoDAO(conn);
            int idNovoPedido = pedidoDAO.criarPedido(pedidoAtual); // Chama o método DAO para inserir

            // *** Confirmar Transação ***
            conn.commit();

            JOptionPane.showMessageDialog(telaMenu, "Pedido nº " + idNovoPedido + " realizado com sucesso!", "Pedido Enviado", JOptionPane.INFORMATION_MESSAGE);

            // Reinicia o pedidoAtual para um novo pedido vazio
            pedidoAtual = new Pedido(usuarioLogado);
            atualizarOutputPedidoAtual(); // Atualiza a exibição do pedido atual (agora vazio)
            carregarPedidosUsuario(); // Recarrega a lista na aba de avaliação para incluir o novo pedido

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(telaMenu, "Erro ao salvar o pedido no banco:\n" + ex.getMessage(), "Erro de Banco de Dados", JOptionPane.ERROR_MESSAGE);
            // Tenta reverter a transação em caso de erro
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException rbEx) { System.err.println("Erro ao reverter transação: " + rbEx.getMessage()); }
            }
        } catch (IllegalArgumentException ex) { // Captura erro do DAO (ex: usuário inválido)
             JOptionPane.showMessageDialog(telaMenu, ex.getMessage(), "Erro ao Criar Pedido", JOptionPane.ERROR_MESSAGE);
              if (conn != null) { // Tenta reverter
                try { conn.rollback(); } catch (SQLException rbEx) { System.err.println("Erro ao reverter transação: " + rbEx.getMessage()); }
            }
        } finally {
             // Garante que a conexão seja fechada e autoCommit restaurado
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Restaura modo padrão
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Erro ao fechar conexão (Fazer Pedido): " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Pega o ID do pedido informado na aba "Editar || Excluir Pedido",
     * pede confirmação ao usuário e chama o DAO para excluir o pedido.
     * Mostra pop-up de erro se o ID for inválido/não encontrado.
     * Atualiza a lista de pedidos após a exclusão bem-sucedida.
     */
    public void excluirPedidoSelecionado() {
        String idPedidoTexto;
        // Use o getter correto para o JTextField da aba Excluir
        if (telaMenu.getTxtInputExcPedido() != null) {
             idPedidoTexto = telaMenu.getTxtInputExcPedido().getText();
        } else {
             System.err.println("AVISO: Campo de texto para ID de exclusão (getTxtInputExcPedido) não encontrado ou sem getter na tela Menu.");
             JOptionPane.showMessageDialog(telaMenu, "Erro interno: Campo de ID para exclusão não encontrado.", "Erro de Interface", JOptionPane.ERROR_MESSAGE);
             return;
        }

        int idPedido;

        try {
            idPedido = Integer.parseInt(idPedidoTexto);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(telaMenu, "Por favor, insira um ID de pedido numérico válido para excluir.", "Erro de Entrada", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirmacao = JOptionPane.showConfirmDialog(
            telaMenu,
            "Tem certeza que deseja excluir o Pedido ID " + idPedido + "?\nEsta ação não pode ser desfeita.",
            "Confirmar Exclusão",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (confirmacao != JOptionPane.YES_OPTION) {
            return; // Usuário cancelou
        }

        Connection conn = null;
        boolean excluidoComSucesso = false; // Flag para controlar o resultado
        try {
            Conexao conexao = new Conexao();
            conn = conexao.getConnection();
            conn.setAutoCommit(false); // Inicia transação

            PedidoDAO pedidoDAO = new PedidoDAO(conn);
            // Chama o método excluirPedido e guarda o resultado
            excluidoComSucesso = pedidoDAO.excluirPedido(idPedido);

            // *** Verifica o resultado antes de commitar ***
            if (excluidoComSucesso) {
                conn.commit(); // Confirma a transação apenas se a exclusão ocorreu
                JOptionPane.showMessageDialog(telaMenu, "Pedido ID " + idPedido + " excluído com sucesso!", "Exclusão Concluída", JOptionPane.INFORMATION_MESSAGE);

                // Limpa o campo e atualiza as listas
                telaMenu.getTxtInputExcPedido().setText("");
                carregarPedidosUsuario();
            } else {
                // *** ID não encontrado - Reverte (embora DELETE não tenha feito nada) e mostra erro ***
                 conn.rollback(); // Desfaz a transação (mesmo que vazia)
                 JOptionPane.showMessageDialog(telaMenu, "Pedido com ID " + idPedido + " não encontrado ou não pertence a você.", "Erro de Exclusão", JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(telaMenu, "Erro ao excluir pedido no banco:\n" + ex.getMessage(), "Erro de Banco de Dados", JOptionPane.ERROR_MESSAGE);
            // Tenta reverter a transação
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException rbEx) { System.err.println("Erro ao reverter transação: " + rbEx.getMessage()); }
            }
        } finally {
            // Garante que a conexão seja fechada e autoCommit restaurado
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Erro ao fechar conexão (Excluir Pedido): " + e.getMessage());
                }
            }
        }
    }
    
    
    /**
     * Pega o ID do pedido, o ID do alimento e a nova quantidade informados
     * na aba "Editar || Excluir Pedido" e chama o DAO para atualizar
     * a quantidade do item nesse pedido.
     * Verifica se o item existe antes de tentar remover (quantidade <= 0).
     * Atualiza a lista de pedidos após a edição.
     */
    public void editarItemPedido() {
        // Obtenção dos textos dos campos (igual a antes)
        String idPedidoTexto;
        String idAlimentoTexto;
        String novaQuantidadeTexto;

        if (telaMenu.getTxtInputEditPedido() != null) { idPedidoTexto = telaMenu.getTxtInputEditPedido().getText(); }
        else { /* ... erro getter ... */ return; }
        if (telaMenu.getTxtInputIDEditAlimentoPedido() != null) { idAlimentoTexto = telaMenu.getTxtInputIDEditAlimentoPedido().getText(); }
        else { /* ... erro getter ... */ return; }
        if (telaMenu.getTxtInputQtdAlimento() != null) { novaQuantidadeTexto = telaMenu.getTxtInputQtdAlimento().getText(); }
        else { /* ... erro getter ... */ return; }


        int idPedido;
        int idAlimento;
        int novaQuantidade;

        // Validação das entradas numéricas (igual a antes)
        try {
            idPedido = Integer.parseInt(idPedidoTexto);
            idAlimento = Integer.parseInt(idAlimentoTexto);
            novaQuantidade = Integer.parseInt(novaQuantidadeTexto);
            if (novaQuantidade < 0) {
                 JOptionPane.showMessageDialog(telaMenu, "A nova quantidade não pode ser negativa.", "Erro de Entrada", JOptionPane.ERROR_MESSAGE);
                 return;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(telaMenu, "ID do Pedido, ID do Alimento e Nova Quantidade devem ser números válidos.", "Erro de Entrada", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Connection conn = null;
        try {
            Conexao conexao = new Conexao();
            conn = conexao.getConnection();
            conn.setAutoCommit(false); // Inicia transação
            PedidoDAO pedidoDAO = new PedidoDAO(conn);

            // *** NOVA VERIFICAÇÃO: Se a intenção é remover (qtd <= 0) ***
            if (novaQuantidade <= 0) {
                boolean itemExiste = pedidoDAO.verificarItemExisteNoPedido(idPedido, idAlimento);
                if (!itemExiste) {
                    // Se o item não existe, mostra erro e NÃO prossegue
                    conn.rollback(); // Desfaz a transação (vazia)
                    JOptionPane.showMessageDialog(telaMenu,
                        "Item (ID Alimento: " + idAlimento + ") não encontrado no Pedido ID " + idPedido + " para remover.",
                        "Erro de Edição", JOptionPane.ERROR_MESSAGE);
                    return; // Interrompe a execução do método
                }
                // Se o item existe, a execução continua para chamar adicionarOuAtualizarItemPedido
            }

            // Chama o método DAO para adicionar ou atualizar (ou remover se qtd <= 0 e passou na verificação)
            pedidoDAO.adicionarOuAtualizarItemPedido(idPedido, idAlimento, novaQuantidade);

            conn.commit(); // Confirma a transação

            // Feedback ao usuário (igual a antes)
            if (novaQuantidade > 0) {
                JOptionPane.showMessageDialog(telaMenu, "Item (ID Alimento: " + idAlimento + ") no Pedido ID " + idPedido + " atualizado para quantidade " + novaQuantidade + ".", "Edição Concluída", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(telaMenu, "Item (ID Alimento: " + idAlimento + ") removido do Pedido ID " + idPedido + ".", "Item Removido", JOptionPane.INFORMATION_MESSAGE);
            }

            // Limpa os campos e atualiza as listas (igual a antes)
            telaMenu.getTxtInputEditPedido().setText("");
            telaMenu.getTxtInputIDEditAlimentoPedido().setText("");
            telaMenu.getTxtInputQtdAlimento().setText("");
            carregarPedidosUsuario();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(telaMenu, "Erro ao editar item do pedido no banco:\n" + ex.getMessage(), "Erro de Banco de Dados", JOptionPane.ERROR_MESSAGE);
            if (conn != null) { try { conn.rollback(); } catch (SQLException rbEx) { /*...*/ } }
        } finally {
            if (conn != null) { try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { /*...*/ } }
        }
    }
    
    // --- Métodos Auxiliares para Adicionar Listeners ---

    /**
     * Adiciona um ChangeListener ao slider de nota para atualizar o JTextArea.
     */
    private void adicionarListenerSliderNota() {
        if (telaMenu.getSliderInputNota() != null) {
            telaMenu.getSliderInputNota().addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    int valor = telaMenu.getSliderInputNota().getValue();
                     if(telaMenu.getTxtOutputNota() != null) {
                        telaMenu.getTxtOutputNota().setText("Nota: " + valor);
                     }
                }
            });
            // Define valor inicial
            if(telaMenu.getTxtOutputNota() != null) {
               telaMenu.getTxtOutputNota().setText("Nota: " + telaMenu.getSliderInputNota().getValue());
            }
        } else {
             System.err.println("AVISO: Slider de nota não encontrado na tela Menu.");
        }
    }
}