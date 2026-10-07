package tree;

/**
 * Representa um nó (uma "caixinha") de uma árvore binária.
 * Cada nó guarda uma chave, um dado associado, e referências
 * para os seus dois filhos (esquerda e direita).
 *
 * K = tipo da chave (precisa ser comparável, ex: Long, Integer)
 * V = tipo do dado associado (ex: objeto Tornado)
 */

public class No<K extends Comparable<K>, V> {

    K chave;
    V dado;
    No<K, V> esquerda;
    No<K, V> direita;

    public No(K chave, V dado) {
        this.chave = chave;
        this.dado = dado;
        this.esquerda = null;
        this.direita = null;
    }
}