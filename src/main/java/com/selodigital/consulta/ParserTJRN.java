package com.selodigital.consulta;

import java.text.Normalizer;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Interpreta exclusivamente o HTML devolvido pela consulta pública do SIEX/TJRN.
 *
 * <p>A página não usa uma grade de campos: ela intercala linhas simples, títulos
 * em negrito e blocos repetidos de histórico. Por isso a extração mantém a ordem
 * do documento e representa cada dado como {@code Seção - Campo}, sem depender
 * de uma relação fixa de atos ou de campos.</p>
 */
public final class ParserTJRN {

    // =======================================================
    // 1. ESTRUTURA DA CLASSE
    // =======================================================

    //region [Membros da Classe]

    private static final String SECAO_GERAL = "Informações Gerais";
    private static final String SECAO_MENSAGENS = "Mensagens";
    private static final Pattern DIV_CONTEUDO = Pattern.compile(
            "(?is)<div\\b[^>]*\\bclass\\s*=\\s*(['\"])" +
                    "[^'\"]*\\bconteudoSemRotulo\\b[^'\"]*\\1[^>]*>");
    private static final Pattern TAG_OU_TEXTO = Pattern.compile("(?is)<[^>]+>|[^<]+");
    private static final Pattern TAG_DIV = Pattern.compile("(?is)^<\\s*(/?)\\s*div\\b");
    private static final Pattern TAG_NOME = Pattern.compile("(?is)^<\\s*/?\\s*([a-z0-9]+)\\b");
    private static final Pattern STATUS_NO_CODIGO = Pattern.compile("(?is)^(.+?)\\s*\\(([^()]+)\\)\\s*$");
    private static final Pattern TOTAL_LANCAMENTOS = Pattern.compile("(?i)^\\d+\\s+lançamento\\(s\\)\\s*$");

    private ParserTJRN() {
    }

    /**
     * Produz chaves {@code Seção - Campo}, preservando a ordem e valores repetidos.
     * Títulos sem dados subsequentes e campos sem valor não são materializados no
     * mapa: eles são elementos estruturais do HTML, não informações consultáveis.
     */
    public static Map<String, String> extrairCampos(String html) {
        Map<String, String> campos = new LinkedHashMap<>();
        String conteudo = extrairConteudoDaConsulta(html);
        if (conteudo.isBlank()) {
            return campos;
        }

        EstadoLeitura estado = new EstadoLeitura();
        StringBuilder linha = new StringBuilder();
        Matcher matcher = TAG_OU_TEXTO.matcher(conteudo);
        while (matcher.find()) {
            String trecho = matcher.group();
            if (trecho.startsWith("<")) {
                String tag = nomeDaTag(trecho);
                if ("br".equals(tag) || "hr".equals(tag) || "p".equals(tag)
                        || "li".equals(tag) || "tr".equals(tag) || tag.matches("h[1-6]")) {
                    processarLinha(campos, estado, linha.toString());
                    linha.setLength(0);
                }
            } else {
                linha.append(decodificarEntidades(trecho));
            }
        }
        processarLinha(campos, estado, linha.toString());
        return campos;
    }

    /** Retorna o estado declarado no cabeçalho "Código: ... (Estado)". */
    public static String extrairStatusSelo(String html) {
        String codigo = valorDoCampo(extrairCampos(html), SECAO_GERAL, "Código");
        Matcher matcher = STATUS_NO_CODIGO.matcher(codigo);
        return matcher.matches() ? matcher.group(2).trim() : "";
    }

    public static boolean possuiCodigoDoSelo(String html, String numeroSelo) {
        String codigo = valorDoCampo(extrairCampos(html), SECAO_GERAL, "Código");
        return !codigo.isBlank() && (numeroSelo == null || numeroSelo.isBlank()
                || normalizar(codigo).contains(normalizar(numeroSelo)));
    }

    public static boolean possuiMensagemCodigoNaoEncontrado(String html) {
        return normalizar(extrairConteudoDaConsulta(html)).contains("codigo nao encontrado");
    }

    public static String extrairTipoAto(Map<String, String> campos) {
        return procurarCampo(campos, "Tipo Ato", "Tipo do Ato", "Ato", "Referente");
    }

