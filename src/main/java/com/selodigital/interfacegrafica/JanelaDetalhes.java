package com.selodigital.interfacegrafica;

import com.selodigital.modelo.DetalhesSelo;
import com.selodigital.util.QrCodeUtil;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Map;

/** Exibe os campos retornados por cada tribunal em seções dinâmicas e exportáveis. */
public class JanelaDetalhes extends JanelaBase {

    // =======================================================
    // 1. ESTRUTURA DA CLASSE
    // =======================================================

    //region [Membros da Classe]

    private final DetalhesSelo detalhes;

    private BufferedImage imagemQrCode;

    public JanelaDetalhes(
            DetalhesSelo detalhes) {

        this.detalhes = detalhes;

        setTitle(
                "Detalhes do Selo: "
                        + obterNumeroTitulo()
        );

        setDefaultCloseOperation(
                JFrame.DISPOSE_ON_CLOSE
        );

        configurarTamanhoMinimo(
                1000,
                600
        );

        setSize(
                1200,
                750
        );

        setLocationRelativeTo(null);

        criarInterface();

        configurarTeclaEnter();
    }

    private String obterNumeroTitulo() {

        if (detalhes.getTribunal()
                == com.selodigital.modelo.Tribunal.TJPE) {

            String numeroSelo =
                    detalhes.getCampos().get("Número do Selo");

            if (numeroSelo != null && !numeroSelo.isBlank()) {
                return numeroSelo;
            }
        }

        return detalhes.getSelo().getNumero();
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

        painelPrincipal.add(
                criarConteudo(),
                BorderLayout.CENTER
        );

        configurarConteudo(
                painelPrincipal
        );

        configurarRodape(
                criarBotoes()
        );
    }

    private void configurarTeclaEnter() {

        getRootPane().getInputMap(
                javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW
        ).put(
                javax.swing.KeyStroke.getKeyStroke("ENTER"),
                "fecharJanela"
        );

        getRootPane().getActionMap().put(
                "fecharJanela",
                new javax.swing.AbstractAction() {

                    @Override
                    public void actionPerformed(
                            java.awt.event.ActionEvent e) {

                        dispose();
                    }
                }
        );
    }

