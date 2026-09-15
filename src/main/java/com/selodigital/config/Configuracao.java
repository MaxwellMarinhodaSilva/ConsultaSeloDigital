package com.selodigital.config;

/**
 * Estado persistível das preferências visuais da janela principal.
 * Os valores padrão permitem iniciar a aplicação mesmo antes do primeiro salvamento.
 */
public class Configuracao {

    // =======================================================
    // 1. ESTRUTURA DA CLASSE
    // =======================================================

    //region [Membros da Classe]

    /*
     * ============================================================
     * JANELA PRINCIPAL
     * ============================================================
     */

    private int janelaPrincipalX = -1;
    private int janelaPrincipalY = -1;

    private int janelaPrincipalLargura = 950;
    private int janelaPrincipalAltura = 650;


    /*
     * ============================================================
     * GETTERS E SETTERS
     * ============================================================
     */

    public int getJanelaPrincipalX() {
        return janelaPrincipalX;
    }

    public void setJanelaPrincipalX(
            int janelaPrincipalX) {

        this.janelaPrincipalX =
                janelaPrincipalX;
    }

    public int getJanelaPrincipalY() {
        return janelaPrincipalY;
    }

    public void setJanelaPrincipalY(
            int janelaPrincipalY) {

        this.janelaPrincipalY =
                janelaPrincipalY;
    }

    public int getJanelaPrincipalLargura() {
        return janelaPrincipalLargura;
    }

    public void setJanelaPrincipalLargura(
            int janelaPrincipalLargura) {

        this.janelaPrincipalLargura =
                janelaPrincipalLargura;
    }

    public int getJanelaPrincipalAltura() {
        return janelaPrincipalAltura;
    }

    public void setJanelaPrincipalAltura(
            int janelaPrincipalAltura) {

        this.janelaPrincipalAltura =
                janelaPrincipalAltura;
    }

    //endregion
}
