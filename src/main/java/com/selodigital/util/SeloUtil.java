package com.selodigital.util;

import com.selodigital.modelo.Tribunal;

/** Aplica as regras de limpeza e normalização próprias do identificador de cada tribunal. */
public final class SeloUtil {

    // =======================================================
    // 1. ESTRUTURA DA CLASSE
    // =======================================================

    //region [Membros da Classe]

    private SeloUtil() {
    }

    public static String limpar(String selo) {

        if (selo == null) {
            return "";
        }

        return selo.trim();
    }

    /*
     * ==================================================
     * NORMALIZAÇÃO DO SELO DE ACORDO COM O TRIBUNAL
     * ==================================================
     */

    public static String normalizar(
            String selo,
            Tribunal tribunal) {

        if (selo == null) {
            return "";
        }

        String valor =
                selo.trim()
                        .toUpperCase();

        if (valor.isBlank()) {
            return "";
        }

        if (tribunal == Tribunal.TJPB) {
            return normalizarTJPB(valor);
        }

        if (tribunal == Tribunal.TJPE) {
            return normalizarTJPE(selo);
        }

        return valor;
    }

    private static String normalizarTJPE(String hash) {

        return hash.trim()
                .replaceAll("\\s+", "")
                .toLowerCase();
    }

    /*
     * ==================================================
     * PADRÃO TJPB
     *
     * Exemplo:
     *
     * ASL27183EQ46
     *
     * vira:
     *
     * ASL27183-EQ46
     *
     * ==================================================
     */

    private static String normalizarTJPB(
            String selo) {

        /*
         * Remove espaços.
         */
        String valor =
                selo.replaceAll(
                        "\\s+",
                        ""
                );

        /*
         * Remove separadores digitados pelo usuário.
         *
         * Depois colocaremos o hífen exatamente no
         * lugar correto.
         */
        valor =
                valor.replace(
                        "-",
                        ""
                );

        /*
         * O padrão informado para o TJPB é:
         *
         * ASL27183-EQ46
         *
         * 8 caracteres antes do hífen
         * 4 caracteres depois.
         *
         * Caso o valor possua exatamente 12 caracteres,
         * formatamos dessa maneira.
         */

        if (valor.matches(
                "[A-Z0-9]{12}"
        )) {

            return valor.substring(0, 8)
                    + "-"
                    + valor.substring(8);
        }

        /*
         * Se já vier em algum formato diferente,
         * não inventamos caracteres.
         *
         * Apenas devolvemos o valor limpo.
         */
        return valor;
    }

    //endregion
}
