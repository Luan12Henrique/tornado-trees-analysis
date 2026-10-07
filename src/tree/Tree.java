package tree;

import java.util.List;

/**
 * Interface que define o "contrato" que toda árvore do projeto precisa seguir.
 * BSTree, AVLTree e RedBlackTree implementam esta interface, cada uma com sua
 * própria lógica interna, mas todas respondendo aos mesmos métodos.
 * Isso permite que o resto do programa (GUI, benchmark) trate qualquer uma
 * das três árvores de forma idêntica, apenas trocando qual implementação usar.
 *
 * K = tipo da chave (precisa ser comparável, ex: Long, Integer)
 * V = tipo do dado associado (ex: objeto Tornado)
 */

public interface Tree<K extends Comparable<K>, V> {

    void inserir(K chave, V dado);

    V buscar(K chave);

    void remover(K chave);

    int altura();

    List<K> percorrer(String ordem);

    int contadorComparacoes();

    void resetarContador();
}