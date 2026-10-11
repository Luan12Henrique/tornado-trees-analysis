package io;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Arquivo: src/io/ResultExporter.java
 *
 * Grava em CSV (pasta results/) os resultados dos benchmarks de altura,
 * tempo, comparações e resumo das operações (inserir, buscar, remover).
 */
public class ResultExporter {

    public void exportarAlturaIncremental(
            String caminhoSaida,
            List<String> linhas) throws IOException {

        FileWriter escritor = new FileWriter(caminhoSaida);
        escritor.write("elementos,altura\n");

        for (String linha : linhas) {
            escritor.write(linha + "\n");
        }

        escritor.close();
    }

    public void exportarTempoIncremental(
            String caminhoSaida,
            List<String> linhas) throws IOException {

        FileWriter escritor = new FileWriter(caminhoSaida);
        escritor.write("elementos,tempo_acumulado_us,tempo_bloco_us,tempo_medio_op_us\n");

        for (String linha : linhas) {
            escritor.write(linha + "\n");
        }

        escritor.close();
    }

    public void exportarComparacoesIncremental(
            String caminhoSaida,
            List<String> linhas) throws IOException {

        FileWriter escritor = new FileWriter(caminhoSaida);
        escritor.write("elementos,comparacoes\n");

        for (String linha : linhas) {
            escritor.write(linha + "\n");
        }

        escritor.close();
    }

    public void exportarOperacoes(
            String caminhoSaida,
            List<String> linhas) throws IOException {

        FileWriter escritor = new FileWriter(caminhoSaida);
        escritor.write("arvore,cenario,operacao,quantidade,tempo_total_us,tempo_medio_us,"
                + "comparacoes_total,comparacoes_medias,altura_final\n");

        for (String linha : linhas) {
            escritor.write(linha + "\n");
        }

        escritor.close();
    }
}