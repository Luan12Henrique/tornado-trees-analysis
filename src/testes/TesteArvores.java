package testes;

import io.TornadoCsvLoader;
import model.Tornado;
import tree.AVLTree;
import tree.BSTree;
import tree.RedBlackTree;
import tree.Tree;

import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * Arquivo: src/testes/TesteArvores.java
 *
 * Testes automatizados das 3 árvores (BST, AVL e Rubro-Negra) e do loader.
 * Não precisa de biblioteca: cada teste roda sozinho e mostra [OK] ou [FALHOU].
 * O programa termina com código 1 se algum teste falhar.
 *
 * Os resultados das árvores (chaves E dados) são comparados com um TreeMap do
 * Java (referência já testada). Para conferir os percursos e a altura sem olhar dentro da
 * árvore, a estrutura é reconstruída a partir do pré-ordem e comparada com o
 * pós-ordem e a altura que a árvore informa.
 *
 * Rodar (na pasta do projeto):
 *   javac -encoding UTF-8 -d out -sourcepath src src\testes\TesteArvores.java
 *   java -cp out testes.TesteArvores
 */
public class TesteArvores {

    // ---------- mini framework de testes ----------
    interface Teste { void rodar() throws Exception; }

    static int passaram = 0, falharam = 0;

    static void exigir(boolean condicao, String mensagem) {
        if (!condicao) throw new AssertionError(mensagem);
    }

    static void executar(String nome, Teste teste) {
        try {
            teste.rodar();
            passaram++;
            System.out.println("  [OK]     " + nome);
        } catch (Throwable e) {
            falharam++;
            System.out.println("  [FALHOU] " + nome + "  ->  " + e);
        }
    }

    // ---------- árvores testadas ----------
    static final String[] NOMES = {"BST", "AVL", "Rubro-Negra"};
    static final List<Supplier<Tree<Long, Tornado>>> FABRICAS = List.of(
            BSTree::new, AVLTree::new, RedBlackTree::new);

