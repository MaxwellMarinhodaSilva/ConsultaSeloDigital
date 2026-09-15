package com.selodigital.modelo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Resultado imutável da consulta inicial, reunindo o status da tabela e os campos já extraídos.
 * Manter os campos aqui evita nova consulta quando o usuário abre a janela de detalhes.
 */
public class ResultadoConsulta {

    // =======================================================
    // 1. ESTRUTURA DA CLASSE
    // =======================================================

    //region [Membros da Classe]

    private final Selo selo;
    private final Tribunal tribunal;
    private final StatusConsulta status;

    /*
     * Dados necessários para a tabela de resultados.
     *
     * Esses dados já são obtidos durante a consulta inicial.
     */
    private final String matriculaProtocolo;
    private final String tipoAto;
    private final String subtipoAto;

    /*
     * Todos os campos extraídos da página de detalhes.
     *
     * Isso permite que a janela de detalhes continue utilizando
     * exatamente os mesmos dados, sem precisar consultar o TJPB
     * novamente.
     */
    private final Map<String, String> campos;

    /*
     * Conteúdo utilizado para gerar o QR Code do selo.
     *
     * Este dado pertence ao resultado da consulta e não à interface.
     *
     * Cada tribunal será responsável por informar o conteúdo
     * quando possuir QR Code.
     *
     * Quando não houver QR Code:
     *
     *     null
     *
     * A interface apenas verifica se o conteúdo existe.
     */
    private final String qrCodeConteudo;

    public ResultadoConsulta(
            Selo selo,
            Tribunal tribunal,
            StatusConsulta status) {

        this(
                selo,
                tribunal,
                status,
                "",
                "",
                "",
                Collections.emptyMap(),
                null
        );
    }

    /*
     * Construtor de compatibilidade.
     *
     * Mantém funcionando o código existente que ainda não
     * informa o conteúdo do QR Code.
     */
    public ResultadoConsulta(
            Selo selo,
            Tribunal tribunal,
            StatusConsulta status,
            String matriculaProtocolo,
            String tipoAto,
            String subtipoAto,
            Map<String, String> campos) {

        this(
                selo,
                tribunal,
                status,
                matriculaProtocolo,
                tipoAto,
                subtipoAto,
                campos,
                null
        );
    }

    @JsonCreator
    public ResultadoConsulta(
            @JsonProperty("selo") Selo selo,
            @JsonProperty("tribunal") Tribunal tribunal,
            @JsonProperty("status") StatusConsulta status,
            @JsonProperty("matriculaProtocolo") String matriculaProtocolo,
            @JsonProperty("tipoAto") String tipoAto,
            @JsonProperty("subtipoAto") String subtipoAto,
            @JsonProperty("campos") Map<String, String> campos,
            @JsonProperty("qrCodeConteudo") String qrCodeConteudo) {

        this.selo = selo;
        this.tribunal = tribunal;
        this.status = status;

        this.matriculaProtocolo =
                matriculaProtocolo == null
                        ? ""
                        : matriculaProtocolo.trim();

        this.tipoAto =
                tipoAto == null
                        ? ""
                        : tipoAto.trim();

        this.subtipoAto =
                subtipoAto == null
                        ? ""
                        : subtipoAto.trim();

        this.campos =
                campos == null
                        ? Collections.emptyMap()
                        : Collections.unmodifiableMap(
                        new LinkedHashMap<>(campos)
                );

        this.qrCodeConteudo =
                qrCodeConteudo == null || qrCodeConteudo.isBlank()
                        ? null
                        : qrCodeConteudo.trim();
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

    public String getMatriculaProtocolo() {
        return matriculaProtocolo;
    }

    public String getTipoAto() {
        return tipoAto;
    }

    public String getSubtipoAto() {
        return subtipoAto;
    }

    public Map<String, String> getCampos() {
        return campos;
    }

    public String getQrCodeConteudo() {
        return qrCodeConteudo;
    }

    //endregion
}
