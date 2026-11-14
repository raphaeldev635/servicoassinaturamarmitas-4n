package br.com.refeicoes.domain;

import br.com.refeicoes.domain.state.EstadoPedido;
import br.com.refeicoes.domain.state.PedidoCancelado;
import br.com.refeicoes.domain.state.PedidoAprovado;
import br.com.refeicoes.domain.state.PedidoConfirmado;
import br.com.refeicoes.domain.state.PedidoEmMontagem;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/** Pedido com transições de estado e operações controladas pelo padrão State. */
public class Pedido {
    private final String id;
    private final LocalDateTime dataPedido;
    private EstadoPedido estado;
    private final List<ItemPedido> itens = new ArrayList<>();

    public Pedido() { this(UUID.randomUUID().toString(), LocalDateTime.now(), "EM_MONTAGEM"); }

    /** Construtor usado pelo repository para restaurar o estado persistido. */
    public Pedido(String id, LocalDateTime dataPedido, String status) {
        this.id = id;
        this.dataPedido = dataPedido;
        this.estado = criarEstado(status);
    }

    public String getId() { return id; }
    public LocalDateTime getDataPedido() { return dataPedido; }
    public String getStatus() { return estado.getNome(); }
    public List<ItemPedido> getItens() { return Collections.unmodifiableList(itens); }
    public boolean podeSerPago() { return estado.podeSerPago(); }

    public void adicionarItem(ItemPedido item) { estado.adicionarItem(this, item); }
    public void confirmar() { estado.confirmar(this); }
    public void aprovar() { estado.aprovar(this); }
    public void cancelar() { estado.cancelar(this); }

    /** Ponto de alteração da coleção chamado pelo estado EM_MONTAGEM. */
    public void adicionarItemInterno(ItemPedido item) { itens.add(item); }
    /** Ponto de transição chamado pelos objetos de estado. */
    public void mudarEstadoInterno(EstadoPedido novoEstado) { this.estado = novoEstado; }

    private static EstadoPedido criarEstado(String status) {
        if (status == null) return new PedidoEmMontagem();
        return switch (status.toUpperCase()) {
            case "EM_MONTAGEM" -> new PedidoEmMontagem();
            case "CONFIRMADO" -> new PedidoConfirmado();
            case "CANCELADO" -> new PedidoCancelado();
            case "APROVADO" -> new PedidoAprovado();
            default -> throw new IllegalArgumentException("Estado de pedido desconhecido: " + status);
        };
    }

    public int quantidadeTotal() { return itens.stream().mapToInt(ItemPedido::getQuantidade).sum(); }

    public int quantidadePorCategoria(String categoria) {
        return itens.stream()
                .filter(i -> i.getItemCardapio().getCategoria().equalsIgnoreCase(categoria))
                .mapToInt(ItemPedido::getQuantidade)
                .sum();
    }

    /** Resumo operacional do pedido: itens e quantidades, sem sugerir cobrança por componente. */
    public String obterResumo() {
        StringBuilder sb = new StringBuilder("Pedido ").append(id.substring(0, 8)).append(" — ")
                .append(dataPedido.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
                .append("\nItens selecionados (incluídos no plano):\n");
        for (ItemPedido item : itens) {
            sb.append("  [").append(item.getItemCardapio().getCategoria()).append("] ")
                    .append(item.getQuantidade()).append("x ")
                    .append(item.getItemCardapio().getNomeItem()).append('\n');
        }
        return sb.append("Status: ").append(getStatus()).toString();
    }
}
