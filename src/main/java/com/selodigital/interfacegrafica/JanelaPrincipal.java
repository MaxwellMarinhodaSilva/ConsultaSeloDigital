package com.selodigital.interfacegrafica;

import com.formdev.flatlaf.FlatClientProperties;
import com.selodigital.config.Configuracao;
import com.selodigital.config.ConfiguracaoService;
import com.selodigital.consulta.ConsultaSeloService;
import com.selodigital.consulta.ConsultaTribunal;
import com.selodigital.consulta.TribunalFactory;
import com.selodigital.modelo.ResultadoConsulta;
import com.selodigital.modelo.Selo;
import com.selodigital.modelo.Tribunal;
import com.selodigital.util.SeloUtil;
import javax.swing.ImageIcon;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import javax.swing.SwingUtilities;

/** Janela inicial que valida selos, dispara a consulta assíncrona e abre os resultados. */
public class JanelaPrincipal extends JanelaBase {

    // =======================================================
    // 1. ESTRUTURA DA CLASSE
    // =======================================================

    //region [Membros da Classe]

    private JComboBox<Tribunal> comboTribunal;
    private JTextArea campoSelos;
    private JLabel labelSelos;

    private JButton botaoConsultar;
    private JButton botaoLimpar;
    private JButton botaoHistorico;
    private JButton botaoImportar;

    /*
     * ==================================================
     * LOGO DO TRIBUNAL SELECIONADO
     * ==================================================
     */

    private JLabel labelLogoTribunal;

    private Configuracao configuracao;

    public JanelaPrincipal() {

        setTitle("Consulta Selo Digital");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        /*
         * O tamanho preferido do formulário é próximo de 750 x 526. A margem
         * adicional preserva a área de entrada dos selos e os controles quando
         * a janela for reduzida, sem interferir no tamanho restaurado pelo usuário.
         */
        configurarTamanhoMinimo(
                800,
                560
        );

        carregarConfiguracaoJanela();

        configurarConteudo(
                criarPainelPrincipal()
        );

        atualizarLogoTribunal();
        atualizarLabelSelos();

        configurarEventos();

        configurarSalvamentoAoFechar();
    }

    private void carregarConfiguracaoJanela() {

        try {

            configuracao =
                    ConfiguracaoService.carregar();

        } catch (IOException ex) {

            configuracao =
                    new Configuracao();

        }

        setSize(
                configuracao.getJanelaPrincipalLargura(),
                configuracao.getJanelaPrincipalAltura()
        );

        if (configuracao.getJanelaPrincipalX() >= 0
                && configuracao.getJanelaPrincipalY() >= 0) {

            setLocation(
                    configuracao.getJanelaPrincipalX(),
                    configuracao.getJanelaPrincipalY()
            );

        } else {

            setLocationRelativeTo(null);
        }
    }

    private JPanel criarPainelPrincipal() {

        JPanel painel =
                new JPanel(
                        new BorderLayout(
                                15,
                                15
                        )
                );

        JPanel painelCabecalho =
                criarCabecalho();

        JPanel painelFormulario =
                criarFormulario();

        JPanel painelBotoes =
                criarBotoes();

        painel.add(
                painelCabecalho,
                BorderLayout.NORTH
        );

        painel.add(
                painelFormulario,
                BorderLayout.CENTER
        );

        configurarRodape(
                painelBotoes
        );

        return painel;
    }

    private void atualizarLogoTribunal() {

        Tribunal tribunal =
                (Tribunal) comboTribunal.getSelectedItem();

        /*
         * Nenhum tribunal selecionado.
         */

        if (tribunal == null) {

            labelLogoTribunal.setIcon(
                    null
            );

        } else {

            /*
             * Carrega a logo correspondente ao tribunal.
             */

            ImageIcon logo =
                    com.selodigital.util.LogoUtil
                            .obterLogoTribunal(
                                    tribunal,
                                190,
                                115
                            );

            labelLogoTribunal.setIcon(
                    logo
            );
        }

        /*
         * Mantém o espaço reservado para a logo, mesmo sem tribunal
         * selecionado, para que o formulário não seja redimensionado.
         */

        labelLogoTribunal.setVisible(
                true
        );

        /*
         * Atualiza visualmente o componente.
         */

        labelLogoTribunal.revalidate();

        labelLogoTribunal.repaint();
    }

