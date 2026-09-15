package com.selodigital.util;

import javax.swing.ImageIcon;
import java.awt.Image;
import java.net.URL;

/** Localiza e devolve o ícone usado nas janelas da aplicação, sem acoplar a interface ao recurso. */
public final class IconeAplicacao {

    // =======================================================
    // 1. ESTRUTURA DA CLASSE
    // =======================================================

    //region [Membros da Classe]

    private static final String CAMINHO =
            "/icones/selo_digital_png.png";

    private IconeAplicacao() {
    }

    public static Image obterImagem() {

        URL recurso =
                IconeAplicacao.class.getResource(
                        CAMINHO
                );

        if (recurso == null) {

            throw new IllegalStateException(
                    "Ícone da aplicação não encontrado: "
                            + CAMINHO
            );
        }

        return new ImageIcon(
                recurso
        ).getImage();
    }

    //endregion
}
