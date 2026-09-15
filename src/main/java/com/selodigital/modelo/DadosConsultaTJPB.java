package com.selodigital.modelo;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Agrupa os campos específicos do TJPB antes de compor o resultado comum. */
public class DadosConsultaTJPB {

    // =======================================================
    // 1. ESTRUTURA DA CLASSE
    // =======================================================

    //region [Membros da Classe]

    private final String numeroSelo;
    private final String tipoAto;
    private final String subtipoAto;
    private final String matriculaProtocolo;
    private final Map<String, String> campos;

    public DadosConsultaTJPB(
            String numeroSelo,
            String tipoAto,
            String subtipoAto,
            String matriculaProtocolo,
            Map<String, String> campos) {

        this.numeroSelo =
                numeroSelo == null
                        ? ""
                        : numeroSelo.trim();

        this.tipoAto =
                tipoAto == null
                        ? ""
                        : tipoAto.trim();

        this.subtipoAto =
                subtipoAto == null
                        ? ""
                        : subtipoAto.trim();

        this.matriculaProtocolo =
                matriculaProtocolo == null
                        ? ""
                        : matriculaProtocolo.trim();

        this.campos =
                campos == null
                        ? Collections.emptyMap()
                        : Collections.unmodifiableMap(
                        new LinkedHashMap<>(campos)
                );
    }

    public String getNumeroSelo() {
        return numeroSelo;
    }

    public String getTipoAto() {
        return tipoAto;
    }

    public String getSubtipoAto() {
        return subtipoAto;
    }

    public String getMatriculaProtocolo() {
        return matriculaProtocolo;
    }

    public Map<String, String> getCampos() {
        return campos;
    }

    //endregion
}
