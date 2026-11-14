package br.com.refeicoes.repository;

import br.com.refeicoes.domain.Assinatura;
import br.com.refeicoes.util.CsvFile;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AssinaturaRepository {
    private final Path arquivo;
    public AssinaturaRepository(Path pasta) { arquivo = pasta.resolve("assinaturas.csv"); }
    public void salvar(String assinanteId, Assinatura a) {
        List<Entry> all = entries(); all.removeIf(e -> e.a.getId().equals(a.getId())); all.add(new Entry(assinanteId, a)); write(all);
    }
    public List<Assinatura> listarPorAssinante(String assinanteId) {
        return entries().stream().filter(e -> e.assinanteId.equals(assinanteId)).map(e -> e.a).toList();
    }
    public Optional<Assinatura> buscarPorId(String id) { return entries().stream().map(e -> e.a).filter(a -> a.getId().equals(id)).findFirst(); }
    public Optional<EntryView> buscarUltimaAtivaOuPendente(String assinanteId) {
        return entries().stream().filter(e -> e.assinanteId.equals(assinanteId))
                .max((a,b) -> a.a.getPrevisaoPrimeiraEntrega().compareTo(b.a.getPrevisaoPrimeiraEntrega()))
                .map(e -> new EntryView(e.assinanteId, e.a));
    }
    public record EntryView(String assinanteId, Assinatura assinatura) { }
    private record Entry(String assinanteId, Assinatura a) { }
    private List<Entry> entries() {
        List<Entry> out = new ArrayList<>();
        for (String[] r : CsvFile.read(arquivo)) if (r.length >= 6)
            out.add(new Entry(r[0], new Assinatura(r[1], r[2], r[3], r[4], LocalDate.parse(r[5]))));
        return out;
    }
    private void write(List<Entry> all) {
        List<String[]> rows = new ArrayList<>();
        for (Entry e : all) rows.add(new String[]{e.assinanteId, e.a.getId(), e.a.getNumeroProtocolo(), e.a.getPlanoId(), e.a.getStatus(), e.a.getPrevisaoPrimeiraEntrega().toString()});
        CsvFile.write(arquivo, "assinanteId;id;numeroProtocolo;planoId;status;previsaoPrimeiraEntrega", rows);
    }
}
