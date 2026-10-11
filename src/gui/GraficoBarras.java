package gui;

import javax.swing.JPanel;
import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Arquivo: src/gui/GraficoBarras.java
 *
 * Gráfico de barras agrupadas (Java2D): uma categoria por operação
 * (inserir, buscar, remover) e uma barra por árvore dentro de cada grupo.
 * Suporta escala linear ou logarítmica e mostra o valor acima de cada barra.
 */
class GraficoBarras extends JPanel {

    static class Serie {
        final String nome;
        final Color cor;
        final double[] valores;   // um valor por categoria

        Serie(String nome, Color cor, double[] valores) {
            this.nome = nome;
            this.cor = cor;
            this.valores = valores;
        }
    }

    private static final int ESQ = 84, DIR = 32, TOPO = 98, BASE = 56;

    private String titulo = "", subtitulo = "", rotuloY = "", unidade = "";
    private String[] categorias = new String[0];
    private List<Serie> series = new ArrayList<>();
    private boolean log;

    private Rectangle2D plot = new Rectangle2D.Double();
    private double yMin = 0, yMax = 1;

    GraficoBarras() {
        setBackground(Estilo.FUNDO);
        setOpaque(true);
    }

    void configurar(String titulo, String subtitulo, String rotuloY, String unidade,
                    String[] categorias, List<Serie> series, boolean log) {
        this.titulo = titulo;
        this.subtitulo = subtitulo;
        this.rotuloY = rotuloY;
        this.unidade = unidade;
        this.categorias = categorias;
        this.series = series;
        this.log = log;
        repaint();
    }

    private void calcularEscalas() {
        double maxY = 0, minPositivo = Double.MAX_VALUE;
        for (Serie s : series) {
            for (double v : s.valores) {
                maxY = Math.max(maxY, v);
                if (v > 0) minPositivo = Math.min(minPositivo, v);
            }
        }
        if (log) {
            if (minPositivo == Double.MAX_VALUE) minPositivo = 1;
            yMin = Math.pow(10, Math.floor(Math.log10(minPositivo)));
            yMax = Math.pow(10, Math.ceil(Math.log10(Math.max(maxY, yMin * 1.0001))));
            if (yMax <= yMin) yMax = yMin * 10;
        } else {
            double[] t = Estilo.ticksLineares(maxY * 1.08, 5);   // folga para o rótulo da barra mais alta
            yMin = 0;
            yMax = t[t.length - 1];
        }
    }

    private double py(double v) {
        double frac;
        if (log) {
            if (v <= 0) return plot.getMaxY();
            frac = (Math.log10(v) - Math.log10(yMin)) / (Math.log10(yMax) - Math.log10(yMin));
        } else {
            frac = (v - yMin) / (yMax - yMin);
        }
        return plot.getMaxY() - frac * plot.getHeight();
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0.create();
        Estilo.qualidade(g);
        int w = getWidth(), h = getHeight();

        List<String> nomes = new ArrayList<>();
        List<Color> cores = new ArrayList<>();
        for (Serie s : series) {
            nomes.add(s.nome);
            cores.add(s.cor);
        }
        Estilo.cabecalho(g, w, DIR, titulo, subtitulo, nomes, cores);

        plot = new Rectangle2D.Double(ESQ, TOPO, Math.max(10, w - ESQ - DIR), Math.max(10, h - TOPO - BASE));
        if (series.isEmpty() || categorias.length == 0) {
            Estilo.mensagemVazia(g, w, h);
            g.dispose();
            return;
        }

        calcularEscalas();
        desenharGrade(g);
        desenharBarras(g);
        g.dispose();
    }

    private void desenharGrade(Graphics2D g) {
        g.setFont(Estilo.fonte(Font.PLAIN, 12f));
        FontMetrics fm = g.getFontMetrics();

        List<Double> ticks = new ArrayList<>();
        if (log) {
            for (double v = yMin; v <= yMax * 1.0001; v *= 10) ticks.add(v);
        } else {
            for (double v : Estilo.ticksLineares(yMax, 5)) ticks.add(v);
        }
        g.setStroke(new BasicStroke(1f));
        for (double v : ticks) {
            double y = py(v);
            g.setColor(Estilo.GRADE);
            g.draw(new Line2D.Double(plot.getX(), y, plot.getMaxX(), y));
            String t = Estilo.numero(v);
            g.setColor(Estilo.TEXTO_SUAVE);
            g.drawString(t, (float) (plot.getX() - 12 - fm.stringWidth(t)), (float) (y + 4));
        }
        g.setColor(Estilo.EIXO);
        g.draw(new Line2D.Double(plot.getX(), plot.getMaxY(), plot.getMaxX(), plot.getMaxY()));

        String ry = rotuloY + (unidade.isEmpty() ? "" : " (" + unidade + ")") + (log ? "  ·  escala logarítmica" : "");
        g.setColor(Estilo.TEXTO_SUAVE);
        g.drawString(ry, (float) plot.getX(), (float) (plot.getY() - 14));
    }

    private void desenharBarras(Graphics2D g) {
        int nCat = categorias.length, nSer = series.size();
        double larguraGrupo = plot.getWidth() / nCat;
        double larguraTotal = Math.min(larguraGrupo * 0.72, nSer * 96.0);
        double espaco = 8;
        double bw = (larguraTotal - (nSer - 1) * espaco) / nSer;

        for (int c = 0; c < nCat; c++) {
            double centro = plot.getX() + larguraGrupo * (c + 0.5);
            double inicio = centro - larguraTotal / 2;

            for (int s = 0; s < nSer; s++) {
                double v = series.get(s).valores[c];
                double x = inicio + s * (bw + espaco);
                double yTopo = py(v);
                double altura = plot.getMaxY() - yTopo;

                if (v > 0 && altura > 0) {
                    Shape antigo = g.getClip();
                    g.setClip(new Rectangle2D.Double(x - 1, 0, bw + 2, plot.getMaxY()));
                    g.setColor(series.get(s).cor);
                    g.fill(new RoundRectangle2D.Double(x, yTopo, bw, altura + 12, 10, 10));
                    g.setClip(antigo);
                }

                String t = Estilo.numero(v);
                g.setFont(Estilo.fonte(Font.BOLD, 12f));
                FontMetrics fm = g.getFontMetrics();
                g.setColor(Estilo.TEXTO);
                g.drawString(t, (float) (x + bw / 2 - fm.stringWidth(t) / 2.0), (float) (yTopo - 7));
            }

            g.setFont(Estilo.fonte(Font.BOLD, 14f));
            FontMetrics fm = g.getFontMetrics();
            g.setColor(Estilo.TEXTO);
            g.drawString(categorias[c], (float) (centro - fm.stringWidth(categorias[c]) / 2.0), (float) (plot.getMaxY() + 26));
        }
    }
}