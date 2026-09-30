package com.selodigital.modelo;

/** Classifica a causa técnica sem alterar o status funcional exibido ao usuário. */
public enum TipoFalhaConsulta {

    SEM_FALHA,
    FALHA_HTTP,
    BLOQUEIO_CLOUDFLARE,
    TIMEOUT,
    FALHA_CONEXAO,
    RESPOSTA_INESPERADA,
    FALHA_DE_PARSING,
    FALHA_INTERNA
}
