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

/** Apresenta os resultados do TJRN e abre os detalhes do selo correspondente à linha visual. */
public class JanelaResultadosTJRN
        extends JanelaBaseResultados {

    // =======================================================
    // 1. ESTRUTURA DA CLASSE
    // =======================================================

    //region [Membros da Classe]

    private JTable tabela;

    public JanelaResultadosTJRN(
            Tribunal tribunal,
            List<ResultadoConsulta> resultados) {

        super(
                tribunal,
                resultados
        );

        setSize(
                1000,
                550
        );

        setMinimumSize(
                new Dimension(
                        900,
                        450
                )
        );

        setLocationRelativeTo(null);
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
     * TABELA ESPECÍFICA DO TJRN
     * ==================================================
     */

    @Override
    protected JScrollPane criarTabela() {

        String[] colunas = {
                "#",
                "Número do Selo",
                "Status",
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

                        return column == 3;
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
                3
        );

        /*
         * ==================================================
         * CENTRALIZAÇÃO DAS INFORMAÇÕES
         * ==================================================
         */

        DefaultTableCellRenderer renderizadorCentralizado =
                new DefaultTableCellRenderer();

        renderizadorCentralizado.setHorizontalAlignment(
                SwingConstants.CENTER
        );

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

        tabela.getTableHeader()
                .setReorderingAllowed(false);

        tabela.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        /*
         * ==================================================
         * LARGURA DA COLUNA #
         * ==================================================
         */

        tabela.getColumnModel()
                .getColumn(0)
                .setPreferredWidth(45);

        tabela.getColumnModel()
                .getColumn(0)
                .setMinWidth(45);

        tabela.getColumnModel()
                .getColumn(0)
                .setMaxWidth(45);

        /*
         * ==================================================
         * LARGURA DAS DEMAIS COLUNAS
         * ==================================================
         */

        tabela.getColumnModel()
                .getColumn(1)
                .setPreferredWidth(200);

        tabela.getColumnModel()
                .getColumn(2)
                .setPreferredWidth(180);

        tabela.getColumnModel()
                .getColumn(3)
                .setPreferredWidth(130);

        tabela.getColumnModel()
                .getColumn(3)
                .setMinWidth(130);

        tabela.getColumnModel()
                .getColumn(3)
                .setMaxWidth(130);

        /*
         * ==================================================
         * COLUNA DETALHES
         * ==================================================
         */

        tabela.getColumnModel()
                .getColumn(3)
                .setCellRenderer(
                        new RenderizadorBotaoDetalhes()
                );

        tabela.getColumnModel()
                .getColumn(3)
                .setCellEditor(
                        new EditorBotaoDetalhes()
                );

        /*
         * ==================================================
         * TOOLTIP E CURSOR DO BOTÃO
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

                        if (linha >= 0 && coluna == 3) {

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
         */

        InputMap inputMap =
                tabela.getInputMap(
                        JComponent.WHEN_FOCUSED
                );

        ActionMap actionMap =
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
