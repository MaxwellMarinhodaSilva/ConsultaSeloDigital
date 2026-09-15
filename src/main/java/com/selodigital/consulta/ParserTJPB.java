package com.selodigital.consulta;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Converte o HTML do TJPB no mapa ordenado consumido pelas telas e exportações.
 * Combina os blocos estáveis do selo com seções específicas do ato descobertas dinamicamente.
 */
public final class ParserTJPB {

    // =======================================================
    // 1. ESTRUTURA DA CLASSE
    // =======================================================

    //region [Membros da Classe]

    private ParserTJPB() {
    }

    public static Map<String, String> extrairCampos(String html) {

        Map<String, String> campos =
                new LinkedHashMap<>();

        if (html == null || html.isBlank()) {
            return campos;
        }

        /*
         * ==================================================
         * INFORMAÇÕES DA SERVENTIA
         * ==================================================
         */

        adicionarCampo(
                campos,
                "Informações da Serventia - Serventia",
                extrairCampo(html, "Serventia")
        );

        adicionarCampo(
                campos,
                "Informações da Serventia - Endereço",
                extrairCampo(html, "Endereço")
        );

        adicionarCampo(
                campos,
                "Informações da Serventia - Bairro",
                extrairCampo(html, "Bairro")
        );

        adicionarCampo(
                campos,
                "Informações da Serventia - Município",
                extrairCampo(html, "Município")
        );

        adicionarCampo(
                campos,
                "Informações da Serventia - E-mail",
                extrairEmail(html)
        );

        adicionarCampo(
                campos,
                "Informações da Serventia - Telefone",
                extrairCampo(html, "Telefone")
        );

        adicionarCampo(
                campos,
                "Informações da Serventia - Cobrança",
                extrairCampo(html, "Cobrança")
        );

        /*
         * ==================================================
         * INFORMAÇÕES DO ATO
         * ==================================================
         */

        adicionarCampo(
                campos,
                "Informações do Ato - Tipo Ato",
                extrairCampo(
                        html,
                        "Informações do ato",
                        "Tipo Ato"
                )
        );

        adicionarCampo(
                campos,
                "Informações do Ato - Responsável",
                extrairCampo(
                        html,
                        "Informações do ato",
                        "Responsável"
                )
        );

        adicionarCampo(
                campos,
                "Informações do Ato - Valor emolumento",
                extrairCampo(
                        html,
                        "Informações do ato",
                        "Valor emolumento"
                )
        );

        adicionarCampo(
                campos,
                "Informações do Ato - Número do recibo",
                extrairCampo(
                        html,
                        "Informações do ato",
                        "Número do recibo"
                )
        );

        adicionarCampo(
                campos,
                "Informações do Ato - Valor do Ato",
                extrairCampo(
                        html,
                        "Informações do ato",
                        "Valor do Ato"
                )
        );

        adicionarCampo(
                campos,
                "Informações do Ato - Data do ato",
                extrairCampo(
                        html,
                        "Informações do ato",
                        "Data do ato"
                )
        );

        adicionarCampo(
                campos,
                "Informações do Ato - Data do recibo",
                extrairCampo(
                        html,
                        "Informações do ato",
                        "Data do recibo"
                )
        );

        adicionarCampo(
                campos,
                "Informações do Ato - Retificador",
                extrairCampo(
                        html,
                        "Informações do ato",
                        "Retificador"
                )
        );

        adicionarCampo(
                campos,
                "Informações do Ato - Selo original",
                extrairCampo(
                        html,
                        "Informações do ato",
                        "Selo original"
                )
        );

        adicionarCampo(
                campos,
                "Informações do Ato - Recolhimento FARPEN",
                extrairCampo(
                        html,
                        "Informações do ato",
                        "Recolhimento FARPEN"
                )
        );

        adicionarCampo(
                campos,
                "Informações do Ato - Recolhimento FEPJ",
                extrairCampo(
                        html,
                        "Informações do ato",
                        "Recolhimento FEPJ"
                )
        );

        adicionarCampo(
                campos,
                "Informações do Ato - Retificado por",
                extrairCampo(
                        html,
                        "Informações do ato",
                        "Retificado por"
                )
        );

        /*
         * ==================================================
         * INFORMAÇÕES DO SELO
         * ==================================================
         */

        adicionarCampo(
                campos,
                "Informações do Selo - Tipo",
                extrairCampo(
                        html,
                        "Informações do selo",
                        "Tipo"
                )
        );

        adicionarCampo(
                campos,
                "Informações do Selo - Valor",
                extrairCampo(
                        html,
                        "Informações do selo",
                        "Valor"
                )
        );

        adicionarCampo(
                campos,
                "Informações do Selo - Selo Nº",
                extrairCampo(
                        html,
                        "Informações do selo",
                        "Selo Nº"
                )
        );

        adicionarCampo(
                campos,
                "Informações do Selo - Validador",
                extrairCampo(
                        html,
                        "Informações do selo",
                        "Validador"
                )
        );

        /*
         * ==================================================
         * SOLICITANTE
         * ==================================================
         */

        adicionarCampo(
                campos,
                "Solicitante - Nome",
                extrairCampo(
                        html,
                        "Solicitante",
                        "Nome"
                )
        );

        adicionarCampo(
                campos,
                "Solicitante - Pessoa",
                extrairCampo(
                        html,
                        "Solicitante",
                        "Pessoa"
                )
        );

        /*
         * ==================================================
         * SEÇÕES DINÂMICAS DO ATO
         * ==================================================
         *
         * O TJPB pode apresentar diferentes seções
         * dependendo do Tipo Ato.
         *
         * Exemplo:
         *
         * Registro de Imóveis
         * Registro
         * Parte 1
         * Detalhes Imóvel 1
         *
         * ou:
         *
         * Registros de Documentos e Títulos e Civis
         * Registro
         * Parte 1
         *
         * A interface não deve precisar conhecer
         * antecipadamente todas essas possibilidades.
         */

        Map<String, SecaoTJPB> secoes =
                extrairSecoesDinamicas(html);

        for (SecaoTJPB secao : secoes.values()) {

            for (
                    Map.Entry<String, String> campo
                    : secao.getCampos().entrySet()
            ) {

                adicionarCampo(
                        campos,
                        secao.getNome()
                                + " - "
                                + campo.getKey(),
                        campo.getValue()
                );
            }
        }

        return campos;
    }

