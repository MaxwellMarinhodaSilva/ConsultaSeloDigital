package com.selodigital.interfacegrafica;

import com.selodigital.historico.HistoricoService;
import com.selodigital.historico.RegistroHistorico;
import com.selodigital.modelo.Tribunal;
import com.selodigital.util.IconeUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** Janela que apresenta o histórico persistido sem alterar sua regra de armazenamento. */
public class JanelaHistorico extends JanelaBase {

    // =======================================================
    // 1. ESTRUTURA DA CLASSE
    // =======================================================

    //region [Membros da Classe]

    private static final DateTimeFormatter DATA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final JanelaPrincipal principal;
    private JTable tabela;
    private List<RegistroHistorico> registros;
    private JPanel painelCentral;
    private JButton botaoAbrir;
    private JButton botaoRefazerConsulta;
    private JButton botaoApagar;
    private JButton botaoApagarTodos;

    public JanelaHistorico(JanelaPrincipal principal) {
        this.principal = principal;
        setTitle("Histórico de Consultas");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        configurarTamanhoMinimo(900, 450);
        setSize(1100, 600);
        setLocationRelativeTo(principal);
        criarInterface();
    }

    private void criarInterface() {
        JPanel painel = new JPanel(new BorderLayout(0, 16));
        painel.add(criarCabecalho(), BorderLayout.NORTH);

        painelCentral = new JPanel(new BorderLayout());
        painelCentral.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIManager.getColor("Component.borderColor")),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        painel.add(painelCentral, BorderLayout.CENTER);

