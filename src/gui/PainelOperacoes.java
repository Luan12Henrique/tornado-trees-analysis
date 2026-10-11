package gui;

import io.TornadoCsvLoader;
import model.Tornado;
import tree.AVLTree;
import tree.BSTree;
import tree.RedBlackTree;
import tree.Tree;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Arquivo: src/gui/PainelOperacoes.java
 *
 * Aba "Operações": demonstração ao vivo das 3 árvores (BST, AVL e Rubro-Negra).
 *
 * Toda operação (inserir, buscar, remover) é aplicada nas TRÊS árvores ao mesmo
 * tempo, e a tabela mostra, para cada uma, o resultado, as comparações feitas,
 * o tempo e a altura. A árvore escolhida em "Árvore ativa" é a usada para
 * mostrar os percursos (pré, em e pós-ordem).
 *
 * Inserção, de dois jeitos:
 *   - manual: você escolhe data, hora, estado, magnitude etc. A chave é montada
 *     com data + hora (aaaaMMddHHmm), igual ao loader do dataset;
 *   - com o dataset: carrega os N primeiros registros, ou insere o próximo
 *     registro do arquivo, um de cada vez.
 */
public class PainelOperacoes extends JPanel {

    private static final String ARQUIVO = "data/tornado_full.csv";
    private static final int LIMITE_EXIBICAO = 60;   // chaves mostradas nos percursos

    // ---------- estado ----------
    private List<Tree<Long, Tornado>> arvores = novasArvores();
    private List<Tornado> dataset;      // carregado na primeira vez que for preciso
    private int proximo = 0;            // próximo registro do dataset a inserir
    private int tamanho = 0;            // elementos em cada árvore
    private final Random sorteio = new Random();

    // ---------- controles ----------
    private final JComboBox<String> comboArvore = new JComboBox<>(Estilo.NOMES);
    private final JSpinner spinQuantidade = spinner(1000, 1, 1_000_000, 100);
    private final JButton botaoCarregar = new JButton("Carregar do dataset");
    private final JButton botaoProximo = new JButton("Inserir próximo do dataset");
    private final JButton botaoLimpar = new JButton("Limpar árvores");

    private final JSpinner spinAno = spinner(2025, 1950, 2100, 1);
    private final JSpinner spinMes = spinner(6, 1, 12, 1);
    private final JSpinner spinDia = spinner(15, 1, 31, 1);
    private final JSpinner spinHora = spinner(14, 0, 23, 1);
    private final JSpinner spinMinuto = spinner(30, 0, 59, 1);
    private final JTextField campoEstado = new JTextField("TX", 4);
    private final JSpinner spinMagnitude = spinner(2, -9, 5, 1);
    private final JSpinner spinFeridos = spinner(0, 0, 100000, 1);
    private final JSpinner spinMortos = spinner(0, 0, 100000, 1);

    private final JTextField campoChave = new JTextField(14);

    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"Árvore", "Resultado", "Comparações", "Tempo (µs)", "Altura", "Elementos"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTextArea areaPercurso = areaTexto(5);
    private final JTextArea areaLog = areaTexto(6);
    private final JLabel info = new JLabel(" ");

    public PainelOperacoes() {
        super(new BorderLayout(14, 0));
        setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        setBackground(new Color(0xF3F4F6));

        JScrollPane lateral = new JScrollPane(montarLateral(),
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        lateral.setBorder(null);
        lateral.setPreferredSize(new Dimension(430, 100));
        lateral.getViewport().setOpaque(false);
        lateral.setOpaque(false);
        add(lateral, BorderLayout.WEST);
        add(montarCentro(), BorderLayout.CENTER);

        botaoCarregar.addActionListener(e -> carregarDoDataset());
        botaoProximo.addActionListener(e -> inserirProximo());
        botaoLimpar.addActionListener(e -> limpar());
        comboArvore.addActionListener(e -> atualizarInfo());

        atualizarInfo();
        log("Árvores vazias. Carregue o dataset ou insira um tornado manualmente.");
    }

    // =====================================================================
    // Montagem da tela
    // =====================================================================

    private JPanel montarLateral() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);

