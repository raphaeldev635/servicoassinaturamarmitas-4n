package br.com.refeicoes.domain.state;

import br.com.refeicoes.domain.ItemPedido;
import br.com.refeicoes.domain.Pedido;

/** Estado final alcançado após a autorização do pagamento pela operadora. */
public class PedidoAprovado implements EstadoPedido {
    @Override public String getNome() { return "APROVADO"; }
    @Override public void adicionarItem(Pedido pedido, ItemPedido item) {
        throw new IllegalStateException("Pedido aprovado não pode ser alterado.");
    }
    @Override public void confirmar(Pedido pedido) {
        throw new IllegalStateException("O pedido já foi aprovado.");
    }
    @Override public void aprovar(Pedido pedido) {
        throw new IllegalStateException("O pedido já foi aprovado.");
    }
    @Override public void cancelar(Pedido pedido) {
        throw new IllegalStateException("O pedido aprovado não pode ser cancelado nesta etapa.");
    }
    @Override public boolean podeSerPago() { return false; }
}
