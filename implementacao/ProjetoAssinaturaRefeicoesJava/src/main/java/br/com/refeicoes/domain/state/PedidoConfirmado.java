package br.com.refeicoes.domain.state;

import br.com.refeicoes.domain.ItemPedido;
import br.com.refeicoes.domain.Pedido;

public class PedidoConfirmado implements EstadoPedido {
    @Override public String getNome() { return "CONFIRMADO"; }
    @Override public void adicionarItem(Pedido pedido, ItemPedido item) {
        throw new IllegalStateException("Pedido confirmado não pode ser alterado.");
    }
    @Override public void confirmar(Pedido pedido) {
        throw new IllegalStateException("O pedido já está confirmado.");
    }
    @Override public void aprovar(Pedido pedido) {
        pedido.mudarEstadoInterno(new PedidoAprovado());
    }
    @Override public void cancelar(Pedido pedido) {
        pedido.mudarEstadoInterno(new PedidoCancelado());
    }
    @Override public boolean podeSerPago() { return true; }
}
