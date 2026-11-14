package br.com.refeicoes.domain.state;

import br.com.refeicoes.domain.ItemPedido;
import br.com.refeicoes.domain.Pedido;

/** Define as operações permitidas em cada etapa do ciclo de vida do pedido. */
public interface EstadoPedido {
    String getNome();
    void adicionarItem(Pedido pedido, ItemPedido item);
    void confirmar(Pedido pedido);
    void aprovar(Pedido pedido);
    void cancelar(Pedido pedido);
    boolean podeSerPago();
}