    /*
     * ==================================================
     * ADICIONA O CAMPO MESMO QUANDO ESTÁ VAZIO
     * ==================================================
     */

    private static void adicionarCampo(
            Map<String, String> campos,
            String nome,
            String valor) {

        if (valor == null) {
            valor = "";
        }

        campos.put(
                nome,
                valor.trim()
        );
    }

    /*
     * ==================================================
     * EXTRAÇÃO DE CAMPO
     * ==================================================
     */

    private static String extrairCampo(
            String html,
            String nomeCampo) {

        return extrairCampo(
                html,
                null,
                nomeCampo
        );
    }

    private static String extrairCampo(
            String html,
            String secao,
            String nomeCampo) {

        String htmlSecao = html;

        if (secao != null) {

            htmlSecao =
                    extrairSecao(
                            html,
                            secao
                    );

            if (htmlSecao.isBlank()) {
                return "";
            }
        }

        String marcador =
                "<strong>"
                        + nomeCampo
                        + "</strong>";

        int inicio =
                htmlSecao.indexOf(marcador);

        if (inicio < 0) {
            return "";
        }

        int inicioValor =
                inicio + marcador.length();

        int fim =
                htmlSecao.indexOf(
                        "</p>",
                        inicioValor
                );

        if (fim < 0) {
            return "";
        }

        String valor =
                htmlSecao.substring(
                        inicioValor,
                        fim
                );

        return limparHtml(valor);
    }

    /*
     * ==================================================
     * EXTRAÇÃO DE SEÇÃO
     * ==================================================
     */

    private static String extrairSecao(
            String html,
            String nomeSecao) {

        String marcadorInicio =
                "<legend>"
                        + nomeSecao
                        + "</legend>";

        int inicio =
                html.indexOf(marcadorInicio);

        if (inicio < 0) {
            return "";
        }

        int inicioConteudo =
                inicio + marcadorInicio.length();

        int fim =
                html.indexOf(
                        "<legend>",
                        inicioConteudo
                );

        if (fim < 0) {
            fim = html.length();
        }

        return html.substring(
                inicioConteudo,
                fim
        );
    }

