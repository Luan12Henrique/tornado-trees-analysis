package metrics;

import model.Tornado;
import tree.Tree;

import java.util.ArrayList;
import java.util.List;

/**
 * Executa testes sistemáticos sobre as árvores do projeto,
 * medindo altura, tempo de execução e número de comparações
 * conforme os dados são inseridos.
 */
public class Benchmark {

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

        public List<String> medirTempoIncremental(
            Tree<Long, Tornado> arvore,
            List<Tornado> dados,
            int passo) {

        List<String> resultados = new ArrayList<>();

        long inicio = System.nanoTime();

        for (int i = 0; i < dados.size(); i++) {

            Tornado t = dados.get(i);
            arvore.inserir(t.getId(), t);

            int quantidadeInserida = i + 1;

            if (quantidadeInserida % passo == 0
                    || quantidadeInserida == dados.size()) {

                long agora = System.nanoTime();
                long tempo = agora - inicio;

                resultados.add(quantidadeInserida + "," + tempo);
            }
        }

        return resultados;
    }

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
