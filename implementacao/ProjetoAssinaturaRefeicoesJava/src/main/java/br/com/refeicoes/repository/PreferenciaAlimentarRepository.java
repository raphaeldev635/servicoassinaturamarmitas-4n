package br.com.refeicoes.repository;

import br.com.refeicoes.domain.PreferenciaAlimentar;
import br.com.refeicoes.util.CsvFile;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Mantém as opções de preferência pré-cadastradas em memória e persiste apenas
 * as escolhas particulares feitas por cada assinante.
 */
public class PreferenciaAlimentarRepository {
    private final Path arquivo;
    private final Map<String, PreferenciaAlimentar> opcoes = new LinkedHashMap<>();

    public PreferenciaAlimentarRepository(Path pasta, List<PreferenciaAlimentar> opcoesIniciais) {
        arquivo = pasta.resolve("preferencias_alimentares.csv");
        for (PreferenciaAlimentar opcao : opcoesIniciais) {
            opcoes.put(normalizar(opcao.getTipo()), opcao);
        }
    }

    public List<PreferenciaAlimentar> listarOpcoesDisponiveis() {
        return new ArrayList<>(opcoes.values());
    }

    public Optional<PreferenciaAlimentar> buscarOpcao(String tipo) {
        return Optional.ofNullable(opcoes.get(normalizar(tipo)));
    }

    public void substituirPreferencias(String assinanteId, List<String> tipos) {
        List<Entry> todas = entradas();
        todas.removeIf(e -> e.assinanteId.equals(assinanteId));
        for (String tipo : tipos) {
            PreferenciaAlimentar opcao = buscarOpcao(tipo)
                    .orElseThrow(() -> new IllegalArgumentException("Preferência não cadastrada: " + tipo));
            todas.add(new Entry(assinanteId, new PreferenciaAlimentar(opcao.getId(), opcao.getTipo())));
        }
        gravar(todas);
    }

    public List<PreferenciaAlimentar> listarPorAssinante(String assinanteId) {
        return entradas().stream()
                .filter(e -> e.assinanteId.equals(assinanteId))
                .map(Entry::preferencia)
                .toList();
    }

    private record Entry(String assinanteId, PreferenciaAlimentar preferencia) { }

    private List<Entry> entradas() {
        List<Entry> resultado = new ArrayList<>();
        for (String[] linha : CsvFile.read(arquivo)) {
            if (linha.length >= 3) {
                resultado.add(new Entry(linha[0], new PreferenciaAlimentar(linha[1], linha[2])));
            }
        }
        return resultado;
    }

    private void gravar(List<Entry> todas) {
        List<String[]> linhas = new ArrayList<>();
        for (Entry entry : todas) {
            linhas.add(new String[]{entry.assinanteId, entry.preferencia.getId(), entry.preferencia.getTipo()});
        }
        CsvFile.write(arquivo, "assinanteId;id;tipo", linhas);
    }

    private static String normalizar(String tipo) {
        return tipo == null ? "" : tipo.trim().toUpperCase().replace(' ', '_');
    }
}
