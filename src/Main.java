import tree.BSTree;

public class Main {
    public static void main(String[] args) {
        BSTree<Integer, String> arvore = new BSTree<>();

        arvore.inserir(50, "Tornado A");
        arvore.inserir(30, "Tornado B");
        arvore.inserir(70, "Tornado C");
        arvore.inserir(20, "Tornado D");
        arvore.inserir(40, "Tornado E");

        System.out.println("Busca pela chave 30: " + arvore.buscar(30));
        System.out.println("Busca pela chave 99: " + arvore.buscar(99));

        System.out.println("Altura da árvore: " + arvore.altura());

        System.out.println("Pré-ordem: " + arvore.percorrer("pre"));
        System.out.println("Em-ordem: " + arvore.percorrer("in"));
        System.out.println("Pós-ordem: " + arvore.percorrer("pos"));

        arvore.remover(30);
        System.out.println("Em-ordem após remover 30: " + arvore.percorrer("in"));

        System.out.println("Comparações até agora: " + arvore.contadorComparacoes());
    }
}