        tabela = new JTable();
        tabela.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                atualizarEstadoBotoes();
            }
        });

        configurarConteudo(painel);
        configurarRodape(criarBotoes());
        carregar();
        configurarAtalhos();
    }

    private JPanel criarCabecalho() {
        JPanel painel = new JPanel(new BorderLayout(0, 4));
        painel.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));

        JLabel titulo = new JLabel("Histórico de Consultas");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 22f));

        JLabel descricao = new JLabel("Consulte, abra ou gerencie as consultas realizadas.");
        descricao.setFont(descricao.getFont().deriveFont(Font.PLAIN, 13f));

        painel.add(titulo, BorderLayout.NORTH);
        painel.add(descricao, BorderLayout.CENTER);
        return painel;
    }

    private JPanel criarBotoes() {
        JPanel painel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 6));
        botaoAbrir = new JButton("Abrir", IconeUtil.abrir());
        botaoRefazerConsulta = new JButton("Refazer Consulta", IconeUtil.refazerConsulta());
        botaoApagar = new JButton("Apagar", IconeUtil.apagar());
        botaoApagarTodos = new JButton("Apagar todos", IconeUtil.apagar());
        JButton fechar = new JButton("Fechar", IconeUtil.fechar());

        Dimension tamanho = new Dimension(140, 35);
        botaoAbrir.setPreferredSize(tamanho);
        botaoRefazerConsulta.setPreferredSize(tamanho);
        botaoApagar.setPreferredSize(tamanho);
        botaoApagarTodos.setPreferredSize(tamanho);
        fechar.setPreferredSize(tamanho);

        botaoAbrir.addActionListener(e -> abrir());
        botaoRefazerConsulta.addActionListener(e -> refazerConsulta());
        botaoApagar.addActionListener(e -> apagar());
        botaoApagarTodos.addActionListener(e -> apagarTudo());
        fechar.addActionListener(e -> dispose());

        painel.add(botaoAbrir);
        painel.add(botaoRefazerConsulta);
        painel.add(botaoApagar);
        painel.add(botaoApagarTodos);
        painel.add(fechar);
        return painel;
    }

    private void carregar() {
        try {
            registros = HistoricoService.carregar();
            painelCentral.removeAll();

            if (registros.isEmpty()) {
                painelCentral.add(criarEstadoVazio(), BorderLayout.CENTER);
            } else {
                DefaultTableModel modelo = new DefaultTableModel(
                        new String[]{"#", "Data da Consulta", "Tribunal", "Quantidade de Selos", "Selos Consultados"}, 0) {
                    @Override
                    public boolean isCellEditable(int linha, int coluna) {
                        return false;
                    }
                };

                for (RegistroHistorico registro : registros) {
                    modelo.addRow(new Object[]{
                            registro.getNumero(),
                            DATA.format(registro.getDataConsulta()),
                            registro.getTribunal(),
                            registro.getSelos().size(),
                            String.join(", ", registro.getSelos())
                    });
                }

                tabela.setModel(modelo);
                configurarTabela();
                painelCentral.add(new JScrollPane(tabela), BorderLayout.CENTER);
            }

            atualizarEstadoBotoes();
            painelCentral.revalidate();
            painelCentral.repaint();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Não foi possível carregar o histórico.\n" + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void configurarTabela() {
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.setRowHeight(34);
        tabela.setFillsViewportHeight(true);
        tabela.setShowVerticalLines(false);
        tabela.setShowHorizontalLines(true);
        tabela.setIntercellSpacing(new Dimension(0, 1));
        tabela.getTableHeader().setReorderingAllowed(false);
        tabela.getTableHeader().setFont(tabela.getTableHeader().getFont().deriveFont(Font.BOLD));
        tabela.getTableHeader().setPreferredSize(new Dimension(0, 34));
        tabela.getColumnModel().getColumn(0).setMaxWidth(55);
        tabela.getColumnModel().getColumn(0).setPreferredWidth(55);
        tabela.getColumnModel().getColumn(1).setPreferredWidth(155);
        tabela.getColumnModel().getColumn(2).setPreferredWidth(110);
        tabela.getColumnModel().getColumn(3).setPreferredWidth(125);
        tabela.getColumnModel().getColumn(4).setPreferredWidth(500);
    }

    private JPanel criarEstadoVazio() {
        JPanel painel = new JPanel(new GridBagLayout());
        JPanel mensagem = new JPanel();
        mensagem.setLayout(new BoxLayout(mensagem, BoxLayout.Y_AXIS));
        mensagem.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JLabel icone = new JLabel(IconeUtil.historico());
        icone.setAlignmentX(Component.CENTER_ALIGNMENT);
        icone.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        JLabel titulo = new JLabel("Nenhum histórico encontrado");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 18f));
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel descricao = new JLabel("As consultas realizadas aparecerão aqui.");
        descricao.setFont(descricao.getFont().deriveFont(Font.PLAIN, 13f));
        descricao.setAlignmentX(Component.CENTER_ALIGNMENT);

        mensagem.add(icone);
        mensagem.add(titulo);
        mensagem.add(Box.createVerticalStrut(7));
        mensagem.add(descricao);
        painel.add(mensagem);
        return painel;
    }

    private void atualizarEstadoBotoes() {
        boolean possuiRegistros = registros != null && !registros.isEmpty();
        boolean possuiSelecao = possuiRegistros && tabela.getSelectedRow() >= 0;
        RegistroHistorico registro = selecionado();
        boolean legado = registro != null && registro.getTribunal() == Tribunal.TJAL;
        botaoAbrir.setEnabled(possuiSelecao && (!legado || !registro.getResultados().isEmpty()));
        botaoRefazerConsulta.setEnabled(possuiSelecao && !legado);
        botaoApagar.setEnabled(possuiSelecao);
        botaoApagarTodos.setEnabled(possuiRegistros);
    }

    private RegistroHistorico selecionado() {
        int indice = tabela.getSelectedRow();
        return indice >= 0 && registros != null && indice < registros.size()
                ? registros.get(indice) : null;
    }

    private void abrir() {
        RegistroHistorico registro = selecionado();
        if (registro == null) {
            return;
        }
        if (!registro.getResultados().isEmpty()) {
            principal.abrirJanelaResultados(
                    registro.getTribunal(),
                    registro.getResultados()
            );
            dispose();
            return;
        }
        if (registro.getTribunal() == Tribunal.TJAL) {
            return;
        }
        principal.preencherConsultaHistorico(registro);
        dispose();
    }

    private void apagar() {
        RegistroHistorico registro = selecionado();
        if (registro == null) {
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Deseja realmente apagar este histórico?",
                "Confirmar exclusão", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            HistoricoService.remover(registro);
            carregar();
        } catch (Exception e) {
            exibirErro(e);
        }
    }

    private void refazerConsulta() {
        RegistroHistorico registro = selecionado();
        if (registro == null || registro.getTribunal() == Tribunal.TJAL) {
            return;
        }
        principal.refazerConsultaHistorico(registro);
        dispose();
    }

    private void apagarTudo() {
        if (JOptionPane.showConfirmDialog(this,
                "Tem certeza de que deseja apagar todo o Histórico de consultas?\n\nEsta ação não poderá ser desfeita.",
                "Apagar todo o histórico", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            HistoricoService.apagarTudo();
            carregar();
        } catch (Exception e) {
            exibirErro(e);
        }
    }

    private void exibirErro(Exception e) {
        JOptionPane.showMessageDialog(this,
                "Não foi possível atualizar o histórico.\n" + e.getMessage(),
                "Erro", JOptionPane.ERROR_MESSAGE);
    }

    private void configurarAtalhos() {
        InputMap inputMap = tabela.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap actionMap = tabela.getActionMap();
        inputMap.put(KeyStroke.getKeyStroke("ENTER"), "abrirHistorico");
        actionMap.put("abrirHistorico", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                abrir();
            }
        });
        inputMap.put(KeyStroke.getKeyStroke("DELETE"), "apagarHistorico");
        actionMap.put("apagarHistorico", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                apagar();
            }
        });
    }

    //endregion
}
