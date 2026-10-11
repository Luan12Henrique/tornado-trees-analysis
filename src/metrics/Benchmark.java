package metrics;

import model.Tornado;
import tree.Tree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.function.Supplier;

/**
 * Arquivo: src/metrics/Benchmark.java
 *
 * Executa os testes sistemáticos sobre as árvores, medindo altura,
 * número de comparações e tempo de execução conforme os dados são inseridos.
 *
 * Tempo de inserção: para evitar ruído da JVM (compilação JIT, coleta de lixo),
 * o teste roda uma passada de aquecimento (descartada) e depois várias
 * repetições, cada uma com uma árvore nova. O valor registrado em cada ponto
 * é a MEDIANA das repetições.
 *
 * Operações (inserir, buscar, remover): medidas com a árvore completa, usando
 * a mesma ordem aleatória (semente fixa) para as 3 árvores, para a comparação
 * ser justa.
 */
public class Benchmark {

    /** Altura da árvore a cada "passo" elementos inseridos. */
    public List<String> medirAlturaIncremental(Tree<Long, Tornado> arvore, List<Tornado> dados, int passo) {
        List<String> resultados = new ArrayList<>();

        for (int i = 0; i < dados.size(); i++) {
            Tornado t = dados.get(i);
            arvore.inserir(t.getId(), t); //polimorfismo

            int quantidadeInserida = i + 1;
            if (quantidadeInserida % passo == 0 || quantidadeInserida == dados.size()) {
                resultados.add(quantidadeInserida + "," + arvore.altura());
            }
        }

        return resultados;
    }

    /**
     * Tempo de inserção a cada "passo" elementos. Cada linha do resultado:
     * elementos, tempo acumulado (µs), tempo do último bloco (µs),
     * tempo médio por inserção no bloco (µs). Os tempos são medidos em
     * nanossegundos e gravados em microssegundos, com 3 casas decimais
     * (0,001 µs = 1 ns), então não há perda de precisão.
     *
     * @param fabrica     cria uma árvore nova a cada repetição (ex.: BSTree::new)
     * @param repeticoes  quantas medições válidas (a mediana é usada)
     */
    public List<String> medirTempoIncremental(
            Supplier<Tree<Long, Tornado>> fabrica,
            List<Tornado> dados,
            int passo,
            int repeticoes) {

        // pontos de medição: passo, 2*passo, ... e o total
        List<Integer> pontos = new ArrayList<>();
        for (int q = passo; q <= dados.size(); q += passo) {
            pontos.add(q);
        }
        if (pontos.isEmpty() || pontos.get(pontos.size() - 1) != dados.size()) {
            pontos.add(dados.size());
        }

        executarUmaVez(fabrica.get(), dados, pontos); // aquecimento (descartado)

        long[][] medidas = new long[repeticoes][];
        for (int r = 0; r < repeticoes; r++) {
            medidas[r] = executarUmaVez(fabrica.get(), dados, pontos);
        }

        List<String> resultados = new ArrayList<>();
        long acumuladoAnterior = 0;
        int pontoAnterior = 0;

        for (int p = 0; p < pontos.size(); p++) {
            long[] amostras = new long[repeticoes];
            for (int r = 0; r < repeticoes; r++) {
                amostras[r] = medidas[r][p];
            }
            long acumulado = mediana(amostras);
            long bloco = acumulado - acumuladoAnterior;
            double medioPorOperacao = (double) bloco / (pontos.get(p) - pontoAnterior);

            resultados.add(pontos.get(p) + "," + micros(acumulado) + "," + micros(bloco)
                    + "," + micros(medioPorOperacao));

            acumuladoAnterior = acumulado;
            pontoAnterior = pontos.get(p);
        }

        return resultados;
    }

    /** Insere todos os dados uma vez e devolve o tempo acumulado em cada ponto. */
    private long[] executarUmaVez(Tree<Long, Tornado> arvore, List<Tornado> dados, List<Integer> pontos) {
        long[] tempos = new long[pontos.size()];
        int proximo = 0;
        long inicio = System.nanoTime();

        for (int i = 0; i < dados.size(); i++) {
            Tornado t = dados.get(i);
            arvore.inserir(t.getId(), t);

            if (i + 1 == pontos.get(proximo)) {
                tempos[proximo] = System.nanoTime() - inicio;
                proximo++;
            }
        }

        return tempos;
    }

    /** Converte nanossegundos para microssegundos, com 3 casas decimais (ex.: 123200 -> "123.200"). */
    private static String micros(double nanos) {
        return String.format(Locale.US, "%.3f", nanos / 1000.0);
    }

    private long mediana(long[] valores) {
        long[] ordenados = valores.clone();
        Arrays.sort(ordenados);
        return ordenados[ordenados.length / 2];
    }

