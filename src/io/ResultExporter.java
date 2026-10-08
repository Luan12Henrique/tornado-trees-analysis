package io;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

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
        escritor.write("elementos,tempo_ns\n");

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
}