    private void atualizarLabelSelos() {

        Tribunal tribunal =
                (Tribunal) comboTribunal.getSelectedItem();

        labelSelos.setText(
                tribunal == Tribunal.TJPE
                        ? "Informe os códigos hash:"
                        : "Informe os selos:"
        );
    }

    private JPanel criarCabecalho() {

        JPanel painel =
                new JPanel(
                        new BorderLayout(
                                15,
                                0
                        )
                );

        painel.setBorder(
                BorderFactory.createEmptyBorder(
                        5,
                        5,
                        10,
                        5
                )
        );

        /*
         * ==================================================
         * ÁREA DA LOGO FIXA
         * ==================================================
         */

        JLabel logoPrincipal =
                new JLabel(
                        com.selodigital.util.LogoUtil
                                .obterLogoPrincipal(
                                        90,
                                        90
                                )
                );

        logoPrincipal.setHorizontalAlignment(
                JLabel.CENTER
        );

        /*
         * ==================================================
         * ÁREA DOS TEXTOS
         * ==================================================
         */

        JPanel painelTextos =
                new JPanel(
                        new GridBagLayout()
                );

        GridBagConstraints gbc =
                new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titulo =
                new JLabel(
                        "Consulta Selo Digital"
                );

        titulo.setFont(
                titulo.getFont().deriveFont(
                        Font.BOLD,
                        26f
                )
        );

        painelTextos.add(
                titulo,
                gbc
        );

        gbc.gridy = 1;

        JLabel subtitulo =
                new JLabel(
                        "Consulte e acompanhe os seus selos digitais."
                );

        subtitulo.setFont(
                subtitulo.getFont().deriveFont(
                        Font.PLAIN,
                        14f
                )
        );

        painelTextos.add(
                subtitulo,
                gbc
        );

        /*
         * ==================================================
         * ADICIONA LOGO E TEXTOS
         * ==================================================
         */

        painel.add(
                logoPrincipal,
                BorderLayout.WEST
        );

        painel.add(
                painelTextos,
                BorderLayout.CENTER
        );

        return painel;
    }

