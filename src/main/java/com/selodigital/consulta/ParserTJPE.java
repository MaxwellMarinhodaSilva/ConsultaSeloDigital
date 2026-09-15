package com.selodigital.consulta;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Extrai os dados do bloco de resultado público do SICASE/TJPE. */
public final class ParserTJPE {

    // =======================================================
    // 1. ESTRUTURA DA CLASSE
    // =======================================================

    //region [Membros da Classe]

    private static final Pattern NUMERO_SELO = Pattern.compile(
            "(?is)Selo\\s+Eletr[^<]*</label>\\s*</td>\\s*</tr>\\s*"
                    + "<tr>\\s*<td>\\s*<label[^>]*>(.*?)</label>");

    private static final Pattern CAMPO = Pattern.compile(
            "(?is)<td>\\s*([^<:]+?):\\s*<span[^>]*>(.*?)</span>\\s*</td>");

    private ParserTJPE() {
    }

    public static Map<String, String> extrairCampos(String html) {

        /* A ordem do LinkedHashMap é a mesma exibida na janela de detalhes. */
        Map<String, String> campos = new LinkedHashMap<>();
        if (html == null || html.isBlank()) {
            return campos;
        }

        String numeroSelo = extrairNumeroSelo(html);
        if (!numeroSelo.isBlank()) {
            campos.put("Tipo de Selo", "Selo Eletrônico de Fiscalização");
            campos.put("Número do Selo", numeroSelo);
        }

        Matcher matcher = CAMPO.matcher(html);
        while (matcher.find()) {
            String nome = limparTexto(matcher.group(1));
            String valor = limparTexto(matcher.group(2));
            if (!nome.isBlank()) {
                campos.put(nome, valor);
            }
        }
        return campos;
    }

    public static String extrairNumeroSelo(String html) {
        if (html == null) {
            return "";
        }
        Matcher matcher = NUMERO_SELO.matcher(html);
        return matcher.find() ? limparTexto(matcher.group(1)) : "";
    }

    public static String extrairCampo(Map<String, String> campos, String nome) {
        if (campos == null || nome == null) {
            return "";
        }
        for (Map.Entry<String, String> campo : campos.entrySet()) {
            if (campo.getKey().equalsIgnoreCase(nome)) {
                return campo.getValue();
            }
        }
        return "";
    }

    private static String limparTexto(String texto) {
        if (texto == null) {
            return "";
        }
        return texto.replaceAll("(?is)<[^>]*>", " ")
                        .replace("&nbsp;", " ")
                        .replace("&amp;", "&")
                        .replace("&ordm;", "º")
                        .replace("&ccedil;", "ç")
                        .replace("&atilde;", "ã")
                        .replace("&aacute;", "á")
                        .replace("&eacute;", "é")
                        .replace("&iacute;", "í")
                        .replace("&oacute;", "ó")
                        .replace("&uacute;", "ú")
                        .replaceAll("\\s+", " ").trim();
    }

    //endregion
}
