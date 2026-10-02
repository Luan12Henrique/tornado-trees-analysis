package tree;

import java.util.ArrayList;
import java.util.List;

/**
 * Árvore Binária de Busca (BST) - sem balanceamento.
 * Para cada nó, todos os valores da subárvore esquerda são menores que a chave,
 * e todos os valores da subárvore direita são maiores.
 * Implementa o contrato definido em Tree<K, V>.
 */
public class BSTree<K extends Comparable<K>, V> implements Tree<K, V> {

    private No<K, V> raiz;
    private int comparacoes;

    public BSTree() {
        this.raiz = null;
        this.comparacoes = 0;
    }

    @Override
    public int contadorComparacoes() {
        return comparacoes;
    }

    @Override
    public void resetarContador() {
        comparacoes = 0;
    }

    @Override
    public void inserir(K chave, V dado) {
        raiz = inserirRecursivo(raiz, chave, dado);
    }

    private No<K, V> inserirRecursivo(No<K, V> atual, K chave, V dado) {
        if (atual == null) {
            return new No<>(chave, dado);
        }

        comparacoes++;
        int cmp = chave.compareTo(atual.chave);

        if (cmp < 0) {
            atual.esquerda = inserirRecursivo(atual.esquerda, chave, dado);
        } else if (cmp > 0) {
            atual.direita = inserirRecursivo(atual.direita, chave, dado);
        } else {
            atual.dado = dado;
        }

        return atual;
    }

    @Override
    public V buscar(K chave) {
        return buscarRecursivo(raiz, chave);
    }

    private V buscarRecursivo(No<K, V> atual, K chave) {
        if (atual == null) {
            return null;
        }

        comparacoes++;
        int cmp = chave.compareTo(atual.chave);

        if (cmp < 0) {
            return buscarRecursivo(atual.esquerda, chave);
        } else if (cmp > 0) {
            return buscarRecursivo(atual.direita, chave);
        } else {
            return atual.dado;
        }
    }

    @Override
    public int altura() {
        return alturaRecursiva(raiz);
    }

    private int alturaRecursiva(No<K, V> atual) {
        if (atual == null) {
            return -1;
        }

        int alturaEsquerda = alturaRecursiva(atual.esquerda);
        int alturaDireita = alturaRecursiva(atual.direita);

        return 1 + Math.max(alturaEsquerda, alturaDireita);
    }

    @Override
    public List<K> percorrer(String ordem) {
        List<K> resultado = new ArrayList<>();

        if (ordem.equals("pre")) {
            preOrdem(raiz, resultado);
        } else if (ordem.equals("in")) {
            emOrdem(raiz, resultado);
        } else if (ordem.equals("pos")) {
            posOrdem(raiz, resultado);
        }

        return resultado;
    }

    private void preOrdem(No<K, V> atual, List<K> resultado) {
        if (atual == null) {
            return;
        }
        resultado.add(atual.chave);
        preOrdem(atual.esquerda, resultado);
        preOrdem(atual.direita, resultado);
    }

    private void emOrdem(No<K, V> atual, List<K> resultado) {
        if (atual == null) {
            return;
        }
        emOrdem(atual.esquerda, resultado);
        resultado.add(atual.chave);
        emOrdem(atual.direita, resultado);
    }

    private void posOrdem(No<K, V> atual, List<K> resultado) {
        if (atual == null) {
            return;
        }
        posOrdem(atual.esquerda, resultado);
        posOrdem(atual.direita, resultado);
        resultado.add(atual.chave);
    }

    @Override
    public void remover(K chave) {
        raiz = removerRecursivo(raiz, chave);
    }

    private No<K, V> removerRecursivo(No<K, V> atual, K chave) {
        if (atual == null) {
            return null;
        }

        comparacoes++;
        int cmp = chave.compareTo(atual.chave);

        if (cmp < 0) {
            atual.esquerda = removerRecursivo(atual.esquerda, chave);
        } else if (cmp > 0) {
            atual.direita = removerRecursivo(atual.direita, chave);
        } else {
            if (atual.esquerda == null) {
                return atual.direita;
            }
            if (atual.direita == null) {
                return atual.esquerda;
            }

            No<K, V> sucessor = menorNo(atual.direita);
            atual.chave = sucessor.chave;
            atual.dado = sucessor.dado;
            atual.direita = removerRecursivo(atual.direita, sucessor.chave);
        }

        return atual;
    }

    private No<K, V> menorNo(No<K, V> atual) {
        while (atual.esquerda != null) {
            atual = atual.esquerda;
        }
        return atual;
    }
}