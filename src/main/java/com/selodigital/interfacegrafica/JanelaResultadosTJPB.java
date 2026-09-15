package com.selodigital.interfacegrafica;

import com.selodigital.modelo.ResultadoConsulta;
import com.selodigital.modelo.Tribunal;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.List;


/** Apresenta os resultados do TJPB mantendo a ação de detalhes fora da ordenação da tabela. */
public class JanelaResultadosTJPB
        extends JanelaBaseResultados {

    // =======================================================
    // 1. ESTRUTURA DA CLASSE
    // =======================================================

    //region [Membros da Classe]

    private JTable tabela;

    public JanelaResultadosTJPB(
            Tribunal tribunal,
            List<ResultadoConsulta> resultados) {

        super(
                tribunal,
                resultados
        );
    }

    private String valorTabela(
            String valor) {

        if (valor == null || valor.isBlank()) {

            return "-";
        }

        return valor.trim();
    }

    /*
     * ==================================================
     * RENDERIZADOR DO NÚMERO DO SELO
     * ==================================================
     *
     * O número do selo continua sendo apresentado
     * normalmente na tabela.
     *
     * A ação principal de abertura dos detalhes passa
     * a ser feita pelo botão "Detalhes".
     * ==================================================
     */

    private static class RenderizadorCentralizado
            extends DefaultTableCellRenderer {

        public RenderizadorCentralizado() {

            setHorizontalAlignment(
                    SwingConstants.CENTER
            );
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable tabela,
                Object valor,
                boolean selecionado,
                boolean foco,
                int linha,
                int coluna) {

            JLabel label =
                    (JLabel) super.getTableCellRendererComponent(
                            tabela,
                            valor,
                            selecionado,
                            foco,
                            linha,
                            coluna
                    );

            label.setHorizontalAlignment(
                    SwingConstants.CENTER
            );

            label.setText(
                    valor == null
                            ? ""
                            : valor.toString()
            );

            return label;
        }
    }

    /*
     * ==================================================
     * RENDERIZADOR DO BOTÃO DETALHES
     * ==================================================
     */

    private static class RenderizadorBotaoDetalhes
            extends JButton
            implements TableCellRenderer {

        public RenderizadorBotaoDetalhes() {

            setOpaque(true);

            setHorizontalAlignment(
                    SwingConstants.CENTER
            );

            setCursor(
                    Cursor.getPredefinedCursor(
                            Cursor.HAND_CURSOR
                    )
            );
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable tabela,
                Object valor,
                boolean selecionado,
                boolean foco,
                int linha,
                int coluna) {

            setText(
                    "🔎 Detalhes"
            );

            setCursor(
                    Cursor.getPredefinedCursor(
                            Cursor.HAND_CURSOR
                    )
            );

            if (selecionado) {

                setBackground(
                        tabela.getSelectionBackground()
                );

                setForeground(
                        tabela.getSelectionForeground()
                );

            } else {

                setBackground(
                        UIManager.getColor(
                                "Button.background"
                        )
                );

                setForeground(
                        UIManager.getColor(
                                "Button.foreground"
                        )
                );
            }

            return this;
        }
    }

    /*
     * ==================================================
     * EDITOR DO BOTÃO DETALHES
     * ==================================================
     *
     * O editor transforma a célula em um botão real.
     *
     * Quando o usuário clicar:
     *
     *     [ 🔎 Detalhes ]
     *
     * abrimos os detalhes da linha correspondente.
     * ==================================================
     */

    private class EditorBotaoDetalhes
            extends AbstractCellEditor
            implements TableCellEditor {

        private final JButton botao;

        private int linhaAtual;

        public EditorBotaoDetalhes() {

            botao =
                    new JButton(
                            "🔎 Detalhes"
                    );

            botao.setHorizontalAlignment(
                    SwingConstants.CENTER
            );

            botao.setCursor(
                    Cursor.getPredefinedCursor(
                            Cursor.HAND_CURSOR
                    )
            );

            botao.addActionListener(
                    e -> {

                        fireEditingStopped();

                        abrirDetalhesDaLinha(
                                linhaAtual
                        );
                    }
            );
        }

        @Override
        public Component getTableCellEditorComponent(
                JTable tabela,
                Object valor,
                boolean selecionado,
                int linha,
                int coluna) {

            linhaAtual = linha;

            botao.setText(
                    "🔎 Detalhes"
            );

            botao.setCursor(
                    Cursor.getPredefinedCursor(
                            Cursor.HAND_CURSOR
                    )
            );

            return botao;
        }

        @Override
        public Object getCellEditorValue() {

            return "🔎 Detalhes";
        }
    }

    /*
     * ==================================================
     * TABELA ESPECÍFICA DO TJPB
     * ==================================================
     */

    @Override
    protected JScrollPane criarTabela() {

        String[] colunas = {
                "#",
                "Número do Selo",
                "Status",
                "Matrícula/Protocolo",
                "Tipo de Ato",
                "Subtipo de Ato",
                "Detalhes"
        };

        DefaultTableModel modelo =
                new DefaultTableModel(
                        colunas,
                        0
                ) {

                    @Override
                    public boolean isCellEditable(
                            int row,
                            int column) {

                        /*
                         * Somente a coluna "Detalhes"
                         * possui comportamento de botão.
                         */

                        return column == 6;
                    }
                };

        int indice = 1;

        for (
                ResultadoConsulta resultado
                : resultados
        ) {

            modelo.addRow(
                    new Object[]{
                            indice++,
                            resultado.getSelo().getNumero(),
                            resultado.getStatus().getDescricao(),
                            valorTabela(
                                    resultado.getMatriculaProtocolo()
                            ),
                            valorTabela(
                                    resultado.getTipoAto()
                            ),
                            valorTabela(
                                    resultado.getSubtipoAto()
                            ),
                            "🔎 Detalhes"
                    }
            );
        }

        tabela =
                new JTable(modelo);

        tabela.setRowHeight(32);

        /* Mantém o botão Detalhes fora da ordenação e preserva o modelo original. */
        configurarOrdenacao(
                tabela,
                modelo,
                6
        );

        tabela.getTableHeader()
                .setReorderingAllowed(false);

        tabela.setSelectionMode(
                javax.swing.ListSelectionModel.SINGLE_SELECTION
        );

        /*
         * ==================================================
         * CENTRALIZAÇÃO DAS INFORMAÇÕES
         * ==================================================
         */

        RenderizadorCentralizado renderizadorCentralizado =
                new RenderizadorCentralizado();

        tabela.getColumnModel()
                .getColumn(0)
                .setCellRenderer(
                        renderizadorCentralizado
                );

        tabela.getColumnModel()
                .getColumn(1)
                .setCellRenderer(
                        renderizadorCentralizado
                );

        tabela.getColumnModel()
                .getColumn(2)
                .setCellRenderer(
                        renderizadorCentralizado
                );

        tabela.getColumnModel()
                .getColumn(3)
                .setCellRenderer(
                        renderizadorCentralizado
                );

        tabela.getColumnModel()
                .getColumn(4)
                .setCellRenderer(
                        renderizadorCentralizado
                );

        tabela.getColumnModel()
                .getColumn(5)
                .setCellRenderer(
                        renderizadorCentralizado
                );

        /*
         * ==================================================
         * COLUNA DETALHES
         * ==================================================
         */

        tabela.getColumnModel()
                .getColumn(6)
                .setCellRenderer(
                        new RenderizadorBotaoDetalhes()
                );

        tabela.getColumnModel()
                .getColumn(6)
                .setCellEditor(
                        new EditorBotaoDetalhes()
                );

        /*
         * ==================================================
         * LARGURAS DAS COLUNAS
         * ==================================================
         */

        tabela.getColumnModel()
                .getColumn(0)
                .setPreferredWidth(45);

        tabela.getColumnModel()
                .getColumn(1)
                .setPreferredWidth(160);

        tabela.getColumnModel()
                .getColumn(2)
                .setPreferredWidth(120);

        tabela.getColumnModel()
                .getColumn(3)
                .setPreferredWidth(190);

        tabela.getColumnModel()
                .getColumn(4)
                .setPreferredWidth(300);

        tabela.getColumnModel()
                .getColumn(5)
                .setPreferredWidth(230);

        tabela.getColumnModel()
                .getColumn(6)
                .setPreferredWidth(130);

        /*
         * ==================================================
         * TOOLTIP DO BOTÃO
         * ==================================================
         */

        tabela.addMouseMotionListener(
                new java.awt.event.MouseMotionAdapter() {

                    @Override
                    public void mouseMoved(
                            MouseEvent e) {

                        int linha =
                                tabela.rowAtPoint(
                                        e.getPoint()
                                );

                        int coluna =
                                tabela.columnAtPoint(
                                        e.getPoint()
                                );

                        if (linha >= 0 && coluna == 6) {

                            tabela.setCursor(
                                    Cursor.getPredefinedCursor(
                                            Cursor.HAND_CURSOR
                                    )
                            );

                            tabela.setToolTipText(
                                    "Clique para visualizar os detalhes do selo."
                            );

                        } else {

                            tabela.setCursor(
                                    Cursor.getDefaultCursor()
                            );

                            tabela.setToolTipText(
                                    null
                            );
                        }
                    }
                }
        );

        /*
         * ==================================================
         * ENTER ABRE OS DETALHES
         * ==================================================
         *
         * O Enter continua funcionando mesmo sem o usuário
         * clicar no botão.
         * ==================================================
         */

        InputMap inputMap =
                tabela.getInputMap(
                        JComponent.WHEN_FOCUSED
                );

        javax.swing.ActionMap actionMap =
                tabela.getActionMap();

        inputMap.put(
                KeyStroke.getKeyStroke(
                        "ENTER"
                ),
                "abrirDetalhesSelo"
        );

        actionMap.put(
                "abrirDetalhesSelo",
                new AbstractAction() {

                    @Override
                    public void actionPerformed(
                            ActionEvent e) {

                        int linhaSelecionada =
                                tabela.getSelectedRow();

                        if (linhaSelecionada < 0) {

                            return;
                        }

                        abrirDetalhesDaLinha(
                                linhaSelecionada
                        );
                    }
                }
        );

        return new JScrollPane(
                tabela
        );
    }

    /*
     * ==================================================
     * ABRIR DETALHES DA LINHA
     * ==================================================
     */

    private void abrirDetalhesDaLinha(
            int linha) {

        /* A conversão de linha evita associar um selo incorreto após ordenar a tabela. */
        ResultadoConsulta resultado =
                obterResultadoDaLinha(
                        tabela,
                        linha
                );

        abrirDetalhes(
                resultado
        );
    }

    //endregion
}
