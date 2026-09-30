package com.selodigital.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PortalTjalUtilTest {

    @Test
    void preservaOCodigoInformadoNaRotaOficialDoPortal() {

        assertEquals(
                "https://seloapp.tjal.jus.br/publico/selo/AHE14098-09RX",
                PortalTjalUtil.criarUrlConsulta("AHE14098-09RX").toString()
        );
    }

    @Test
    void rejeitaSeloVazioOuEmFormatoInvalido() {

        assertThrows(
                IllegalArgumentException.class,
                () -> PortalTjalUtil.criarUrlConsulta(" ")
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> PortalTjalUtil.criarUrlConsulta("AHE1409809RX")
        );
    }
}