    public static String extrairSubtipoAto(Map<String, String> campos) {
        return procurarCampo(campos, "Subtipo Ato", "Subtipo do Ato", "Subtipo");
    }

    public static String extrairMatriculaProtocolo(Map<String, String> campos) {
        return procurarCampo(campos, "Matrícula/Protocolo", "Matrícula", "Protocolo",
                "Número do Protocolo", "Nº de Ordem");
    }

    public static String extrairQrCodeConteudo(String html, String numeroSelo) {
        return numeroSelo == null || numeroSelo.isBlank() ? ""
                : "https://selodigital.tjrn.jus.br/siex/qrcode?" + numeroSelo.trim();
    }

    /**
     * Classifica cada linha conforme o contexto corrente da página. O SIEX separa
     * as atualizações do histórico com uma linha de asteriscos; esse delimitador
     * é estrutural e precisa reiniciar o bloco antes de ler campos repetidos.
     */
    private static void processarLinha(Map<String, String> campos, EstadoLeitura estado,
                                       String textoOriginal) {
        String linha = normalizarEspacos(textoOriginal);
        if (linha.isBlank() || ignorarLinha(linha)) {
            return;
        }
        if (ehSeparadorDeHistorico(linha)) {
            estado.iniciarAtualizacaoDoHistorico();
            return;
        }
        if (ehTituloDeSecao(linha)) {
            String novaSecao = removerDoisPontos(linha);
            estado.definirSecao(novaSecao);
            return;
        }

        int separador = linha.indexOf(':');
        if (separador > 0) {
            String rotulo = linha.substring(0, separador).trim();
            String valor = linha.substring(separador + 1).trim();
            if (ehRotuloValido(rotulo)) {
                if (estado.secao.toLowerCase(Locale.ROOT).contains("lançamento")
                        && normalizar(rotulo).equals("data do ato")) {
                    estado.lancamentoAtual++;
                }
                String prefixo = estado.secao;
                if (estado.lancamentoAtual > 0
                        && estado.secao.toLowerCase(Locale.ROOT).contains("lançamento")) {
                    prefixo += " - Lançamento " + estado.lancamentoAtual;
                }
                /*
                 * O SIEX escreve, por exemplo, "Observação: <br>". O rótulo
                 * existe no HTML, mas não há dado a apresentar. Não criamos uma
                 * entrada vazia, pois ela acabaria se tornando um campo ou painel
                 * visual sem conteúdo. Quando houver valor, ele é mantido.
                 */
                if (!valor.isBlank()) {
                    adicionarCampo(campos, prefixo + " - " + rotulo, valor);
                }
                return;
            }
        }

        if (TOTAL_LANCAMENTOS.matcher(linha).matches()
                && estado.secao.toLowerCase(Locale.ROOT).contains("lançamento")) {
            adicionarCampo(campos, estado.secao + " - Resumo", linha);
        } else if (linha.regionMatches(true, 0, "Em ", 0, 3)
                && estado.secao.toLowerCase(Locale.ROOT).contains("lançamento")) {
            adicionarCampo(campos, estado.secao + " - Data", linha.substring(3).trim());
        } else {
            adicionarCampo(campos, SECAO_MENSAGENS + " - Mensagem", linha);
        }
    }

    private static String extrairConteudoDaConsulta(String html) {
        if (html == null || html.isBlank()) {
            return "";
        }
        Matcher abertura = DIV_CONTEUDO.matcher(html);
        if (!abertura.find()) {
            return html;
        }
        int inicio = abertura.end();
        int profundidade = 1;
        Matcher tags = Pattern.compile("(?is)</?div\\b[^>]*>").matcher(html);
        tags.region(inicio, html.length());
        while (tags.find()) {
            Matcher div = TAG_DIV.matcher(tags.group());
            if (div.find() && !div.group(1).isBlank()) {
                profundidade--;
            } else {
                profundidade++;
            }
            if (profundidade == 0) {
                return html.substring(inicio, tags.start());
            }
        }
        return html.substring(inicio);
    }

    private static String nomeDaTag(String tag) {
        Matcher matcher = TAG_NOME.matcher(tag);
        return matcher.find() ? matcher.group(1).toLowerCase(Locale.ROOT) : "";
    }

