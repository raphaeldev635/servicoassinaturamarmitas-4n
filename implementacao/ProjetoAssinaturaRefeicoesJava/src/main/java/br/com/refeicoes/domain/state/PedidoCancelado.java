package br.com.refeicoes.domain.state;

import br.com.refeicoes.domain.ItemPedido;
import br.com.refeicoes.domain.Pedido;

public class PedidoCancelado implements EstadoPedido {
    @Override public String getNome() { return "CANCELADO"; }
    @Override public void adicionarItem(Pedido pedido, ItemPedido item) {
        throw new IllegalStateException("Pedido cancelado não pode ser alterado.");
    }
    @Override public void confirmar(Pedido pedido) {
        throw new IllegalStateException("Pedido cancelado não pode ser confirmado.");
    }
    @Override public void aprovar(Pedido pedido) {
        throw new IllegalStateException("Pedido cancelado não pode ser aprovado.");
    }
    @Override public void cancelar(Pedido pedido) {
        throw new IllegalStateException("O pedido já está cancelado.");
    }
    @Override public boolean podeSerPago() { return false; }
}
