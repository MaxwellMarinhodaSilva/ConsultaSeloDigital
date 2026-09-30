package com.selodigital.consulta;

import com.selodigital.modelo.StatusConsulta;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConsultaTribunalHttpStatusTest {

    @Test
    void rejeitaTodoHttpNao2xxAntesDoParser() {

        String paginaTjrn = "<div class=\"conteudoSemRotulo\">"
                + "Código: RN202600954140038134JDT (Ativo)</div>";

        for (int codigo : new int[]{302, 403, 404, 429, 500}) {
            assertEquals(StatusConsulta.ERRO,
                    ConsultaTJRN.analisarResposta(
                            codigo,
                            paginaTjrn,
                            "RN202600954140038134JDT"
                    ));
            assertEquals(StatusConsulta.ERRO,
                    ConsultaTJPE.analisarResposta(
                            codigo,
                            "<html><label>Selo Eletrônico</label></html>"
                    ));
            assertEquals(StatusConsulta.ERRO,
                    ConsultaTJPB.analisarResposta(
                            codigo,
                            "<html>conteúdo</html>",
                            "ASX42202-HWSO",
                            "https://exemplo.test/visualizarSeloAtoPublico.jsf"
                    ));
        }
    }
}
