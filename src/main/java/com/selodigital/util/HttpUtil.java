package com.selodigital.util;

import com.selodigital.modelo.DiagnosticoConsulta;
import com.selodigital.modelo.TipoFalhaConsulta;

import java.io.IOException;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.URI;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpConnectTimeoutException;
import java.net.http.HttpRequest;
import java.net.http.HttpTimeoutException;
import java.net.SocketTimeoutException;
import java.net.http.HttpResponse;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.List;
import java.util.Map;
import javax.net.ssl.SSLSession;
import java.time.Duration;

/**
 * Executa consultas HTTP com redirecionamento, limites de tempo e decodificação conforme o charset informado.
 * A leitura em bytes é necessária para compatibilidade com a página ISO-8859-1 do SIEX/TJRN.
 */
public final class HttpUtil {

    // =======================================================
    // 1. ESTRUTURA DA CLASSE
    // =======================================================

    //region [Membros da Classe]

    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(20))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    private HttpUtil() {
    }

    public static HttpResponse<String> get(String url)
            throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(40))
                .header(
                        "User-Agent",
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
                                + "AppleWebKit/537.36 "
                                + "(KHTML, like Gecko) "
                                + "Chrome/139.0.0.0 Safari/537.36"
                )
                .header(
                        "Accept",
                        "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"
                )
                .GET()
                .build();

        HttpResponse<byte[]> resposta = CLIENT.send(
                request,
                HttpResponse.BodyHandlers.ofByteArray()
        );

        return new RespostaTexto(resposta);
    }

    /** Cria diagnóstico seguro, sem reter corpo ou cabeçalhos. */
    public static DiagnosticoConsulta diagnosticarResposta(
            String urlSolicitada, HttpResponse<String> resposta) {

        if (resposta == null) {
            return diagnosticarResposta(-1, urlSolicitada, "", "", "", Map.of(), 0);
        }
        return diagnosticarResposta(
                resposta.statusCode(), urlSolicitada, resposta.uri().toString(),
                resposta.headers().firstValue("content-type").orElse(""),
                resposta.body(), resposta.headers().map(), contarRedirecionamentos(resposta));
    }

    static DiagnosticoConsulta diagnosticarResposta(
            int codigoHttp,
            String urlSolicitada,
            String urlFinal,
            String contentType,
            String corpo,
            Map<String, List<String>> cabecalhos,
            int quantidadeRedirecionamentos) {

        TipoFalhaConsulta tipo = TipoFalhaConsulta.SEM_FALHA;
        if (indicaDesafioCloudflare(corpo, cabecalhos)) {
            tipo = TipoFalhaConsulta.BLOQUEIO_CLOUDFLARE;
        } else if (codigoHttp < 200 || codigoHttp >= 300) {
            tipo = TipoFalhaConsulta.FALHA_HTTP;
        } else if (corpo == null || corpo.isBlank()) {
            tipo = TipoFalhaConsulta.RESPOSTA_INESPERADA;
        }
        return new DiagnosticoConsulta(tipo, codigoHttp, urlSolicitada, urlFinal,
                contentType, quantidadeRedirecionamentos, "");
    }

    public static DiagnosticoConsulta diagnosticarExcecao(
            String urlSolicitada, Exception excecao) {
        return new DiagnosticoConsulta(classificarExcecao(excecao), -1,
                urlSolicitada, "", "", 0,
                DiagnosticoConsulta.descreverCausa(excecao));
    }

    private static TipoFalhaConsulta classificarExcecao(Exception excecao) {
        if (excecao instanceof HttpTimeoutException
                || excecao instanceof HttpConnectTimeoutException
                || excecao instanceof SocketTimeoutException) {
            return TipoFalhaConsulta.TIMEOUT;
        }
        if (excecao instanceof ConnectException
                || excecao instanceof UnknownHostException
                || excecao instanceof NoRouteToHostException
                || excecao instanceof IOException
                || excecao instanceof InterruptedException) {
            return TipoFalhaConsulta.FALHA_CONEXAO;
        }
        return TipoFalhaConsulta.FALHA_INTERNA;
    }

    private static boolean indicaDesafioCloudflare(
            String corpo, Map<String, List<String>> cabecalhos) {
        String conteudo = corpo == null ? "" : corpo.toLowerCase(java.util.Locale.ROOT);
        if (conteudo.contains("cf-mitigated")
                || conteudo.contains("/cdn-cgi/challenge-platform/")
                || conteudo.contains("challenge-error-text")
                || conteudo.contains("just a moment...")
                || conteudo.contains("attention required! | cloudflare")) {
            return true;
        }
        if (cabecalhos == null) {
            return false;
        }
        for (Map.Entry<String, List<String>> cabecalho : cabecalhos.entrySet()) {
            if ("cf-mitigated".equalsIgnoreCase(cabecalho.getKey())
                    && cabecalho.getValue().stream().anyMatch(valor -> valor != null
                    && valor.toLowerCase(java.util.Locale.ROOT).contains("challenge"))) {
                return true;
            }
        }
        return false;
    }

    private static int contarRedirecionamentos(HttpResponse<?> resposta) {
        int quantidade = 0;
        Optional<? extends HttpResponse<?>> anterior = resposta.previousResponse();
        while (anterior.isPresent()) {
            quantidade++;
            anterior = anterior.get().previousResponse();
        }
        return quantidade;
    }

    public static HttpResponse<String> postJson(String url, String jsonBody)
            throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(40))
                .header(
                        "User-Agent",
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
                                + "AppleWebKit/537.36 "
                                + "(KHTML, like Gecko) "
                                + "Chrome/139.0.0.0 Safari/537.36"
                )
                .header("Accept", "application/json, text/plain, */*")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(
                        jsonBody == null ? "{}" : jsonBody,
                        StandardCharsets.UTF_8
                ))
                .build();

        HttpResponse<byte[]> resposta = CLIENT.send(
                request,
                HttpResponse.BodyHandlers.ofByteArray()
        );

        return new RespostaTexto(resposta);
    }

    /*
     * BodyHandlers.ofString usa UTF-8 quando não recebe explicitamente um
     * charset. O SIEX/TJRN informa ISO-8859-1; por isso decodificamos os bytes
     * somente depois de poder consultar os cabeçalhos da resposta.
     */
    private static final class RespostaTexto implements HttpResponse<String> {

        private final HttpResponse<byte[]> resposta;
        private final String corpo;

        private RespostaTexto(HttpResponse<byte[]> resposta) {
            this.resposta = resposta;
            byte[] bytes = resposta.body();
            this.corpo = bytes == null
                    ? ""
                    : new String(bytes, extrairCharset(resposta.headers()));
        }

        @Override public int statusCode() { return resposta.statusCode(); }
        @Override public HttpRequest request() { return resposta.request(); }
        @Override public Optional<HttpResponse<String>> previousResponse() {
            return resposta.previousResponse().map(RespostaTexto::new);
        }
        @Override public HttpHeaders headers() { return resposta.headers(); }
        @Override public String body() { return corpo; }
        @Override public Optional<SSLSession> sslSession() { return resposta.sslSession(); }
        @Override public URI uri() { return resposta.uri(); }
        @Override public HttpClient.Version version() { return resposta.version(); }
    }

    private static Charset extrairCharset(HttpHeaders headers) {
        String contentType = headers.firstValue("content-type").orElse("");
        java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile("(?i)\\bcharset\\s*=\\s*([^;\\s]+)")
                .matcher(contentType);
        if (matcher.find()) {
            try {
                return Charset.forName(matcher.group(1).replace("\"", ""));
            } catch (Exception ignored) {
                // Um cabeçalho inválido não deve converter uma resposta válida em erro.
            }
        }
        return StandardCharsets.UTF_8;
    }

    //endregion
}
