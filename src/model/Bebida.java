package model;

/**
 * Classe Bebida, herda de Alimento  e implementa Imposto_Alcool.
 */
public class Bebida extends Alimento implements Imposto_Alcool {

    // Atributo específico de bebida, ex: percentual de álcool
    private double percentualAlcool;
    
    // Vamos definir o imposto como 8% para bebidas alcoólicas
    private static final double PERCENTUAL_IMPOSTO = 0.08; 

    public Bebida() {
        super();
    }

    public Bebida(int id, String nome, String descricao, double preco, Estabelecimento estabelecimento, double percentualAlcool) {
        super(id, nome, descricao, preco, estabelecimento);
        this.percentualAlcool = percentualAlcool;
    }

    public double getPercentualAlcool() {
        return percentualAlcool;
    }

    public void setPercentualAlcool(double percentualAlcool) {
        this.percentualAlcool = percentualAlcool;
    }

    /**
     * Implementação do método da interface Imposto_Alcool.
     * Calcula 8% de imposto sobre o preço se for alcoólica.
     */
    @Override
    public double calcularImposto(double preco) {
        if (this.percentualAlcool > 0) {
            return preco * PERCENTUAL_IMPOSTO;
        }
        return 0; // Sem imposto para bebidas não alcoólicas
    }
    
    /**
     * Sobrescreve o getPreco para já incluir o imposto (opcional, mas útil)
     * Ou você pode lidar com isso na lógica do Pedido.
     * Vamos manter simples por enquanto e deixar o getPreco() original.
     */
}