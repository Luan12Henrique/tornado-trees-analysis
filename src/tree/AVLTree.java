package tree;

import java.util.List;
import java.util.ArrayList;

public class AVLTree<K extends Comparable<K>,V> implements Tree<K,V> {

    private NoAVL raiz;
    private int comparacoes; 
   
    private class NoAVL {
        
        K chave;
        V valor;
        NoAVL esquerda;
        NoAVL direita;
        int altura; 

        NoAVL(K chave, V valor) {
            this.chave = chave; 
            this.valor = valor; 
            this.esquerda = null; 
            this.direita = null; 
            this.altura = 0; 
        }
    }
    
    public AVLTree() {
        raiz = null; 
        this.comparacoes = 0; 
    }

    public void inserir(K chave, V valor) {
        raiz = inserirRecursivo(raiz, chave, valor); 
    }
         
    private NoAVL inserirRecursivo(NoAVL atual, K chave, V valor) {
        if (atual == null) {
            return new NoAVL(chave, valor);   
        } 

        comparacoes++;
        int cmp = chave.compareTo(atual.chave);

        if (cmp < 0) {
            atual.esquerda = inserirRecursivo(atual.esquerda, chave, valor);
        } else if (cmp > 0) {
            atual.direita = inserirRecursivo(atual.direita, chave, valor); 
        } else {
            atual.valor = valor; // chave já existe: atualiza o valor em vez de duplicar
            return atual;
        }

        atual.altura = Math.max(altura(atual.esquerda), altura(atual.direita)) + 1;

        return balancear(atual);
    }

    public V buscar(K chave) {
        NoAVL atual = raiz;

        while (atual != null) {
            comparacoes++;
            int cmp = chave.compareTo(atual.chave);

            if (cmp == 0) {
                return atual.valor; 
            } else if (cmp < 0) {
                atual = atual.esquerda; 
            } else {
                atual = atual.direita;
            } 
        } 

        return null; 
    }

    public void remover(K chave) {
        raiz = removerRecursivo(raiz, chave); 
    }

    private NoAVL removerRecursivo(NoAVL atual, K chave) {
       
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
            if (atual.esquerda == null && atual.direita == null) {
                return null; 
            }
            if (atual.esquerda == null) {
                return atual.direita; 
            } 
            if (atual.direita == null) {
                return atual.esquerda; 
            }

            NoAVL sucessor = atual.direita; 
            while (sucessor.esquerda != null) {
                sucessor = sucessor.esquerda;
            }

            atual.chave = sucessor.chave; 
            atual.valor = sucessor.valor; 

            atual.direita = removerRecursivo(atual.direita, sucessor.chave); 
        }  

        atual.altura = Math.max(altura(atual.esquerda), altura(atual.direita)) + 1;

        return balancear(atual);  
    }

    public int altura() {
        return altura(raiz); 
    }

    private int altura(NoAVL no) {
        if (no == null) {
            return -1; 
        }
        return no.altura; 
    }
    
    public List<K> percorrer(String ordem) {
        List<K> resultado = new ArrayList<>();

        if ("pre".equals(ordem)) {
            preOrdem(raiz, resultado);
        } else if ("in".equals(ordem)) {
            emOrdem(raiz, resultado);
        } else if ("pos".equals(ordem)) {
            posOrdem(raiz, resultado);
        }

        return resultado;
    }

    private void preOrdem(NoAVL atual, List<K> resultado) {
        if (atual == null) return;
        resultado.add(atual.chave);
        preOrdem(atual.esquerda, resultado);
        preOrdem(atual.direita, resultado);
    }

    private void emOrdem(NoAVL atual, List<K> resultado) {
        if (atual == null) return;
        emOrdem(atual.esquerda, resultado);
        resultado.add(atual.chave);
        emOrdem(atual.direita, resultado);
    }

    private void posOrdem(NoAVL atual, List<K> resultado) {
        if (atual == null) return;
        posOrdem(atual.esquerda, resultado);
        posOrdem(atual.direita, resultado);
        resultado.add(atual.chave);
    }

    public int contadorComparacoes() {
        return comparacoes;
    }
    
    public void resetarContador() {
        comparacoes = 0;
    }

    private int fatorBalanceamento(NoAVL atual) {
        if (atual == null) {
            return 0;
        }
        return altura(atual.esquerda) - altura(atual.direita);
    }

    private NoAVL balancear(NoAVL atual) {
        int fator = fatorBalanceamento(atual);

        if (fator > 1 && fatorBalanceamento(atual.esquerda) >= 0) {
            return rotacaoDireita(atual);
        }

        if (fator < -1 && fatorBalanceamento(atual.direita) <= 0) {
            return rotacaoEsquerda(atual);
        }

        if (fator > 1 && fatorBalanceamento(atual.esquerda) < 0) {
            atual.esquerda = rotacaoEsquerda(atual.esquerda);
            return rotacaoDireita(atual);
        }

        if (fator < -1 && fatorBalanceamento(atual.direita) > 0) {
            atual.direita = rotacaoDireita(atual.direita);
            return rotacaoEsquerda(atual);
        }

        return atual;
    }

    private NoAVL rotacaoDireita(NoAVL atual) {
        NoAVL novaRaiz = atual.esquerda;
        NoAVL subarvore = novaRaiz.direita;

        novaRaiz.direita = atual;
        atual.esquerda = subarvore;

        atual.altura = Math.max(altura(atual.esquerda), altura(atual.direita)) + 1;
        novaRaiz.altura = Math.max(altura(novaRaiz.esquerda), altura(novaRaiz.direita)) + 1;

        return novaRaiz;
    }

    private NoAVL rotacaoEsquerda(NoAVL atual) {
        NoAVL novaRaiz = atual.direita;
        NoAVL subarvore = novaRaiz.esquerda;

        novaRaiz.esquerda = atual;
        atual.direita = subarvore;

        atual.altura = Math.max(altura(atual.esquerda), altura(atual.direita)) + 1;
        novaRaiz.altura = Math.max(altura(novaRaiz.esquerda), altura(novaRaiz.direita)) + 1;

        return novaRaiz;
    }
}