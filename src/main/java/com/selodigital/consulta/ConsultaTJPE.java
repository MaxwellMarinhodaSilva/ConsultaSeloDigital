package com.selodigital.consulta;

import com.selodigital.modelo.DetalhesSelo;
import com.selodigital.modelo.DiagnosticoConsulta;
import com.selodigital.modelo.ResultadoConsulta;
import com.selodigital.modelo.Selo;
import com.selodigital.modelo.StatusConsulta;
import com.selodigital.modelo.TipoFalhaConsulta;
import com.selodigital.modelo.Tribunal;
import com.selodigital.util.HttpUtil;
import java.net.URLEncoder;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/** Consulta pública do TJPE pelo hash do selo, sem automação de CAPTCHA. */
public class ConsultaTJPE implements ConsultaTribunal {

    // =======================================================
    // 1. ESTRUTURA DA CLASSE
    // =======================================================

    //region [Membros da Classe]

    private static final String URL_BASE =
            "https://sicase.tjpe.jus.br/sicase/externo/autenticidadeselo/"
                    + "form_validarautenticidadeselo.jsf?hash=";

    @Override
    public Tribunal getTribunal() {
        return Tribunal.TJPE;
    }

    @Override
    public ResultadoConsulta consultar(Selo selo) {
        String urlSolicitada = "";
        try {
            urlSolicitada = montarUrl(selo);
            HttpResponse<String> resposta = HttpUtil.get(urlSolicitada);
            DiagnosticoConsulta diagnostico = HttpUtil.diagnosticarResposta(
                    urlSolicitada, resposta);
            StatusConsulta status = analisarResposta(resposta.statusCode(), resposta.body());
            diagnostico = ajustarDiagnostico(status, diagnostico);
            if (status != StatusConsulta.ENVIADO) {
                return new ResultadoConsulta(selo, Tribunal.TJPE, status,
                        "", "", "", java.util.Collections.emptyMap(), null, diagnostico);
            }

            /* O HTML recebido abastece tabela e detalhes, sem uma segunda chamada. */
            Map<String, String> campos;
            try {
                campos = ParserTJPE.extrairCampos(resposta.body());
            } catch (RuntimeException e) {
                return resultadoComFalhaDeParsing(selo, diagnostico, e);
            }
            return new ResultadoConsulta(selo, Tribunal.TJPE, status,
                    "", ParserTJPE.extrairCampo(campos, "Ato"), "", campos, null, diagnostico);
        } catch (Exception e) {
            registrarErro("consulta", e);
            return new ResultadoConsulta(selo, Tribunal.TJPE, StatusConsulta.ERRO,
                    "", "", "", java.util.Collections.emptyMap(), null,
                    HttpUtil.diagnosticarExcecao(urlSolicitada, e));
        }
    }

    @Override
    public DetalhesSelo consultarDetalhes(Selo selo) {
        String urlSolicitada = "";
        try {
            urlSolicitada = montarUrl(selo);
            HttpResponse<String> resposta = HttpUtil.get(urlSolicitada);
            DiagnosticoConsulta diagnostico = HttpUtil.diagnosticarResposta(
                    urlSolicitada, resposta);
            StatusConsulta status = analisarResposta(resposta.statusCode(), resposta.body());
            diagnostico = ajustarDiagnostico(status, diagnostico);
            Map<String, String> campos;
            try {
                campos = status == StatusConsulta.ENVIADO
                        ? ParserTJPE.extrairCampos(resposta.body())
                        : new LinkedHashMap<>();
            } catch (RuntimeException e) {
                return detalhesComFalhaDeParsing(selo, diagnostico, e);
            }
            return new DetalhesSelo(selo, Tribunal.TJPE, status, campos, null,
                    diagnostico);
        } catch (Exception e) {
            registrarErro("detalhes", e);
            return new DetalhesSelo(selo, Tribunal.TJPE, StatusConsulta.ERRO,
                    new LinkedHashMap<>(), null,
                    HttpUtil.diagnosticarExcecao(urlSolicitada, e));
        }
    }

    private static DiagnosticoConsulta ajustarDiagnostico(
            StatusConsulta status,
            DiagnosticoConsulta diagnostico) {
        if (status != StatusConsulta.ERRO && diagnostico.temFalha()) {
            return diagnostico.comTipoFalha(
                    TipoFalhaConsulta.SEM_FALHA, null);
        }
        return status == StatusConsulta.ERRO && !diagnostico.temFalha()
                ? diagnostico.comTipoFalha(
                TipoFalhaConsulta.RESPOSTA_INESPERADA, null)
                : diagnostico;
    }

    private ResultadoConsulta resultadoComFalhaDeParsing(
            Selo selo,
            DiagnosticoConsulta diagnostico,
            RuntimeException e) {
        return new ResultadoConsulta(selo, Tribunal.TJPE, StatusConsulta.ERRO,
                "", "", "", java.util.Collections.emptyMap(), null,
                diagnostico.comTipoFalha(TipoFalhaConsulta.FALHA_DE_PARSING, e));
    }

    private DetalhesSelo detalhesComFalhaDeParsing(
            Selo selo,
            DiagnosticoConsulta diagnostico,
            RuntimeException e) {
        return new DetalhesSelo(selo, Tribunal.TJPE, StatusConsulta.ERRO,
                new LinkedHashMap<>(), null,
                diagnostico.comTipoFalha(TipoFalhaConsulta.FALHA_DE_PARSING, e));
    }

    static StatusConsulta analisarResposta(int statusCode, String html) {
        if (statusCode < 200 || statusCode >= 300 || html == null || html.isBlank()) {
            return StatusConsulta.ERRO;
        }
        String texto = html.replaceAll("(?is)<[^>]*>", " ")
                .replaceAll("\\s+", " ").toUpperCase();
        if (texto.contains("NÃO FOI ENCONTRADO NENHUM SELO COM O HASH INFORMADO")
                || texto.contains("N&ATILDE;O FOI ENCONTRADO NENHUM SELO COM O HASH INFORMADO")) {
            return StatusConsulta.NAO_ENVIADO;
        }
        return ParserTJPE.extrairNumeroSelo(html).isBlank()
                ? StatusConsulta.ERRO : StatusConsulta.ENVIADO;
    }

    private String montarUrl(Selo selo) {
        String hash = selo == null || selo.getNumero() == null ? "" : selo.getNumero().trim();
        return URL_BASE + URLEncoder.encode(hash, StandardCharsets.UTF_8);
    }

    private void registrarErro(String operacao, Exception e) {
    }

    //endregion
}
