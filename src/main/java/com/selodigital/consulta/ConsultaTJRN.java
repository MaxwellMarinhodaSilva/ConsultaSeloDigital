package com.selodigital.consulta;

import com.selodigital.modelo.DetalhesSelo;
import com.selodigital.modelo.ResultadoConsulta;
import com.selodigital.modelo.Selo;
import com.selodigital.modelo.StatusConsulta;
import com.selodigital.modelo.Tribunal;
import com.selodigital.util.HttpUtil;

import java.text.Normalizer;
import java.net.http.HttpResponse;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/** Consulta o SIEX e delega a interpretação do documento ao ParserTJRN. */
public class ConsultaTJRN implements ConsultaTribunal {

    // =======================================================
    // 1. ESTRUTURA DA CLASSE
    // =======================================================

    //region [Membros da Classe]

    private static final String URL_BASE = "https://selodigital.tjrn.jus.br/siex/qrcode?";

    @Override
    public Tribunal getTribunal() {
        return Tribunal.TJRN;
    }

    @Override
    public ResultadoConsulta consultar(Selo selo) {
        try {
            HttpResponse<String> resposta = HttpUtil.get(montarUrlConsulta(selo.getNumero()));
            StatusConsulta status = analisarResposta(resposta.statusCode(), resposta.body(), selo.getNumero());
            Map<String, String> campos = ParserTJRN.extrairCampos(resposta.body());

            return new ResultadoConsulta(
                    selo, Tribunal.TJRN, status,
                    ParserTJRN.extrairMatriculaProtocolo(campos),
                    ParserTJRN.extrairTipoAto(campos),
                    ParserTJRN.extrairSubtipoAto(campos),
                    campos,
                    ParserTJRN.extrairQrCodeConteudo(resposta.body(), selo.getNumero()));
        } catch (Exception e) {
            registrarErro("consulta", e);
            return new ResultadoConsulta(selo, Tribunal.TJRN, StatusConsulta.ERRO);
        }
    }

    @Override
    public DetalhesSelo consultarDetalhes(Selo selo) {
        try {
            HttpResponse<String> resposta = HttpUtil.get(montarUrlConsulta(selo.getNumero()));
            StatusConsulta status = analisarResposta(resposta.statusCode(), resposta.body(), selo.getNumero());
            Map<String, String> campos = ParserTJRN.extrairCampos(resposta.body());
            return new DetalhesSelo(selo, Tribunal.TJRN, status, campos,
                    ParserTJRN.extrairQrCodeConteudo(resposta.body(), selo.getNumero()));
        } catch (Exception e) {
            registrarErro("detalhes", e);
            return new DetalhesSelo(selo, Tribunal.TJRN, StatusConsulta.ERRO,
                    new LinkedHashMap<>(), "");
        }
    }

    private String montarUrlConsulta(String numeroSelo) {
        return URL_BASE + (numeroSelo == null ? "" : numeroSelo.trim());
    }

    /* Visível ao teste do pacote: decide status, sem realizar HTTP. */
    static StatusConsulta analisarResposta(int statusCode, String html, String numeroSelo) {
        if (html == null || html.isBlank() || statusCode >= 500) {
            return StatusConsulta.ERRO;
        }

        boolean possuiCodigo = ParserTJRN.possuiCodigoDoSelo(html, numeroSelo);
        String estadoDeclarado = normalizar(ParserTJRN.extrairStatusSelo(html));

        // A mensagem pode aparecer em "Selos vinculados" de um selo cancelado.
        // Por isso o cabeçalho do selo tem precedência absoluta sobre ela.
        if (possuiCodigo) {
            if (estadoDeclarado.contains("cancelado")) {
                return StatusConsulta.CANCELADO;
            }
            if (estadoDeclarado.contains("apenas gerado")
                    && estadoDeclarado.contains("nao utilizado")) {
                return StatusConsulta.APENAS_GERADO_NAO_UTILIZADO;
            }
            // Estados conhecidos (Atualizado) e futuros ainda são um selo encontrado.
            return StatusConsulta.ENVIADO;
        }

        if (ParserTJRN.possuiMensagemCodigoNaoEncontrado(html)) {
            return StatusConsulta.NAO_ENVIADO;
        }
        return StatusConsulta.ERRO;
    }

    private static String normalizar(String valor) {
        return Normalizer.normalize(valor == null ? "" : valor, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);
    }

    private static void registrarErro(String operacao, Exception e) {
    }

    //endregion
}
