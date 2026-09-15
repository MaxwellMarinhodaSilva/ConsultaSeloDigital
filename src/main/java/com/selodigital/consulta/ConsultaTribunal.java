package com.selodigital.consulta;

import com.selodigital.modelo.DetalhesSelo;
import com.selodigital.modelo.ResultadoConsulta;
import com.selodigital.modelo.Selo;
import com.selodigital.modelo.Tribunal;

/** Contrato comum para consultar um selo e, quando solicitado, seus detalhes. */
public interface ConsultaTribunal {

    // =======================================================
    // 1. ESTRUTURA DA CLASSE
    // =======================================================

    //region [Membros da Classe]

    Tribunal getTribunal();

    ResultadoConsulta consultar(Selo selo);

    DetalhesSelo consultarDetalhes(Selo selo);

    //endregion
}
