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

        String urlSolicitada = "";

        try {

            String numeroSelo = selo.getNumero();

            urlSolicitada = URL_BASE + numeroSelo;

            HttpResponse<String> resposta = HttpUtil.get(urlSolicitada);

            DiagnosticoConsulta diagnostico = HttpUtil.diagnosticarResposta(
                    urlSolicitada,
                    resposta
            );

            StatusConsulta status =
                    analisarResposta(
                            resposta.statusCode(),
                            resposta.body(),
                            numeroSelo,
                            resposta.uri().toString()
                    );

            diagnostico = ajustarDiagnostico(status, diagnostico);

            /*
             * ==================================================
             * SELO NÃO ENVIADO
             *
             * Não precisamos extrair os detalhes porque a página
             * não possui os dados do ato.
             * ==================================================
             */

            if (status != StatusConsulta.ENVIADO) {

                return new ResultadoConsulta(
                        selo,
                        Tribunal.TJPB,
                        status,
                        "",
                        "",
                        "",
                        java.util.Collections.emptyMap(),
                        null,
                        diagnostico
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

            Map<String, String> campos;

            try {
                campos = ParserTJPB.extrairCampos(resposta.body());
            } catch (RuntimeException e) {
                return resultadoComFalhaDeParsing(selo, diagnostico, e);
            }

            if (!correspondeAoSeloConsultado(campos, numeroSelo)) {

                return new ResultadoConsulta(
                        selo,
                        Tribunal.TJPB,
                        StatusConsulta.ERRO,
                        "",
                        "",
                        "",
                        java.util.Collections.emptyMap(),
                        null,
                        diagnostico.comTipoFalha(
                                TipoFalhaConsulta.RESPOSTA_INESPERADA,
                                null
                        )
                );
            }

            String tipoAto;
            String subtipoAto;
            String matriculaProtocolo;

            try {
                tipoAto = ParserTJPB.extrairTipoAto(campos);
                subtipoAto = ParserTJPB.extrairSubtipoAto(campos);
                matriculaProtocolo = ParserTJPB.extrairMatriculaProtocolo(campos);
            } catch (RuntimeException e) {
                return resultadoComFalhaDeParsing(selo, diagnostico, e);
            }

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
                    qrCodeConteudo,
                    diagnostico
            );

        } catch (Exception e) {

            return new ResultadoConsulta(
                    selo,
                    Tribunal.TJPB,
                    StatusConsulta.ERRO,
                    "",
                    "",
                    "",
                    java.util.Collections.emptyMap(),
                    null,
                    HttpUtil.diagnosticarExcecao(urlSolicitada, e)
            );
        }
    }

    @Override
    public DetalhesSelo consultarDetalhes(Selo selo) {

        String urlSolicitada = "";

        try {

            String numeroSelo = selo.getNumero();

            urlSolicitada = URL_BASE + numeroSelo;

            HttpResponse<String> resposta = HttpUtil.get(urlSolicitada);

            DiagnosticoConsulta diagnostico = HttpUtil.diagnosticarResposta(
                    urlSolicitada,
                    resposta
            );

            StatusConsulta status =
                    analisarResposta(
                            resposta.statusCode(),
                            resposta.body(),
                            numeroSelo,
                            resposta.uri().toString()
                    );

            diagnostico = ajustarDiagnostico(
                    status,
                    diagnostico
            );

            Map<String, String> campos;
            try {
                campos = status == StatusConsulta.ENVIADO
                        ? ParserTJPB.extrairCampos(resposta.body())
                        : new LinkedHashMap<>();
            } catch (RuntimeException e) {
                return detalhesComFalhaDeParsing(selo, diagnostico, e);
            }

            if (status == StatusConsulta.ENVIADO
                    && !correspondeAoSeloConsultado(campos, numeroSelo)) {

                status = StatusConsulta.ERRO;
                campos = new LinkedHashMap<>();
                diagnostico = diagnostico.comTipoFalha(
                        TipoFalhaConsulta.RESPOSTA_INESPERADA,
                        null
                );
            }

            String qrCodeConteudo =
                    "https://selodigital.tjpb.jus.br/selocgj/QRCode?q="
                            + numeroSelo;

            return new DetalhesSelo(
                    selo,
                    Tribunal.TJPB,
                    status,
                    campos,
                    qrCodeConteudo,
                    diagnostico
            );

        } catch (Exception e) {

            return new DetalhesSelo(
                    selo,
                    Tribunal.TJPB,
                    StatusConsulta.ERRO,
                    new LinkedHashMap<>(),
                    "",
                    HttpUtil.diagnosticarExcecao(urlSolicitada, e)
            );
        }
    }

    private DiagnosticoConsulta ajustarDiagnostico(
            StatusConsulta status,
            DiagnosticoConsulta diagnostico) {

        if (status != StatusConsulta.ERRO
                && diagnostico.temFalha()) {

            return diagnostico.comTipoFalha(
                    TipoFalhaConsulta.SEM_FALHA,
                    null
            );
        }

        if (status == StatusConsulta.ERRO
                && !diagnostico.temFalha()) {

            return diagnostico.comTipoFalha(
                    TipoFalhaConsulta.RESPOSTA_INESPERADA,
                    null
            );
        }

        return diagnostico;
    }

    private ResultadoConsulta resultadoComFalhaDeParsing(
            Selo selo,
            DiagnosticoConsulta diagnostico,
            RuntimeException e) {

        return new ResultadoConsulta(
                selo,
                Tribunal.TJPB,
                StatusConsulta.ERRO,
                "",
                "",
                "",
                java.util.Collections.emptyMap(),
                null,
                diagnostico.comTipoFalha(TipoFalhaConsulta.FALHA_DE_PARSING, e)
        );
    }

    private DetalhesSelo detalhesComFalhaDeParsing(
            Selo selo,
            DiagnosticoConsulta diagnostico,
            RuntimeException e) {

        return new DetalhesSelo(
                selo,
                Tribunal.TJPB,
                StatusConsulta.ERRO,
                new LinkedHashMap<>(),
                "",
                diagnostico.comTipoFalha(TipoFalhaConsulta.FALHA_DE_PARSING, e)
        );
    }

    static StatusConsulta analisarResposta(
            int statusCode,
            String html,
            String numeroSelo,
            String urlFinal) {

        if (statusCode < 200 || statusCode >= 300) {

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

    /*
     * O portal expõe o código do selo e o validador em campos separados.
     * Quando ambos estão presentes, eles precisam corresponder exatamente ao
     * identificador consultado; em layouts que omitam um deles mantemos a
     * validação estrutural para não rejeitar um formato ainda não comprovado.
     */
    static boolean correspondeAoSeloConsultado(
            Map<String, String> campos,
            String numeroSelo) {

        if (campos == null || numeroSelo == null) {
            return true;
        }

        String esperado = numeroSelo
                .replaceAll("[^A-Za-z0-9]", "")
                .toUpperCase(Locale.ROOT);

        if (!esperado.matches("[A-Z0-9]{12}")) {
            return true;
        }

        String numeroNaPagina = campos.getOrDefault(
                "Informações do Selo - Selo Nº",
                ""
        ).replaceAll("[^A-Za-z0-9]", "").toUpperCase(Locale.ROOT);

        String validadorNaPagina = campos.getOrDefault(
                "Informações do Selo - Validador",
                ""
        ).replaceAll("[^A-Za-z0-9]", "").toUpperCase(Locale.ROOT);

        if (numeroNaPagina.isBlank() || validadorNaPagina.isBlank()) {
            return true;
        }

        return esperado.substring(0, 8).equals(numeroNaPagina)
                && esperado.substring(8).equals(validadorNaPagina);
    }

    private static String normalizar(String texto) {

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
