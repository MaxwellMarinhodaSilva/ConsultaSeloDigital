package com.selodigital.modelo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Valor imutável do código normalizado que será enviado ao tribunal. */
public class Selo {

    // =======================================================
    // 1. ESTRUTURA DA CLASSE
    // =======================================================

    //region [Membros da Classe]

    private final String numero;

    @JsonCreator
    public Selo(@JsonProperty("numero") String numero) {
        this.numero = numero;
    }

    public String getNumero() {
        return numero;
    }

    @Override
    public String toString() {
        return numero;
    }

    //endregion
}
