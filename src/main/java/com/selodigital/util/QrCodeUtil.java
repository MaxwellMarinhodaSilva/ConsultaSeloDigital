package com.selodigital.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/** Gera imagens QR Code a partir do conteúdo fornecido pelas consultas que o suportam. */
public final class QrCodeUtil {

    // =======================================================
    // 1. ESTRUTURA DA CLASSE
    // =======================================================

    //region [Membros da Classe]

    private QrCodeUtil() {
    }

    public static BufferedImage gerar(
            String texto,
            int largura,
            int altura) {

        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException(
                    "O conteúdo do QR Code não pode ser vazio."
            );
        }

        try {

            Map<EncodeHintType, Object> configuracoes =
                    new HashMap<>();

            configuracoes.put(
                    EncodeHintType.MARGIN,
                    1
            );

            BitMatrix matriz =
                    new MultiFormatWriter().encode(
                            texto,
                            BarcodeFormat.QR_CODE,
                            largura,
                            altura,
                            configuracoes
                    );

            BufferedImage imagem =
                    new BufferedImage(
                            largura,
                            altura,
                            BufferedImage.TYPE_INT_RGB
                    );

            for (int x = 0; x < largura; x++) {

                for (int y = 0; y < altura; y++) {

                    imagem.setRGB(
                            x,
                            y,
                            matriz.get(x, y)
                                    ? 0xFF000000
                                    : 0xFFFFFFFF
                    );
                }
            }

            return imagem;

        } catch (Exception e) {

            throw new IllegalStateException(
                    "Não foi possível gerar o QR Code.",
                    e
            );
        }
    }

    //endregion
}