        // --- dataset ---
        JPanel dataset = cartao("1. Dataset");
        JPanel linhaQtd = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        linhaQtd.setOpaque(false);
        linhaQtd.add(new JLabel("Quantidade:"));
        linhaQtd.add(spinQuantidade);
        dataset.add(linhaQtd);
        dataset.add(Box.createVerticalStrut(6));
        dataset.add(larguraTotal(botaoCarregar));
        dataset.add(Box.createVerticalStrut(4));
        dataset.add(larguraTotal(botaoProximo));
        dataset.add(Box.createVerticalStrut(4));
        dataset.add(larguraTotal(botaoLimpar));
        p.add(dataset);
        p.add(Box.createVerticalStrut(10));

        // --- inserção manual ---
        JPanel manual = cartao("2. Inserir manualmente");
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        int l = 0;
        linha(form, l++, "Data (ano / mês / dia)", trio(spinAno, spinMes, spinDia));
        linha(form, l++, "Hora (h / min)", trio(spinHora, spinMinuto, null));
        linha(form, l++, "Estado (sigla)", campoEstado);
        linha(form, l++, "Magnitude (-9 a 5)", spinMagnitude);
        linha(form, l++, "Feridos", spinFeridos);
        linha(form, l++, "Mortos", spinMortos);
        manual.add(form);
        manual.add(Box.createVerticalStrut(6));
        JButton inserir = new JButton("Inserir nas 3 árvores");
        inserir.addActionListener(e -> inserirManual());
        manual.add(larguraTotal(inserir));
        p.add(manual);
        p.add(Box.createVerticalStrut(10));

