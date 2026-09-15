package com.selodigital.modelo;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Dados completos de um selo usados pela janela de detalhes e pela exportação. */
public class DetalhesSelo {

    // =======================================================
    // 1. ESTRUTURA DA CLASSE
    // =======================================================

    //region [Membros da Classe]

    private final Selo selo;
    private final Tribunal tribunal;
    private final StatusConsulta status;

    private final Map<String, String> campos;
    private final String qrCodeConteudo;

    public DetalhesSelo(
            Selo selo,
            Tribunal tribunal,
            StatusConsulta status,
            Map<String, String> campos,
            String qrCodeConteudo) {

        this.selo = selo;
        this.tribunal = tribunal;
        this.status = status;

        this.campos =
                campos == null
                        ? Collections.emptyMap()
                        : Collections.unmodifiableMap(
                        new LinkedHashMap<>(campos)
                );

        this.qrCodeConteudo =
                qrCodeConteudo == null
                        ? ""
                        : qrCodeConteudo;
    }

    public Selo getSelo() {
        return selo;
    }

    public Tribunal getTribunal() {
        return tribunal;
    }

    public StatusConsulta getStatus() {
        return status;
    }

    public Map<String, String> getCampos() {
        return campos;
    }

    public String getQrCodeConteudo() {
        return qrCodeConteudo;
    }

    //endregion
}
