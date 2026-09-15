package com.selodigital.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Lê e grava as preferências locais sem expor detalhes do arquivo properties à interface.
 * Dados ausentes ou numéricos inválidos preservam os valores padrão de {@link Configuracao}.
 */
public final class ConfiguracaoService {

    // =======================================================
    // 1. ESTRUTURA DA CLASSE
    // =======================================================

    //region [Membros da Classe]

    private ConfiguracaoService() {
    }

    private static final String NOME_ARQUIVO =
            "config.properties";


    /*
     * ============================================================
     * LOCAL DO ARQUIVO DE CONFIGURAÇÃO
     * ============================================================
     */

    private static File obterArquivoConfiguracao() {

        return new File(
                System.getProperty("user.dir"),
                NOME_ARQUIVO
        );
    }


    /*
     * ============================================================
     * CARREGAR CONFIGURAÇÃO
     * ============================================================
     */

    public static Configuracao carregar()
            throws IOException {

        Configuracao configuracao =
                new Configuracao();

        File arquivo =
                obterArquivoConfiguracao();

        /*
         * Se ainda não existir configuração,
         * utiliza os valores padrão da classe
         * Configuracao.
         */
        if (!arquivo.exists()) {

            return configuracao;
        }

        Properties properties =
                new Properties();

        try (FileInputStream input =
                     new FileInputStream(arquivo)) {

            properties.load(input);
        }

        configuracao.setJanelaPrincipalX(
                lerInteiro(
                        properties,
                        "janela.principal.x",
                        configuracao.getJanelaPrincipalX()
                )
        );

        configuracao.setJanelaPrincipalY(
                lerInteiro(
                        properties,
                        "janela.principal.y",
                        configuracao.getJanelaPrincipalY()
                )
        );

        configuracao.setJanelaPrincipalLargura(
                lerInteiro(
                        properties,
                        "janela.principal.largura",
                        configuracao.getJanelaPrincipalLargura()
                )
        );

        configuracao.setJanelaPrincipalAltura(
                lerInteiro(
                        properties,
                        "janela.principal.altura",
                        configuracao.getJanelaPrincipalAltura()
                )
        );

        return configuracao;
    }


    /*
     * ============================================================
     * SALVAR CONFIGURAÇÃO
     * ============================================================
     */

    public static void salvar(
            Configuracao configuracao)
            throws IOException {

        Properties properties =
                new Properties();

        properties.setProperty(
                "janela.principal.x",
                String.valueOf(
                        configuracao.getJanelaPrincipalX()
                )
        );

        properties.setProperty(
                "janela.principal.y",
                String.valueOf(
                        configuracao.getJanelaPrincipalY()
                )
        );

        properties.setProperty(
                "janela.principal.largura",
                String.valueOf(
                        configuracao.getJanelaPrincipalLargura()
                )
        );

        properties.setProperty(
                "janela.principal.altura",
                String.valueOf(
                        configuracao.getJanelaPrincipalAltura()
                )
        );

        File arquivo =
                obterArquivoConfiguracao();

        try (FileOutputStream output =
                     new FileOutputStream(arquivo)) {

            properties.store(
                    output,
                    "Configuracoes - Consulta Selo Digital"
            );
        }
    }


    /*
     * ============================================================
     * LEITURA SEGURA DE NÚMEROS
     * ============================================================
     */

    private static int lerInteiro(
            Properties properties,
            String chave,
            int valorPadrao) {

        String valor =
                properties.getProperty(chave);

        if (valor == null) {

            return valorPadrao;
        }

        try {

            return Integer.parseInt(
                    valor.trim()
            );

        } catch (NumberFormatException ex) {

            return valorPadrao;
        }
    }

    //endregion
}
