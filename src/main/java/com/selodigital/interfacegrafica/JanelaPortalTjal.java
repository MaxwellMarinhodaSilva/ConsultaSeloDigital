package com.selodigital.interfacegrafica;

import com.selodigital.util.PortalTjalUtil;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** Permite escolher uma consulta TJAL por vez, sem consultar dados pelo aplicativo. */
public final class JanelaPortalTjal extends JDialog {

    private final List<String> selos;
    private final JTable tabela;
    private final JButton botaoAbrir;
    private final JButton botaoCopiar;
    private final JLabel feedback;
    private final Supplier<Clipboard> areaTransferencia;

    public JanelaPortalTjal(Frame origem, List<String> codigos, Consumer<String> abrirSelo) {
        this(origem, codigos, abrirSelo,
                () -> Toolkit.getDefaultToolkit().getSystemClipboard());
    }

    JanelaPortalTjal(Frame origem, List<String> codigos, Consumer<String> abrirSelo,
                    Supplier<Clipboard> areaTransferencia) {
        super(origem, "Consultas TJAL", Dialog.ModalityType.DOCUMENT_MODAL);
        this.areaTransferencia = areaTransferencia;

        selos = codigos.stream()
                .map(String::trim)
                .filter(codigo -> !codigo.isBlank())
                .toList();

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        DefaultTableModel modelo = new DefaultTableModel(
                new Object[]{"Selo", "Validação local"}, 0
        ) {
            @Override
            public boolean isCellEditable(int linha, int coluna) {
                return false;
            }
        };
        for (String selo : selos) {
            modelo.addRow(new Object[]{
                    selo,
                    formatoAceito(selo) ? "Formato aceito" : "Formato inválido"
            });
        }

        tabela = new JTable(modelo);
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.setRowHeight(Math.max(tabela.getRowHeight(), 28));
        tabela.setFillsViewportHeight(true);

        JLabel explicacao = new JLabel(
                "As consultas serão abertas no portal oficial do TJAL; os resultados aparecerão no navegador."
        );
        explicacao.setFont(explicacao.getFont().deriveFont(Font.PLAIN, 12f));

        botaoAbrir = new JButton("Abrir no TJAL");
        botaoCopiar = new JButton("Copiar código");
        JButton botaoFechar = new JButton("Fechar");
        feedback = new JLabel(" ");
        feedback.setFont(feedback.getFont().deriveFont(Font.PLAIN, 11f));

        botaoAbrir.setEnabled(false);
        botaoCopiar.setEnabled(false);
        tabela.getSelectionModel().addListSelectionListener(e -> {
            int linha = tabela.getSelectedRow();
            boolean selecionado = linha >= 0;
            botaoCopiar.setEnabled(selecionado);
            botaoAbrir.setEnabled(selecionado && formatoAceito(selos.get(linha)));
        });

        botaoAbrir.addActionListener(e -> {
            int linha = tabela.getSelectedRow();
            if (linha >= 0 && formatoAceito(selos.get(linha))) {
                abrirSelo.accept(selos.get(linha));
            }
        });
        botaoCopiar.addActionListener(e -> copiarCodigoSelecionado());
        botaoFechar.addActionListener(e -> dispose());

        JPanel acoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        acoes.add(botaoCopiar);
        acoes.add(botaoAbrir);
        acoes.add(botaoFechar);

        JPanel rodape = new JPanel(new BorderLayout());
        rodape.add(feedback, BorderLayout.WEST);
        rodape.add(acoes, BorderLayout.EAST);

        JPanel conteudo = new JPanel(new BorderLayout(0, 12));
        conteudo.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        conteudo.add(explicacao, BorderLayout.NORTH);
        conteudo.add(new JScrollPane(tabela), BorderLayout.CENTER);
        conteudo.add(rodape, BorderLayout.SOUTH);
        setContentPane(conteudo);

        setMinimumSize(new Dimension(640, 350));
        setSize(new Dimension(720, 420));
        setLocationRelativeTo(origem);
    }

    private static boolean formatoAceito(String selo) {
        try {
            PortalTjalUtil.criarUrlConsulta(selo);
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    private void copiarCodigoSelecionado() {
        int linha = tabela.getSelectedRow();
        if (linha < 0) {
            return;
        }
        try {
            areaTransferencia.get().setContents(
                    new StringSelection(selos.get(linha)), null
            );
            feedback.setText("Código copiado.");
        } catch (IllegalStateException | SecurityException ex) {
            feedback.setText("Não foi possível copiar o código.");
        }
    }
}
