package com.selodigital.interfacegrafica;

import com.selodigital.modelo.ProgressoConsulta;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;

/** Diálogo modal de progresso atualizado durante a execução assíncrona de uma consulta em lote. */
public class ProgressoConsultaDialog extends JDialog {

    // =======================================================
    // 1. ESTRUTURA DA CLASSE
    // =======================================================

    //region [Membros da Classe]

    private final JLabel labelStatus;
    private final JLabel labelPorcentagem;
    private final JProgressBar barra;

    public ProgressoConsultaDialog(
            java.awt.Frame owner) {

        super(
                owner,
                "Consultando selos",
                Dialog.ModalityType.APPLICATION_MODAL
        );

        setDefaultCloseOperation(
                JDialog.DO_NOTHING_ON_CLOSE
        );

        labelStatus =
                new JLabel(
                        "Aguardando consultas..."
                );

        labelStatus.setFont(
                labelStatus.getFont().deriveFont(
                        Font.PLAIN,
                        14f
                )
        );

        labelPorcentagem =
                new JLabel(
                        "0%",
                        JLabel.RIGHT
                );

        labelPorcentagem.setFont(
                labelPorcentagem.getFont().deriveFont(
                        Font.BOLD,
                        14f
                )
        );

        barra =
                new JProgressBar(
                        0,
                        100
                );

        barra.setValue(0);
        barra.setStringPainted(false);

        JPanel painel =
                new JPanel(
                        new BorderLayout(
                                10,
                                10
                        )
                );

        painel.setBorder(
                BorderFactory.createEmptyBorder(
                        20,
                        20,
                        20,
                        20
                )
        );

        painel.add(
                labelStatus,
                BorderLayout.NORTH
        );

        painel.add(
                barra,
                BorderLayout.CENTER
        );

        painel.add(
                labelPorcentagem,
                BorderLayout.EAST
        );

        setContentPane(painel);

        setSize(
                new Dimension(
                        500,
                        140
                )
        );

        setResizable(false);

        setLocationRelativeTo(owner);
    }

    public void atualizar(
            ProgressoConsulta progresso) {

        int porcentagem =
                progresso.getPorcentagem();

        barra.setValue(
                porcentagem
        );

        labelPorcentagem.setText(
                porcentagem + "%"
        );

        labelStatus.setText(
                "Consultando "
                        + progresso.getAtual()
                        + "/"
                        + progresso.getTotal()
                        + " — "
                        + progresso.getSelo().getNumero()
        );
    }

    public void finalizar() {

        barra.setValue(100);

        labelPorcentagem.setText(
                "100%"
        );

        labelStatus.setText(
                "Consulta concluída."
        );
    }

    //endregion
}
