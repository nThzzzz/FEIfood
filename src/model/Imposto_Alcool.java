package model;

/**
 * Interface <<interface>> Imposto_Alcool do diagrama de classes.
 * Define um contrato para classes que precisam calcular imposto sobre álcool.
 */
public interface Imposto_Alcool {
    
    /**
     * Define um método para calcular o imposto.
     * O valor do imposto pode ser um percentual ou um valor fixo.
     * Vamos assumir que retorna o valor do imposto com base no preço.
     * @param preco O preço base do item.
     * @return O valor do imposto.
     */
    double calcularImposto(double preco);
}