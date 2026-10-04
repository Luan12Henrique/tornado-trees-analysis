import io.ResultExporter;
import io.TornadoCsvLoader;
import metrics.Benchmark;
import model.Tornado;
import tree.BSTree;

import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        TornadoCsvLoader loader = new TornadoCsvLoader();
        List<Tornado> tornados = loader.carregar("data/tornado_sample.csv");

        System.out.println("Total de tornados carregados: " + tornados.size());

        BSTree<Long, Tornado> arvore = new BSTree<>();
        Benchmark benchmark = new Benchmark();

        List<String> resultados = benchmark.medirAlturaIncremental(arvore, tornados, 100);

        ResultExporter exporter = new ResultExporter();
        exporter.exportarAlturaIncremental("results/altura_bst.csv", resultados);

        System.out.println("Resultados exportados para results/altura_bst.csv");
    }
}