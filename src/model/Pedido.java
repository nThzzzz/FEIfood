package model;

import java.time.LocalDateTime; // Usando java.time para data/hora
import java.util.HashMap;
import java.util.Map;
import java.util.Set; // Para iterar sobre os itens

/**
 * Classe que representa a entidade Pedido.
 * Corresponde à classe Pedido no diagrama de classes  e armazena
 * informações sobre os alimentos solicitados por um usuário.
 */
public class Pedido {

    private int id; // id_pedido
    private LocalDateTime dataHora; // data_hora
    private Integer avaliacao; // avaliacao (Integer permite null)
    private Usuario usuario; // id_usuario (referência ao objeto Usuario)

    // Relação de Composição: Um Pedido "tem" Alimentos com suas quantidades.
    // Usamos um Map onde a chave é o Alimento e o valor é a quantidade.
    private Map<Alimento, Integer> itens;

    /**
     * Construtor padrão.
     */
    public Pedido() {
        this.itens = new HashMap<>();
        this.dataHora = LocalDateTime.now(); // Define a data/hora atual por padrão
    }

    /**
     * Construtor para criar um novo pedido associado a um usuário.
     * @param usuario O usuário que está fazendo o pedido.
     */
    public Pedido(Usuario usuario) {
        this(); // Chama o construtor padrão
        this.usuario = usuario;
    }

    /**
     * Construtor completo (útil ao carregar do banco).
     * @param id O ID do pedido.
     * @param dataHora A data e hora do pedido.
     * @param avaliacao A avaliação (pode ser null).
     * @param usuario O usuário associado.
     */
    public Pedido(int id, LocalDateTime dataHora, Integer avaliacao, Usuario usuario) {
        this.id = id;
        this.dataHora = dataHora;
        this.avaliacao = avaliacao;
        this.usuario = usuario;
        this.itens = new HashMap<>(); // Inicializa a lista de itens vazia
    }

    // --- Getters e Setters ---

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public Integer getAvaliacao() {
        return avaliacao;
    }

    /**
     * Define a avaliação do pedido, garantindo que esteja entre 0 e 5[cite: 167].
     * @param avaliacao A nota da avaliação (0 a 5).
     * @throws IllegalArgumentException Se a avaliação estiver fora do intervalo permitido.
     */
    public void setAvaliacao(Integer avaliacao) {
        if (avaliacao != null && (avaliacao < 0 || avaliacao > 5)) {
            throw new IllegalArgumentException("A avaliação deve ser entre 0 e 5.");
        }
        this.avaliacao = avaliacao;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    /**
     * Retorna o mapa de itens do pedido (Alimento -> Quantidade).
     * @return Um Map não modificável dos itens.
     */
    public Map<Alimento, Integer> getItens() {
        // Retorna uma cópia ou visão não modificável para proteger o estado interno
        return new HashMap<>(itens); // Retorna cópia simples
        // Alternativa: return Collections.unmodifiableMap(itens);
    }

     // Não é comum ter um setItens que substitui todo o mapa,
     // geralmente se gerencia com adicionar/remover.
     // public void setItens(Map<Alimento, Integer> itens) { this.itens = itens; }


    // --- Métodos para Gerenciar Itens [cite: 165] ---

    /**
     * Adiciona um alimento ao pedido ou atualiza sua quantidade se já existir.
     * @param alimento O alimento a ser adicionado.
     * @param quantidade A quantidade a ser adicionada (deve ser > 0).
     */
    public void adicionarItem(Alimento alimento, int quantidade) {
        if (alimento == null || quantidade <= 0) {
            throw new IllegalArgumentException("Alimento inválido ou quantidade deve ser positiva.");
        }
        // Se o alimento já existe no mapa, soma a quantidade. Senão, adiciona.
        this.itens.put(alimento, this.itens.getOrDefault(alimento, 0) + quantidade);
    }

    /**
     * Remove um alimento completamente do pedido.
     * @param alimento O alimento a ser removido.
     */
    public void removerItem(Alimento alimento) {
        if (alimento != null) {
            this.itens.remove(alimento);
        }
    }

    /**
     * Diminui a quantidade de um alimento no pedido. Se a quantidade chegar a zero ou menos,
     * o item é removido.
     * @param alimento O alimento a ter quantidade diminuída.
     * @param quantidadeARemover A quantidade a ser subtraída.
     */
    public void diminuirQuantidadeItem(Alimento alimento, int quantidadeARemover) {
         if (alimento == null || quantidadeARemover <= 0) {
            throw new IllegalArgumentException("Alimento inválido ou quantidade a remover deve ser positiva.");
        }
        Integer quantidadeAtual = this.itens.get(alimento);
        if (quantidadeAtual != null) {
            int novaQuantidade = quantidadeAtual - quantidadeARemover;
            if (novaQuantidade <= 0) {
                this.itens.remove(alimento); // Remove se zerar ou ficar negativo
            } else {
                this.itens.put(alimento, novaQuantidade); // Atualiza com a nova quantidade
            }
        }
        // Se o item não existia, não faz nada.
    }

    /**
     * Calcula o valor total do pedido somando o preço de cada item multiplicado por sua quantidade.
     * @return O valor total do pedido.
     */
    public double calcularTotal() {
        double total = 0.0;
        for (Map.Entry<Alimento, Integer> entry : itens.entrySet()) {
            Alimento alimento = entry.getKey();
            Integer quantidade = entry.getValue();
            total += alimento.getPreco() * quantidade;
            // Poderia adicionar lógica de imposto aqui se a classe Bebida não incluísse no getPreco
            // if (alimento instanceof Bebida) {
            //     total += ((Bebida) alimento).calcularImposto(alimento.getPreco()) * quantidade;
            // }
        }
        return total;
    }

    @Override
    public String toString() {
        return "Pedido{" +
                "id=" + id +
                ", dataHora=" + dataHora +
                ", usuario=" + (usuario != null ? usuario.getNome() : "N/A") +
                ", avaliacao=" + avaliacao +
                ", totalItens=" + itens.size() +
                ", valorTotal=" + String.format("%.2f", calcularTotal()) +
                '}';
    }
}