package br.com.refeicoes.repository;

import br.com.refeicoes.domain.Endereco;
import br.com.refeicoes.util.CsvFile;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EnderecoRepository {
    private final Path arquivo;
    public EnderecoRepository(Path pasta) { arquivo = pasta.resolve("enderecos.csv"); }
    public void salvar(String assinanteId, Endereco e) {
        List<Entry> all = entries(); all.removeIf(x -> x.endereco.getId().equals(e.getId())); all.add(new Entry(assinanteId, e)); write(all);
    }
    public List<Endereco> listarPorAssinante(String assinanteId) {
        return entries().stream().filter(e -> e.assinanteId.equals(assinanteId)).map(e -> e.endereco).toList();
    }
    public Optional<Endereco> buscarMaisRecente(String assinanteId) {
        List<Endereco> list = listarPorAssinante(assinanteId);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(list.size() - 1));
    }
    private record Entry(String assinanteId, Endereco endereco) { }
    private List<Entry> entries() {
        List<Entry> out = new ArrayList<>();
        for (String[] r : CsvFile.read(arquivo)) if (r.length >= 8)
            out.add(new Entry(r[0], new Endereco(r[1], r[2], r[3], r[4], r[5], r[6], r[7])));
        return out;
    }
    private void write(List<Entry> all) {
        List<String[]> rows = new ArrayList<>();
        for (Entry e : all) {
            Endereco a = e.endereco;
            rows.add(new String[]{e.assinanteId, a.getId(), a.getLogradouro(), a.getNumero(), a.getComplemento(), a.getBairro(), a.getCidade(), a.getCep()});
        }
        CsvFile.write(arquivo, "assinanteId;id;logradouro;numero;complemento;bairro;cidade;cep", rows);
    }
}
