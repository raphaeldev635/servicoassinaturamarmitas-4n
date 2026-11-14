package br.com.refeicoes.repository;

import br.com.refeicoes.domain.Cartao;
import br.com.refeicoes.util.CsvFile;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Persiste apenas os quatro últimos dígitos. Número completo e CVV não são gravados. */
public class CartaoRepository {
    private final Path arquivo;
    public CartaoRepository(Path pasta) { arquivo = pasta.resolve("cartoes.csv"); }
    public void salvar(String assinanteId, Cartao cartao) {
        List<Entry> all = entries(); all.removeIf(e -> e.cartao.getId().equals(cartao.getId())); all.add(new Entry(assinanteId, cartao)); write(all);
    }
    public List<Cartao> listarPorAssinante(String assinanteId) {
        return entries().stream().filter(e -> e.assinanteId.equals(assinanteId)).map(e -> e.cartao).toList();
    }
    private record Entry(String assinanteId, Cartao cartao) { }
    private List<Entry> entries() {
        List<Entry> out = new ArrayList<>();
        for (String[] r : CsvFile.read(arquivo)) if (r.length >= 6)
            out.add(new Entry(r[0], new Cartao(r[1], "", r[2], r[3], r[4], "", r[5])));
        return out;
    }
    private void write(List<Entry> all) {
        List<String[]> rows = new ArrayList<>();
        for (Entry e : all) {
            Cartao c = e.cartao;
            rows.add(new String[]{e.assinanteId, c.getId(), c.getUltimosQuatroDigitos(), c.getNomeTitular(), c.getValidade(), c.getOperadora()});
        }
        CsvFile.write(arquivo, "assinanteId;id;ultimos4;nomeTitular;validade;operadora", rows);
    }
}
