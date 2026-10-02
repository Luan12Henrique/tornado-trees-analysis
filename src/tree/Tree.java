package tree;

import java.util.List;

public interface Tree<K extends Comparable<K>, V> {

    void inserir(K chave, V dado);

    V buscar(K chave);

    void remover(K chave);

    int altura();

    List<K> percorrer(String ordem);

    int contadorComparacoes();

    void resetarContador();
}