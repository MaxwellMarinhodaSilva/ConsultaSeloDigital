package com.selodigital.util;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;

/** Abre a consulta pública do TJAL sem realizar consulta automática pela aplicação. */
public final class PortalTjalUtil {

    private static final String URL_BASE =
            "https://seloapp.tjal.jus.br/publico/selo/";

    private PortalTjalUtil() {
    }

    public static URI criarUrlConsulta(String selo) {

        String valor = selo == null ? "" : selo.trim();

        if (!valor.matches("[A-Za-z0-9]{8}-[A-Za-z0-9]{4}")) {
            throw new IllegalArgumentException(
                    "Informe um selo TJAL no formato ABC12345-X1X2."
            );
        }

        return URI.create(URL_BASE + valor);
    }

    public static void abrirConsulta(String selo) throws IOException {

        URI url = criarUrlConsulta(selo);

        if (!Desktop.isDesktopSupported()
                || !Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {

            throw new IOException(
                    "O navegador padrão não está disponível neste sistema."
            );
        }

        Desktop.getDesktop().browse(url);
    }
}
