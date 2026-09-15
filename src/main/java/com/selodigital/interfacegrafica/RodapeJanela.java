package com.selodigital.interfacegrafica;

import com.selodigital.util.VersaoAplicacao;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;

/** Constrói o rodapé compartilhado, incluindo a versão e a área de ações da janela. */
public final class RodapeJanela {

    // =======================================================
    // 1. ESTRUTURA DA CLASSE
    // =======================================================

    //region [Membros da Classe]

    private RodapeJanela() {
    }

    public static JPanel criar(
            JPanel painelBotoes) {

        JPanel rodape =
                new JPanel(
                        new BorderLayout()
                );

        // ==================================================
        // ÁREA DOS BOTÕES
        //
        // Os botões continuam independentes da versão.
        // ==================================================

        JPanel areaBotoes =
                new JPanel(
                        new BorderLayout()
                );

        areaBotoes.add(
                painelBotoes,
                BorderLayout.CENTER
        );

        rodape.add(
                areaBotoes,
                BorderLayout.CENTER
        );

        // ==================================================
        // ÁREA EXCLUSIVA DA VERSÃO
        // ==================================================

        JPanel painelVersao =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.RIGHT,
                                0,
                                0
                        )
                );

        painelVersao.setBorder(
                BorderFactory.createEmptyBorder(
                        5,
                        0,
                        3,
                        5
                )
        );

        JLabel versao =
                new JLabel(
                        "Versão "
                                + VersaoAplicacao.VERSAO
                );

        versao.setFont(
                versao.getFont().deriveFont(
                        Font.PLAIN,
                        11f
                )
        );

        painelVersao.add(
                versao
        );

        rodape.add(
                painelVersao,
                BorderLayout.SOUTH
        );

        return rodape;
    }

    //endregion
}