    private JPanel criarCabecalho() {

        JPanel painel =
                new JPanel(
                        new BorderLayout()
                );

        JLabel titulo =
                new JLabel(
                        "Detalhes do Selo",
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

    private JScrollPane criarConteudo() {

        JPanel painel =
                new JPanel(
                        new GridBagLayout()
                );

        painel.setBorder(
                BorderFactory.createEmptyBorder(
                        5,
                        5,
                        5,
                        5
                )
        );

        GridBagConstraints gbc =
                new GridBagConstraints();

        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets =
                new Insets(
                        5,
                        5,
                        5,
                        5
                );

        int linha = 0;

        // ==================================================
        // INFORMAÇÕES GERAIS
        // ==================================================

        JPanel painelGeral =
                criarPainelSecao(
                        "Informações Gerais"
                );

        adicionarCampo(
                painelGeral,
                "Tribunal",
                detalhes.getTribunal().toString()
        );

        if (detalhes.getTribunal()
                == com.selodigital.modelo.Tribunal.TJPE) {

            adicionarCampo(
                    painelGeral,
                    "Hash consultado",
                    detalhes.getSelo().getNumero()
            );

            adicionarCampo(
                    painelGeral,
                    "Número do Selo",
                    detalhes.getCampos().get("Número do Selo")
            );

        } else {

            adicionarCampo(
                    painelGeral,
                    "Número do Selo",
                    detalhes.getSelo().getNumero()
            );
        }

        adicionarCampo(
                painelGeral,
                "Status",
                detalhes.getStatus().getDescricao()
        );

        gbc.gridy = linha++;

        painel.add(
                painelGeral,
                gbc
        );

        // ==================================================
        // CAMPOS EXTRAÍDOS DO TJPB
        // ==================================================

        Map<String, String> campos =
                detalhes.getCampos();

        /*
         * ==================================================
         * SEÇÕES DINÂMICAS
         *
         * A chave do Map deve possuir o formato:
         *
         * "Nome da Seção - Nome do Campo"
         *
         * Exemplo:
         *
         * "Solicitante - Nome"
         * "Registro - Código do Livro"
         * "Detalhes Imóvel 1 - Matrícula"
         *
         * Dessa forma não precisamos conhecer previamente
         * todos os tipos de ato existentes no TJPB.
         * ==================================================
         */

        Map<String, JPanel> secoes =
                new java.util.LinkedHashMap<>();

        for (
                Map.Entry<String, String> campo
                : campos.entrySet()
        ) {

            String nome =
                    campo.getKey();

            String valor =
                    campo.getValue();

            if (detalhes.getTribunal()
                    == com.selodigital.modelo.Tribunal.TJPE
                    && "Número do Selo".equals(nome)) {
                continue;
            }

            String nomeSecao;
            String nomeCampo;

            int separador =
                    nome.indexOf(" - ");

            if (separador > 0) {

                nomeSecao =
                        nome.substring(
                                0,
                                separador
                        ).trim();

                nomeCampo =
                        nome.substring(
                                separador + 3
                        ).trim();

            } else {

                /*
                 * Campo que não pertence a uma seção
                 * específica: permanece em "Informações do Ato".
                 */

                nomeSecao =
                        "Outras Informações";

                nomeCampo =
                        nome.trim();
            }

            /*
             * Uma seção é criada sob demanda, somente depois de confirmar que
             * esta entrada representa um campo. No TJRN, títulos estruturais e
             * campos vazios (como "Observação:") não devem ocupar uma borda
             * própria: a página original também não lhes associa conteúdo.
             */
            if (nomeCampo.isBlank()
                    || (detalhes.getTribunal() == com.selodigital.modelo.Tribunal.TJRN
                    && (valor == null || valor.isBlank()))) {
                continue;
            }

            JPanel painelSecao =
                    secoes.get(nomeSecao);

            if (painelSecao == null) {

                painelSecao =
                        criarPainelSecao(
                                nomeSecao
                        );

                secoes.put(
                        nomeSecao,
                        painelSecao
                );
            }

            adicionarCampo(
                    painelSecao,
                    nomeCampo,
                    valor
            );
        }

        // ==================================================
        // ADICIONA AS SEÇÕES DINAMICAMENTE
        // ==================================================

        for (
                JPanel painelSecao
                : secoes.values()
        ) {

            gbc.gridy = linha++;

            painel.add(
                    painelSecao,
                    gbc
            );
        }

        // ==================================================
        // QR CODE
        // ==================================================
        //
        // O TJPB possui QR Code.
        // O TJRN não possui QR Code.
        //
        // Portanto, o painel somente é criado para o TJPB.
        // ==================================================

        if (detalhes.getTribunal() == com.selodigital.modelo.Tribunal.TJPB) {

            JPanel painelQrCode =
                    criarPainelQrCode();

            gbc.gridy = linha++;

            painel.add(
                    painelQrCode,
                    gbc
            );
        }

        // ==================================================
        // ESPAÇO VERTICAL NO FINAL
        // ==================================================

        gbc.gridy = linha;
        gbc.weighty = 1.0;

        painel.add(
                new JPanel(),
                gbc
        );

        JScrollPane scrollPane =
                new JScrollPane(
                        painel
                );

        scrollPane.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        );

        scrollPane.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
        );

        javax.swing.SwingUtilities.invokeLater(() -> {

            scrollPane.getVerticalScrollBar().setValue(0);
            scrollPane.getHorizontalScrollBar().setValue(0);

        });

        return scrollPane;
    }

