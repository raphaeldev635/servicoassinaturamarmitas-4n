package br.com.refeicoes.domain.state;

import br.com.refeicoes.domain.ItemPedido;
import br.com.refeicoes.domain.Pedido;

public class PedidoEmMontagem implements EstadoPedido {
    @Override public String getNome() { return "EM_MONTAGEM"; }
    @Override public void adicionarItem(Pedido pedido, ItemPedido item) { pedido.adicionarItemInterno(item); }

    @Override public void confirmar(Pedido pedido) {
        if (pedido.getItens().isEmpty()) {
            throw new IllegalStateException("O pedido precisa conter itens antes de ser confirmado.");
        }
        pedido.mudarEstadoInterno(new PedidoConfirmado());
    }

    @Override public void aprovar(Pedido pedido) {
        throw new IllegalStateException("O pedido precisa ser confirmado antes da aprovação.");
    }

    @Override public void cancelar(Pedido pedido) {
        pedido.mudarEstadoInterno(new PedidoCancelado());
    }

    @Override public boolean podeSerPago() { return false; }
}
