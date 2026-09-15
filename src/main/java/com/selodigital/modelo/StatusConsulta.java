package com.selodigital.modelo;

/** Estados exibidos para uma resposta de consulta, inclusive falhas de comunicação. */
public enum StatusConsulta {

    // =======================================================
    // 1. ESTRUTURA DA CLASSE
    // =======================================================

    //region [Membros da Classe]

    ENVIADO("Enviado"),
    CANCELADO("Cancelado"),
    APENAS_GERADO_NAO_UTILIZADO("Apenas gerado - Não utilizado"),
    NAO_ENVIADO("Não Enviado"),
    ERRO("Erro");

    private final String descricao;

    StatusConsulta(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    @Override
    public String toString() {
        return descricao;
    }

    //endregion
}
