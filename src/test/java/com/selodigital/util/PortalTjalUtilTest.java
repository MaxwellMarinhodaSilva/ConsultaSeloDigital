package com.selodigital.util;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PortalTjalUtilTest {

    @Test
    void aceitaOsSeteCodigosEConstroiSuasUrlsSemAlterarOCodigo() {
        List<String> selos = List.of(
                "AHE14098-09RX",
                "AHD71760-BYB2",
                "AHE13171-XV48",
                "AHE13968-G4U5",
                "AHE14808-TDWY",
                "ACJ86365-E55M",
                "ACJ57544-JCPZ"
        );

        for (String selo : selos) {
            assertEquals(
                    "https://seloapp.tjal.jus.br/publico/selo/" + selo,
                    PortalTjalUtil.criarUrlConsulta(selo).toString()
            );
        }
    }

    @Test
    void removeApenasEspacosNasExtremidadesEPreservaCaixaDoCodigo() {
        assertEquals(
                "https://seloapp.tjal.jus.br/publico/selo/AHE14098-09RX",
                PortalTjalUtil.criarUrlConsulta("  AHE14098-09RX  ").toString()
        );
        assertEquals(
                "https://seloapp.tjal.jus.br/publico/selo/ahe14098-09rx",
                PortalTjalUtil.criarUrlConsulta("ahe14098-09rx").toString()
        );
    }

    @Test
    void rejeitaEntradasVaziasOuComCaracteresIndevidos() {
        for (String invalido : new String[]{
                null, "", " ", "AHE1409809RX", "AHE14098-09R",
                "AHE14098-09RX/", "AHE14098-09RX?x=1",
                "AHE 14098-09RX", "AHE14098-09R#"
        }) {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> PortalTjalUtil.criarUrlConsulta(invalido)
            );
        }
    }
}
