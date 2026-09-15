package com.selodigital.util;

import com.selodigital.modelo.Tribunal;

import javax.swing.ImageIcon;
import java.awt.Image;
import java.net.URL;

/** Centraliza o carregamento e a escala dos logotipos exibidos para cada tribunal. */
public final class LogoUtil {

    // =======================================================
    // 1. ESTRUTURA DA CLASSE
    // =======================================================

    //region [Membros da Classe]

    private LogoUtil() {
    }

    /*
     * ==================================================
     * LOGO PRINCIPAL DA APLICAÇÃO
     * ==================================================
     */

    public static ImageIcon obterLogoPrincipal(
            int largura,
            int altura) {

        return carregarRedimensionada(
                "/logos/logo_principal.png",
                largura,
                altura
        );
    }

    /*
     * ==================================================
     * LOGO DO TRIBUNAL
     * ==================================================
     */

    public static ImageIcon obterLogoTribunal(
            Tribunal tribunal,
            int largura,
            int altura) {

        if (tribunal == null) {

            return null;
        }

        String caminho;

        switch (tribunal) {

            case TJPB:

                caminho =
                        "/logos/logo_tjpb.png";

                break;

            case TJRN:

                caminho =
                        "/logos/logo_tjrn.png";

                break;

            case TJPE:

                caminho =
                        "/logos/logo_tjpe.png";

                break;

            default:

                return null;
        }

        return carregarRedimensionadaPreservandoProporcao(
                caminho,
                largura,
                altura
        );
    }

    /*
     * ==================================================
     * CARREGAMENTO DA IMAGEM
     * ==================================================
     */

    private static ImageIcon carregarRedimensionada(
            String caminho,
            int largura,
            int altura) {

        URL recurso =
                LogoUtil.class.getResource(
                        caminho
                );

        if (recurso == null) {

            return null;
        }

        Image imagem =
                new ImageIcon(
                        recurso
                ).getImage();

        Image imagemRedimensionada =
                imagem.getScaledInstance(
                        largura,
                        altura,
                        Image.SCALE_SMOOTH
                );

        return new ImageIcon(
                imagemRedimensionada
        );
    }

    private static ImageIcon carregarRedimensionadaPreservandoProporcao(
            String caminho,
            int largura,
            int altura) {

        URL recurso =
                LogoUtil.class.getResource(
                        caminho
                );

        if (recurso == null) {

            return null;
        }

        Image imagem =
                new ImageIcon(
                        recurso
                ).getImage();

        int larguraOriginal =
                imagem.getWidth(
                        null
                );

        int alturaOriginal =
                imagem.getHeight(
                        null
                );

        double escala = Math.min(
                (double) largura / larguraOriginal,
                (double) altura / alturaOriginal
        );

        int larguraRedimensionada =
                Math.max(
                        1,
                        (int) Math.round(
                                larguraOriginal * escala
                        )
                );

        int alturaRedimensionada =
                Math.max(
                        1,
                        (int) Math.round(
                                alturaOriginal * escala
                        )
                );

        Image imagemRedimensionada =
                imagem.getScaledInstance(
                        larguraRedimensionada,
                        alturaRedimensionada,
                        Image.SCALE_SMOOTH
                );

        return new ImageIcon(
                imagemRedimensionada
        );
    }

    //endregion
}
