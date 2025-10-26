package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe que representa a entidade Estabelecimento (restaurante).
 * [cite_start]Corresponde à classe Estabelecimento no diagrama de classes[cite: 51].
 */
public class Estabelecimento {

    private int id;
    private String nome;
    private String endereco;
    // Outros campos úteis: String cnpj, String telefone, etc.

    // Relação de Composição: Um Estabelecimento "tem" Alimentos.
    private List<Alimento> cardapio;

    /**
     * Construtor padrão.
     */
    public Estabelecimento() {
        this.cardapio = new ArrayList<>();
    }

    /**
     * Construtor para criar um novo estabelecimento (sem ID).
     * @param nome Nome do estabelecimento.
     * @param endereco Endereço do estabelecimento.
     */
    public Estabelecimento(String nome, String endereco) {
        this.nome = nome;
        this.endereco = endereco;
        this.cardapio = new ArrayList<>();
    }

    /**
     * Construtor completo (útil ao carregar do banco).
     * @param id ID do estabelecimento.
     * @param nome Nome do estabelecimento.
     * @param endereco Endereço do estabelecimento.
     */
    public Estabelecimento(int id, String nome, String endereco) {
        this.id = id;
        this.nome = nome;
        this.endereco = endereco;
        this.cardapio = new ArrayList<>();
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

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public List<Alimento> getCardapio() {
        return cardapio;
    }

    public void setCardapio(List<Alimento> cardapio) {
        this.cardapio = cardapio;
    }

    // --- Métodos Utilitários ---

    /**
     * Adiciona um alimento ao cardápio deste estabelecimento.
     * @param alimento O alimento a ser adicionado.
     */
    public void adicionarAlimento(Alimento alimento) {
        this.cardapio.add(alimento);
        alimento.setEstabelecimento(this); // Garante a via de mão dupla da relação
    }

    @Override
    public String toString() {
        return nome + " (" + endereco + ")";
    }
}