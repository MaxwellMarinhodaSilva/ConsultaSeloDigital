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
import java.util.List;

/** Tabela de resultados do TJPE, cujo identificador consultado é o hash. */
public class JanelaResultadosTJPE extends JanelaBaseResultados {

    // =======================================================
    // 1. ESTRUTURA DA CLASSE
    // =======================================================

    //region [Membros da Classe]

    private JTable tabela;

    public JanelaResultadosTJPE(Tribunal tribunal, List<ResultadoConsulta> resultados) {
        super(tribunal, resultados);
        setSize(1300, 550);
    }

    @Override
    protected JScrollPane criarTabela() {
        String[] colunas = {"#", "Número do Selo", "Status", "Ato", "Detalhes"};
        DefaultTableModel modelo = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int linha, int coluna) {
                return coluna == 4;
            }
        };

        int indice = 1;
        for (ResultadoConsulta resultado : resultados) {
            modelo.addRow(new Object[]{
                    indice++,
                    valor(resultado, "Número do Selo"),
                    resultado.getStatus().getDescricao(),
                    valor(resultado, "Ato"),
                    "🔎 Detalhes"
            });
        }

        tabela = new JTable(modelo);
        tabela.setRowHeight(32);
        /* Detalhes é uma ação; as demais colunas alternam ordem pelo cabeçalho. */
        configurarOrdenacao(tabela, modelo, 4);
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.getTableHeader().setReorderingAllowed(false);

        DefaultTableCellRenderer centralizado = new DefaultTableCellRenderer();
        centralizado.setHorizontalAlignment(SwingConstants.CENTER);
        for (int coluna = 0; coluna < 4; coluna++) {
            tabela.getColumnModel().getColumn(coluna).setCellRenderer(centralizado);
        }
        tabela.getColumnModel().getColumn(4).setCellRenderer(new RenderizadorDetalhes());
        tabela.getColumnModel().getColumn(4).setCellEditor(new EditorDetalhes());

        tabela.getColumnModel().getColumn(0).setMaxWidth(45);
        tabela.getColumnModel().getColumn(0).setMinWidth(45);
        tabela.getColumnModel().getColumn(1).setPreferredWidth(210);
        tabela.getColumnModel().getColumn(2).setPreferredWidth(110);
        tabela.getColumnModel().getColumn(3).setPreferredWidth(210);
        tabela.getColumnModel().getColumn(4).setMinWidth(125);
        tabela.getColumnModel().getColumn(4).setMaxWidth(125);

        tabela.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ENTER"), "detalhes");
        ActionMap actionMap = tabela.getActionMap();
        actionMap.put("detalhes", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                abrirDetalhesDaLinha(tabela.getSelectedRow());
            }
        });
        return new JScrollPane(tabela);
    }

    private String valor(ResultadoConsulta resultado, String campo) {
        String valor = resultado.getCampos().get(campo);
        return valor == null || valor.isBlank() ? "-" : valor.trim();
    }

    private void abrirDetalhesDaLinha(int linha) {
        /* A linha visual é convertida para o modelo pela classe base. */
        abrirDetalhes(obterResultadoDaLinha(tabela, linha));
    }

    private static class RenderizadorDetalhes extends JButton implements TableCellRenderer {
        private RenderizadorDetalhes() {
            setHorizontalAlignment(SwingConstants.CENTER);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        public Component getTableCellRendererComponent(JTable tabela, Object valor,
                                                       boolean selecionado, boolean foco, int linha, int coluna) {
            setText("🔎 Detalhes");
            return this;
        }
    }

    private class EditorDetalhes extends AbstractCellEditor implements TableCellEditor {
        private final JButton botao = new JButton("🔎 Detalhes");
        private int linha;

        private EditorDetalhes() {
            botao.addActionListener(e -> {
                fireEditingStopped();
                abrirDetalhesDaLinha(linha);
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable tabela, Object valor,
                                                     boolean selecionado, int linha, int coluna) {
            this.linha = linha;
            return botao;
        }

        @Override
        public Object getCellEditorValue() {
            return "🔎 Detalhes";
        }
    }

    //endregion
}
