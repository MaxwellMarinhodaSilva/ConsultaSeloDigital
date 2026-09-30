package com.selodigital.consulta;

import com.selodigital.modelo.StatusConsulta;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConsultaTJPBDiagnosticoTest {

    @Test
    void identificaSeloEncontradoEConsultaSemSelo() {

        String paginaEncontrada = """
                <form id="visualizarSeloAtoForm">
                <input name="javax.faces.ViewState">
                <legend>Solicitante</legend>
                <strong>Validador</strong>HWSO
                <canvas id="qrcode-canvas"></canvas>
                """;

        assertEquals(StatusConsulta.ENVIADO,
                ConsultaTJPB.analisarResposta(
                        200,
                        paginaEncontrada,
                        "ASX42202-HWSO",
                        "https://exemplo.test/visualizarSeloAtoPublico.jsf"
                ));
        assertEquals(StatusConsulta.NAO_ENVIADO,
                ConsultaTJPB.analisarResposta(
                        200,
                        "<html>consulta</html>",
                        "ASX42202-HWSO",
                        "https://exemplo.test/consultarSeloAtoPublico.jsf"
                ));
    }

    @Test
    void rejeitaHttpComProblemaEHtmlInesperado() {

        assertEquals(StatusConsulta.ERRO,
                ConsultaTJPB.analisarResposta(
                        403,
                        "<html>desafio</html>",
                        "ASX42202-HWSO",
                        "https://exemplo.test/visualizarSeloAtoPublico.jsf"
                ));
        assertEquals(StatusConsulta.ERRO,
                ConsultaTJPB.analisarResposta(
                        200,
                        "<html>página não reconhecida</html>",
                        "ASX42202-HWSO",
                        "https://exemplo.test/qualquer"
                ));
        assertEquals(StatusConsulta.ERRO,
                ConsultaTJPB.analisarResposta(
                        200,
                        "",
                        "ASX42202-HWSO",
                        "https://exemplo.test/qualquer"
                ));
    }

    @Test
    void confirmaSeloEValidadorQuandoOsDoisCamposEstaoPresentes() {

        Map<String, String> campos = Map.of(
                "Informações do Selo - Selo Nº", "ASX42202",
                "Informações do Selo - Validador", "HWSO"
        );

        assertEquals(true,
                ConsultaTJPB.correspondeAoSeloConsultado(
                        campos,
                        "ASX42202-HWSO"
                ));
        assertEquals(false,
                ConsultaTJPB.correspondeAoSeloConsultado(
                        campos,
                        "ASX42202-ABCD"
                ));
    }
}
