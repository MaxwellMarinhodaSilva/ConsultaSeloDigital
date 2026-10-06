package com.selodigital.interfacegrafica;

import org.junit.jupiter.api.Test;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Container;
import java.awt.GraphicsEnvironment;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

class JanelaPortalTjalTest {

    @Test
    void listaMantemOrdemIgnoraVaziosENaoAbreNadaAutomaticamente() throws Exception {
        assumeFalse(GraphicsEnvironment.isHeadless());
        List<String> abertos = new ArrayList<>();
        Clipboard clipboard = new Clipboard("teste-tjal");

        SwingUtilities.invokeAndWait(() -> {
            JanelaPortalTjal janela = new JanelaPortalTjal(
                    null,
                    List.of("AHE14098-09RX", " ", "ABC", "AHD71760-BYB2"),
                    abertos::add,
                    () -> clipboard
            );
            try {
                JTable tabela = encontrar(janela, JTable.class, null);
                JButton abrir = encontrar(janela, JButton.class, "Abrir no TJAL");
                JButton copiar = encontrar(janela, JButton.class, "Copiar código");

                assertEquals(JDialog.DISPOSE_ON_CLOSE, janela.getDefaultCloseOperation());
                assertEquals(3, tabela.getRowCount());
                assertEquals("AHE14098-09RX", tabela.getValueAt(0, 0));
                assertEquals("ABC", tabela.getValueAt(1, 0));
                assertEquals("AHD71760-BYB2", tabela.getValueAt(2, 0));
                assertEquals("Formato inválido", tabela.getValueAt(1, 1));
                assertFalse(abrir.isEnabled());
                assertTrue(abertos.isEmpty());

                tabela.setRowSelectionInterval(1, 1);
                assertFalse(abrir.isEnabled());
                abrir.doClick();
                assertTrue(abertos.isEmpty());

                copiar.doClick();
                assertEquals("ABC", clipboard.getData(DataFlavor.stringFlavor));

                tabela.setRowSelectionInterval(0, 0);
                abrir.doClick();
                assertEquals(List.of("AHE14098-09RX"), abertos);

                tabela.setRowSelectionInterval(2, 2);
                abrir.doClick();
                assertEquals(List.of("AHE14098-09RX", "AHD71760-BYB2"), abertos);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            } finally {
                janela.dispose();
            }
        });
    }

    @Test
    void evitaSegundoAcionamentoRapidoApenasDoMesmoSelo() {
        long instante = 5_000_000_000L;
        assertTrue(JanelaPrincipal.acionamentoDuplicadoTjal(
                "AHE14098-09RX", "AHE14098-09RX", instante + 100_000_000L, instante));
        assertFalse(JanelaPrincipal.acionamentoDuplicadoTjal(
                "AHD71760-BYB2", "AHE14098-09RX", instante + 100_000_000L, instante));
        assertFalse(JanelaPrincipal.acionamentoDuplicadoTjal(
                "AHE14098-09RX", "AHE14098-09RX", instante + 900_000_000L, instante));
    }

    @Test
    void falhaDoNavegadorMostraUrlSelecionavelECopiaSomenteOEndereco() throws Exception {
        Clipboard clipboard = new Clipboard("teste-endereco-tjal");
        String url = "https://seloapp.tjal.jus.br/publico/selo/AHE14098-09RX";

        SwingUtilities.invokeAndWait(() -> {
            JPanel conteudo = JanelaPrincipal.criarConteudoFalhaAberturaTjal(
                    url, () -> clipboard
            );
            JTextField campo = encontrar(conteudo, JTextField.class, null);
            JButton copiar = encontrar(conteudo, JButton.class, "Copiar endereço");

            assertEquals(url, campo.getText());
            assertFalse(campo.isEditable());
            campo.selectAll();
            assertEquals(url, campo.getSelectedText());
            try {
                copiar.doClick();
                assertEquals(url, clipboard.getData(DataFlavor.stringFlavor));
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    private static <T extends Component> T encontrar(
            Container raiz, Class<T> tipo, String textoBotao
    ) {
        for (Component componente : raiz.getComponents()) {
            if (tipo.isInstance(componente)
                    && (textoBotao == null || textoBotao.equals(((JButton) componente).getText()))) {
                return tipo.cast(componente);
            }
            if (componente instanceof Container container) {
                T encontrado = encontrar(container, tipo, textoBotao);
                if (encontrado != null) {
                    return encontrado;
                }
            }
        }
        return null;
    }
}
