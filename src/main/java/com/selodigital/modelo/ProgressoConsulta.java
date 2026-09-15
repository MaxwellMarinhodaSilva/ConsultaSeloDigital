package com.selodigital.modelo;

/** Instantâneo imutável usado para atualizar o diálogo de progresso da consulta em lote. */
public class ProgressoConsulta {

    // =======================================================
    // 1. ESTRUTURA DA CLASSE
    // =======================================================

    //region [Membros da Classe]

    private final int atual;
    private final int total;
    private final Selo selo;

    public ProgressoConsulta(
            int atual,
            int total,
            Selo selo) {

        this.atual = atual;
        this.total = total;
        this.selo = selo;
    }

    public int getAtual() {
        return atual;
    }

    public int getTotal() {
        return total;
    }

    public Selo getSelo() {
        return selo;
    }

    public int getPorcentagem() {

        if (total <= 0) {
            return 0;
        }

        return (int) (
                ((double) atual / total) * 100
        );
    }

    //endregion
}
