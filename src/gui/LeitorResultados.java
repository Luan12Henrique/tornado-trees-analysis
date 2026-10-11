package gui;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Arquivo: src/gui/LeitorResultados.java
 *
 * Lê os CSVs gerados pelo Main na pasta results/ e entrega os dados prontos
 * para os gráficos. Se um arquivo não existir ou estiver inválido, devolve
 * null (a interface então mostra "sem dados" em vez de dar erro).
 */
final class LeitorResultados {

    private LeitorResultados() {}

    static final String PASTA = "results";
    static final String[] ARVORES = {"bst", "avl", "rbt"};   // mesma ordem de Estilo.NOMES

    /**
     * Lê results/<prefixo>_<arvore><sufixo>.csv (ex.: altura_bst.csv; o sufixo fica vazio).
     * Devolve {x[], y[]}: x = elementos (coluna 0), y = a coluna "coluna".
     */
    static double[][] lerSerie(String prefixo, String arvore, String sufixo, int coluna) {
        Path caminho = Paths.get(PASTA, prefixo + "_" + arvore + sufixo + ".csv");
        if (!Files.exists(caminho)) return null;
        try {
            List<String> linhas = Files.readAllLines(caminho);
            List<double[]> pontos = new ArrayList<>();
            for (int i = 1; i < linhas.size(); i++) {          // linha 0 = cabeçalho
                if (linhas.get(i).isBlank()) continue;
                String[] c = linhas.get(i).split(",");
                pontos.add(new double[]{Double.parseDouble(c[0]), Double.parseDouble(c[coluna])});
            }
            if (pontos.isEmpty()) return null;
            double[] x = new double[pontos.size()], y = new double[pontos.size()];
            for (int i = 0; i < pontos.size(); i++) {
                x[i] = pontos.get(i)[0];
                y[i] = pontos.get(i)[1];
            }
            return new double[][]{x, y};
        } catch (IOException | NumberFormatException | ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }

    /**
     * Lê results/operacoes<sufixo>.csv.
     * Devolve: arvore ("BST", "AVL", "RBT") -> operacao ("inserir", "buscar", "remover") -> colunas da linha.
     */
    static Map<String, Map<String, String[]>> lerOperacoes(String sufixo) {
        Path caminho = Paths.get(PASTA, "operacoes" + sufixo + ".csv");
        if (!Files.exists(caminho)) return null;
        try {
            Map<String, Map<String, String[]>> mapa = new HashMap<>();
            List<String> linhas = Files.readAllLines(caminho);
            for (int i = 1; i < linhas.size(); i++) {
                if (linhas.get(i).isBlank()) continue;
                String[] c = linhas.get(i).split(",");
                mapa.computeIfAbsent(c[0], k -> new HashMap<>()).put(c[2], c);
            }
            return mapa.isEmpty() ? null : mapa;
        } catch (IOException e) {
            return null;
        }
    }
}