package com.selodigital.modelo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Contexto técnico seguro, sem reter corpo, cabeçalhos, cookies ou tokens. */
public final class DiagnosticoConsulta {

    private final TipoFalhaConsulta tipoFalha;
    private final int codigoHttp;
    private final String urlSolicitada;
    private final String urlFinal;
    private final String contentType;
    private final int quantidadeRedirecionamentos;
    private final String causaTecnica;

    @JsonCreator
    public DiagnosticoConsulta(
            @JsonProperty("tipoFalha") TipoFalhaConsulta tipoFalha,
            @JsonProperty("codigoHttp") int codigoHttp,
            @JsonProperty("urlSolicitada") String urlSolicitada,
            @JsonProperty("urlFinal") String urlFinal,
            @JsonProperty("contentType") String contentType,
            @JsonProperty("quantidadeRedirecionamentos") int quantidadeRedirecionamentos,
            @JsonProperty("causaTecnica") String causaTecnica) {

        this.tipoFalha = tipoFalha == null ? TipoFalhaConsulta.FALHA_INTERNA : tipoFalha;
        this.codigoHttp = codigoHttp;
        this.urlSolicitada = texto(urlSolicitada);
        this.urlFinal = texto(urlFinal);
        this.contentType = texto(contentType);
        this.quantidadeRedirecionamentos = Math.max(quantidadeRedirecionamentos, 0);
        this.causaTecnica = texto(causaTecnica);
    }

    public static DiagnosticoConsulta semFalha() {
        return new DiagnosticoConsulta(TipoFalhaConsulta.SEM_FALHA, -1,
                "", "", "", 0, "");
    }

    public DiagnosticoConsulta comTipoFalha(TipoFalhaConsulta tipo, Throwable causa) {
        return new DiagnosticoConsulta(tipo, codigoHttp, urlSolicitada, urlFinal,
                contentType, quantidadeRedirecionamentos, descreverCausa(causa));
    }

    public TipoFalhaConsulta getTipoFalha() { return tipoFalha; }
    public int getCodigoHttp() { return codigoHttp; }
    public String getUrlSolicitada() { return urlSolicitada; }
    public String getUrlFinal() { return urlFinal; }
    public String getContentType() { return contentType; }
    public int getQuantidadeRedirecionamentos() { return quantidadeRedirecionamentos; }
    public String getCausaTecnica() { return causaTecnica; }
    public boolean temFalha() { return tipoFalha != TipoFalhaConsulta.SEM_FALHA; }

    public boolean houveRedirecionamento() {
        return quantidadeRedirecionamentos > 0
                || (!urlSolicitada.isBlank() && !urlFinal.isBlank()
                && !urlSolicitada.equals(urlFinal));
    }

    public static String descreverCausa(Throwable causa) {
        if (causa == null) {
            return "";
        }
        String mensagem = texto(causa.getMessage()).replaceAll("[\\r\\n]+", " ");
        if (mensagem.length() > 240) {
            mensagem = mensagem.substring(0, 240);
        }
        return mensagem.isBlank() ? causa.getClass().getName()
                : causa.getClass().getName() + ": " + mensagem;
    }

    private static String texto(String valor) {
        return valor == null ? "" : valor.trim();
    }
}
