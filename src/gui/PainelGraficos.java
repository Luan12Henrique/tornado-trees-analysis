package gui;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Arquivo: src/gui/PainelGraficos.java
 *
 * Aba "Gráficos": barra de controles (tipo de gráfico, escala
 * logarítmica, atualizar, salvar PNG) e a área do gráfico. Os dados vêm dos
 * CSVs da pasta results/ (use "Atualizar" depois de rodar o Main de novo).
 *
 * Quando as árvores têm valores muito diferentes (ex.: BST no cenário
 * cronológico), a escala logarítmica é ligada automaticamente; dá para
 * desligar na caixa de seleção.
 */
public class PainelGraficos extends JPanel {

    private enum Tipo { LINHAS, BARRAS }

    /** Cada gráfico disponível: de onde vêm os dados, unidade e legenda. */
    private enum Grafico {
        ALTURA("Altura × elementos inseridos", Tipo.LINHAS, "altura", 1, 1.0,
                "Altura da árvore", "",
                "Altura de cada árvore a cada 1.000 elementos inseridos. Uma árvore bem balanceada tem altura próxima de log₂(n)."),
        COMPARACOES("Comparações acumuladas × elementos", Tipo.LINHAS, "comparacoes", 1, 1.0,
                "Comparações de chaves", "",
                "Total acumulado de comparações de chaves feitas pelas inserções até cada ponto."),
        TEMPO_ACUMULADO("Tempo acumulado de inserção × elementos", Tipo.LINHAS, "tempo", 1, 1e-3,
                "Tempo acumulado", "ms",
                "Tempo total para inserir os elementos até cada ponto (mediana de 5 repetições, após aquecimento da JVM)."),
        TEMPO_MEDIO("Tempo médio por inserção × elementos", Tipo.LINHAS, "tempo", 3, 1.0,
                "Tempo médio por inserção", "µs",
                "Tempo médio de uma inserção em cada bloco de 1.000 elementos (mediana de 5 repetições)."),
        OP_COMPARACOES("Comparações médias por operação", Tipo.BARRAS, "operacoes", 7, 1.0,
                "Comparações por operação", "",
                "Média de comparações por operação, com a árvore completa. Remoção: 50% das chaves, em ordem aleatória."),
        OP_TEMPO("Tempo médio por operação", Tipo.BARRAS, "operacoes", 5, 1.0,
                "Tempo médio por operação", "µs",
                "Tempo médio de cada operação (mediana de 5 repetições). Em tempos muito pequenos há ruído da JVM; as comparações são mais exatas."),
        OP_ALTURA("Altura após cada operação", Tipo.BARRAS, "operacoes", 8, 1.0,
                "Altura da árvore", "",
                "Altura ao final de cada operação. Inserir e buscar usam a árvore completa; remover mostra a altura depois de remover 50% das chaves.");

        final String nome;
        final Tipo tipo;
        final String fonte;       // prefixo do arquivo CSV
        final int coluna;         // coluna do CSV usada como valor
        final double fator;       // conversão de unidade (os CSVs de tempo estão em µs; o acumulado vira ms)
        final String rotuloY;
        final String unidade;
        final String legenda;

        Grafico(String nome, Tipo tipo, String fonte, int coluna, double fator,
                String rotuloY, String unidade, String legenda) {
            this.nome = nome;
            this.tipo = tipo;
            this.fonte = fonte;
            this.coluna = coluna;
            this.fator = fator;
            this.rotuloY = rotuloY;
            this.unidade = unidade;
            this.legenda = legenda;
        }

        @Override public String toString() { return nome; }
    }

    private static final String[] OPERACOES = {"inserir", "buscar", "remover"};
    private static final String[] ROTULOS_OPERACOES = {"Inserir", "Buscar", "Remover"};
    private static final String[] ARVORES_OPERACOES = {"BST", "AVL", "RBT"};

    private final JComboBox<Grafico> comboGrafico = new JComboBox<>(Grafico.values());
    private final JCheckBox checkLog = new JCheckBox("Escala logarítmica");

    private final GraficoLinhas graficoLinhas = new GraficoLinhas();
    private final GraficoBarras graficoBarras = new GraficoBarras();
    private final CardLayout cartas = new CardLayout();
    private final JPanel area = new JPanel(cartas);
    private final JLabel legenda = new JLabel(" ");

    public PainelGraficos() {
        super(new BorderLayout(0, 10));
        setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        setBackground(new Color(0xF3F4F6));

        add(montarControles(), BorderLayout.NORTH);

        area.add(graficoLinhas, Tipo.LINHAS.name());
        area.add(graficoBarras, Tipo.BARRAS.name());
        area.setBorder(BorderFactory.createLineBorder(new Color(0xE0E3E8)));
        add(area, BorderLayout.CENTER);

        legenda.setFont(Estilo.fonte(Font.PLAIN, 13f));
        legenda.setForeground(Estilo.TEXTO_SUAVE);
        legenda.setBorder(BorderFactory.createEmptyBorder(2, 4, 0, 4));
        add(legenda, BorderLayout.SOUTH);

        comboGrafico.addActionListener(e -> atualizar(true));
        checkLog.addActionListener(e -> atualizar(false));

        atualizar(true);
    }