    private JPanel criarPainelSecao(
            String titulo) {

        JPanel painel =
                new JPanel(
                        new GridBagLayout()
                );

        painel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder(
                                titulo
                        ),
                        BorderFactory.createEmptyBorder(
                                5,
                                8,
                                8,
                                8
                        )
                )
        );

        return painel;
    }

    private void adicionarCampo(
            JPanel painel,
            String nome,
            String valor) {

        GridBagConstraints gbc =
                new GridBagConstraints();

        gbc.insets =
                new Insets(
                        4,
                        4,
                        4,
                        4
                );

        gbc.anchor =
                GridBagConstraints.NORTHWEST;

        gbc.fill =
                GridBagConstraints.HORIZONTAL;

        gbc.gridy =
                painel.getComponentCount() / 2;

        JLabel labelNome =
                new JLabel(
                        nome + ":"
                );

        labelNome.setFont(
                labelNome.getFont().deriveFont(
                        Font.BOLD,
                        13f
                )
        );

        gbc.gridx = 0;
        gbc.weightx = 0.0;

        painel.add(
                labelNome,
                gbc
        );

        String textoValor =
                valor == null || valor.isBlank()
                        ? "Não informado"
                        : valor;

        /*
         * ==================================================
         * SERVENTIA
         *
         * Fica em uma única linha.
         * Não permite seleção/cópia.
         * A tooltip permite visualizar o texto completo.
         * ==================================================
         */

        if ("Serventia".equals(nome)) {

            JLabel campoServentia =
                    new JLabel(
                            textoValor
                    );

            campoServentia.setFont(
                    campoServentia.getFont().deriveFont(
                            13f
                    )
            );

            campoServentia.setFocusable(false);

            gbc.gridx = 1;
            gbc.weightx = 1.0;

            painel.add(
                    campoServentia,
                    gbc
            );

            return;
        }

        /*
         * ==================================================
         * DEMAIS CAMPOS
         *
         * JTextArea responsivo somente leitura.
         *
         * Permite:
         *
         * Ctrl + A
         * Ctrl + C
         * seleção com o mouse
         * ==================================================
         */

        JTextAreaResponsivo campoValor =
                new JTextAreaResponsivo(
                        textoValor
                );

        campoValor.setEditable(false);
        campoValor.setFocusable(true);

        campoValor.setLineWrap(true);
        campoValor.setWrapStyleWord(true);

        campoValor.setMinimumSize(
                new Dimension(
                        0,
                        campoValor.getPreferredSize().height
                )
        );

        campoValor.setBorder(null);
        campoValor.setOpaque(false);

        campoValor.setFont(
                campoValor.getFont().deriveFont(
                        13f
                )
        );

        campoValor.setCaretPosition(0);

        /*
         * ==================================================
         * AJUSTE AUTOMÁTICO DA ALTURA
         * ==================================================
         *
         * A largura do JTextArea acompanha a largura
         * disponível na janela.
         *
         * Quando a janela aumenta:
         *
         *     mais largura
         *     menos linhas
         *
         * Quando a janela diminui:
         *
         *     menos largura
         *     mais linhas
         *
         * O texto quebra automaticamente entre palavras.
         * ==================================================
         */

        /*
         * Permite copiar o conteúdo.
         */

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        painel.add(
                campoValor,
                gbc
        );
    }

    /*
     * ==========================================================
     * TEXT AREA RESPONSIVO
     * ==========================================================
     *
     * Componente utilizado para exibir textos longos.
     *
     * O comportamento é semelhante ao Word:
     *
     * Janela maior:
     *     mais largura disponível
     *     -> menos linhas
     *
     * Janela menor:
     *     menos largura disponível
     *     -> mais linhas
     *
     * A altura do componente acompanha automaticamente
     * a quantidade de linhas necessárias.
     *
     * A quebra acontece entre palavras.
     * ==========================================================
     */

    private static class JTextAreaResponsivo
            extends javax.swing.JTextArea {

        public JTextAreaResponsivo(
                String texto) {

            super(texto);

            setLineWrap(true);

            setWrapStyleWord(true);

            setEditable(false);

            setFocusable(true);

            setBorder(null);

            setOpaque(false);

            setFont(
                    getFont().deriveFont(
                            13f
                    )
            );

            addComponentListener(
                    new java.awt.event.ComponentAdapter() {

                        @Override
                        public void componentResized(
                                java.awt.event.ComponentEvent e) {

                            revalidate();
                            repaint();
                        }
                    }
            );
        }

        @Override
        public Dimension getPreferredSize() {

            Dimension tamanho =
                    super.getPreferredSize();

            /*
             * Se o componente já possui uma largura válida,
             * recalcula a altura necessária para essa largura.
             */

            if (getWidth() > 0) {

                int largura =
                        getWidth();

                setSize(
                        largura,
                        Integer.MAX_VALUE
                );

                tamanho =
                        super.getPreferredSize();

                tamanho.width =
                        largura;
            }

            return tamanho;
        }
    }

    private JPanel criarPainelQrCode() {

        JPanel painel =
                new JPanel(
                        new BorderLayout()
                );

        painel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder(
                                "QR Code"
                        ),
                        BorderFactory.createEmptyBorder(
                                10,
                                10,
                                10,
                                10
                        )
                )
        );

        try {

            String conteudoQrCode =
                    detalhes.getQrCodeConteudo();

            imagemQrCode =
                    QrCodeUtil.gerar(
                            conteudoQrCode,
                            220,
                            220
                    );

            JLabel imagem =
                    new JLabel(
                            new ImageIcon(
                                    imagemQrCode
                            )
                    );

            imagem.setHorizontalAlignment(
                    SwingConstants.LEFT
            );

            imagem.setToolTipText(
                    conteudoQrCode
            );

            painel.add(
                    imagem,
                    BorderLayout.WEST
            );

            /*
             * ==================================================
             * BOTÃO BAIXAR IMAGEM
             * ==================================================
             */

            JButton botaoBaixar =
                    new JButton(
                            "Baixar Imagem",
                            com.selodigital.util.IconeUtil.baixar()
                    );

            botaoBaixar.setPreferredSize(
                    new Dimension(
                            140,
                            35
                    )
            );

            botaoBaixar.addActionListener(
                    e -> baixarImagemQrCode()
            );

            JPanel painelBotao =
                    new JPanel(
                            new FlowLayout(
                                    FlowLayout.LEFT,
                                    0,
                                    10
                            )
                    );

            painelBotao.add(
                    botaoBaixar
            );

            painel.add(
                    painelBotao,
                    BorderLayout.SOUTH
            );

        } catch (Exception e) {

            JLabel erro =
                    new JLabel(
                            "Não foi possível gerar o QR Code.",
                            SwingConstants.CENTER
                    );

            erro.setFont(
                    erro.getFont().deriveFont(
                            Font.BOLD,
                            13f
                    )
            );

            painel.add(
                    erro,
                    BorderLayout.CENTER
            );

        }

        return painel;
    }

    private void baixarImagemQrCode() {

        if (imagemQrCode == null) {

            JOptionPane.showMessageDialog(
                    this,
                    "A imagem do QR Code não está disponível.",
                    "Erro",
                    JOptionPane.ERROR_MESSAGE
            );

            return;
        }

        JFileChooser seletorArquivo =
                new JFileChooser();

        seletorArquivo.setDialogTitle(
                "Salvar QR Code"
        );

        seletorArquivo.setSelectedFile(
                new java.io.File(
                        "qrcode-"
                                + detalhes.getSelo().getNumero()
                                + ".png"
                )
        );

        FileNameExtensionFilter filtro =
                new FileNameExtensionFilter(
                        "Imagem PNG (*.png)",
                        "png"
                );

        seletorArquivo.setFileFilter(
                filtro
        );

        int resultado =
                seletorArquivo.showSaveDialog(
                        this
                );

        if (resultado != JFileChooser.APPROVE_OPTION) {
            return;
        }

        java.io.File arquivo =
                seletorArquivo.getSelectedFile();

        String caminho =
                arquivo.getAbsolutePath();

        if (!caminho.toLowerCase().endsWith(".png")) {

            arquivo =
                    new java.io.File(
                            caminho + ".png"
                    );
        }

        /*
         * ==================================================
         * CONFIRMAÇÃO DE SOBRESCRITA
         * ==================================================
         */

        if (arquivo.exists()) {

            int confirmar =
                    JOptionPane.showConfirmDialog(
                            this,
                            "O arquivo já existe.\n"
                                    + "Deseja substituí-lo?",
                            "Confirmar substituição",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE
                    );

            if (confirmar != JOptionPane.YES_OPTION) {
                return;
            }
        }

        try {

            javax.imageio.ImageIO.write(
                    imagemQrCode,
                    "PNG",
                    arquivo
            );

            JOptionPane.showMessageDialog(
                    this,
                    "QR Code salvo com sucesso em:\n"
                            + arquivo.getAbsolutePath(),
                    "Sucesso",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (Exception e) {


            JOptionPane.showMessageDialog(
                    this,
                    "Não foi possível salvar o QR Code.\n\n"
                            + e.getMessage(),
                    "Erro",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }


    private JPanel criarBotoes() {

        JPanel painel =
                new JPanel();

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
                        .exportarDetalhes(this, detalhes, true));

        exportarXlsx.addActionListener(e ->
                com.selodigital.exportacao.ExportadorConsulta
                        .exportarDetalhes(this, detalhes, false));

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

        painel.add(botaoExportar);

        painel.add(
                botaoFechar
        );

        return painel;
    }

    //endregion
}
