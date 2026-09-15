package com.selodigital.consulta;

import com.selodigital.modelo.ResultadoConsulta;
import com.selodigital.modelo.Selo;
import com.selodigital.modelo.Tribunal;

import java.util.List;

/** Executa a estratégia selecionada em lote e notifica o progresso por selo. */
public class ConsultaSeloService {

    // =======================================================
    // 1. ESTRUTURA DA CLASSE
    // =======================================================

    //region [Membros da Classe]

    private final ConsultaTribunal consultaTribunal;

    public ConsultaSeloService(ConsultaTribunal consultaTribunal) {
        this.consultaTribunal = consultaTribunal;
    }

    public List<ResultadoConsulta> consultar(List<Selo> selos) {

        return consultar(
                selos,
                null
        );
    }

    public List<ResultadoConsulta> consultar(
            List<Selo> selos,
            ProgressoConsultaListener listener) {

        /* A ordem de entrada é mantida para resultados e exportação. */
        List<ResultadoConsulta> resultados =
                new java.util.ArrayList<>();

        int total = selos.size();

        for (int i = 0; i < total; i++) {

            Selo selo = selos.get(i);

            ResultadoConsulta resultado =
                    consultaTribunal.consultar(selo);

            resultados.add(resultado);

            if (listener != null) {

                listener.atualizar(
                        new com.selodigital.modelo.ProgressoConsulta(
                                i + 1,
                                total,
                                selo
                        )
                );
            }
        }

        return resultados;
    }

    public Tribunal getTribunal() {
        return consultaTribunal.getTribunal();
    }

    //endregion
}
