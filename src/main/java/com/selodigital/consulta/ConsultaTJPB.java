package com.selodigital.consulta;

import com.selodigital.modelo.*;
import com.selodigital.util.HttpUtil;

import java.net.http.HttpResponse;
import java.text.Normalizer;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/** Estratégia de consulta pública do TJPB, incluindo seus campos e QR Code próprios. */
public class ConsultaTJPB implements ConsultaTribunal {

    // =======================================================
    // 1. ESTRUTURA DA CLASSE
    // =======================================================

    //region [Membros da Classe]

    private static final String URL_BASE =
            "https://selodigital.tjpb.jus.br/selocgj/paginas/publico/"
                    + "visualizarSeloAtoPublico.jsf?seloPesquisaPublica=";

    @Override
    public Tribunal getTribunal() {
        return Tribunal.TJPB;
    }

    @Override
    public ResultadoConsulta consultar(Selo selo) {

        try {

            String numeroSelo = selo.getNumero();

            String url = URL_BASE + numeroSelo;

            HttpResponse<String> resposta = HttpUtil.get(url);

            StatusConsulta status =
                    analisarResposta(
                            resposta.statusCode(),
                            resposta.body(),
                            numeroSelo,
                            resposta.uri().toString()
                    );

            /*
             * ==================================================
             * SELO NÃO ENVIADO
             *
             * Não precisamos extrair os detalhes porque a página
             * não possui os dados do ato.
             * ==================================================
             */

            if (status == StatusConsulta.NAO_ENVIADO) {

                return new ResultadoConsulta(
                        selo,
                        Tribunal.TJPB,
                        status,
                        "",
                        "",
                        "",
                        java.util.Collections.emptyMap(),
                        null
                );
            }

            /*
             * ==================================================
             * SELO ENCONTRADO
             *
             * A própria resposta que acabou de chegar contém
             * os dados necessários para a tabela.
             *
             * NÃO fazemos uma segunda requisição.
             * ==================================================
             */

            Map<String, String> campos =
                    ParserTJPB.extrairCampos(
                            resposta.body()
                    );

            String tipoAto =
                    ParserTJPB.extrairTipoAto(
                            campos
                    );

            String subtipoAto =
                    ParserTJPB.extrairSubtipoAto(
                            campos
                    );

            String matriculaProtocolo =
                    ParserTJPB.extrairMatriculaProtocolo(
                            campos
                    );

            /*
             * ==================================================
             * NÚMERO DO SELO CONSULTADO
             * ==================================================
             *
             * O número que deve aparecer na tabela é o mesmo
             * número que foi enviado para a consulta.
             *
             * O TJPB pode apresentar no campo "Selo Nº" da
             * página um valor parcial, sem o sufixo completo.
             *
             * Portanto, NÃO substituímos o selo consultado
             * pelo valor encontrado no HTML.
             *
             * O objeto "selo" já contém o número normalizado
             * pelo SeloUtil antes da consulta.
             * ==================================================
             */

            Selo seloNormalizado =
                    selo;

            /*
             * ==================================================
             * QR CODE DO TJPB
             * ==================================================
             *
             * O conteúdo específico do QR Code pertence à
             * implementação do tribunal.
             *
             * A interface gráfica NÃO precisa conhecer
             * a URL do TJPB.
             * ==================================================
             */

            String qrCodeConteudo =
                    "https://selodigital.tjpb.jus.br/selocgj/QRCode?q="
                            + numeroSelo;

            return new ResultadoConsulta(
                    seloNormalizado,
                    Tribunal.TJPB,
                    status,
                    matriculaProtocolo,
                    tipoAto,
                    subtipoAto,
                    campos,
                    qrCodeConteudo
            );

        } catch (Exception e) {

            return new ResultadoConsulta(
                    selo,
                    Tribunal.TJPB,
                    StatusConsulta.ERRO
            );
        }
    }

    @Override
    public DetalhesSelo consultarDetalhes(Selo selo) {

        try {

            String numeroSelo = selo.getNumero();

            String url = URL_BASE + numeroSelo;

            HttpResponse<String> resposta = HttpUtil.get(url);

            StatusConsulta status =
                    analisarResposta(
                            resposta.statusCode(),
                            resposta.body(),
                            numeroSelo,
                            resposta.uri().toString()
                    );

            Map<String, String> campos =
                    ParserTJPB.extrairCampos(
                            resposta.body()
                    );

            String qrCodeConteudo =
                    "https://selodigital.tjpb.jus.br/selocgj/QRCode?q="
                            + numeroSelo;

            return new DetalhesSelo(
                    selo,
                    Tribunal.TJPB,
                    status,
                    campos,
                    qrCodeConteudo
            );

        } catch (Exception e) {

            return new DetalhesSelo(
                    selo,
                    Tribunal.TJPB,
                    StatusConsulta.ERRO,
                    new LinkedHashMap<>(),
                    ""
            );
        }
    }

    private StatusConsulta analisarResposta(
            int statusCode,
            String html,
            String numeroSelo,
            String urlFinal) {

        if (statusCode < 200 || statusCode >= 400) {

            return StatusConsulta.ERRO;
        }

        if (html == null || html.isBlank()) {

            return StatusConsulta.ERRO;
        }

        String conteudo = normalizar(html);

        /*
         * ==================================================
         * PÁGINA DE SELO ENCONTRADO
         * ==================================================
         *
         * Quando o selo existe, o TJPB retorna a página:
         *
         * visualizarSeloAtoPublico.jsf
         *
         * contendo os dados do ato.
         */

        boolean possuiSolicitante =
                conteudo.contains("<legend>solicitante</legend>");

        boolean possuiQrCode =
                conteudo.contains("qrcode-canvas")
                        || conteudo.contains("qrcode-svg");

        boolean possuiValidador =
                conteudo.contains("validador");

        boolean possuiViewState =
                conteudo.contains("javax.faces.viewstate");

        boolean possuiFormularioSelo =
                conteudo.contains("visualizarseloatoform");

        /*
         * ==================================================
         * PÁGINA DE CONSULTA
         * ==================================================
         *
         * Quando o selo não existe, o TJPB redireciona
         * para consultarSeloAtoPublico.jsf.
         *
         * Essa informação é mais confiável do que procurar
         * uma mensagem textual específica.
         */

        String urlFinalNormalizada =
                urlFinal == null
                        ? ""
                        : urlFinal.toLowerCase(Locale.ROOT);

        boolean voltouParaPaginaConsulta =
                urlFinalNormalizada.contains(
                        "consultarseloatopublico.jsf"
                );

        /*
         * ==================================================
         * SELO ENCONTRADO
         * ==================================================
         */

        boolean paginaVisualizacao =
                urlFinalNormalizada.contains(
                        "visualizarseloatopublico.jsf"
                );

        if (paginaVisualizacao
                && possuiSolicitante
                && possuiQrCode
                && possuiValidador
                && possuiViewState
                && possuiFormularioSelo) {

            return StatusConsulta.ENVIADO;
        }

        if (voltouParaPaginaConsulta) {

            return StatusConsulta.NAO_ENVIADO;
        }

        /*
         * ==================================================
         * OUTRAS SITUAÇÕES
         * ==================================================
         *
         * A comunicação funcionou, mas recebemos uma página
         * que não conseguimos identificar.
         */

        return StatusConsulta.ERRO;
    }

    private String normalizar(String texto) {

        String normalizado = Normalizer.normalize(
                texto,
                Normalizer.Form.NFD
        );

        return normalizado
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
    }

    //endregion
}
