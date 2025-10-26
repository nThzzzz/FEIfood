package model;

/**
 * Classe Comida, herda de Alimento.
 */
public class Comida extends Alimento {

    // Pode ter atributos específicos de Comida, se necessário
    // Ex: String tipoCozinha (italiana, japonesa, etc.)
    
    public Comida() {
        super();
    }

    public Comida(int id, String nome, String descricao, double preco, Estabelecimento estabelecimento) {
        super(id, nome, descricao, preco, estabelecimento);
    }
    
    // Aqui poderiam entrar métodos específicos de Comida
}