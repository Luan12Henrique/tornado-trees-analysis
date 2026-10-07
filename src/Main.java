import io.ResultExporter;
import io.TornadoCsvLoader;
import metrics.Benchmark;
import model.Tornado;
import tree.BSTree;
import tree.AVLTree;

import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        TornadoCsvLoader loader = new TornadoCsvLoader();
        List<Tornado> tornados = loader.carregar("data/tornado_sample.csv");

        System.out.println("Total de tornados carregados: " + tornados.size());

        BSTree<Long, Tornado> arvoreBST = new BSTree<>();
        AVLTree<Long, Tornado> arvoreAVL = new AVLTree<>();
        
        Benchmark benchmark = new Benchmark();

        List<String> resultadosBST = benchmark.medirAlturaIncremental(arvoreBST, tornados, 100);

        ResultExporter exporter = new ResultExporter();
        exporter.exportarAlturaIncremental("results/altura_bst.csv", resultadosBST);

        List<String> resultadosAVL = benchmark.medirAlturaIncremental(arvoreAVL, tornados, 100);
        exporter.exportarAlturaIncremental("results/altura_avl.csv", resultadosAVL);

        System.out.println("Resultados exportados para results/altura_bst.csv");
        System.out.println("Resultados exportados para results/altura_avl.csv");
    }
}