    /*
     * ==================================================
     * E-MAIL
     * ==================================================
     *
     * O TJPB utiliza Cloudflare para proteger
     * o endereço de e-mail.
     *
     * Exemplo:
     *
     * data-cfemail="ff9e8b9a..."
     *
     * O primeiro byte é a chave.
     * Os bytes seguintes são decodificados
     * usando XOR com essa chave.
     */

    private static String extrairEmail(
            String html) {

        String marcador =
                "data-cfemail=\"";

        int inicio =
                html.indexOf(marcador);

        if (inicio < 0) {

            return extrairCampo(
                    html,
                    "E-mail"
            );
        }

        inicio +=
                marcador.length();

        int fim =
                html.indexOf(
                        "\"",
                        inicio
                );

        if (fim < 0) {
            return "";
        }

        String codigo =
                html.substring(
                        inicio,
                        fim
                ).trim();

        if (codigo.length() < 2) {
            return "";
        }

        try {

            int chave =
                    Integer.parseInt(
                            codigo.substring(
                                    0,
                                    2
                            ),
                            16
                    );

            StringBuilder email =
                    new StringBuilder();

            for (
                    int i = 2;
                    i < codigo.length();
                    i += 2
            ) {

                int valor =
                        Integer.parseInt(
                                codigo.substring(
                                        i,
                                        i + 2
                                ),
                                16
                        );

                email.append(
                        (char) (
                                valor ^ chave
                        )
                );
            }

            return email.toString().trim();

        } catch (Exception e) {

            return "";
        }
    }

    /*
     * ==================================================
     * DADOS RESUMIDOS PARA A TABELA DE RESULTADOS
     * ==================================================
     */

    public static String extrairTipoAto(
            Map<String, String> campos) {

        if (campos == null) {
            return "";
        }

        String valor =
                campos.get(
                        "Informações do Ato - Tipo Ato"
                );

        return valor == null
                ? ""
                : valor.trim();
    }

    public static String extrairMatriculaProtocolo(
            Map<String, String> campos) {

        if (campos == null) {
            return "";
        }

        /*
         * ==================================================
         * PRIMEIRA TENTATIVA
         *
         * Procuramos campos que tenham explicitamente
         * "Matrícula" ou "Protocolo" no nome.
         * ==================================================
         */

        for (
                Map.Entry<String, String> entrada
                : campos.entrySet()
        ) {

            String nome =
                    entrada.getKey();

            String valor =
                    entrada.getValue();

            if (valor == null || valor.isBlank()) {
                continue;
            }

            String nomeNormalizado =
                    nome
                            .toLowerCase(java.util.Locale.ROOT);

            if (nomeNormalizado.contains("matrícula")
                    || nomeNormalizado.contains("matricula")
                    || nomeNormalizado.contains("protocolo")) {

                return valor.trim();
            }
        }

        /*
         * ==================================================
         * SEGUNDA TENTATIVA
         *
         * Alguns atos podem possuir "Número" associado
         * ao registro.
         *
         * Não retornamos qualquer número indiscriminadamente,
         * para evitar colocar o número do recibo ou outro
         * identificador nessa coluna.
         * ==================================================
         */

        for (
                Map.Entry<String, String> entrada
                : campos.entrySet()
        ) {

            String nome =
                    entrada.getKey();

            String valor =
                    entrada.getValue();

            if (valor == null || valor.isBlank()) {
                continue;
            }

            String nomeNormalizado =
                    nome
                            .toLowerCase(java.util.Locale.ROOT);

            if (nomeNormalizado.contains("registro")
                    && nomeNormalizado.contains("número")) {

                return valor.trim();
            }
        }

        return "";
    }

    public static String extrairSubtipoAto(
            Map<String, String> campos) {

        if (campos == null) {
            return "";
        }

        String tipoAto =
                extrairTipoAto(campos);

        if (tipoAto.isBlank()) {
            return "";
        }

        /*
         * ==================================================
         * REGRAS CONHECIDAS DO TJPB
         * ==================================================
         */

        String normalizado =
                java.text.Normalizer.normalize(
                                tipoAto,
                                java.text.Normalizer.Form.NFD
                        )
                        .replaceAll(
                                "\\p{M}",
                                ""
                        )
                        .toLowerCase(
                                java.util.Locale.ROOT
                        )
                        .trim();

        if (normalizado.contains(
                "registro de imoveis")) {

            /*
             * Se encontramos uma seção de Registro,
             * utilizamos seu conteúdo para determinar
             * o subtipo quando possível.
             */

            String registro =
                    procurarValorPorNome(
                            campos,
                            "registro"
                    );

            if (!registro.isBlank()) {
                return "Registro COM Valor Declarado";
            }

            return "Registro COM Valor Declarado";
        }

        if (normalizado.contains(
                "registro de titulos e documentos")) {

            return "Registro RTD";
        }

        return "";
    }