        // --- buscar / remover ---
        JPanel busca = cartao("3. Buscar / Remover");
        JPanel linhaChave = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        linhaChave.setOpaque(false);
        linhaChave.add(new JLabel("Chave:"));
        linhaChave.add(campoChave);
        busca.add(linhaChave);
        busca.add(Box.createVerticalStrut(6));
        JButton sortear = new JButton("Escolher uma chave existente");
        sortear.addActionListener(e -> sortearChave());
        busca.add(larguraTotal(sortear));
        busca.add(Box.createVerticalStrut(4));
        JPanel doisBotoes = new JPanel(new GridLayout(1, 2, 6, 0));
        doisBotoes.setOpaque(false);
        JButton buscar = new JButton("Buscar");
        buscar.addActionListener(e -> buscar());
        JButton remover = new JButton("Remover");
        remover.addActionListener(e -> remover());
        doisBotoes.add(buscar);
        doisBotoes.add(remover);
        busca.add(larguraTotal(doisBotoes));
        p.add(busca);
        p.add(Box.createVerticalGlue());
        return p;
    }

    private JPanel montarCentro() {
        JPanel centro = new JPanel(new BorderLayout(0, 10));
        centro.setOpaque(false);

        // tabela com o resultado da última operação
        JTable tabela = new JTable(modelo);
        tabela.setRowHeight(28);
        tabela.setFont(Estilo.fonte(Font.PLAIN, 13f));
        tabela.getTableHeader().setFont(Estilo.fonte(Font.BOLD, 13f));
        tabela.setFillsViewportHeight(true);
        tabela.setShowVerticalLines(false);
        DefaultTableCellRenderer cor = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel,
                                                                      boolean foco, int linha, int col) {
                Component c = super.getTableCellRendererComponent(t, v, sel, foco, linha, col);
                c.setForeground(Estilo.CORES[Math.min(linha, Estilo.CORES.length - 1)]);
                c.setFont(Estilo.fonte(Font.BOLD, 13f));
                return c;
            }
        };
        tabela.getColumnModel().getColumn(0).setCellRenderer(cor);
        JScrollPane scrollTabela = new JScrollPane(tabela);
        scrollTabela.setPreferredSize(new Dimension(100, 130));
        JPanel cartaoTabela = cartaoComConteudo("Resultado da última operação (nas 3 árvores)", scrollTabela);
        centro.add(cartaoTabela, BorderLayout.NORTH);

        // percursos
        JPanel percursos = new JPanel(new BorderLayout(0, 8));
        percursos.setOpaque(false);
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        barra.setOpaque(false);
        barra.add(new JLabel("Árvore ativa:"));
        barra.add(comboArvore);
        for (String[] o : new String[][]{{"pre", "Pré-ordem"}, {"in", "Em ordem"}, {"pos", "Pós-ordem"}}) {
            JButton b = new JButton(o[1]);
            b.addActionListener(e -> percurso(o[0], o[1]));
            barra.add(b);
        }
        percursos.add(barra, BorderLayout.NORTH);
        info.setFont(Estilo.fonte(Font.PLAIN, 13f));
        info.setForeground(Estilo.TEXTO_SUAVE);
        JPanel miolo = new JPanel(new BorderLayout(0, 6));
        miolo.setOpaque(false);
        miolo.add(info, BorderLayout.NORTH);
        miolo.add(new JScrollPane(areaPercurso), BorderLayout.CENTER);
        percursos.add(miolo, BorderLayout.CENTER);
        centro.add(cartaoComConteudo("Percursos e altura", percursos), BorderLayout.CENTER);

        // log
        JScrollPane scrollLog = new JScrollPane(areaLog);
        scrollLog.setPreferredSize(new Dimension(100, 150));
        centro.add(cartaoComConteudo("Mensagens", scrollLog), BorderLayout.SOUTH);
        return centro;
    }

    // =====================================================================
    // Dataset
    // =====================================================================

    /** Carrega os N primeiros registros do dataset nas 3 árvores (em segundo plano). */
    void carregarDoDataset() {
        final int n = (Integer) spinQuantidade.getValue();
        ocupado(true);
        log("Carregando " + Estilo.numero(n) + " registros do dataset...");

        new SwingWorker<List<Tree<Long, Tornado>>, Void>() {
            int inseridos;

            @Override protected List<Tree<Long, Tornado>> doInBackground() throws Exception {
                if (dataset == null) dataset = new TornadoCsvLoader().carregar(ARQUIVO);
                inseridos = Math.min(n, dataset.size());
                List<Tree<Long, Tornado>> novas = novasArvores();
                for (int i = 0; i < inseridos; i++) {
                    Tornado t = dataset.get(i);
                    for (Tree<Long, Tornado> a : novas) a.inserir(t.getId(), t);
                }
                return novas;
            }

            @Override protected void done() {
                try {
                    arvores = get();
                    proximo = inseridos;
                    tamanho = inseridos;
                    modelo.setRowCount(0);
                    log("Carregados " + Estilo.numero(inseridos) + " registros em cada árvore.");
                } catch (Exception e) {
                    log("ERRO ao carregar " + ARQUIVO + ": " + (e.getCause() != null ? e.getCause() : e));
                } finally {
                    ocupado(false);
                    atualizarInfo();
                }
            }
        }.execute();
    }

    /** Insere o próximo registro do arquivo nas 3 árvores. */
    private void inserirProximo() {
        try {
            if (dataset == null) dataset = new TornadoCsvLoader().carregar(ARQUIVO);
        } catch (Exception e) {
            log("ERRO ao ler " + ARQUIVO + ": " + e.getMessage());
            return;
        }
        if (proximo >= dataset.size()) {
            log("Todos os " + Estilo.numero(dataset.size()) + " registros do dataset já foram inseridos.");
            return;
        }
        inserirEmTodas(dataset.get(proximo++));
    }

    private void limpar() {
        arvores = novasArvores();
        proximo = 0;
        tamanho = 0;
        modelo.setRowCount(0);
        areaPercurso.setText("");
        campoChave.setText("");
        log("Árvores esvaziadas.");
        atualizarInfo();
    }

    // =====================================================================
    // Operações
    // =====================================================================

    /** Monta um Tornado com os campos do formulário e insere nas 3 árvores. */
    void inserirManual() {
        int ano = (Integer) spinAno.getValue(), mes = (Integer) spinMes.getValue(), dia = (Integer) spinDia.getValue();
        int hora = (Integer) spinHora.getValue(), minuto = (Integer) spinMinuto.getValue();
        String estado = campoEstado.getText().trim().toUpperCase();

        try {
            LocalDate.of(ano, mes, dia);
        } catch (DateTimeException e) {
            log("Data inválida: " + dia + "/" + mes + "/" + ano + ".");
            return;
        }
        if (!estado.matches("[A-Z]{2}")) {
            log("Estado deve ter 2 letras (ex.: TX, OK, KS).");
            return;
        }

        // chave = aaaaMMddHHmm * 1000 (+ desempate se já existir), igual ao loader
        long base = (((ano * 100L + mes) * 100 + dia) * 100 + hora) * 100 + minuto;
        long id = base * 1000;
        while (arvores.get(0).buscar(id) != null) id++;

        Tornado t = new Tornado(id, ano, mes, dia, estado, (Integer) spinMagnitude.getValue(),
                (Integer) spinFeridos.getValue(), (Integer) spinMortos.getValue(),
                0, 0, 0, 0, 0, 0, 0);
        inserirEmTodas(t);
    }

    private void inserirEmTodas(Tornado t) {
        tamanho++;
        List<Object[]> linhas = new ArrayList<>();
        for (int i = 0; i < arvores.size(); i++) {
            Tree<Long, Tornado> a = arvores.get(i);
            a.resetarContador();
            long inicio = System.nanoTime();
            a.inserir(t.getId(), t);
            long dt = System.nanoTime() - inicio;
            linhas.add(linha(i, "inserido", a.contadorComparacoes(), dt, a.altura()));
        }
        mostrar(linhas);
        campoChave.setText(String.valueOf(t.getId()));
        log("Inserido: " + t + "  (chave " + t.getId() + ")");
        atualizarInfo();
    }

    void buscar() {
        Long chave = lerChave();
        if (chave == null) return;

        List<Object[]> linhas = new ArrayList<>();
        Tornado achado = null;
        for (int i = 0; i < arvores.size(); i++) {
            Tree<Long, Tornado> a = arvores.get(i);
            a.resetarContador();
            long inicio = System.nanoTime();
            Tornado r = a.buscar(chave);
            long dt = System.nanoTime() - inicio;
            if (r != null) achado = r;
            linhas.add(linha(i, r != null ? "encontrado" : "não encontrado", a.contadorComparacoes(), dt, a.altura()));
        }
        mostrar(linhas);
        if (achado == null) {
            log("Busca: chave " + chave + " não existe nas árvores.");
        } else {
            log("Busca: " + detalhes(achado));
        }
    }

    void remover() {
        Long chave = lerChave();
        if (chave == null) return;

        boolean existia = arvores.get(0).buscar(chave) != null;
        List<Object[]> linhas = new ArrayList<>();
        for (int i = 0; i < arvores.size(); i++) {
            Tree<Long, Tornado> a = arvores.get(i);
            a.resetarContador();
            long inicio = System.nanoTime();
            a.remover(chave);
            long dt = System.nanoTime() - inicio;
            linhas.add(linha(i, existia ? "removido" : "não encontrado", a.contadorComparacoes(), dt, a.altura()));
        }
        if (existia) tamanho--;
        for (Object[] l : linhas) l[5] = Estilo.numero(tamanho);
        mostrar(linhas);
        log(existia ? "Removida a chave " + chave + ". As árvores balanceadas foram rebalanceadas."
                    : "Remoção: chave " + chave + " não existe nas árvores.");
        atualizarInfo();
    }

    private void sortearChave() {
        List<Long> chaves = ativa().percorrer("in");
        if (chaves.isEmpty()) {
            log("As árvores estão vazias; carregue o dataset ou insira algo antes.");
            return;
        }
        campoChave.setText(String.valueOf(chaves.get(sorteio.nextInt(chaves.size()))));
    }

    private void percurso(String ordem, String nome) {
        List<Long> chaves = ativa().percorrer(ordem);
        if (chaves.isEmpty()) {
            areaPercurso.setText("Árvore vazia.");
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(nome).append(" (").append(comboArvore.getSelectedItem()).append("): ")
          .append(Estilo.numero(chaves.size())).append(" chaves\n\n");
        int mostrar = Math.min(LIMITE_EXIBICAO, chaves.size());
        for (int i = 0; i < mostrar; i++) {
            if (i > 0) sb.append(", ");
            sb.append(chaves.get(i));
        }
        if (chaves.size() > mostrar) {
            sb.append(", ...  (e mais ").append(Estilo.numero(chaves.size() - mostrar)).append(")");
        }
        areaPercurso.setText(sb.toString());
        areaPercurso.setCaretPosition(0);
    }

    // =====================================================================
    // Apoio
    // =====================================================================

    private Tree<Long, Tornado> ativa() {
        return arvores.get(comboArvore.getSelectedIndex());
    }

    private Long lerChave() {
        try {
            return Long.parseLong(campoChave.getText().trim());
        } catch (NumberFormatException e) {
            log("Digite uma chave numérica (ex.: 202506151430000) ou use \"Escolher uma chave existente\".");
            return null;
        }
    }

    private Object[] linha(int indiceArvore, String resultado, int comparacoes, long nanos, int altura) {
        return new Object[]{Estilo.NOMES[indiceArvore], resultado, Estilo.numero(comparacoes),
                Estilo.numero(nanos / 1000.0), Estilo.numero(altura), Estilo.numero(tamanho)};
    }

    private void mostrar(List<Object[]> linhas) {
        modelo.setRowCount(0);
        for (Object[] l : linhas) modelo.addRow(l);
    }

    private String detalhes(Tornado t) {
        return "chave " + t.getId() + " | " + t.getDia() + "/" + t.getMes() + "/" + t.getAno()
                + " | estado " + t.getEstado() + " | magnitude " + t.getMagnitude()
                + " | feridos " + t.getFeridos() + " | mortos " + t.getMortos();
    }

    private void atualizarInfo() {
        info.setText("Árvore ativa: " + comboArvore.getSelectedItem() + "  ·  "
                + Estilo.numero(tamanho) + " elementos  ·  altura " + ativa().altura());
    }

    private void log(String mensagem) {
        areaLog.append(mensagem + "\n");
        areaLog.setCaretPosition(areaLog.getDocument().getLength());
    }

    private void ocupado(boolean sim) {
        botaoCarregar.setEnabled(!sim);
        botaoProximo.setEnabled(!sim);
        botaoLimpar.setEnabled(!sim);
        setCursor(sim ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) : Cursor.getDefaultCursor());
    }

    private static List<Tree<Long, Tornado>> novasArvores() {
        List<Tree<Long, Tornado>> l = new ArrayList<>();
        l.add(new BSTree<>());
        l.add(new AVLTree<>());
        l.add(new RedBlackTree<>());
        return l;
    }

    // ---------- fábricas de componentes ----------

    private static JSpinner spinner(int valor, int min, int max, int passo) {
        JSpinner s = new JSpinner(new SpinnerNumberModel(valor, min, max, passo));
        s.setEditor(new JSpinner.NumberEditor(s, "#"));   // sem separador de milhar no ano
        return s;
    }

    private static JTextArea areaTexto(int linhas) {
        JTextArea a = new JTextArea(linhas, 40);
        a.setEditable(false);
        a.setLineWrap(true);
        a.setWrapStyleWord(true);
        a.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        return a;
    }

    private static JPanel cartao(String titulo) {
        JPanel p = new JPanel() {
            // não deixa o cartão esticar na vertical: fica do tamanho do conteúdo
            @Override public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
            }
        };
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(Color.WHITE);
        p.setBorder(moldura(titulo));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        return p;
    }

    private static JPanel cartaoComConteudo(String titulo, JComponent conteudo) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(moldura(titulo));
        p.add(conteudo, BorderLayout.CENTER);
        return p;
    }

    private static TitledBorder moldura(String titulo) {
        TitledBorder tb = BorderFactory.createTitledBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(0xE0E3E8)),
                        BorderFactory.createEmptyBorder(6, 8, 8, 8)),
                titulo);
        tb.setTitleFont(Estilo.fonte(Font.BOLD, 14f));
        return tb;
    }

    private static JComponent larguraTotal(JComponent c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, c.getPreferredSize().height));
        return c;
    }

    private static void linha(JPanel form, int linha, String rotulo, JComponent campo) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridy = linha;
        g.insets = new Insets(3, 0, 3, 8);
        g.anchor = GridBagConstraints.WEST;
        g.gridx = 0;
        form.add(new JLabel(rotulo), g);
        g.gridx = 1;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        form.add(campo, g);
    }

    private static JPanel trio(JComponent a, JComponent b, JComponent c) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        p.setOpaque(false);
        p.add(a);
        p.add(b);
        if (c != null) p.add(c);
        return p;
    }
}