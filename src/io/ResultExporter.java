package io;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Exporta resultados de benchmark (altura, tempo, comparações)
 * para arquivos CSV, que depois podem ser abertos no Excel/Sheets
 * para gerar os gráficos exigidos pelo trabalho.
 */
public class ResultExporter {

    public void exportarAlturaIncremental(String caminhoSaida, List<String> linhas) throws IOException {
        FileWriter escritor = new FileWriter(caminhoSaida);
        escritor.write("elementos,altura\n");

        for (String linha : linhas) {
            escritor.write(linha + "\n");
        }

        escritor.close();
    }
} 