    private JPanel montarControles() {
        // esquerda: seleção do que mostrar; direita: botões (não quebram de linha)
        JPanel esquerda = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        esquerda.setOpaque(false);
        esquerda.add(new JLabel("Gráfico:"));
        esquerda.add(comboGrafico);
        esquerda.add(Box.createHorizontalStrut(8));
        checkLog.setOpaque(false);
        esquerda.add(checkLog);

        JPanel direita = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 4));
        direita.setOpaque(false);
        JButton atualizar = new JButton("Atualizar");
        atualizar.setToolTipText("Relê os CSVs da pasta results/");
        atualizar.addActionListener(e -> atualizar(true));
        direita.add(atualizar);

        JButton salvar = new JButton("Salvar PNG");
        salvar.setToolTipText("Salva o gráfico atual como imagem (para os slides)");
        salvar.addActionListener(e -> salvarImagem());
        direita.add(salvar);

        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.add(esquerda, BorderLayout.WEST);
        p.add(direita, BorderLayout.EAST);
        return p;
    }

    // ---------- montagem do gráfico ----------
    private void atualizar(boolean ligarLogAutomatico) {
        Grafico gr = (Grafico) comboGrafico.getSelectedItem();
        String sufixo = "";
        String subtitulo = "Dados inseridos na ordem do arquivo (cronológica, chaves quase crescentes)";

        legenda.setText(gr.legenda);

        if (gr.tipo == Tipo.LINHAS) {
            List<GraficoLinhas.Serie> series = new ArrayList<>();
            for (int i = 0; i < LeitorResultados.ARVORES.length; i++) {
                double[][] d = LeitorResultados.lerSerie(gr.fonte, LeitorResultados.ARVORES[i], sufixo, gr.coluna);
                if (d == null) continue;
                for (int k = 0; k < d[1].length; k++) d[1][k] *= gr.fator;
                series.add(new GraficoLinhas.Serie(Estilo.NOMES[i], Estilo.CORES[i], d[0], d[1]));
            }
            if (ligarLogAutomatico) checkLog.setSelected(diferencaGrande(maximos(series)));
            graficoLinhas.configurar(gr.nome, subtitulo, "Elementos inseridos", gr.rotuloY,
                    gr.unidade, series, checkLog.isSelected());
            cartas.show(area, Tipo.LINHAS.name());
        } else {
            List<GraficoBarras.Serie> series = new ArrayList<>();
            Map<String, Map<String, String[]>> ops = LeitorResultados.lerOperacoes(sufixo);
            if (ops != null) {
                for (int i = 0; i < ARVORES_OPERACOES.length; i++) {
                    Map<String, String[]> daArvore = ops.get(ARVORES_OPERACOES[i]);
                    if (daArvore == null) continue;
                    double[] valores = new double[OPERACOES.length];
                    for (int c = 0; c < OPERACOES.length; c++) {
                        String[] linha = daArvore.get(OPERACOES[c]);
                        valores[c] = linha == null ? 0 : Double.parseDouble(linha[gr.coluna]) * gr.fator;
                    }
                    series.add(new GraficoBarras.Serie(Estilo.NOMES[i], Estilo.CORES[i], valores));
                }
            }
            if (ligarLogAutomatico) checkLog.setSelected(diferencaGrande(maximosBarras(series)));
            graficoBarras.configurar(gr.nome, subtitulo, gr.rotuloY, gr.unidade,
                    ROTULOS_OPERACOES, series, checkLog.isSelected());
            cartas.show(area, Tipo.BARRAS.name());
        }
    }

    private static List<Double> maximos(List<GraficoLinhas.Serie> series) {
        List<Double> m = new ArrayList<>();
        for (GraficoLinhas.Serie s : series) {
            double max = 0;
            for (double v : s.y) max = Math.max(max, v);
            m.add(max);
        }
        return m;
    }

    private static List<Double> maximosBarras(List<GraficoBarras.Serie> series) {
        List<Double> m = new ArrayList<>();
        for (GraficoBarras.Serie s : series) {
            double max = 0;
            for (double v : s.valores) max = Math.max(max, v);
            m.add(max);
        }
        return m;
    }

    /** True se o maior valor de uma árvore for mais de 20x o de outra (gráfico ficaria achatado). */
    private static boolean diferencaGrande(List<Double> maximos) {
        double min = Double.MAX_VALUE, max = 0;
        for (double v : maximos) {
            if (v <= 0) continue;
            min = Math.min(min, v);
            max = Math.max(max, v);
        }
        return max > 0 && max / min > 20;
    }

    // ---------- exportar imagem ----------
    BufferedImage imagem() {
        Component atual = ((Grafico) comboGrafico.getSelectedItem()).tipo == Tipo.LINHAS ? graficoLinhas : graficoBarras;
        int escala = 2;   // dobro da resolução, para ficar nítido nos slides
        BufferedImage img = new BufferedImage(
                Math.max(1, atual.getWidth() * escala), Math.max(1, atual.getHeight() * escala),
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.scale(escala, escala);
        atual.paint(g);
        g.dispose();
        return img;
    }

    private void salvarImagem() {
        JFileChooser fc = new JFileChooser();
        Grafico gr = (Grafico) comboGrafico.getSelectedItem();
        String nome = gr.name().toLowerCase() + ".png";
        fc.setSelectedFile(new File(nome));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File arquivo = fc.getSelectedFile();
        if (!arquivo.getName().toLowerCase().endsWith(".png")) {
            arquivo = new File(arquivo.getPath() + ".png");
        }
        try {
            ImageIO.write(imagem(), "png", arquivo);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Não foi possível salvar a imagem:\n" + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Usado nos testes: escolhe o gráfico sem o mouse. */
    void mostrar(int indiceGrafico) {
        comboGrafico.setSelectedIndex(indiceGrafico);
    }
}