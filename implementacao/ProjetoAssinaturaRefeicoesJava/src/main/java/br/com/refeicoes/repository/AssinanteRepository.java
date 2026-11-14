package br.com.refeicoes.repository;

import br.com.refeicoes.domain.Assinante;
import br.com.refeicoes.util.CsvFile;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

public class AssinanteRepository {
    private final Path arquivo;
    public AssinanteRepository(Path pasta) { arquivo = pasta.resolve("assinantes.csv"); }
    public void salvar(Assinante a) {
        List<Assinante> todos = listarTodos();
        todos.removeIf(x -> x.getId().equals(a.getId())); todos.add(a); gravar(todos);
    }
    public Optional<Assinante> buscarPorCpf(String cpf) {
        return listarTodos().stream().filter(a -> a.getCpf().equals(cpf)).findFirst();
    }
    public Optional<Assinante> buscarPorId(String id) {
        return listarTodos().stream().filter(a -> a.getId().equals(id)).findFirst();
    }
    public List<Assinante> listarTodos() {
        List<Assinante> out = new ArrayList<>();
        for (String[] r : CsvFile.read(arquivo)) if (r.length >= 3) {
            // Compatibilidade: linhas antigas tinham apenas id, CPF e celular.
            String status = r.length >= 4 && !r[3].isBlank() ? r[3] : "BLOQUEADO";
            LocalDateTime inicio = r.length >= 5 && !r[4].isBlank()
                    ? LocalDateTime.parse(r[4]) : LocalDateTime.now();
            out.add(new Assinante(r[0], r[1], r[2], status, inicio));
        }
        return out;
    }
    private void gravar(List<Assinante> todos) {
        List<String[]> rows = new ArrayList<>();
        for (Assinante a : todos) rows.add(new String[]{a.getId(), a.getCpf(), a.getNumeroCelular(), a.getStatus(), a.getDataInicioEstado().toString()});
        CsvFile.write(arquivo, "id;cpf;numeroCelular;status;dataInicioEstado", rows);
    }
}