    static Tornado tornado(long id) {
        return new Tornado(id, 2000, 1, 1, "TX", 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
    }

    /** Insere "quantidade" chaves distintas embaralhadas e devolve a referência (TreeMap). */
    static TreeMap<Long, Tornado> encher(Tree<Long, Tornado> arvore, int quantidade, long semente) {
        List<Long> chaves = new ArrayList<>();
        for (long i = 0; i < quantidade; i++) chaves.add(i * 7 + 3);
        Collections.shuffle(chaves, new Random(semente));
        TreeMap<Long, Tornado> ref = new TreeMap<>();
        for (long k : chaves) {
            Tornado t = tornado(k);
            arvore.inserir(k, t);
            ref.put(k, t);
        }
        return ref;
    }

    /** Altura máxima permitida (em arestas) para uma árvore balanceada com n elementos. */
    static double limiteAltura(int indiceArvore, int n) {
        double log2 = Math.log(n + 2) / Math.log(2);
        return indiceArvore == 1 ? 1.44 * log2 : 2 * log2;   // AVL : Rubro-Negra
    }

    // ---------- reconstrução da estrutura a partir do pré-ordem ----------
    static class No {
        long chave;
        No esq, dir;
        No(long chave) { this.chave = chave; }
    }

    static No reconstruir(List<Long> preOrdem) {
        No raiz = null;
        for (long k : preOrdem) {
            No novo = new No(k);
            if (raiz == null) { raiz = novo; continue; }
            No atual = raiz;
            while (true) {
                if (k < atual.chave) {
                    if (atual.esq == null) { atual.esq = novo; break; }
                    atual = atual.esq;
                } else {
                    if (atual.dir == null) { atual.dir = novo; break; }
                    atual = atual.dir;
                }
            }
        }
        return raiz;
    }

    static int alturaDe(No n) { return n == null ? -1 : 1 + Math.max(alturaDe(n.esq), alturaDe(n.dir)); }

    static void posOrdemDe(No n, List<Long> saida) {
        if (n == null) return;
        posOrdemDe(n.esq, saida);
        posOrdemDe(n.dir, saida);
        saida.add(n.chave);
    }

    /** Altura se TODOS os nós satisfazem a regra da AVL (|h_esq - h_dir| <= 1); -2 se algum não satisfaz. */
    static int alturaSeAvl(No n) {
        if (n == null) return -1;
        int e = alturaSeAvl(n.esq), d = alturaSeAvl(n.dir);
        if (e == -2 || d == -2 || Math.abs(e - d) > 1) return -2;
        return 1 + Math.max(e, d);
    }

    static int caminhoMaisCurto(No n) { return n == null ? 0 : 1 + Math.min(caminhoMaisCurto(n.esq), caminhoMaisCurto(n.dir)); }

    static int caminhoMaisLongo(No n) { return n == null ? 0 : 1 + Math.max(caminhoMaisLongo(n.esq), caminhoMaisLongo(n.dir)); }

    /**
     * Confere pré-ordem, em-ordem, pós-ordem e altura da árvore contra a referência.
     * Também confere o balanceamento pela estrutura reconstruída:
     *   - AVL: em todo nó, as alturas dos filhos diferem em no máximo 1;
     *   - Rubro-Negra: o caminho mais longo da raiz até um vazio tem no máximo
     *     o dobro do mais curto (consequência das regras de cores).
     */
    static void conferirEstrutura(int indiceArvore, Tree<Long, Tornado> arvore, TreeMap<Long, Tornado> ref) {
        List<Long> emOrdem = arvore.percorrer("in");
        exigir(emOrdem.equals(new ArrayList<>(ref.keySet())), "em-ordem diferente da referência");

        for (var e : ref.entrySet()) {
            exigir(arvore.buscar(e.getKey()) == e.getValue(), "a chave " + e.getKey() + " não devolve o dado certo");
        }

        List<Long> pre = arvore.percorrer("pre"), pos = arvore.percorrer("pos");
        exigir(pre.size() == ref.size() && pos.size() == ref.size(), "pré/pós-ordem com tamanho errado");
        if (ref.isEmpty()) return;
        exigir(pre.get(0).equals(pos.get(pos.size() - 1)), "a raiz deveria ser a primeira do pré e a última do pós");

        No reconstruida = reconstruir(pre);
        List<Long> posEsperado = new ArrayList<>();
        posOrdemDe(reconstruida, posEsperado);
        exigir(posEsperado.equals(pos), "pós-ordem não corresponde à mesma árvore do pré-ordem");
        exigir(alturaDe(reconstruida) == arvore.altura(),
                "altura() = " + arvore.altura() + ", mas a estrutura tem altura " + alturaDe(reconstruida));
        if (indiceArvore == 1) {
            exigir(alturaSeAvl(reconstruida) != -2, "a árvore AVL ficou desbalanceada (algum nó com diferença > 1)");
        } else if (indiceArvore == 2) {
            exigir(caminhoMaisLongo(reconstruida) <= 2 * caminhoMaisCurto(reconstruida),
                    "a Rubro-Negra ficou desbalanceada (caminho mais longo > 2x o mais curto)");
        }
    }

    // =====================================================================
    // Testes por árvore
    // =====================================================================

    static void testarArvore(int idx) {
        Supplier<Tree<Long, Tornado>> nova = FABRICAS.get(idx);
        System.out.println("\n" + NOMES[idx]);

        executar("árvore vazia", () -> {
            Tree<Long, Tornado> a = nova.get();
            exigir(a.altura() == -1, "altura de árvore vazia deveria ser -1");
            exigir(a.buscar(1L) == null, "buscar em vazia deveria dar null");
            exigir(a.percorrer("in").isEmpty(), "percurso de vazia deveria ser vazio");
            a.remover(1L);   // não deve dar erro
            exigir(a.altura() == -1, "remover em vazia não deveria mudar nada");
        });

        executar("inserir e buscar (2.000 chaves aleatórias; chaves ausentes dão null)", () -> {
            Tree<Long, Tornado> a = nova.get();
            TreeMap<Long, Tornado> ref = encher(a, 2000, 1);
            for (var e : ref.entrySet()) {
                exigir(a.buscar(e.getKey()) == e.getValue(), "não achou a chave " + e.getKey());
            }
            for (long k = 0; k < 500; k++) {
                exigir(a.buscar(k * 7 + 4) == null, "achou uma chave que não foi inserida: " + (k * 7 + 4));
            }
        });

        executar("inserir chave repetida substitui o dado (sem duplicar)", () -> {
            Tree<Long, Tornado> a = nova.get();
            Tornado antigo = tornado(10), novo = tornado(10);
            a.inserir(10L, antigo);
            a.inserir(20L, tornado(20));
            a.inserir(10L, novo);
            exigir(a.buscar(10L) == novo, "o dado novo deveria ter substituído o antigo");
            exigir(a.percorrer("in").size() == 2, "a chave repetida foi duplicada");
        });

        executar("percursos pré, em e pós-ordem + altura conferem com a estrutura", () -> {
            Tree<Long, Tornado> a = nova.get();
            conferirEstrutura(idx, a, encher(a, 1500, 2));
        });

        executar("percurso com ordem inválida devolve lista vazia", () -> {
            Tree<Long, Tornado> a = nova.get();
            encher(a, 10, 3);
            exigir(a.percorrer("xyz").isEmpty(), "ordem inválida deveria devolver lista vazia");
        });

        executar("remover 2/3 das chaves: estrutura e buscas corretas", () -> {
            Tree<Long, Tornado> a = nova.get();
            TreeMap<Long, Tornado> ref = encher(a, 2000, 4);
            List<Long> chaves = new ArrayList<>(ref.keySet());
            Collections.shuffle(chaves, new Random(5));
            Set<Long> removidas = new HashSet<>(chaves.subList(0, 1333));
            for (long k : removidas) {
                a.remover(k);
                ref.remove(k);
            }
            conferirEstrutura(idx, a, ref);
            for (long k : removidas) exigir(a.buscar(k) == null, "chave removida ainda é encontrada: " + k);
            for (var e : ref.entrySet()) {
                exigir(a.buscar(e.getKey()) == e.getValue(), "chave que deveria existir sumiu ou perdeu o dado: " + e.getKey());
            }
        });

        executar("remover chave inexistente não altera a árvore", () -> {
            Tree<Long, Tornado> a = nova.get();
            TreeMap<Long, Tornado> ref = encher(a, 300, 6);
            a.remover(-1L);
            a.remover(1_000_000L);
            conferirEstrutura(idx, a, ref);
        });

        executar("remover até esvaziar", () -> {
            Tree<Long, Tornado> a = nova.get();
            TreeMap<Long, Tornado> ref = encher(a, 500, 7);
            for (long k : new ArrayList<>(ref.keySet())) a.remover(k);
            exigir(a.altura() == -1, "árvore vazia deveria ter altura -1");
            exigir(a.percorrer("in").isEmpty(), "ainda há chaves depois de remover todas");
        });

        executar("operações misturadas (20.000 inserir/remover/buscar) contra o TreeMap", () -> {
            Tree<Long, Tornado> a = nova.get();
            TreeMap<Long, Tornado> ref = new TreeMap<>();
            Random r = new Random(8);
            for (int i = 1; i <= 20000; i++) {
                long k = r.nextInt(3000);
                int op = r.nextInt(3);
                if (op == 0) {
                    Tornado t = tornado(k);
                    a.inserir(k, t);
                    ref.put(k, t);
                } else if (op == 1) {
                    a.remover(k);
                    ref.remove(k);
                } else {
                    exigir((a.buscar(k) != null) == ref.containsKey(k), "busca divergiu da referência na chave " + k);
                }
                if (i % 2000 == 0) {
                    conferirEstrutura(idx, a, ref);
                    if (idx > 0 && !ref.isEmpty()) {
                        exigir(a.altura() <= limiteAltura(idx, ref.size()),
                                "altura " + a.altura() + " acima do limite com " + ref.size() + " elementos");
                    }
                }
            }
        });

        executar("contador de comparações: zera, conta na busca e na inserção", () -> {
            Tree<Long, Tornado> a = nova.get();
            encher(a, 200, 9);
            a.resetarContador();
            exigir(a.contadorComparacoes() == 0, "contador deveria zerar");
            a.buscar(3L);
            int depoisBusca = a.contadorComparacoes();
            exigir(depoisBusca > 0, "a busca deveria contar comparações");
            a.inserir(100_001L, tornado(100_001L));
            exigir(a.contadorComparacoes() > depoisBusca, "a inserção deveria contar comparações");
            a.resetarContador();
            exigir(a.contadorComparacoes() == 0, "contador deveria zerar de novo");
        });

        if (idx == 0) {
            executar("BST com chaves crescentes vira lista (altura = n - 1)", () -> {
                Tree<Long, Tornado> a = nova.get();
                for (long k = 0; k < 1000; k++) a.inserir(k, tornado(k));
                exigir(a.altura() == 999, "altura esperada 999, veio " + a.altura());
            });
        } else {
            executar("balanceamento: chaves crescentes e decrescentes (3.000) ficam dentro do limite", () -> {
                Tree<Long, Tornado> cresc = nova.get(), decresc = nova.get();
                for (long k = 0; k < 3000; k++) {
                    cresc.inserir(k, tornado(k));
                    decresc.inserir(3000 - k, tornado(k));
                }
                double limite = limiteAltura(idx, 3000);
                exigir(cresc.altura() <= limite, "crescente: altura " + cresc.altura() + " > " + limite);
                exigir(decresc.altura() <= limite, "decrescente: altura " + decresc.altura() + " > " + limite);
            });

            executar("balanceamento: remover em ordem crescente mantém a altura (rebalanceia)", () -> {
                Tree<Long, Tornado> a = nova.get();
                TreeMap<Long, Tornado> ref = new TreeMap<>();
                for (long k = 0; k < 6000; k++) {
                    Tornado t = tornado(k);
                    a.inserir(k, t);
                    ref.put(k, t);
                }
                for (long k = 0; k < 4500; k++) {
                    a.remover(k);
                    ref.remove(k);
                    if (k % 500 == 0 && !ref.isEmpty()) {
                        exigir(a.altura() <= limiteAltura(idx, ref.size()),
                                "altura " + a.altura() + " acima do limite com " + ref.size() + " elementos");
                    }
                }
                conferirEstrutura(idx, a, ref);
                exigir(a.altura() <= limiteAltura(idx, ref.size()), "altura final acima do limite");
            });
        }
    }

    // =====================================================================
    // Loader e integração com o dataset
    // =====================================================================

    static void testarDataset() {
        System.out.println("\nDataset");
        String arquivo = new File("data/tornado_full.csv").exists() ? "data/tornado_full.csv" : "data/tornado_sample.csv";
        if (!new File(arquivo).exists()) {
            System.out.println("  [IGNORADO] nenhum CSV em data/ (rode da pasta do projeto)");
            return;
        }

        final AtomicReference<List<Tornado>> dados = new AtomicReference<>();

        executar("loader lê " + arquivo + " sem erro", () -> {
            dados.set(new TornadoCsvLoader().carregar(arquivo));
            exigir(dados.get().size() > 0, "nenhum registro lido");
        });
        if (dados.get() == null) return;

        executar("chaves únicas: nenhum registro é sobrescrito (" + dados.get().size() + " linhas)", () -> {
            Set<Long> ids = new HashSet<>();
            for (Tornado t : dados.get()) ids.add(t.getId());
            exigir(ids.size() == dados.get().size(), "só " + ids.size() + " chaves únicas em " + dados.get().size() + " linhas");
        });

        for (int i = 0; i < FABRICAS.size(); i++) {
            final int idx = i;
            executar(NOMES[i] + ": inserir todo o dataset, achar todos e remover todos", () -> {
                Tree<Long, Tornado> a = FABRICAS.get(idx).get();
                TreeMap<Long, Tornado> ref = new TreeMap<>();
                for (Tornado t : dados.get()) {
                    a.inserir(t.getId(), t);
                    ref.put(t.getId(), t);
                }
                exigir(a.percorrer("in").equals(new ArrayList<>(ref.keySet())), "em-ordem diferente da referência");
                for (Tornado t : dados.get()) exigir(a.buscar(t.getId()) == t, "não achou " + t.getId());
                if (idx > 0) {
                    exigir(a.altura() <= limiteAltura(idx, ref.size()), "altura " + a.altura() + " acima do limite");
                }
                for (Tornado t : dados.get()) a.remover(t.getId());
                exigir(a.altura() == -1 && a.percorrer("in").isEmpty(), "a árvore deveria ficar vazia");
            });
        }
    }

    // =====================================================================

    public static void main(String[] args) {
        long inicio = System.nanoTime();
        System.out.println("Testes automatizados das árvores");

        for (int i = 0; i < NOMES.length; i++) testarArvore(i);
        testarDataset();

        System.out.println("\n" + passaram + " passaram, " + falharam + " falharam ("
                + (System.nanoTime() - inicio) / 1_000_000 + " ms)");
        System.exit(falharam == 0 ? 0 : 1);
    }
}