package com.selodigital;

import com.selodigital.interfacegrafica.JanelaPrincipal;
import com.selodigital.theme.WindowsTheme;

import javax.swing.*;

/** Ponto de entrada: aplica o tema antes de criar a interface na EDT do Swing. */
public class ConsultaSeloDigital {

    // =======================================================
    // 1. ESTRUTURA DA CLASSE
    // =======================================================

    //region [Membros da Classe]

    public static void main(String[] args) {

        try {

            WindowsTheme.aplicarTemaInicial();

            WindowsTheme.iniciarMonitor();

        } catch (Throwable ex) {


        }

        /* Toda criação de componentes Swing precisa ocorrer na Event Dispatch Thread. */
        SwingUtilities.invokeLater(() -> {

            try {

                JanelaPrincipal janela =
                        new JanelaPrincipal();

                janela.setVisible(true);

            } catch (Throwable ex) {


            }
        });
    }

    //endregion
}
