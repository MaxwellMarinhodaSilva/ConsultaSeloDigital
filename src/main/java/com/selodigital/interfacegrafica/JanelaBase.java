package com.selodigital.interfacegrafica;

import com.selodigital.util.IconeAplicacao;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;


/** Base das janelas Swing: aplica ícone, conteúdo com margem e rodapé padronizado. */
public abstract class JanelaBase extends JFrame {

    // =======================================================
    // 1. ESTRUTURA DA CLASSE
    // =======================================================

    //region [Membros da Classe]

    private final JPanel painelRaiz;
    private final JPanel painelConteudo;

    protected JanelaBase() {

        setIconImage(
                IconeAplicacao.obterImagem()
        );

        /*
         * ==================================================
         * CONFIGURAÇÕES PADRÃO DA JANELA
         * ==================================================
         */

        setResizable(true);

        painelRaiz =
                new JPanel(
                        new BorderLayout()
                );

        painelRaiz.setBorder(
                BorderFactory.createEmptyBorder(
                        0,
                        0,
                        0,
                        0
                )
        );

        painelConteudo =
                new JPanel(
                        new BorderLayout()
                );

        painelRaiz.add(
                painelConteudo,
                BorderLayout.CENTER
        );

        setContentPane(
                painelRaiz
        );
    }

    protected final void configurarConteudo(
            JPanel conteudo) {

        painelConteudo.removeAll();

        painelConteudo.setBorder(
                BorderFactory.createEmptyBorder(
                        20,
                        25,
                        20,
                        25
                )
        );

        painelConteudo.add(
                conteudo,
                BorderLayout.CENTER
        );

        painelConteudo.revalidate();
        painelConteudo.repaint();
    }

    protected final void configurarRodape(
            JPanel painelBotoes) {

        painelRaiz.add(
                RodapeJanela.criar(
                        painelBotoes
                ),
                BorderLayout.SOUTH
        );

        painelRaiz.revalidate();
        painelRaiz.repaint();
    }

    protected final void configurarTamanhoMinimo(
            int largura,
            int altura) {

        setMinimumSize(
                new Dimension(
                        largura,
                        altura
                )
        );
    }

    /**
     * Centraliza esta janela na área útil do monitor onde a janela originadora
     * está no momento da abertura. Quando a origem ocupa duas telas, é escolhido
     * o monitor com a maior área compartilhada.
     */
    protected final void posicionarNoMonitorDa(
            Window janelaOrigem) {

        if (janelaOrigem == null) {
            setLocationRelativeTo(null);
            return;
        }

        GraphicsConfiguration configuracao =
                encontrarConfiguracaoDaJanela(
                        janelaOrigem
                );

        Rectangle limitesMonitor =
                configuracao.getBounds();

        Insets margensSistema =
                Toolkit.getDefaultToolkit().getScreenInsets(
                        configuracao
                );

        Rectangle areaUtil =
                new Rectangle(
                        limitesMonitor.x + margensSistema.left,
                        limitesMonitor.y + margensSistema.top,
                        limitesMonitor.width - margensSistema.left - margensSistema.right,
                        limitesMonitor.height - margensSistema.top - margensSistema.bottom
                );

        int posicaoX =
                areaUtil.x + (areaUtil.width - getWidth()) / 2;

        int posicaoY =
                areaUtil.y + (areaUtil.height - getHeight()) / 2;

        setLocation(
                limitar(posicaoX, areaUtil.x, areaUtil.x + areaUtil.width - getWidth()),
                limitar(posicaoY, areaUtil.y, areaUtil.y + areaUtil.height - getHeight())
        );
    }

    private static GraphicsConfiguration encontrarConfiguracaoDaJanela(
            Window janela) {

        Rectangle limitesJanela =
                janela.getBounds();

        GraphicsConfiguration configuracaoEncontrada =
                janela.getGraphicsConfiguration();

        long maiorAreaCompartilhada =
                -1;

        for (
                GraphicsDevice dispositivo
                : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()
        ) {

            GraphicsConfiguration configuracao =
                    dispositivo.getDefaultConfiguration();

            Rectangle intersecao =
                    limitesJanela.intersection(
                            configuracao.getBounds()
                    );

            long areaCompartilhada =
                    Math.max(0, intersecao.width)
                            * (long) Math.max(0, intersecao.height);

            if (areaCompartilhada > maiorAreaCompartilhada) {
                maiorAreaCompartilhada = areaCompartilhada;
                configuracaoEncontrada = configuracao;
            }
        }

        return configuracaoEncontrada != null
                ? configuracaoEncontrada
                : GraphicsEnvironment.getLocalGraphicsEnvironment()
                        .getDefaultScreenDevice()
                        .getDefaultConfiguration();
    }

    private static int limitar(
            int valor,
            int minimo,
            int maximo) {

        return Math.max(
                minimo,
                Math.min(valor, Math.max(minimo, maximo))
        );
    }

    //endregion
}
