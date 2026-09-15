package com.selodigital.theme;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatLaf;

import javax.swing.SwingUtilities;
import java.awt.Window;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/** Sincroniza o FlatLaf com a preferência de tema do Windows durante a execução. */
public final class WindowsTheme {

    // =======================================================
    // 1. ESTRUTURA DA CLASSE
    // =======================================================

    //region [Membros da Classe]

    private WindowsTheme() {
    }

    /*
     * Guarda o último estado conhecido do tema do Windows.
     *
     * true  = modo escuro
     * false = modo claro
     */
    private static boolean ultimoModoEscuro =
            windowsEstaNoModoEscuro();

    /*
     * Monitor responsável por verificar periodicamente
     * se o tema do Windows foi alterado.
     */
    private static final ScheduledExecutorService monitorTema =
            Executors.newSingleThreadScheduledExecutor();

    public static boolean windowsEstaNoModoEscuro() {

        try {

            Process process =
                    new ProcessBuilder(
                            "reg",
                            "query",
                            "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize",
                            "/v",
                            "AppsUseLightTheme"
                    ).start();

            String texto =
                    new String(
                            process.getInputStream().readAllBytes()
                    );

            return texto.contains("0x0");

        } catch (Exception e) {

            /*
             * Se não for possível descobrir o tema,
             * assume o modo claro.
             */
            return false;
        }
    }

    public static void aplicarTemaInicial() {

        FlatLaf.setup(
                ultimoModoEscuro
                        ? new FlatDarkLaf()
                        : new FlatIntelliJLaf()
        );
    }

    public static void iniciarMonitor() {

        monitorTema.scheduleAtFixedRate(
                () -> {

                    try {

                        boolean modoEscuroAtual =
                                windowsEstaNoModoEscuro();

                        /*
                         * Só atualiza a interface quando
                         * realmente houver mudança.
                         */
                        if (modoEscuroAtual != ultimoModoEscuro) {

                            ultimoModoEscuro =
                                    modoEscuroAtual;

                            SwingUtilities.invokeLater(
                                    () -> {

                                        FlatLaf.setup(
                                                modoEscuroAtual
                                                        ? new FlatDarkLaf()
                                                        : new FlatIntelliJLaf()
                                        );

                                        FlatLaf.updateUI();

                                        /*
                                         * Atualiza todas as janelas
                                         * Swing abertas.
                                         *
                                         * Isso inclui JFrame e JDialog.
                                         */
                                        for (Window window :
                                                Window.getWindows()) {

                                            SwingUtilities
                                                    .updateComponentTreeUI(
                                                            window
                                                    );

                                            window.invalidate();
                                            window.validate();
                                            window.repaint();
                                        }
                                    }
                            );
                        }

                    } catch (Exception ex) {

                    }

                },
                2,
                2,
                TimeUnit.SECONDS
        );
    }

    public static void pararMonitor() {

        monitorTema.shutdownNow();
    }

    //endregion
}
