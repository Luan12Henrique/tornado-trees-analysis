package io;

import model.Tornado;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Arquivo: src/io/TornadoCsvLoader.java
 *
 * Lê o CSV de tornados da NOAA/SPC (data/tornado_full.csv) e converte cada
 * linha em um objeto Tornado, pronto para ser inserido nas árvores.
 *
 * Chave: o campo "om" muda de formato em 2007 (vira texto, ex.: 0710162316-01),
 * então a chave é montada com data + hora (aaaaMMddHHmm) e um número de
 * desempate para tornados no mesmo minuto. Assim ela é única, em ordem
 * cronológica e funciona para todos os anos.
 */
public class TornadoCsvLoader {

    public List<Tornado> carregar(String caminhoArquivo) throws IOException {
        List<Tornado> tornados = new ArrayList<>();
        // Conta registros que caem no mesmo minuto para desempatar a chave
        Map<Long, Integer> empates = new HashMap<>();

        BufferedReader leitor = new BufferedReader(new FileReader(caminhoArquivo));
        String linha;
        boolean primeiraLinha = true;

        while ((linha = leitor.readLine()) != null) {
            if (primeiraLinha) {
                primeiraLinha = false;
                continue;
            }

                String[] campos = linha.split(",", -1);

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

            // campos[5] = "HH:mm:ss". Em registros recentes o campo "om" não é numérico
            // (ex.: 0710162316-01), então a chave é data+hora (cronológica) + desempate.
            String[] hora = campos[5].split(":");
            long minuto = (((long) ano * 100 + mes) * 100 + dia) * 10000L
                    + Integer.parseInt(hora[0]) * 100L + Integer.parseInt(hora[1]);
            int seq = empates.merge(minuto, 1, Integer::sum) - 1;
            long id = minuto * 1000L + seq;

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