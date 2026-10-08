import io.ResultExporter;
import io.TornadoCsvLoader;
import metrics.Benchmark;
import model.Tornado;
import tree.BSTree;
import tree.AVLTree;
import tree.RedBlackTree;

import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        TornadoCsvLoader loader = new TornadoCsvLoader();
        List<Tornado> tornados = loader.carregar("data/tornado_sample.csv");

        System.out.println("Total de tornados carregados: " + tornados.size());

        BSTree<Long, Tornado> alturaBST = new BSTree<>();
        AVLTree<Long, Tornado> alturaAVL = new AVLTree<>();
        RedBlackTree<Long, Tornado> alturaRBT = new RedBlackTree<>();
        
        Benchmark benchmark = new Benchmark();
        ResultExporter exporter = new ResultExporter();

        List<String> resultadosBST = benchmark.medirAlturaIncremental(alturaBST, tornados, 100);
        exporter.exportarAlturaIncremental("results/altura_bst.csv", resultadosBST);

        List<String> resultadosAVL = benchmark.medirAlturaIncremental(alturaAVL, tornados, 100);
        exporter.exportarAlturaIncremental("results/altura_avl.csv", resultadosAVL);

        List<String> resultadosRBT = benchmark.medirAlturaIncremental(alturaRBT, tornados, 100);
        exporter.exportarAlturaIncremental("results/altura_rbt.csv", resultadosRBT);

        System.out.println("Resultados exportados para results/altura_bst.csv");
        System.out.println("Resultados exportados para results/altura_avl.csv");
        System.out.println("Resultados exportados para results/altura_rbt.csv");
    


        BSTree<Long, Tornado> tempoBST = new BSTree<>();
        AVLTree<Long, Tornado> tempoAVL = new AVLTree<>();
        RedBlackTree<Long, Tornado> tempoRBT = new RedBlackTree<>();

        List<String> resultadosTempoBST = benchmark.medirTempoIncremental(tempoBST, tornados, 100);
        exporter.exportarTempoIncremental("results/tempo_bst.csv", resultadosTempoBST);

        List<String> resultadosTempoAVL = benchmark.medirTempoIncremental(tempoAVL, tornados, 100);
        exporter.exportarTempoIncremental("results/tempo_avl.csv", resultadosTempoAVL);

        List<String> resultadosTempoRBT = benchmark.medirTempoIncremental(tempoRBT, tornados, 100);
        exporter.exportarTempoIncremental("results/tempo_rbt.csv", resultadosTempoRBT);

        System.out.println("Resultados exportados para results/tempo_bst.csv");
        System.out.println("Resultados exportados para results/tempo_avl.csv");
        System.out.println("Resultados exportados para results/tempo_rbt.csv");

        

        BSTree<Long, Tornado> comparacoesBST = new BSTree<>();
        AVLTree<Long, Tornado> comparacoesAVL = new AVLTree<>();
        RedBlackTree<Long, Tornado> comparacoesRBT = new RedBlackTree<>();

        List<String> resultadosComparacoesBST = benchmark.medirComparacoesIncremental(comparacoesBST, tornados, 100);
        exporter.exportarComparacoesIncremental("results/comparacoes_bst.csv", resultadosComparacoesBST);

        List<String> resultadosComparacoesAVL = benchmark.medirComparacoesIncremental(comparacoesAVL, tornados, 100);
        exporter.exportarComparacoesIncremental("results/comparacoes_avl.csv", resultadosComparacoesAVL);

        List<String> resultadosComparacoesRBT = benchmark.medirComparacoesIncremental(comparacoesRBT, tornados, 100);
        exporter.exportarComparacoesIncremental("results/comparacoes_rbt.csv", resultadosComparacoesRBT);

        System.out.println("Resultados exportados para results/comparacoes_bst.csv");
        System.out.println("Resultados exportados para results/comparacoes_avl.csv");
        System.out.println("Resultados exportados para results/comparacoes_rbt.csv");


        }

}