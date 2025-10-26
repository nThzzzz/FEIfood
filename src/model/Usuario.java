package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe que representa a entidade Usuário.
 * Corresponde à classe Usuario no diagrama de classes [cite: 55] e armazena os dados
 * necessários para as funcionalidades de cadastro  e login.
 */
public class Usuario {

    private int id; // Identificador único (chave primária no banco)
    private String nome;
    private String email; // Usado para o login
    private String senha; // Usada para o login

    // Relação de Agregação: Um Usuário "tem" Pedidos 
    private List<Pedido> pedidos;

    /**
     * Construtor padrão.
     */
    public Usuario() {
        // Inicializa a lista para evitar NullPointerException
        this.pedidos = new ArrayList<>();
    }

    /**
     * Construtor para cadastro de um novo usuário (sem ID, pois o banco gera).
     *
     * @param nome  Nome do usuário.
     * @param email Email para login.
     * @param senha Senha para login.
     */
    public Usuario(String nome, String email, String senha) {
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.pedidos = new ArrayList<>();
    }

    /**
     * Construtor completo (útil ao carregar dados do banco).
     *
     * @param id    ID vindo do banco de dados.
     * @param nome  Nome do usuário.
     * @param email Email para login.
     * @param senha Senha para login.
     */
    public Usuario(int id, String nome, String email, String senha) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.pedidos = new ArrayList<>();
    }

    // --- Getters e Setters ---

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public List<Pedido> getPedidos() {
        return pedidos;
    }

    public void setPedidos(List<Pedido> pedidos) {
        this.pedidos = pedidos;
    }

    // --- Métodos Utilitários (Opcional) ---

    /**
     * Adiciona um pedido à lista de pedidos do usuário.
     * @param pedido O pedido a ser adicionado.
     */
    public void adicionarPedido(Pedido pedido) {
        this.pedidos.add(pedido);
        // Se a classe Pedido tiver um setUsuario(), seria bom configurar aqui:
        // pedido.setUsuario(this);
    }

    @Override
    public String toString() {
        return "Usuario{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}