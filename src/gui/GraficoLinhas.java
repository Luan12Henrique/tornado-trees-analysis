package gui;

import javax.swing.JPanel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Arquivo: src/gui/GraficoLinhas.java
 *
 * Gráfico de linhas desenhado com Java2D (sem bibliotecas externas), com uma
 * linha por árvore. Suporta escala linear ou logarítmica no eixo Y e mostra
 * uma dica (tooltip) com os valores quando o mouse passa sobre o gráfico.
 */
class GraficoLinhas extends JPanel {

    static class Serie {
        final String nome;
        final Color cor;
        final double[] x;
        final double[] y;

        Serie(String nome, Color cor, double[] x, double[] y) {
            this.nome = nome;
            this.cor = cor;
            this.x = x;
            this.y = y;
        }
    }

    private static final int ESQ = 84, DIR = 32, TOPO = 98, BASE = 64;

    private String titulo = "", subtitulo = "", rotuloX = "", rotuloY = "", unidade = "";
    private List<Serie> series = new ArrayList<>();
    private boolean log;
    private int hover = -1;

    private Rectangle2D plot = new Rectangle2D.Double();
    private double xMax = 1, yMin = 0, yMax = 1;

    GraficoLinhas() {
        setBackground(Estilo.FUNDO);
        setOpaque(true);
        MouseAdapter m = new MouseAdapter() {
            @Override public void mouseMoved(MouseEvent e) { atualizarHover(e.getX()); }
            @Override public void mouseExited(MouseEvent e) { hover = -1; repaint(); }
        };
        addMouseMotionListener(m);
        addMouseListener(m);
    }

    void configurar(String titulo, String subtitulo, String rotuloX, String rotuloY,
                    String unidade, List<Serie> series, boolean log) {
        this.titulo = titulo;
        this.subtitulo = subtitulo;
        this.rotuloX = rotuloX;
        this.rotuloY = rotuloY;
        this.unidade = unidade;
        this.series = series;
        this.log = log;
        this.hover = -1;
        repaint();
    }

    // ---------- interação ----------
    private void atualizarHover(int mx) {
        if (series.isEmpty() || mx < plot.getX() || mx > plot.getMaxX()) {
            hover = -1;
        } else {
            double alvo = (mx - plot.getX()) / plot.getWidth() * xMax;
            hover = indiceMaisProximo(series.get(0).x, alvo);
        }
        repaint();
    }

    private static int indiceMaisProximo(double[] xs, double alvo) {
        int melhor = 0;
        for (int i = 1; i < xs.length; i++) {
            if (Math.abs(xs[i] - alvo) < Math.abs(xs[melhor] - alvo)) melhor = i;
        }
        return melhor;
    }

    // ---------- escalas ----------
    private void calcularEscalas() {
        xMax = 1;
        double maxY = 0, minPositivo = Double.MAX_VALUE;
        for (Serie s : series) {
            for (double v : s.x) xMax = Math.max(xMax, v);
            for (double v : s.y) {
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
            double[] t = Estilo.ticksLineares(maxY, 5);
            yMin = 0;
            yMax = t[t.length - 1];
        }
    }

    private double px(double x) {
        return plot.getX() + x / xMax * plot.getWidth();
    }

    private double py(double v) {
        double frac;
        if (log) {
            if (v <= 0) return Double.NaN;
            frac = (Math.log10(v) - Math.log10(yMin)) / (Math.log10(yMax) - Math.log10(yMin));
        } else {
            frac = (v - yMin) / (yMax - yMin);
        }
        return plot.getMaxY() - frac * plot.getHeight();
    }

    // ---------- desenho ----------
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
        if (series.isEmpty()) {
            Estilo.mensagemVazia(g, w, h);
            g.dispose();
            return;
        }

        calcularEscalas();
        desenharGrade(g, h);
        desenharLinhas(g);
        if (hover >= 0) desenharHover(g);
        g.dispose();
    }

