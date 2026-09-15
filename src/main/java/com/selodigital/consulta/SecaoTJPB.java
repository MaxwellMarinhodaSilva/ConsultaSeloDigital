package com.selodigital.consulta;

import java.util.LinkedHashMap;
import java.util.Map;

/** Representa um bloco HTML do TJPB durante a extração ordenada dos campos. */
public class SecaoTJPB {

    // =======================================================
    // 1. ESTRUTURA DA CLASSE
    // =======================================================

    //region [Membros da Classe]

    private final String nome;

    private final Map<String, String> campos =
            new LinkedHashMap<>();

    public SecaoTJPB(String nome) {
        this.nome = nome;
    }

    public String getNome() {
        return nome;
    }

    public Map<String, String> getCampos() {
        return campos;
    }

    public void adicionarCampo(
            String nome,
            String valor) {

        if (valor == null) {
            valor = "";
        }

        campos.put(
                nome,
                valor.trim()
        );
    }

    //endregion
}