    private JPanel criarFormulario() {

        JPanel painel =
                new JPanel(
                        new BorderLayout(
                                10,
                                10
                        )
                );

        painel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder(
                                "Dados da consulta"
                        ),
                        BorderFactory.createEmptyBorder(
                                15,
                                20,
                                20,
                                20
                        )
                )
        );

        /*
         * ==================================================
         * ÁREA SUPERIOR
         * ==================================================
         *
         * Esta área contém somente:
         *
         * ESQUERDA:
         *     Local da Consulta
         *     JComboBox
         *
         * DIREITA:
         *     Logo do Tribunal
         *
         * A logo fica completamente separada da área
         * responsável pelo campo de selos.
         * ==================================================
         */

        JPanel painelSuperior =
                new JPanel(
                        new BorderLayout(
                                20,
                                0
                        )
                );

        /*
         * ==================================================
         * ÁREA ESQUERDA
         * ==================================================
         */

        JPanel painelEsquerdo =
                new JPanel(
                        new GridBagLayout()
                );

        GridBagConstraints gbc =
                new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets =
                new Insets(
                        0,
                        0,
                        5,
                        0
                );

        JLabel labelTribunal =
                new JLabel(
                        "Local da Consulta:"
                );

        labelTribunal.setFont(
                labelTribunal.getFont().deriveFont(
                        Font.BOLD,
                        14f
                )
        );

        painelEsquerdo.add(
                labelTribunal,
                gbc
        );

        /*
         * ==================================================
         * COMBOBOX
         * ==================================================
         */

        comboTribunal =
                new JComboBox<>();

        comboTribunal.setModel(
                new DefaultComboBoxModel<>(
                        new Tribunal[]{Tribunal.TJPB, Tribunal.TJRN, Tribunal.TJPE}
                )
        );

        comboTribunal.insertItemAt(
                null,
                0
        );

        comboTribunal.setSelectedIndex(0);

        comboTribunal.setPreferredSize(
                new Dimension(
                        320,
                        30
                )
        );

        gbc.gridy = 1;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets =
                new Insets(
                        0,
                        0,
                        0,
                        0
                );

        painelEsquerdo.add(
                comboTribunal,
                gbc
        );

        /*
         * ==================================================
         * ÁREA DIREITA — LOGO
         * ==================================================
         *
         * Esta área NÃO participa da estrutura vertical
         * do campo de selos.
         * ==================================================
         */

        JPanel painelLogo =
                new JPanel(
                        new BorderLayout()
                );

        painelLogo.setPreferredSize(
                new Dimension(
                        220,
                        115
                )
        );

        painelLogo.setMinimumSize(
                new Dimension(
                        220,
                        115
                )
        );

        painelLogo.setOpaque(false);

        labelLogoTribunal =
                new JLabel();

        labelLogoTribunal.setHorizontalAlignment(
                JLabel.CENTER
        );

        labelLogoTribunal.setVerticalAlignment(
                JLabel.CENTER
        );

        labelLogoTribunal.setVisible(
                false
        );

        painelLogo.add(
                labelLogoTribunal,
                BorderLayout.CENTER
        );

        /*
         * ==================================================
         * MONTA A ÁREA SUPERIOR
         * ==================================================
         */

        painelSuperior.add(
                painelEsquerdo,
                BorderLayout.CENTER
        );

        painelSuperior.add(
                painelLogo,
                BorderLayout.EAST
        );

        /*
         * ==================================================
         * ÁREA INFERIOR
         * ==================================================
         *
         * A partir daqui a logo não participa mais.
         *
         * Esta região será responsável exclusivamente por:
         *
         *     Informe os selos
         *     JScrollPane
         *
         * ==================================================
         */

        JPanel painelSelos =
                new JPanel(
                        new BorderLayout(
                                0,
                                5
                        )
                );

        /*
         * ==================================================
         * LABEL DOS SELOS
         * ==================================================
         */

        labelSelos =
                new JLabel(
                        "Informe os selos:"
                );

        labelSelos.setFont(
                labelSelos.getFont().deriveFont(
                        Font.BOLD,
                        14f
                )
        );

        painelSelos.add(
                labelSelos,
                BorderLayout.NORTH
        );

        /*
         * ==================================================
         * CAMPO DE SELOS
         * ==================================================
         */

        campoSelos =
                new JTextArea();

        campoSelos.setRows(0);
        campoSelos.setColumns(0);

        configurarColagemSelos();

        campoSelos.setLineWrap(false);
        campoSelos.setWrapStyleWord(false);

        campoSelos.setFont(
                campoSelos.getFont().deriveFont(
                        14f
                )
        );

        campoSelos.setBorder(
                BorderFactory.createEmptyBorder(
                        10,
                        10,
                        10,
                        10
                )
        );

        campoSelos.putClientProperty(
                FlatClientProperties.PLACEHOLDER_TEXT,
                "Informe um selo por linha"
        );

        /*
         * ==================================================
         * SCROLL
         * ==================================================
         */

        JScrollPane scrollSelos =
                new JScrollPane(
                        campoSelos,
                        JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                        JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
                );

        scrollSelos.setBorder(
                BorderFactory.createLineBorder(
                        javax.swing.UIManager.getColor(
                                "Component.borderColor"
                        )
                )
        );

        /*
         * ==================================================
         * ADICIONA O SCROLL AO PAINEL DOS SELOS
         * ==================================================
         */

        painelSelos.add(
                scrollSelos,
                BorderLayout.CENTER
        );

        /*
         * ==================================================
         * MONTA O FORMULÁRIO
         * ==================================================
         */

        painel.add(
                painelSuperior,
                BorderLayout.NORTH
        );

        painel.add(
                painelSelos,
                BorderLayout.CENTER
        );

        return painel;
    }

    private void configurarColagemSelos() {

        campoSelos.getInputMap(
                javax.swing.JComponent.WHEN_FOCUSED
        ).put(
                javax.swing.KeyStroke.getKeyStroke(
                        "ctrl V"
                ),
                "colarSelos"
        );

        campoSelos.getActionMap().put(
                "colarSelos",
                new javax.swing.AbstractAction() {

                    @Override
                    public void actionPerformed(
                            java.awt.event.ActionEvent e) {

                        try {

                            java.awt.datatransfer.Clipboard clipboard =
                                    java.awt.Toolkit.getDefaultToolkit()
                                            .getSystemClipboard();

                            if (!clipboard.isDataFlavorAvailable(
                                    java.awt.datatransfer.DataFlavor.stringFlavor
                            )) {

                                return;
                            }

                            String texto =
                                    (String) clipboard.getData(
                                            java.awt.datatransfer.DataFlavor.stringFlavor
                                    );

                            if (texto == null) {

                                return;
                            }

                            /*
                             * ==================================================
                             * LIMPA OS ESPAÇOS
                             * ==================================================
                             *
                             * Remove espaços no início e no final
                             * de cada linha.
                             *
                             * As linhas vazias também são removidas.
                             *
                             * Exemplo:
                             *
                             * "  RN123...  "
                             *
                             * torna-se:
                             *
                             * "RN123..."
                             * ==================================================
                             */

                            StringBuilder textoLimpo =
                                    new StringBuilder();

                            String[] linhas =
                                    texto.split("\\R");

                            for (String linha : linhas) {

                                String linhaLimpa =
                                        linha.trim();

                                if (!linhaLimpa.isBlank()) {

                                    if (textoLimpo.length() > 0) {

                                        textoLimpo.append(
                                                System.lineSeparator()
                                        );
                                    }

                                    textoLimpo.append(
                                            linhaLimpa
                                    );
                                }
                            }

                            /*
                             * ==================================================
                             * INSERE O TEXTO LIMPO
                             * ==================================================
                             */

                            campoSelos.replaceSelection(
                                    textoLimpo.toString()
                            );

                        } catch (Exception ex) {


                            /*
                             * Se ocorrer algum problema com a área de
                             * transferência, mantém o comportamento normal
                             * de colagem do JTextArea.
                             */

                            campoSelos.paste();
                        }
                    }
                }
        );
    }

    private JPanel criarBotoes() {

        JPanel painel =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.RIGHT,
                                12,
                                8
                        )
                );

        botaoLimpar =
                new JButton(
                        "Limpar",
                        com.selodigital.util.IconeUtil.limpar()
                );

        botaoConsultar =
                new JButton(
                        "Consultar",
                        com.selodigital.util.IconeUtil.consultar()
                );

        botaoHistorico =
                new JButton(
                        "Histórico",
                        com.selodigital.util.IconeUtil.historico()
                );

        botaoImportar =
                new JButton(
                        "Importar",
                        com.selodigital.util.IconeUtil.abrir()
                );

        botaoConsultar.putClientProperty(
                "JButton.buttonType",
                "default"
        );

        botaoConsultar.setPreferredSize(
                new Dimension(
                        130,
                        40
                )
        );

        botaoLimpar.setPreferredSize(
                new Dimension(
                        130,
                        40
                )
        );

        painel.add(
                botaoHistorico
        );

        botaoImportar.setPreferredSize(
                new Dimension(
                        140,
                        40
                )
        );

        painel.add(
                botaoImportar
        );

        botaoHistorico.setPreferredSize(
                new Dimension(
                        140,
                        40
                )
        );

        painel.add(
                botaoLimpar
        );

        painel.add(
                botaoConsultar
        );

        return painel;
    }

    private void configurarSalvamentoAoFechar() {

        addWindowListener(
                new java.awt.event.WindowAdapter() {

                    @Override
                    public void windowClosing(
                            java.awt.event.WindowEvent e) {

                        salvarConfiguracaoJanela();
                    }
                }
        );
    }

    private void salvarConfiguracaoJanela() {

        if (configuracao == null) {

            return;
        }

        configuracao.setJanelaPrincipalX(
                getX()
        );

        configuracao.setJanelaPrincipalY(
                getY()
        );

        configuracao.setJanelaPrincipalLargura(
                getWidth()
        );

        configuracao.setJanelaPrincipalAltura(
                getHeight()
        );

        try {

            ConfiguracaoService.salvar(
                    configuracao
            );

        } catch (IOException ex) {

        }
    }

    private void configurarEventos() {

        /*
         * ==================================================
         * BOTÃO LIMPAR
         * ==================================================
         */

        botaoLimpar.addActionListener(
                e -> limparFormulario()
        );

        /*
         * ==================================================
         * BOTÃO CONSULTAR
         * ==================================================
         */

        botaoConsultar.addActionListener(
                e -> realizarConsulta()
        );

        botaoHistorico.addActionListener(
                e -> new JanelaHistorico(this).setVisible(true)
        );

        botaoImportar.addActionListener(
                e -> importarSelos()
        );

        /*
         * ==================================================
         * ALTERAÇÃO DO TRIBUNAL
         * ==================================================
         *
         * Sempre que o usuário escolher:
         *
         * TJPB
         * TJRN
         *
         * atualizamos automaticamente a logo exibida.
         * ==================================================
         */

        comboTribunal.addActionListener(
                e -> {
                    atualizarLogoTribunal();
                    atualizarLabelSelos();
                }
        );
    }

    private void importarSelos() {

        JFileChooser seletor = new JFileChooser();
        seletor.setDialogTitle("Importar selos");
        seletor.setFileFilter(new FileNameExtensionFilter(
                "Arquivos de selos (*.txt, *.csv, *.xlsx)", "txt", "csv", "xlsx"
        ));

        if (seletor.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File arquivo = seletor.getSelectedFile();
        try {
            List<String> valores = lerSelosImportados(arquivo);
            adicionarSelosImportados(valores);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Não foi possível importar os selos.\n" + ex.getMessage(),
                    "Erro de importação",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private List<String> lerSelosImportados(File arquivo) throws Exception {
        String nome = arquivo.getName().toLowerCase(java.util.Locale.ROOT);
        if (nome.endsWith(".txt")) {
            return dividirValores(Files.readString(arquivo.toPath(), StandardCharsets.UTF_8), false);
        }
        if (nome.endsWith(".csv")) {
            return dividirValores(Files.readString(arquivo.toPath(), StandardCharsets.UTF_8), true);
        }
        if (nome.endsWith(".xlsx")) {
            return lerXlsx(arquivo);
        }
        throw new IOException("Formato de arquivo não suportado.");
    }

    private List<String> dividirValores(String conteudo, boolean separarColunas) {
        List<String> valores = new ArrayList<>();
        if (conteudo == null) {
            return valores;
        }
        for (String linha : conteudo.split("\\R")) {
            String[] itens = separarColunas ? linha.split("[,;\\t]") : new String[]{linha};
            for (String item : itens) {
                String valor = item.trim();
                if (!valor.isBlank()) {
                    valores.add(valor);
                }
            }
        }
        return valores;
    }

    private List<String> lerXlsx(File arquivo) throws IOException {
        List<String> valores = new ArrayList<>();
        DataFormatter formatador = new DataFormatter();
        try (Workbook pasta = WorkbookFactory.create(arquivo)) {
            for (Sheet planilha : pasta) {
                for (Row linha : planilha) {
                    for (Cell celula : linha) {
                        String valor = formatador.formatCellValue(celula).trim();
                        if (!valor.isBlank()) {
                            valores.add(valor);
                        }
                    }
                }
            }
        }
        return valores;
    }

    private void adicionarSelosImportados(List<String> valores) {
        if (valores.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nenhum selo foi encontrado no arquivo.",
                    "Importação", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String texto = String.join(System.lineSeparator(), valores);
        if (!campoSelos.getText().isBlank() && !texto.isBlank()) {
            campoSelos.append(System.lineSeparator());
        }
        campoSelos.append(texto);
        campoSelos.requestFocus();
    }

    private void limparFormulario() {

        comboTribunal.setSelectedIndex(
                0
        );

        campoSelos.setText(
                ""
        );

        /*
         * Garante que a logo também seja atualizada.
         */

        atualizarLogoTribunal();

        campoSelos.requestFocus();
    }

    public void abrirJanelaResultados(
            Tribunal tribunal,
            List<ResultadoConsulta> resultados) {

        if (tribunal == Tribunal.TJPB) {

            JanelaResultadosTJPB janela =
                    new JanelaResultadosTJPB(
                            tribunal,
                            resultados
                    );

            janela.posicionarNoMonitorDa(this);
            janela.setVisible(true);

            return;
        }

        if (tribunal == Tribunal.TJRN) {

            JanelaResultadosTJRN janela =
                    new JanelaResultadosTJRN(
                            tribunal,
                            resultados
                    );

            janela.posicionarNoMonitorDa(this);
            janela.setVisible(true);

            return;
        }

        // Históricos legados usam os mesmos campos da tabela existente, sem consulta à rede.
        if (tribunal == Tribunal.TJAL) {

            JanelaResultadosTJPB janela =
                    new JanelaResultadosTJPB(
                            tribunal,
                            resultados
                    );

            janela.setSize(1100, 550);
            janela.setMinimumSize(new Dimension(1000, 450));
            janela.posicionarNoMonitorDa(this);
            janela.setVisible(true);

            return;
        }

        if (tribunal == Tribunal.TJPE) {

            JanelaResultadosTJPE janela =
                    new JanelaResultadosTJPE(
                            tribunal,
                            resultados
                    );

            janela.posicionarNoMonitorDa(this);
            janela.setVisible(true);

            return;
        }

        JOptionPane.showMessageDialog(
                this,
                "Ainda não existe uma janela de resultados para o tribunal "
                        + tribunal + ".",
                "Tribunal não suportado",
                JOptionPane.WARNING_MESSAGE
        );
    }

    private void realizarConsulta() {

        Tribunal tribunal =
                (Tribunal) comboTribunal.getSelectedItem();

        if (tribunal == null) {

            JOptionPane.showMessageDialog(
                    this,
                    "Selecione o local da consulta.",
                    "Atenção",
                    JOptionPane.WARNING_MESSAGE
            );

            comboTribunal.requestFocus();

            return;
        }

        String textoSelos = campoSelos.getText();

        if (textoSelos == null ||
                textoSelos.isBlank()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Informe pelo menos um selo para realizar a consulta.",
                    "Atenção",
                    JOptionPane.WARNING_MESSAGE
            );

            campoSelos.requestFocus();

            return;
        }

        List<Selo> selos = new ArrayList<>();

        String[] linhas = textoSelos.split("\\R");

        for (String linha : linhas) {

            String numero =
                    SeloUtil.normalizar(
                            linha,
                            tribunal
                    );

            if (!numero.isBlank()) {
                selos.add(
                        new Selo(numero)
                );
            }
        }

        if (selos.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Informe pelo menos um selo válido.",
                    "Atenção",
                    JOptionPane.WARNING_MESSAGE
            );

            campoSelos.requestFocus();

            return;
        }

        executarConsulta(tribunal, selos);
    }

    public void refazerConsultaHistorico(
            com.selodigital.historico.RegistroHistorico registro) {

        List<Selo> selos = registro.getSelos().stream()
                .map(Selo::new)
                .toList();

        executarConsulta(registro.getTribunal(), selos);
    }

    private void executarConsulta(Tribunal tribunal, List<Selo> selos) {

        // Registros legados permanecem disponíveis somente para leitura.
        if (tribunal == Tribunal.TJAL) {
            return;
        }

        ConsultaTribunal consultaTribunal = TribunalFactory.criar(tribunal);

        ConsultaSeloService service =
                new ConsultaSeloService(consultaTribunal);

        botaoConsultar.setEnabled(false);
        botaoLimpar.setEnabled(false);
        botaoImportar.setEnabled(false);

        ProgressoConsultaDialog progressoDialog =
                new ProgressoConsultaDialog(
                        this
                );

        new Thread(() -> {

            List<ResultadoConsulta> resultados;

            try {

                resultados = service.consultar(
                        selos,
                        progresso -> SwingUtilities.invokeLater(
                                () -> progressoDialog.atualizar(progresso)
                        )
                );

            } catch (Exception e) {


                SwingUtilities.invokeLater(() -> {

                    progressoDialog.dispose();

                    botaoConsultar.setEnabled(true);
                    botaoLimpar.setEnabled(true);
                    botaoImportar.setEnabled(true);

                    JOptionPane.showMessageDialog(
                            this,
                            "Ocorreu um erro durante a consulta.",
                            "Erro",
                            JOptionPane.ERROR_MESSAGE
                    );
                });

                return;
            }

            SwingUtilities.invokeLater(() -> {

                progressoDialog.finalizar();

                progressoDialog.dispose();

                botaoConsultar.setEnabled(true);
                botaoLimpar.setEnabled(true);
                botaoImportar.setEnabled(true);

                try {
                    com.selodigital.historico.HistoricoService.registrar(
                            tribunal,
                            selos.stream().map(Selo::getNumero).toList(),
                            resultados
                    );
                } catch (IOException ignored) {
                }

                abrirJanelaResultados(
                        tribunal,
                        resultados
                );
            });

        }).start();

        progressoDialog.setVisible(true);
    }

    public void preencherConsultaHistorico(
            com.selodigital.historico.RegistroHistorico registro) {

        if (registro.getTribunal() == Tribunal.TJAL) {
            return;
        }

        comboTribunal.setSelectedItem(registro.getTribunal());
        campoSelos.setText(String.join(System.lineSeparator(), registro.getSelos()));
        atualizarLogoTribunal();
        atualizarLabelSelos();
        campoSelos.requestFocus();
    }

    //endregion
}