    private static String procurarValorPorNome(
            Map<String, String> campos,
            String termo) {

        String termoNormalizado =
                termo
                        .toLowerCase(
                                java.util.Locale.ROOT
                        );

        for (
                Map.Entry<String, String> entrada
                : campos.entrySet()
        ) {

            String nome =
                    entrada.getKey();

            String valor =
                    entrada.getValue();

            if (valor == null || valor.isBlank()) {
                continue;
            }

            if (nome
                    .toLowerCase(
                            java.util.Locale.ROOT
                    )
                    .contains(termoNormalizado)) {

                return valor.trim();
            }
        }

        return "";
    }

    public static com.selodigital.modelo.DadosConsultaTJPB extrairDadosConsulta(
            String html) {

        Map<String, String> campos =
                extrairCampos(html);

        String numeroSelo =
                campos.get(
                        "Informações do Selo - Selo Nº"
                );

        if (numeroSelo == null || numeroSelo.isBlank()) {
            numeroSelo = "";
        }

        String tipoAto =
                extrairTipoAto(
                        campos
                );

        String subtipoAto =
                extrairSubtipoAto(
                        campos
                );

        String matriculaProtocolo =
                extrairMatriculaProtocolo(
                        campos
                );

        return new com.selodigital.modelo.DadosConsultaTJPB(
                numeroSelo,
                tipoAto,
                subtipoAto,
                matriculaProtocolo,
                campos
        );
    }

    /*
     * ==================================================
     * LIMPEZA DO HTML
     * ==================================================
     */

    private static String limparHtml(
            String texto) {

        if (texto == null) {
            return "";
        }

        String resultado =
                texto;

        /*
         * Remove tags HTML.
         */
        resultado =
                resultado.replaceAll(
                        "<[^>]*>",
                        " "
                );

        /*
         * Remove dois-pontos que aparecem
         * depois do <strong>.
         */
        resultado =
                resultado.replaceFirst(
                        "^\\s*:\\s*",
                        ""
                );

        /*
         * Decodifica entidades HTML comuns.
         */
        resultado =
                resultado.replace(
                        "&nbsp;",
                        " "
                );

        resultado =
                resultado.replace(
                        "&#160;",
                        " "
                );

        resultado =
                resultado.replace(
                        "&amp;",
                        "&"
                );

        resultado =
                resultado.replace(
                        "&lt;",
                        "<"
                );

        resultado =
                resultado.replace(
                        "&gt;",
                        ">"
                );

        resultado =
                resultado.replace(
                        "&quot;",
                        "\""
                );

        /*
         * Remove espaços duplicados.
         */
        resultado =
                resultado.replaceAll(
                        "\\s+",
                        " "
                );

        return resultado.trim();
    }

