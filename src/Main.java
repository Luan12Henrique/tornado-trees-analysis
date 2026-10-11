import io.ResultExporter;
import io.TornadoCsvLoader;
import metrics.Benchmark;
import model.Tornado;
import tree.BSTree;
import tree.AVLTree;
import tree.RedBlackTree;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Arquivo: src/Main.java
 *
 * Carrega o dataset de tornados e roda os benchmarks nas 3 árvores
 * (BST, AVL e Rubro-Negra), inserindo os dados na ordem original do arquivo
 * (cronológica, quase ordenada). Os resultados são exportados para results/.
 */
public class Main {

    static final String ARQUIVO = "data/tornado_full.csv";
    static final int PASSO = 1000;      // a cada quantos elementos medir
    static final int REPETICOES = 5;    // repetições dos testes de tempo (usa a mediana)

    public static void main(String[] args) throws IOException {
        TornadoCsvLoader loader = new TornadoCsvLoader();
        List<Tornado> tornados = loader.carregar(ARQUIVO);

        System.out.println("Total de tornados carregados: " + tornados.size());

        rodarBenchmarks(tornados);
    }

    /** Roda altura, tempo, comparações e resumo das operações (inserir, buscar, remover). */
    static void rodarBenchmarks(List<Tornado> dados) throws IOException {
        String cenario = "cronologico";
        String sufixo = "";
        Benchmark benchmark = new Benchmark();
        ResultExporter exporter = new ResultExporter();

        // ---------- ALTURA ----------
        exporter.exportarAlturaIncremental("results/altura_bst" + sufixo + ".csv",
                benchmark.medirAlturaIncremental(new BSTree<>(), dados, PASSO));
        exporter.exportarAlturaIncremental("results/altura_avl" + sufixo + ".csv",
                benchmark.medirAlturaIncremental(new AVLTree<>(), dados, PASSO));
        exporter.exportarAlturaIncremental("results/altura_rbt" + sufixo + ".csv",
                benchmark.medirAlturaIncremental(new RedBlackTree<>(), dados, PASSO));
        System.out.println("Alturas exportadas");

        // ---------- TEMPO DE INSERÇÃO (aquecimento + mediana) ----------
        exporter.exportarTempoIncremental("results/tempo_bst" + sufixo + ".csv",
                benchmark.medirTempoIncremental(BSTree::new, dados, PASSO, REPETICOES));
        exporter.exportarTempoIncremental("results/tempo_avl" + sufixo + ".csv",
                benchmark.medirTempoIncremental(AVLTree::new, dados, PASSO, REPETICOES));
        exporter.exportarTempoIncremental("results/tempo_rbt" + sufixo + ".csv",
                benchmark.medirTempoIncremental(RedBlackTree::new, dados, PASSO, REPETICOES));
        System.out.println("Tempos exportados");

        // ---------- COMPARAÇÕES DE INSERÇÃO ----------
        exporter.exportarComparacoesIncremental("results/comparacoes_bst" + sufixo + ".csv",
                benchmark.medirComparacoesIncremental(new BSTree<>(), dados, PASSO));
        exporter.exportarComparacoesIncremental("results/comparacoes_avl" + sufixo + ".csv",
                benchmark.medirComparacoesIncremental(new AVLTree<>(), dados, PASSO));
        exporter.exportarComparacoesIncremental("results/comparacoes_rbt" + sufixo + ".csv",
                benchmark.medirComparacoesIncremental(new RedBlackTree<>(), dados, PASSO));
        System.out.println("Comparações exportadas");

        // ---------- RESUMO: INSERIR, BUSCAR E REMOVER ----------
        List<String> operacoes = new ArrayList<>();
        operacoes.addAll(benchmark.medirOperacoes("BST", cenario, BSTree::new, dados, REPETICOES));
        operacoes.addAll(benchmark.medirOperacoes("AVL", cenario, AVLTree::new, dados, REPETICOES));
        operacoes.addAll(benchmark.medirOperacoes("RBT", cenario, RedBlackTree::new, dados, REPETICOES));
        exporter.exportarOperacoes("results/operacoes" + sufixo + ".csv", operacoes);
        System.out.println("Operações exportadas (results/operacoes" + sufixo + ".csv)");
    }
}