    private static void adicionarCampo(Map<String, String> campos, String chave, String valor) {
        String base = chave.trim();
        String chaveFinal = base;
        int numero = 2;
        while (campos.containsKey(chaveFinal)) {
            chaveFinal = base + " " + numero++;
        }
        campos.put(chaveFinal, valor == null ? "" : valor.trim());
    }

    private static String procurarCampo(Map<String, String> campos, String... nomes) {
        for (String nome : nomes) {
            for (Map.Entry<String, String> campo : campos.entrySet()) {
                String chave = campo.getKey();
                int posicao = chave.lastIndexOf(" - ");
                String rotulo = posicao >= 0 ? chave.substring(posicao + 3).replaceFirst(" \\d+$", "") : chave;
                if (rotulo.equalsIgnoreCase(nome) && campo.getValue() != null) {
                    return campo.getValue().trim();
                }
            }
        }
        return "";
    }

    private static String valorDoCampo(Map<String, String> campos, String secao, String campo) {
        String valor = campos.get(secao + " - " + campo);
        return valor == null ? "" : valor.trim();
    }

    private static boolean ehTituloDeSecao(String linha) {
        return linha.endsWith(":") && linha.length() > 2 && linha.length() < 160;
    }

    private static boolean ehSeparadorDeHistorico(String linha) {
        return linha.matches("\\*{3,}");
    }

    private static boolean ehRotuloValido(String rotulo) {
        return rotulo.length() <= 160 && !rotulo.contains("{") && !rotulo.contains("}")
                && !rotulo.contains(";") && !normalizar(rotulo).startsWith("http");
    }

    private static boolean ignorarLinha(String linha) {
        String normalizada = normalizar(linha);
        return normalizada.startsWith("function ") || normalizada.startsWith("eval(")
                || normalizada.contains("mod_pagespeed");
    }

    private static String removerDoisPontos(String texto) {
        return texto.replaceFirst(":\\s*$", "").trim();
    }

    private static String normalizarEspacos(String texto) {
        return decodificarEntidades(texto).replaceAll("\\s+", " ").trim();
    }

    private static String decodificarEntidades(String texto) {
        String resultado = texto.replace("&nbsp;", " ").replace("&#160;", " ")
                .replace("&amp;", "&").replace("&lt;", "<").replace("&gt;", ">")
                .replace("&quot;", "\"").replace("&#39;", "'");
        Matcher decimal = Pattern.compile("&#(\\d+);").matcher(resultado);
        StringBuffer buffer = new StringBuffer();
        while (decimal.find()) {
            decimal.appendReplacement(buffer, Matcher.quoteReplacement(
                    String.valueOf((char) Integer.parseInt(decimal.group(1)))));
        }
        decimal.appendTail(buffer);
        return buffer.toString();
    }

    private static String normalizar(String texto) {
        return Normalizer.normalize(texto == null ? "" : texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);
    }

    /** Mantém o contexto necessário para distinguir blocos repetidos do histórico. */
    private static final class EstadoLeitura {
        private String secao = SECAO_GERAL;
        private boolean dentroDoHistorico;
        private String tituloHistorico;
        private int atualizacaoHistorico;
        private int lancamentoAtual;

        private void definirSecao(String titulo) {
            lancamentoAtual = 0;
            if (normalizar(titulo).startsWith("historico")) {
                dentroDoHistorico = true;
                tituloHistorico = titulo;
                atualizacaoHistorico = 0;
                secao = titulo;
                return;
            }
            if (dentroDoHistorico && normalizar(titulo).startsWith("lancamentos realizados")) {
                secao = secaoDaAtualizacao() + " - " + titulo;
                return;
            }
            secao = titulo;
        }

        private void iniciarAtualizacaoDoHistorico() {
            if (!dentroDoHistorico) {
                return;
            }
            atualizacaoHistorico++;
            lancamentoAtual = 0;
            secao = secaoDaAtualizacao();
        }

        private String secaoDaAtualizacao() {
            return tituloHistorico + " - Atualização " + atualizacaoHistorico;
        }
    }

    //endregion
}
