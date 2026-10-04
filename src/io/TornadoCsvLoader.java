package io;

import model.Tornado;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Lê o arquivo CSV de tornados da NOAA/SPC e converte cada linha
 * em um objeto Tornado, pronto para ser inserido nas árvores.
 */
public class TornadoCsvLoader {

    public List<Tornado> carregar(String caminhoArquivo) throws IOException {
        List<Tornado> tornados = new ArrayList<>();

        BufferedReader leitor = new BufferedReader(new FileReader(caminhoArquivo));
        String linha;
        boolean primeiraLinha = true;

        while ((linha = leitor.readLine()) != null) {
            if (primeiraLinha) {
                primeiraLinha = false;
                continue;
            }

                String[] campos = linha.split(",", -1);

            int om = Integer.parseInt(campos[0]);
            int ano = Integer.parseInt(campos[1]);
            int mes = Integer.parseInt(campos[2]);
            int dia = Integer.parseInt(campos[3]);
            String estado = campos[7];
            int magnitude = Integer.parseInt(campos[10]);
            int feridos = Integer.parseInt(campos[11]);
            int mortos = Integer.parseInt(campos[12]);
            double prejuizo = Double.parseDouble(campos[13]);
            double latitudeInicio = Double.parseDouble(campos[15]);
            double longitudeInicio = Double.parseDouble(campos[16]);
            double latitudeFim = Double.parseDouble(campos[17]);
            double longitudeFim = Double.parseDouble(campos[18]);
            double comprimento = Double.parseDouble(campos[19]);
            double largura = Double.parseDouble(campos[20]);

            long id = (long) ano * 100000 + om;

            Tornado tornado = new Tornado(id, ano, mes, dia, estado, magnitude,
                    feridos, mortos, prejuizo,
                    latitudeInicio, longitudeInicio,
                    latitudeFim, longitudeFim,
                    comprimento, largura);

            tornados.add(tornado);
        }

        leitor.close();
        return tornados;
    }
}
