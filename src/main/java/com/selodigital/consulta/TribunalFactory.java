package com.selodigital.consulta;

import com.selodigital.modelo.Tribunal;

/** Centraliza a escolha da estratégia de consulta para cada tribunal suportado. */
public final class TribunalFactory {

    // =======================================================
    // 1. ESTRUTURA DA CLASSE
    // =======================================================

    //region [Membros da Classe]

    private TribunalFactory() {
    }

    public static ConsultaTribunal criar(Tribunal tribunal) {

        return switch (tribunal) {
            case TJPB -> new ConsultaTJPB();
            case TJRN -> new ConsultaTJRN();
            case TJPE -> new ConsultaTJPE();
            default -> throw new IllegalArgumentException("Tribunal sem consulta disponível: " + tribunal);
        };
    }

    //endregion
}
