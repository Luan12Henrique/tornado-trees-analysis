package gui;

import javax.swing.*;
import java.awt.*;

/**
 * Arquivo: src/gui/JanelaPrincipal.java
 *
 * Janela principal da aplicação (Swing), com duas abas:
 *   - "Operações": inserir, buscar e remover nas 3 árvores, com percursos e altura
 *   - "Gráficos": comparação de desempenho e balanceamento (lê os CSVs de results/)
 *
 * Para abrir: java -cp out gui.JanelaPrincipal   (rode o Main antes, para
 * gerar a pasta results/).
 */
public class JanelaPrincipal extends JFrame {

    public JanelaPrincipal() {
        super("Árvores de busca × Tornados — BST, AVL e Rubro-Negra");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JTabbedPane abas = new JTabbedPane();
        abas.addTab("Operações", new PainelOperacoes());
        abas.addTab("Gráficos", new PainelGraficos());
        add(abas, BorderLayout.CENTER);

        setSize(1200, 780);
        setMinimumSize(new Dimension(1100, 650));
        setLocationRelativeTo(null);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignorado) {
                // segue com o visual padrão do Swing
            }
            new JanelaPrincipal().setVisible(true);
        });
    }
}