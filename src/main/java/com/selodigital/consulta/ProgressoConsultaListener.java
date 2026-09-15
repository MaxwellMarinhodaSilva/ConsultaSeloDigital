package com.selodigital.consulta;

import com.selodigital.modelo.ProgressoConsulta;

/** Recebe atualizações unitárias da consulta em lote sem acoplar serviço e interface. */
@FunctionalInterface
public interface ProgressoConsultaListener {

    // =======================================================
    // 1. ESTRUTURA DA CLASSE
    // =======================================================

    //region [Membros da Classe]

    void atualizar(ProgressoConsulta progresso);

    //endregion
}
