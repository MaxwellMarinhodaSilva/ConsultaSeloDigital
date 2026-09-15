package com.selodigital.interfacegrafica;

import com.formdev.flatlaf.FlatClientProperties;
import com.selodigital.modelo.DetalhesSelo;
import com.selodigital.modelo.ResultadoConsulta;
import com.selodigital.modelo.Tribunal;
import com.selodigital.modelo.StatusConsulta;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Estrutura comum das janelas de resultado. Mantém os dados originais para
 * exportação e fornece a ponte segura entre uma linha ordenada da tabela e
 * seu respectivo {@link ResultadoConsulta}.
 */
public abstract class JanelaBaseResultados
        extends JanelaBase {

    // =======================================================
    // 1. ESTRUTURA DA CLASSE
    // =======================================================

    //region [Membros da Classe]

    protected final Tribunal tribunal;

    protected final List<ResultadoConsulta> resultados;

    private JTable tabelaResultados;
    private TableRowSorter<DefaultTableModel> ordenacaoResultados;
    private JTextField campoFiltro;
    private JComboBox<String> comboStatus;
    private JLabel indicadorRegistros;

    protected JanelaBaseResultados(
            Tribunal tribunal,
            List<ResultadoConsulta> resultados) {

        this.tribunal = tribunal;
        this.resultados = resultados;

        setTitle(
                "Consulta Realizada: " + tribunal
        );

        setDefaultCloseOperation(
                JFrame.DISPOSE_ON_CLOSE
        );

        configurarTamanhoMinimo(
                1100,
                450
        );

        setSize(
                1450,
                600
        );

        setLocationRelativeTo(null);

        criarInterface();
    }

    private void criarInterface() {

        JPanel painelPrincipal =
                new JPanel(
                        new BorderLayout(
                                10,
                                10
                        )
                );

        painelPrincipal.add(
                criarCabecalho(),
                BorderLayout.NORTH
        );

        JScrollPane tabela = criarTabela();

        painelPrincipal.add(
                criarAreaResultados(tabela),
                BorderLayout.CENTER
        );

        configurarConteudo(
                painelPrincipal
        );

        configurarRodape(
                criarBotoes()
        );
    }

    private JPanel criarCabecalho() {

        JPanel painel =
                new JPanel(
                        new BorderLayout()
                );

        JLabel titulo =
                new JLabel(
                        "Consulta Realizada: "
                                + tribunal,
                        SwingConstants.CENTER
                );

        titulo.setFont(
                titulo.getFont().deriveFont(
                        Font.BOLD,
                        22f
                )
        );

        painel.add(
                titulo,
                BorderLayout.CENTER
        );

        return painel;
    }

    /**
     * Cada tribunal é responsável por construir
     * sua própria tabela.
     */
    protected abstract JScrollPane criarTabela();

    /*
     * ==================================================
     * FILTRO E INDICADOR DE REGISTROS
     * ==================================================
     *
     * A tabela específica é criada pelo tribunal, mas o filtro é comum porque
     * todas usam o sorter configurado nesta classe. O filtro atua somente na
     * visualização e não modifica a lista original de resultados.
     * ==================================================
     */
    private JPanel criarAreaResultados(
            JScrollPane tabela) {

        JPanel painel =
                new JPanel(
                        new BorderLayout(
                                0,
                                10
                        )
                );

        JPanel painelFiltro =
                new JPanel(
                        new BorderLayout(
                                10,
                                0
                        )
                );

        JLabel labelFiltro =
                new JLabel(
                        "Filtrar:"
                );

        JLabel labelStatus = new JLabel("Status:");

        comboStatus = new JComboBox<>();
        comboStatus.addItem("Todos");
        for (StatusConsulta status : StatusConsulta.values()) {
            comboStatus.addItem(status.getDescricao());
        }

        campoFiltro =
                new JTextField();

        campoFiltro.setPreferredSize(
                new Dimension(
                        320,
                        32
                )
        );

        campoFiltro.putClientProperty(
                FlatClientProperties.PLACEHOLDER_TEXT,
                "Pesquisar nos resultados..."
        );
        campoFiltro.getInputMap(JComponent.WHEN_FOCUSED).put(
                KeyStroke.getKeyStroke("DOWN"),
                "selecionarPrimeiroResultado"
        );
        campoFiltro.getActionMap().put(
                "selecionarPrimeiroResultado",
                new AbstractAction() {
                    @Override
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        if (tabelaResultados.getRowCount() > 0) {
                            tabelaResultados.setRowSelectionInterval(0, 0);
                            tabelaResultados.requestFocusInWindow();
                        }
                    }
                }
        );

        indicadorRegistros =
                new JLabel(
                        ""
                );

        indicadorRegistros.setHorizontalAlignment(
                SwingConstants.RIGHT
        );

        painelFiltro.add(
                labelFiltro,
                BorderLayout.WEST
        );

        JPanel filtros = new JPanel(new BorderLayout(10, 0));
        filtros.add(campoFiltro, BorderLayout.CENTER);
        JPanel filtroStatus = new JPanel(new BorderLayout(6, 0));
        filtroStatus.add(labelStatus, BorderLayout.WEST);
        filtroStatus.add(comboStatus, BorderLayout.CENTER);
        filtros.add(filtroStatus, BorderLayout.EAST);
        painelFiltro.add(filtros, BorderLayout.CENTER);

        painelFiltro.add(
                indicadorRegistros,
                BorderLayout.EAST
        );

        configurarFiltro();
        atualizarIndicadorRegistros();

        painel.add(
                painelFiltro,
                BorderLayout.NORTH
        );

        painel.add(
                tabela,
                BorderLayout.CENTER
        );

        return painel;
    }

    private void configurarFiltro() {

        campoFiltro.getDocument().addDocumentListener(
                new DocumentListener() {

                    @Override
                    public void insertUpdate(
                            DocumentEvent e) {

                        aplicarFiltro();
                    }

                    @Override
                    public void removeUpdate(
                            DocumentEvent e) {

                        aplicarFiltro();
                    }

                    @Override
                    public void changedUpdate(
                            DocumentEvent e) {

                        aplicarFiltro();
                    }
                }
        );

        ordenacaoResultados.addRowSorterListener(
                e -> atualizarIndicadorRegistros()
        );

        comboStatus.addActionListener(e -> aplicarFiltro());
    }

    private void aplicarFiltro() {

        String texto =
                campoFiltro.getText().trim();

        java.util.List<RowFilter<DefaultTableModel, Integer>> filtros =
                new java.util.ArrayList<>();

        if (!texto.isBlank()) {
            filtros.add(RowFilter.regexFilter("(?i)" + Pattern.quote(texto)));
        }

        String statusSelecionado = (String) comboStatus.getSelectedItem();
        if (statusSelecionado != null && !"Todos".equals(statusSelecionado)) {
            filtros.add(RowFilter.regexFilter(
                    "(?i)^" + Pattern.quote(statusSelecionado) + "$",
                    encontrarColunaStatus()
            ));
        }

        ordenacaoResultados.setRowFilter(
                filtros.isEmpty() ? null : RowFilter.andFilter(filtros)
        );

        atualizarIndicadorRegistros();
    }

    private void atualizarIndicadorRegistros() {

        if (indicadorRegistros == null
                || tabelaResultados == null) {

            return;
        }

        indicadorRegistros.setText(
                "Exibindo "
                        + tabelaResultados.getRowCount()
                        + " de "
                        + resultados.size()
                        + " registros"
        );
    }

    /*
     * ==================================================
     * ORDENAÇÃO DA TABELA
     * ==================================================
     *
     * O sorter reorganiza apenas a visualização. O modelo preserva a ordem
     * original de "resultados", que também é usada pela exportação. A coluna
     * de detalhes é excluída porque contém somente o botão de ação.
     * ==================================================
     */
    protected final void configurarOrdenacao(
            JTable tabela,
            DefaultTableModel modelo,
            int colunaDetalhes) {

        TableRowSorter<DefaultTableModel> ordenacao =
                new TableRowSorter<>(modelo);

        ordenacao.setSortable(colunaDetalhes, false);

        /* A coluna "#" representa uma sequência numérica, não texto. */
        ordenacao.setComparator(
                0,
                Comparator.comparingInt(valor -> Integer.parseInt(valor.toString()))
        );

        tabela.setRowSorter(ordenacao);

        tabelaResultados = tabela;
        ordenacaoResultados = ordenacao;
    }

    /**
     * Converte a linha da tela para a linha original do modelo antes de abrir
     * detalhes. Sem essa conversão, uma ordenação poderia abrir outro selo.
     */
    protected final ResultadoConsulta obterResultadoDaLinha(
            JTable tabela,
            int linhaVisual) {

        if (linhaVisual < 0) {
            return null;
        }

        int linhaModelo =
                tabela.convertRowIndexToModel(linhaVisual);

        if (linhaModelo < 0 || linhaModelo >= resultados.size()) {
            return null;
        }

        return resultados.get(linhaModelo);
    }

    protected final List<ResultadoConsulta> obterResultadosFiltrados() {
        List<ResultadoConsulta> filtrados = new java.util.ArrayList<>();
        if (tabelaResultados == null) {
            return filtrados;
        }
        for (int linha = 0; linha < tabelaResultados.getRowCount(); linha++) {
            ResultadoConsulta resultado = obterResultadoDaLinha(tabelaResultados, linha);
            if (resultado != null) {
                filtrados.add(resultado);
            }
        }
        return filtrados;
    }

    private int encontrarColunaStatus() {
        for (int coluna = 0; coluna < tabelaResultados.getColumnCount(); coluna++) {
            if ("Status".equals(tabelaResultados.getColumnName(coluna))) {
                return tabelaResultados.convertColumnIndexToModel(coluna);
            }
        }
        return 2;
    }

    /**
     * Abre a janela de detalhes de um resultado.
     *
     * A lógica é comum para todos os tribunais.
     */
    protected void abrirDetalhes(
            ResultadoConsulta resultado) {

        if (resultado == null) {
            return;
        }

        DetalhesSelo detalhes =
                new DetalhesSelo(
                        resultado.getSelo(),
                        resultado.getTribunal(),
                        resultado.getStatus(),
                        resultado.getCampos(),
                        resultado.getQrCodeConteudo()
                );

        JanelaDetalhes janela =
                new JanelaDetalhes(
                        detalhes
                );

        janela.posicionarNoMonitorDa(this);
        janela.setVisible(true);
    }

    protected JPanel criarBotoes() {

        JPanel painel =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.RIGHT
                        )
                );

        JButton botaoExportar =
                new JButton(
                        "Exportar ▾",
                        com.selodigital.util.IconeUtil.baixar()
                );

        botaoExportar.setPreferredSize(
                new Dimension(
                        140,
                        35
                )
        );

        JPopupMenu menuExportar =
                new JPopupMenu();

        JMenuItem exportarPdf =
                new JMenuItem("Exportar em PDF");

        JMenuItem exportarXlsx =
                new JMenuItem("Exportar em XLSX");

        exportarPdf.addActionListener(e ->
                com.selodigital.exportacao.ExportadorConsulta
                        .exportarResultados(this, tribunal, obterResultadosFiltrados(), true));

        exportarXlsx.addActionListener(e ->
                com.selodigital.exportacao.ExportadorConsulta
                        .exportarResultados(this, tribunal, obterResultadosFiltrados(), false));

        menuExportar.add(exportarPdf);
        menuExportar.add(exportarXlsx);

        botaoExportar.addActionListener(e ->
                menuExportar.show(botaoExportar, 0, botaoExportar.getHeight()));

        JButton botaoFechar =
                new JButton(
                        "Fechar",
                        com.selodigital.util.IconeUtil.fechar()
                );

        botaoFechar.setPreferredSize(
                new Dimension(
                        140,
                        35
                )
        );

        botaoFechar.addActionListener(
                e -> dispose()
        );

        JButton botaoCopiarNumeroSelo = new JButton(
                "Copiar N\u00famero do Selo",
                com.selodigital.util.IconeUtil.copiar()
        );
        botaoCopiarNumeroSelo.setPreferredSize(new Dimension(190, 35));
        botaoCopiarNumeroSelo.setEnabled(false);
        tabelaResultados.getSelectionModel().addListSelectionListener(e ->
                botaoCopiarNumeroSelo.setEnabled(tabelaResultados.getSelectedRow() >= 0));
        botaoCopiarNumeroSelo.addActionListener(e -> copiarNumeroSeloSelecionado());

        painel.add(botaoExportar);

        painel.add(botaoCopiarNumeroSelo);

        painel.add(
                botaoFechar
        );

        return painel;
    }

    private void copiarNumeroSeloSelecionado() {
        ResultadoConsulta resultado = obterResultadoDaLinha(
                tabelaResultados, tabelaResultados.getSelectedRow());
        if (resultado == null) {
            return;
        }
        String numeroSelo = resultado.getSelo().getNumero();
        if (tribunal == Tribunal.TJPE) {
            String numeroExibido = resultado.getCampos().get("N\u00famero do Selo");
            if (numeroExibido != null && !numeroExibido.isBlank()) {
                numeroSelo = numeroExibido.trim();
            }
        }
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
                new StringSelection(numeroSelo), null);
    }

    //endregion
}
