package br.com.refeicoes.repository;

import br.com.refeicoes.domain.*;
import br.com.refeicoes.util.CsvFile;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PedidoRepository {
    private final Path pedidosFile;
    private final Path itensFile;
    private final ItemCardapioRepository itemRepository;
    public PedidoRepository(Path pasta, ItemCardapioRepository itemRepository) {
        pedidosFile = pasta.resolve("pedidos.csv"); itensFile = pasta.resolve("itens_pedido.csv"); this.itemRepository = itemRepository;
    }
    public void salvar(String assinanteId, String assinaturaId, Pedido pedido) {
        List<Entry> all = entries(); all.removeIf(e -> e.pedido.getId().equals(pedido.getId())); all.add(new Entry(assinanteId, assinaturaId, pedido));
        List<String[]> pRows = new ArrayList<>(); List<String[]> iRows = CsvFile.read(itensFile);
        iRows.removeIf(r -> r.length > 0 && r[0].equals(pedido.getId()));
        for (Entry e : all) {
            Pedido p = e.pedido;
            pRows.add(new String[]{e.assinanteId, e.assinaturaId, p.getId(), p.getDataPedido().toString(), p.getStatus()});
        }
        for (ItemPedido item : pedido.getItens())
            iRows.add(new String[]{pedido.getId(), item.getId(), item.getItemCardapio().getId(), String.valueOf(item.getQuantidade())});
        CsvFile.write(pedidosFile, "assinanteId;assinaturaId;pedidoId;dataPedido;status", pRows);
        CsvFile.write(itensFile, "pedidoId;itemPedidoId;itemCardapioId;quantidade", iRows);
    }
    public List<Pedido> listarPorAssinante(String assinanteId) {
        return entries().stream().filter(e -> e.assinanteId.equals(assinanteId)).map(e -> e.pedido).toList();
    }
    public List<Pedido> listarPorAssinatura(String assinaturaId) {
        return entries().stream().filter(e -> e.assinaturaId.equals(assinaturaId)).map(e -> e.pedido).toList();
    }
    public Optional<Pedido> buscarPorId(String id) { return entries().stream().map(e -> e.pedido).filter(p -> p.getId().equals(id)).findFirst(); }
    private record Entry(String assinanteId, String assinaturaId, Pedido pedido) { }
    private List<Entry> entries() {
        List<Entry> out = new ArrayList<>();
        List<String[]> itemRows = CsvFile.read(itensFile);
        for (String[] r : CsvFile.read(pedidosFile)) if (r.length >= 5) {
            Pedido p = new Pedido(r[2], LocalDateTime.parse(r[3]), "EM_MONTAGEM");
            // A quinta coluna de arquivos antigos (valorUnitario) é ignorada.
            // Itens de cardápio não têm preço: quantidade e referência são suficientes.
            for (String[] ir : itemRows) if (ir.length >= 4 && ir[0].equals(p.getId())) {
                ItemCardapio item = itemRepository.buscarPorId(ir[2]).orElse(null);
                if (item != null) p.adicionarItem(new ItemPedido(ir[1], item, Integer.parseInt(ir[3])));
            }
            if ("CONFIRMADO".equals(r[4])) p.confirmar();
            else if ("APROVADO".equals(r[4])) { p.confirmar(); p.aprovar(); }
            else if ("CANCELADO".equals(r[4])) p.cancelar();
            out.add(new Entry(r[0], r[1], p));
        }
        return out;
    }
}
