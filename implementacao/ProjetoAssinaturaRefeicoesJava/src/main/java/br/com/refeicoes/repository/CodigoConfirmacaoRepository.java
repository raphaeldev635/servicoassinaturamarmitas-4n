package br.com.refeicoes.repository;

import br.com.refeicoes.domain.CodigoConfirmacao;
import br.com.refeicoes.util.CsvFile;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class CodigoConfirmacaoRepository {
    private final Path arquivo;
    public CodigoConfirmacaoRepository(Path pasta) { arquivo = pasta.resolve("codigos_confirmacao.csv"); }
    public void salvar(String assinanteId, CodigoConfirmacao codigo) {
        List<Entry> all = entries(); all.removeIf(e -> e.codigo.getId().equals(codigo.getId())); all.add(new Entry(assinanteId, codigo)); write(all);
    }
    public Optional<CodigoConfirmacao> buscarMaisRecente(String assinanteId) {
        return entries().stream().filter(e -> e.assinanteId.equals(assinanteId))
                .max(Comparator.comparing(e -> e.codigo.getDataEnvio())).map(e -> e.codigo);
    }
    private record Entry(String assinanteId, CodigoConfirmacao codigo) { }
    private List<Entry> entries() {
        List<Entry> out = new ArrayList<>();
        for (String[] r : CsvFile.read(arquivo)) if (r.length >= 7)
            out.add(new Entry(r[0], new CodigoConfirmacao(r[1], r[2], LocalDateTime.parse(r[3]), LocalDateTime.parse(r[4]), r[5], Integer.parseInt(r[6]))));
        return out;
    }
    private void write(List<Entry> all) {
        List<String[]> rows = new ArrayList<>();
        for (Entry e : all) {
            CodigoConfirmacao c = e.codigo;
            rows.add(new String[]{e.assinanteId, c.getId(), c.getCodigo(), c.getDataEnvio().toString(), c.getDataExpiracao().toString(), c.getStatus(), String.valueOf(c.getTentativas())});
        }
        CsvFile.write(arquivo, "assinanteId;id;codigo;dataEnvio;dataExpiracao;status;tentativas", rows);
    }
}
