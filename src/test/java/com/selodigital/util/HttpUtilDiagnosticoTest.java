package com.selodigital.util;

import com.selodigital.modelo.DiagnosticoConsulta;
import com.selodigital.modelo.TipoFalhaConsulta;
import org.junit.jupiter.api.Test;

import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.http.HttpTimeoutException;
import java.util.List;
import java.util.Map;
import com.sun.net.httpserver.HttpServer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HttpUtilDiagnosticoTest {

    private static final String URL_SOLICITADA =
            "https://exemplo.test/consulta?selo=ABC";

    @Test
    void registraRespostaValidaERedirecionamentoSemFalha() {

        DiagnosticoConsulta diagnostico =
                HttpUtil.diagnosticarResposta(
                        200,
                        URL_SOLICITADA,
                        "https://exemplo.test/resultado?selo=ABC",
                        "text/html; charset=UTF-8",
                        "<html>resultado</html>",
                        Map.of(),
                        1
                );

        assertEquals(TipoFalhaConsulta.SEM_FALHA,
                diagnostico.getTipoFalha());
        assertEquals(200, diagnostico.getCodigoHttp());
        assertEquals(URL_SOLICITADA, diagnostico.getUrlSolicitada());
        assertEquals("https://exemplo.test/resultado?selo=ABC",
                diagnostico.getUrlFinal());
        assertTrue(diagnostico.houveRedirecionamento());
    }

    @Test
    void segueRedirecionamentoComRespostaIntermediariaSemCorpo()
            throws Exception {

        HttpServer servidor = HttpServer.create(
                new InetSocketAddress("127.0.0.1", 0),
                0
        );
        servidor.createContext("/origem", troca -> {
            troca.getResponseHeaders().add("Location", "/destino");
            troca.sendResponseHeaders(302, -1);
            troca.close();
        });
        servidor.createContext("/destino", troca -> {
            byte[] corpo = "resultado".getBytes(java.nio.charset.StandardCharsets.UTF_8);
            troca.sendResponseHeaders(200, corpo.length);
            try (java.io.OutputStream saida = troca.getResponseBody()) {
                saida.write(corpo);
            }
        });
        servidor.start();

        try {
            String origem = "http://127.0.0.1:" + servidor.getAddress().getPort()
                    + "/origem";

            var resposta = HttpUtil.get(origem);
            DiagnosticoConsulta diagnostico = HttpUtil.diagnosticarResposta(
                    origem,
                    resposta
            );

            assertEquals(200, resposta.statusCode());
            assertEquals("resultado", resposta.body());
            assertTrue(resposta.previousResponse().isPresent());
            assertEquals(302, resposta.previousResponse().orElseThrow().statusCode());
            assertEquals("", resposta.previousResponse().orElseThrow().body());
            assertEquals(1, diagnostico.getQuantidadeRedirecionamentos());
        } finally {
            servidor.stop(0);
        }
    }

    @Test
    void identificaCloudflareSomenteComEvidenciaConcreta() {

        DiagnosticoConsulta peloCorpo =
                HttpUtil.diagnosticarResposta(
                        403,
                        URL_SOLICITADA,
                        URL_SOLICITADA,
                        "text/html",
                        "<title>Just a moment...</title>"
                                + "<div id=\"challenge-error-text\">",
                        Map.of(),
                        0
                );

        DiagnosticoConsulta peloCabecalho =
                HttpUtil.diagnosticarResposta(
                        403,
                        URL_SOLICITADA,
                        URL_SOLICITADA,
                        "text/html",
                        "<html>Acesso bloqueado</html>",
                        Map.of("cf-mitigated", List.of("challenge")),
                        0
                );

        DiagnosticoConsulta semEvidencia =
                HttpUtil.diagnosticarResposta(
                        403,
                        URL_SOLICITADA,
                        URL_SOLICITADA,
                        "text/html",
                        "<html>Acesso negado</html>",
                        Map.of("server", List.of("cloudflare")),
                        0
                );

        assertEquals(TipoFalhaConsulta.BLOQUEIO_CLOUDFLARE,
                peloCorpo.getTipoFalha());
        assertEquals(TipoFalhaConsulta.BLOQUEIO_CLOUDFLARE,
                peloCabecalho.getTipoFalha());
        assertEquals(TipoFalhaConsulta.FALHA_HTTP,
                semEvidencia.getTipoFalha());
    }

    @Test
    void classificaCodigosHttpERespostaVazia() {

        for (int codigo : List.of(404, 429, 500)) {
            DiagnosticoConsulta diagnostico =
                    HttpUtil.diagnosticarResposta(
                            codigo,
                            URL_SOLICITADA,
                            URL_SOLICITADA,
                            "text/html",
                            "<html>erro</html>",
                            Map.of(),
                            0
                    );

            assertEquals(TipoFalhaConsulta.FALHA_HTTP,
                    diagnostico.getTipoFalha());
        }

        DiagnosticoConsulta vazia =
                HttpUtil.diagnosticarResposta(
                        200,
                        URL_SOLICITADA,
                        URL_SOLICITADA,
                        "text/html",
                        " ",
                        Map.of(),
                        0
                );

        assertEquals(TipoFalhaConsulta.RESPOSTA_INESPERADA,
                vazia.getTipoFalha());
    }

    @Test
    void preservaTipoETecnicaDeTimeoutConexaoEParsing() {

        DiagnosticoConsulta timeout =
                HttpUtil.diagnosticarExcecao(
                        URL_SOLICITADA,
                        new HttpTimeoutException("limite atingido")
                );
        DiagnosticoConsulta conexao =
                HttpUtil.diagnosticarExcecao(
                        URL_SOLICITADA,
                        new ConnectException("host inacessível")
                );
        DiagnosticoConsulta parsing = new DiagnosticoConsulta(
                TipoFalhaConsulta.SEM_FALHA,
                200,
                URL_SOLICITADA,
                URL_SOLICITADA,
                "text/html",
                0,
                ""
        ).comTipoFalha(
                TipoFalhaConsulta.FALHA_DE_PARSING,
                new IllegalStateException("estrutura inválida")
        );

        assertEquals(TipoFalhaConsulta.TIMEOUT, timeout.getTipoFalha());
        assertEquals(TipoFalhaConsulta.FALHA_CONEXAO, conexao.getTipoFalha());
        assertEquals(TipoFalhaConsulta.FALHA_DE_PARSING,
                parsing.getTipoFalha());
        assertTrue(parsing.getCausaTecnica().contains(
                "IllegalStateException"
        ));
    }
}
