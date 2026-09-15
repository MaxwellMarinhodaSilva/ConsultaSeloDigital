package com.selodigital.util;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
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
            this.corpo = new String(resposta.body(), extrairCharset(resposta.headers()));
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