    /**
     * Mede as três operações em uma árvore: inserir todos os dados, buscar todas
     * as chaves (ordem aleatória) e remover 50% das chaves (ordem aleatória).
     *
     * Cada linha: arvore, cenario, operacao, quantidade de operacoes,
     * tempo total (µs), tempo medio por operacao (µs), comparacoes totais,
     * comparacoes medias por operacao, altura da arvore ao final da operacao.
     * O tempo é a mediana de "repeticoes" execucoes (apos 1 de aquecimento).
     */
    public List<String> medirOperacoes(
            String nomeArvore,
            String cenario,
            Supplier<Tree<Long, Tornado>> fabrica,
            List<Tornado> dados,
            int repeticoes) {

        int n = dados.size();

        List<Long> chaves = new ArrayList<>();
        for (Tornado t : dados) {
            chaves.add(t.getId());
        }
        List<Long> ordemBusca = new ArrayList<>(chaves);
        Collections.shuffle(ordemBusca, new Random(42));
        List<Long> ordemRemocao = new ArrayList<>(chaves);
        Collections.shuffle(ordemRemocao, new Random(43));
        ordemRemocao = new ArrayList<>(ordemRemocao.subList(0, n / 2));

        long[] tempoInserir = new long[repeticoes];
        long[] tempoBuscar = new long[repeticoes];
        long[] tempoRemover = new long[repeticoes];
        int compInserir = 0, compBuscar = 0, compRemover = 0;
        int alturaInserir = 0, alturaRemover = 0;

        for (int r = -1; r < repeticoes; r++) { // r = -1 é o aquecimento (descartado)
            Tree<Long, Tornado> arvore = fabrica.get();
            arvore.resetarContador();

            long inicio = System.nanoTime();
            for (Tornado t : dados) {
                arvore.inserir(t.getId(), t);
            }
            long tInserir = System.nanoTime() - inicio;
            compInserir = arvore.contadorComparacoes();
            alturaInserir = arvore.altura();

            arvore.resetarContador();
            int naoEncontrados = 0;
            inicio = System.nanoTime();
            for (Long chave : ordemBusca) {
                if (arvore.buscar(chave) == null) {
                    naoEncontrados++;
                }
            }
            long tBuscar = System.nanoTime() - inicio;
            compBuscar = arvore.contadorComparacoes();
            if (naoEncontrados > 0) {
                throw new IllegalStateException(nomeArvore + ": " + naoEncontrados + " chaves nao encontradas");
            }

            arvore.resetarContador();
            inicio = System.nanoTime();
            for (Long chave : ordemRemocao) {
                arvore.remover(chave);
            }
            long tRemover = System.nanoTime() - inicio;
            compRemover = arvore.contadorComparacoes();
            alturaRemover = arvore.altura();

            if (r >= 0) {
                tempoInserir[r] = tInserir;
                tempoBuscar[r] = tBuscar;
                tempoRemover[r] = tRemover;
            }
        }

        List<String> linhas = new ArrayList<>();
        linhas.add(linhaOperacao(nomeArvore, cenario, "inserir", n, mediana(tempoInserir), compInserir, alturaInserir));
        linhas.add(linhaOperacao(nomeArvore, cenario, "buscar", n, mediana(tempoBuscar), compBuscar, alturaInserir));
        linhas.add(linhaOperacao(nomeArvore, cenario, "remover", ordemRemocao.size(), mediana(tempoRemover), compRemover, alturaRemover));
        return linhas;
    }

    private String linhaOperacao(String arvore, String cenario, String operacao,
                                 int quantidade, long tempoTotal, int comparacoes, int altura) {
        return arvore + "," + cenario + "," + operacao + "," + quantidade + ","
                + micros(tempoTotal) + "," + micros((double) tempoTotal / quantidade) + ","
                + comparacoes + ","
                + String.format(Locale.US, "%.2f", (double) comparacoes / quantidade) + ","
                + altura;
    }

    /** Número de comparações acumulado a cada "passo" elementos inseridos. */
    public List<String> medirComparacoesIncremental(
            Tree<Long, Tornado> arvore,
            List<Tornado> dados,
            int passo) {

        List<String> resultados = new ArrayList<>();

        arvore.resetarContador();

        for (int i = 0; i < dados.size(); i++) {

            Tornado t = dados.get(i);
            arvore.inserir(t.getId(), t);

            int quantidadeInserida = i + 1;

            if (quantidadeInserida % passo == 0
                    || quantidadeInserida == dados.size()) {

                resultados.add(
                    quantidadeInserida + "," + arvore.contadorComparacoes()
                );
            }
        }

        return resultados;
    }
}