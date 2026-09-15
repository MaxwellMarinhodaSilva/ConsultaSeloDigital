package com.selodigital.historico;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.selodigital.modelo.Tribunal;
import com.selodigital.modelo.ResultadoConsulta;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/** Persistência local do histórico, independente dos resultados devolvidos pelos tribunais. */
public final class HistoricoService {

    // =======================================================
    // 1. ESTRUTURA DA CLASSE
    // =======================================================

    //region [Membros da Classe]
    private static final File ARQUIVO = new File(System.getProperty("user.dir"), "historico-consultas.json");
    private static final ObjectMapper MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());
    private HistoricoService() { }
    /** O primeiro uso não cria arquivo: apenas apresenta uma lista vazia. */
    public static synchronized List<RegistroHistorico> carregar() throws IOException { return ARQUIVO.exists() ? MAPPER.readValue(ARQUIVO, new TypeReference<List<RegistroHistorico>>() {}) : new ArrayList<>(); }
    public static synchronized void registrar(Tribunal tribunal, List<String> selos) throws IOException { registrar(tribunal, selos, new ArrayList<>()); }
    public static synchronized void registrar(Tribunal tribunal, List<String> selos, List<ResultadoConsulta> resultados) throws IOException {
        List<RegistroHistorico> lista = carregar();
        List<String> selosConsultados = new ArrayList<>(new LinkedHashSet<>(selos));
        RegistroHistorico existente = lista.stream()
                .filter(registro -> registro.getTribunal() == tribunal
                        && new LinkedHashSet<>(registro.getSelos()).equals(
                        new LinkedHashSet<>(selosConsultados)))
                .findFirst()
                .orElse(null);

        if (existente == null) {
            int proximo = lista.stream().mapToInt(RegistroHistorico::getNumero).max().orElse(0) + 1;
            lista.add(new RegistroHistorico(
                    proximo, LocalDateTime.now(), tribunal,
                    selosConsultados, resultados));
        } else {
            existente.setDataConsulta(LocalDateTime.now());
            existente.setResultados(resultados);
        }
        salvar(lista);
    }
    public static synchronized void remover(RegistroHistorico registro) throws IOException { List<RegistroHistorico> lista=carregar(); lista.removeIf(r -> r.getNumero()==registro.getNumero()); salvar(lista); }
    public static synchronized void apagarTudo() throws IOException { salvar(new ArrayList<>()); }
    private static void salvar(List<RegistroHistorico> lista) throws IOException { MAPPER.writerWithDefaultPrettyPrinter().writeValue(ARQUIVO, lista); }

    //endregion
}