    private void desenharGrade(Graphics2D g, int h) {
        g.setFont(Estilo.fonte(Font.PLAIN, 12f));
        FontMetrics fm = g.getFontMetrics();

        // eixo Y
        List<Double> ticksY = new ArrayList<>();
        if (log) {
            for (double v = yMin; v <= yMax * 1.0001; v *= 10) ticksY.add(v);
        } else {
            for (double v : Estilo.ticksLineares(yMax, 5)) ticksY.add(v);
        }
        g.setStroke(new BasicStroke(1f));
        for (double v : ticksY) {
            double y = py(v);
            g.setColor(Estilo.GRADE);
            g.draw(new java.awt.geom.Line2D.Double(plot.getX(), y, plot.getMaxX(), y));
            String t = Estilo.numero(v);
            g.setColor(Estilo.TEXTO_SUAVE);
            g.drawString(t, (float) (plot.getX() - 12 - fm.stringWidth(t)), (float) (y + 4));
        }

        // eixo X
        g.setColor(Estilo.EIXO);
        g.draw(new java.awt.geom.Line2D.Double(plot.getX(), plot.getMaxY(), plot.getMaxX(), plot.getMaxY()));
        for (double v : Estilo.ticksLineares(xMax, 6)) {
            double x = px(v);
            if (x > plot.getMaxX() + 1) continue;
            g.setColor(Estilo.EIXO);
            g.draw(new java.awt.geom.Line2D.Double(x, plot.getMaxY(), x, plot.getMaxY() + 5));
            String t = Estilo.numero(v);
            g.setColor(Estilo.TEXTO_SUAVE);
            g.drawString(t, (float) (x - fm.stringWidth(t) / 2.0), (float) (plot.getMaxY() + 22));
        }

        // rótulos dos eixos
        String ry = rotuloY + (unidade.isEmpty() ? "" : " (" + unidade + ")") + (log ? "  ·  escala logarítmica" : "");
        g.setColor(Estilo.TEXTO_SUAVE);
        g.setFont(Estilo.fonte(Font.PLAIN, 12f));
        g.drawString(ry, (float) plot.getX(), (float) (plot.getY() - 14));
        FontMetrics fm2 = g.getFontMetrics();
        g.drawString(rotuloX, (float) (plot.getCenterX() - fm2.stringWidth(rotuloX) / 2.0), h - 16);
    }

    private void desenharLinhas(Graphics2D g) {
        g.setStroke(new BasicStroke(2.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (Serie s : series) {
            Path2D path = new Path2D.Double();
            boolean aberto = false;
            for (int i = 0; i < s.x.length; i++) {
                double x = px(s.x[i]), y = py(s.y[i]);
                if (Double.isNaN(y)) {
                    aberto = false;
                    continue;
                }
                if (!aberto) {
                    path.moveTo(x, y);
                    aberto = true;
                } else {
                    path.lineTo(x, y);
                }
            }
            g.setColor(s.cor);
            g.draw(path);
        }
    }

    private void desenharHover(Graphics2D g) {
        double xRef = series.get(0).x[Math.min(hover, series.get(0).x.length - 1)];
        double x = px(xRef);

        g.setColor(new Color(0x9CA3AF));
        g.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[]{4f, 4f}, 0f));
        g.draw(new java.awt.geom.Line2D.Double(x, plot.getY(), x, plot.getMaxY()));

        List<String> linhas = new ArrayList<>();
        List<Color> cores = new ArrayList<>();
        linhas.add(rotuloX + ": " + Estilo.numero(xRef));
        cores.add(null);

        for (Serie s : series) {
            int i = indiceMaisProximo(s.x, xRef);
            double y = py(s.y[i]);
            if (!Double.isNaN(y)) {
                g.setStroke(new BasicStroke(2.4f));
                g.setColor(Color.WHITE);
                g.fill(new Ellipse2D.Double(x - 5, y - 5, 10, 10));
                g.setColor(s.cor);
                g.draw(new Ellipse2D.Double(x - 5, y - 5, 10, 10));
            }
            linhas.add(s.nome + "   " + Estilo.numero(s.y[i]) + (unidade.isEmpty() ? "" : " " + unidade));
            cores.add(s.cor);
        }

        g.setFont(Estilo.fonte(Font.PLAIN, 12.5f));
        FontMetrics fm = g.getFontMetrics();
        int larguraTexto = 0;
        for (String l : linhas) larguraTexto = Math.max(larguraTexto, fm.stringWidth(l));
        double bw = larguraTexto + 42, bh = 16 + linhas.size() * 20;
        double bx = x + 16;
        if (bx + bw > getWidth() - 8) bx = x - 16 - bw;
        double by = plot.getY() + 8;

        g.setColor(new Color(0xE8111827, true));
        g.fill(new RoundRectangle2D.Double(bx, by, bw, bh, 12, 12));
        for (int i = 0; i < linhas.size(); i++) {
            double ty = by + 22 + i * 20;
            if (cores.get(i) != null) {
                g.setColor(cores.get(i));
                g.fill(new Ellipse2D.Double(bx + 14, ty - 9, 9, 9));
                g.setColor(Color.WHITE);
                g.drawString(linhas.get(i), (float) (bx + 30), (float) ty);
            } else {
                g.setColor(new Color(0xD1D5DB));
                g.drawString(linhas.get(i), (float) (bx + 14), (float) ty);
            }
        }
    }
}