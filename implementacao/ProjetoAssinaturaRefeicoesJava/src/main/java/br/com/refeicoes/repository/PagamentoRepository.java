package br.com.refeicoes.repository;

import br.com.refeicoes.domain.Pagamento;
import br.com.refeicoes.util.CsvFile;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PagamentoRepository {
    private final Path arquivo;
    public PagamentoRepository(Path pasta) { arquivo = pasta.resolve("pagamentos.csv"); }
    public void salvar(String assinanteId, String assinaturaId, Pagamento pagamento) {
        List<Entry> all = entries(); all.removeIf(e -> e.pagamento.getId().equals(pagamento.getId()));
        all.add(new Entry(assinanteId, assinaturaId, pagamento)); write(all);
    }
    public List<Pagamento> listarPorAssinante(String assinanteId) {
        return entries().stream().filter(e -> e.assinanteId.equals(assinanteId)).map(e -> e.pagamento).toList();
    }
    private record Entry(String assinanteId, String assinaturaId, Pagamento pagamento) { }
    private List<Entry> entries() {
        List<Entry> out = new ArrayList<>();
        for (String[] r : CsvFile.read(arquivo)) if (r.length >= 8)
            out.add(new Entry(r[0], r[1], new Pagamento(r[2], new BigDecimal(r[3]), LocalDateTime.parse(r[4]), r[5], r[6], r[7])));
        return out;
    }
    private void write(List<Entry> all) {
        List<String[]> rows = new ArrayList<>();
        for (Entry e : all) {
            Pagamento p = e.pagamento;
            rows.add(new String[]{e.assinanteId, e.assinaturaId, p.getId(), p.getValor().toPlainString(),
                    p.getDataPagamento().toString(), p.getStatus(), p.getCartaoId(), p.getPedidoId()});
        }
        CsvFile.write(arquivo, "assinanteId;assinaturaId;id;valor;dataPagamento;status;cartaoId;pedidoId", rows);
    }
}