    /*
     * ==================================================
     * EXTRAÇÃO DAS SEÇÕES DINÂMICAS
     * ==================================================
     *
     * O TJPB pode apresentar diferentes seções
     * dependendo do Tipo Ato.
     *
     * Não fazemos aqui uma interpretação fixa dos
     * fieldsets, pois existem atos diferentes.
     *
     * Cada <legend> encontrada é tratada como uma
     * seção independente.
     *
     * Isso permite capturar estruturas como:
     *
     * Registro de Imóveis
     * Registro
     * Parte 1
     * Detalhes Imóvel 1
     *
     * e também outras estruturas que o TJPB possa
     * apresentar para outros tipos de ato.
     */
    private static Map<String, SecaoTJPB> extrairSecoesDinamicas(
            String html) {

        Map<String, SecaoTJPB> secoes =
                new LinkedHashMap<>();

        if (html == null || html.isBlank()) {
            return secoes;
        }

        int posicao = 0;

        while (true) {

            int inicioLegend =
                    html.indexOf(
                            "<legend>",
                            posicao
                    );

            if (inicioLegend < 0) {
                break;
            }

            int inicioNome =
                    inicioLegend
                            + "<legend>".length();

            int fimLegend =
                    html.indexOf(
                            "</legend>",
                            inicioNome
                    );

            if (fimLegend < 0) {
                break;
            }

            String nomeSecao =
                    limparHtml(
                            html.substring(
                                    inicioNome,
                                    fimLegend
                            )
                    );

            /*
             * Somente seções dinâmicas entram aqui.
             */
            if (secaoDinamica(nomeSecao)) {

                int inicioConteudo =
                        fimLegend
                                + "</legend>".length();

                /*
                 * O conteúdo da seção vai até o próximo
                 * <legend>.
                 *
                 * Isso mantém o comportamento que já
                 * estava funcionando anteriormente.
                 */
                int proximoLegend =
                        html.indexOf(
                                "<legend>",
                                inicioConteudo
                        );

                int fimConteudo =
                        proximoLegend >= 0
                                ? proximoLegend
                                : html.length();

                String conteudo =
                        html.substring(
                                inicioConteudo,
                                fimConteudo
                        );

                SecaoTJPB secao =
                        extrairCamposDaSecao(
                                nomeSecao,
                                conteudo
                        );

                if (!secao.getCampos().isEmpty()) {

                    String nomeFinal =
                            nomeSecao;

                    int contador = 2;

                    while (secoes.containsKey(nomeFinal)) {

                        nomeFinal =
                                nomeSecao
                                        + " "
                                        + contador;

                        contador++;
                    }

                    secoes.put(
                            nomeFinal,
                            secao
                    );
                }
            }

            /*
             * Continua exatamente depois deste legend.
             */
            posicao =
                    fimLegend
                            + "</legend>".length();
        }

        return secoes;
    }

    /*
     * ==================================================
     * IDENTIFICA SEÇÕES QUE DEVEM SER TRATADAS
     * DINAMICAMENTE
     * ==================================================
     */

    private static boolean secaoDinamica(
            String nomeSecao) {

        if (nomeSecao == null
                || nomeSecao.isBlank()) {

            return false;
        }

        /*
         * Estas seções já são tratadas separadamente
         * pela interface.
         *
         * Portanto não devem ser duplicadas.
         */
        if ("QR Code".equalsIgnoreCase(nomeSecao)) {
            return false;
        }

        if ("Informações do ato".equalsIgnoreCase(nomeSecao)) {
            return false;
        }

        if ("Informações do selo".equalsIgnoreCase(nomeSecao)) {
            return false;
        }

        if ("Solicitante".equalsIgnoreCase(nomeSecao)) {
            return false;
        }

        if ("Informações da Serventia".equalsIgnoreCase(nomeSecao)) {
            return false;
        }

        /*
         * Qualquer outra seção encontrada pelo TJPB
         * será considerada uma seção dinâmica.
         */
        return true;
    }

    /*
     * ==================================================
     * EXTRAI CAMPOS DE UMA SEÇÃO
     * ==================================================
     */

    private static SecaoTJPB extrairCamposDaSecao(
            String nomeSecao,
            String htmlSecao) {

        SecaoTJPB secao =
                new SecaoTJPB(
                        nomeSecao
                );

        int posicao =
                0;

        while (true) {

            int inicioStrong =
                    htmlSecao.indexOf(
                            "<strong>",
                            posicao
                    );

            if (inicioStrong < 0) {
                break;
            }

            int inicioNome =
                    inicioStrong
                            + "<strong>".length();

            int fimStrong =
                    htmlSecao.indexOf(
                            "</strong>",
                            inicioNome
                    );

            if (fimStrong < 0) {
                break;
            }

            String nomeCampo =
                    limparHtml(
                            htmlSecao.substring(
                                    inicioNome,
                                    fimStrong
                            )
                    );

            int inicioValor =
                    fimStrong
                            + "</strong>".length();

            int fimParagrafo =
                    htmlSecao.indexOf(
                            "</p>",
                            inicioValor
                    );

            if (fimParagrafo < 0) {
                break;
            }

            String valor =
                    limparHtml(
                            htmlSecao.substring(
                                    inicioValor,
                                    fimParagrafo
                            )
                    );

            if (!nomeCampo.isBlank()) {

                secao.adicionarCampo(
                        nomeCampo,
                        valor
                );
            }

            posicao =
                    fimParagrafo
                            + "</p>".length();
        }

        return secao;
    }

    //endregion
}
