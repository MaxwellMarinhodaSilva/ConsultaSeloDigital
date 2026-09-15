package com.selodigital.historico;

import com.selodigital.modelo.Tribunal;
import com.selodigital.modelo.ResultadoConsulta;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Registro serializável de uma consulta, separado dos dados retornados pelo tribunal. */
public class RegistroHistorico {

    // =======================================================
    // 1. ESTRUTURA DA CLASSE
    // =======================================================

    //region [Membros da Classe]
    private int numero;
    private LocalDateTime dataConsulta;
    private Tribunal tribunal;
    private List<String> selos = new ArrayList<>();
    private List<ResultadoConsulta> resultados = new ArrayList<>();
    public RegistroHistorico() { }
    public RegistroHistorico(int numero, LocalDateTime dataConsulta, Tribunal tribunal, List<String> selos) { this.numero=numero; this.dataConsulta=dataConsulta; this.tribunal=tribunal; this.selos=new ArrayList<>(selos); }
    public RegistroHistorico(int numero, LocalDateTime dataConsulta, Tribunal tribunal, List<String> selos, List<ResultadoConsulta> resultados) { this(numero, dataConsulta, tribunal, selos); this.resultados=resultados==null?new ArrayList<>():new ArrayList<>(resultados); }
    public int getNumero(){return numero;} public void setNumero(int numero){this.numero=numero;}
    public LocalDateTime getDataConsulta(){return dataConsulta;} public void setDataConsulta(LocalDateTime dataConsulta){this.dataConsulta=dataConsulta;}
    public Tribunal getTribunal(){return tribunal;} public void setTribunal(Tribunal tribunal){this.tribunal=tribunal;}
    public List<String> getSelos(){return selos;} public void setSelos(List<String> selos){this.selos=selos==null?new ArrayList<>():new ArrayList<>(selos);}
    public List<ResultadoConsulta> getResultados(){return resultados;} public void setResultados(List<ResultadoConsulta> resultados){this.resultados=resultados==null?new ArrayList<>():new ArrayList<>(resultados);}

    //endregion
}
