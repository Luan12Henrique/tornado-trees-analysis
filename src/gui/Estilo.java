package gui;

import java.awt.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

/**
 * Arquivo: src/gui/Estilo.java
 *
 * Cores, fontes e funções de apoio compartilhadas pelos gráficos da interface:
 * paleta (uma cor fixa por árvore), formatação de números em pt-BR, cálculo
 * dos valores "redondos" dos eixos e desenho do cabeçalho (título + legenda).
 */
final class Estilo {

    private Estilo() {}

    // ---------- paleta ----------
    static final Color FUNDO = Color.WHITE;
    static final Color GRADE = new Color(0xE8EBF0);
    static final Color EIXO = new Color(0xC4CAD3);
    static final Color TEXTO = new Color(0x1F2937);
    static final Color TEXTO_SUAVE = new Color(0x6B7280);

    // A mesma ordem em todo o programa: BST, AVL, Rubro-Negra
    static final String[] NOMES = {"BST", "AVL", "Rubro-Negra"};
    static final Color[] CORES = {
            new Color(0xE4572E),   // BST: laranja
            new Color(0x2E86AB),   // AVL: azul
            new Color(0x3BB273)    // Rubro-Negra: verde
    };

    static Font fonte(int estilo, float tamanho) {
        return new Font("Segoe UI", estilo, 12).deriveFont(tamanho);
    }

    static void qualidade(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }

    // ---------- números ----------
    private static final DecimalFormatSymbols PT = DecimalFormatSymbols.getInstance(Locale.forLanguageTag("pt-BR"));
    private static final DecimalFormat INTEIRO = new DecimalFormat("#,##0", PT);
    private static final DecimalFormat UMA_DECIMAL = new DecimalFormat("#,##0.#", PT);
    private static final DecimalFormat TRES_DECIMAIS = new DecimalFormat("0.###", PT);

    /** Formata para exibição: 12345 -> "12.345", 2.5 -> "2,5", 0.093 -> "0,093". */
    static String numero(double v) {
        double a = Math.abs(v);
        if (a >= 100) return INTEIRO.format(v);
        if (a >= 1) return UMA_DECIMAL.format(v);
        return TRES_DECIMAIS.format(v);
    }

    /** Valores 0, passo, 2*passo ... até cobrir "max", com passo 1, 2, 5 ou 10 x 10^n. */
    static double[] ticksLineares(double max, int alvo) {
        if (max <= 0) return new double[]{0, 1};
        double bruto = max / alvo;
        double pot = Math.pow(10, Math.floor(Math.log10(bruto)));
        double f = bruto / pot;
        double passo = (f <= 1 ? 1 : f <= 2 ? 2 : f <= 5 ? 5 : 10) * pot;
        int n = (int) Math.ceil(max / passo - 1e-9);
        double[] t = new double[n + 1];
        for (int i = 0; i <= n; i++) t[i] = i * passo;
        return t;
    }

    // ---------- desenho ----------
    /** Título, subtítulo e legenda (alinhada à direita) no topo do gráfico. */
    static void cabecalho(Graphics2D g, int largura, int margemDir,
                          String titulo, String subtitulo, List<String> nomes, List<Color> cores) {
        g.setColor(TEXTO);
        g.setFont(fonte(Font.BOLD, 19f));
        g.drawString(titulo, 28, 36);
        g.setColor(TEXTO_SUAVE);
        g.setFont(fonte(Font.PLAIN, 13f));
        g.drawString(subtitulo, 28, 58);

        g.setFont(fonte(Font.PLAIN, 13f));
        FontMetrics fm = g.getFontMetrics();
        int total = 0;
        for (String n : nomes) total += 22 + 8 + fm.stringWidth(n) + 22;
        int x = largura - margemDir - total + 22;
        for (int i = 0; i < nomes.size(); i++) {
            g.setColor(cores.get(i));
            g.setStroke(new BasicStroke(3.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawLine(x, 31, x + 20, 31);
            g.setColor(TEXTO);
            g.drawString(nomes.get(i), x + 28, 36);
            x += 22 + 8 + fm.stringWidth(nomes.get(i)) + 22;
        }
    }

    static void mensagemVazia(Graphics2D g, int w, int h) {
        g.setColor(TEXTO_SUAVE);
        g.setFont(fonte(Font.PLAIN, 15f));
        String a = "Sem dados para este gráfico.";
        String b = "Rode o Main para gerar os arquivos da pasta results/.";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(a, (w - fm.stringWidth(a)) / 2, h / 2 - 6);
        g.drawString(b, (w - fm.stringWidth(b)) / 2, h / 2 + 18);
    }
}