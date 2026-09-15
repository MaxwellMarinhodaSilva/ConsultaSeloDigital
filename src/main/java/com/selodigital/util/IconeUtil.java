package com.selodigital.util;

import javax.swing.Icon;
import javax.swing.JComponent;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;

/** Desenha ícones vetoriais leves, usando a cor ativa do tema FlatLaf. */
public final class IconeUtil {

    // =======================================================
    // 1. ESTRUTURA DA CLASSE
    // =======================================================

    //region [Membros da Classe]

    private static final int TAMANHO = 18;

    private IconeUtil() {
    }

    public static Icon limpar() {
        return criarIcone(
                IconeUtil::desenharLimpar
        );
    }

    public static Icon consultar() {
        return criarIcone(
                IconeUtil::desenharConsultar
        );
    }

    public static Icon fechar() {
        return criarIcone(
                IconeUtil::desenharFechar
        );
    }

    public static Icon baixar() {
        return criarIcone(
                IconeUtil::desenharBaixar
        );
    }

    public static Icon historico() {
        return criarIcone(IconeUtil::desenharHistorico);
    }

    public static Icon abrir() {
        return criarIcone(IconeUtil::desenharAbrir);
    }

    public static Icon apagar() {
        return criarIcone(IconeUtil::desenharApagar);
    }

    public static Icon copiar() {
        return criarIcone(IconeUtil::desenharCopiar);
    }

    public static Icon refazerConsulta() {
        return criarIcone(IconeUtil::desenharRefazerConsulta);
    }

    private static Icon criarIcone(
            Desenhador desenhador) {

        BufferedImage imagem =
                new BufferedImage(
                        TAMANHO,
                        TAMANHO,
                        BufferedImage.TYPE_INT_ARGB
                );

        Graphics2D g =
                imagem.createGraphics();

        try {

            g.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            g.setRenderingHint(
                    RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY
            );

            desenhador.desenhar(g);

        } finally {

            g.dispose();
        }

        return new javax.swing.ImageIcon(
                imagem
        );
    }

    private static void preparar(
            Graphics2D g) {

        g.setColor(
                obterCor()
        );

        g.setStroke(
                new BasicStroke(
                        1.8f,
                        BasicStroke.CAP_ROUND,
                        BasicStroke.JOIN_ROUND
                )
        );
    }

    private static Color obterCor() {

        Color cor =
                javax.swing.UIManager.getColor(
                        "Button.foreground"
                );

        if (cor == null) {
            return Color.DARK_GRAY;
        }

        return cor;
    }

    private static void desenharLimpar(
            Graphics2D g) {

        preparar(g);

        Path2D borracha =
                new Path2D.Double();

        borracha.moveTo(3, 12);
        borracha.lineTo(9, 6);
        borracha.quadTo(10, 5, 11, 6);
        borracha.lineTo(15, 10);
        borracha.quadTo(16, 11, 15, 12);
        borracha.lineTo(10, 17);
        borracha.lineTo(3, 17);
        borracha.closePath();

        g.draw(borracha);
        g.drawLine(7, 16, 14, 9);
        g.drawLine(3, 17, 16, 17);
    }

    private static void desenharConsultar(
            Graphics2D g) {

        preparar(g);

        g.drawOval(
                2,
                2,
                10,
                10
        );

        g.drawLine(9, 9, 12, 12);
        g.drawLine(
                12,
                12,
                16,
                16
        );
    }

    private static void desenharFechar(
            Graphics2D g) {

        preparar(g);

        g.drawRoundRect(2, 2, 14, 14, 4, 4);
        g.drawLine(6, 6, 12, 12);
        g.drawLine(12, 6, 6, 12);
    }

    private static void desenharBaixar(
            Graphics2D g) {

        preparar(g);

        /*
         * Seta para baixo.
         */

        g.drawLine(
                9,
                3,
                9,
                12
        );

        g.drawLine(
                5,
                9,
                9,
                13
        );

        g.drawLine(
                9,
                13,
                13,
                9
        );

        /*
         * Linha inferior.
         */

        g.drawLine(
                4,
                16,
                14,
                16
        );
    }

    private static void desenharHistorico(Graphics2D g) {
        preparar(g);
        g.drawOval(3, 3, 12, 12);
        g.drawLine(9, 6, 9, 10);
        g.drawLine(9, 10, 12, 12);
        g.drawLine(3, 9, 6, 9);
    }

    private static void desenharAbrir(Graphics2D g) {
        preparar(g);
        g.drawRect(3, 7, 12, 8);
        g.drawLine(4, 7, 7, 4);
        g.drawLine(7, 4, 11, 4);
        g.drawLine(10, 11, 15, 6);
        g.drawLine(15, 6, 12, 6);
        g.drawLine(15, 6, 15, 9);
    }

    private static void desenharApagar(Graphics2D g) {
        preparar(g);
        g.drawRect(5, 6, 8, 10);
        g.drawLine(4, 6, 14, 6);
        g.drawLine(7, 4, 11, 4);
        g.drawLine(8, 9, 8, 13);
        g.drawLine(10, 9, 10, 13);
    }

    private static void desenharCopiar(Graphics2D g) {
        preparar(g);
        g.drawRoundRect(6, 2, 8, 10, 2, 2);
        g.drawRoundRect(3, 6, 8, 10, 2, 2);
    }

    private static void desenharRefazerConsulta(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        preparar(g);

        // Define pontas arredondadas para que o desenho das setas fique suave
        g.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        // ---- 1. LUPA CENTRALIZADA ----
        g.drawOval(4, 4, 8, 8);       // Lente da lupa centralizada (tamanho 8x8)
        g.drawLine(10, 10, 14, 14);   // Cabo da lupa apontando para o canto inferior direito

        // ---- 2. SETA SUPERIOR (Afastada para fora - Diâmetro mudou de 12 para 14) ----
        g.drawArc(1, 1, 14, 14, 35, 120); // Arco superior aberto e ligeiramente mais curto
        // Ponta da seta superior esquerda (recalculada para a nova borda externa)
        g.drawLine(2, 5, 1, 8);
        g.drawLine(1, 8, 5, 8);

        // ---- 3. SETA INFERIOR (Afastada para fora - Diâmetro mudou de 12 para 14) ----
        g.drawArc(1, 1, 14, 14, 215, 120); // Arco inferior aberto e ligeiramente mais curto
        // Ponta da seta inferior direita (recalculada para a nova borda externa)
        g.drawLine(14, 11, 15, 8);
        g.drawLine(15, 8, 11, 8);
    }


    @FunctionalInterface
    private interface Desenhador {

        void desenhar(Graphics2D g);
    }

    //endregion
}
