package model;

/**
 * Classe abstrata Alimento.
 * Representa a generalização de Comida e Bebida no diagrama.
 */
public abstract class Alimento {

    private int id;
    private String nome;
    private String descricao;
    private double preco;
    
    // Relação com Estabelecimento (um Alimento pertence a um Estabelecimento)
    private Estabelecimento estabelecimento;

    public Alimento() {
    }

    public Alimento(int id, String nome, String descricao, double preco, Estabelecimento estabelecimento) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.preco = preco;
        this.estabelecimento = estabelecimento;
    }

    // Getters e Setters

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

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public double getPreco() {
        return preco;
    }

    public void setPreco(double preco) {
        this.preco = preco;
    }

    public Estabelecimento getEstabelecimento() {
        return estabelecimento;
    }

    public void setEstabelecimento(Estabelecimento estabelecimento) {
        this.estabelecimento = estabelecimento;
    }

    @Override
    public String toString() {
        return nome + " - R$" + String.format("%.2f", preco);
    }
}