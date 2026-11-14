package br.com.refeicoes.domain;

import java.util.UUID;

public class ItemPedido {
    private final String id;
    private final ItemCardapio itemCardapio;
    private int quantidade;

    public ItemPedido(ItemCardapio itemCardapio, int quantidade) {
        this(UUID.randomUUID().toString(), itemCardapio, quantidade);
    }
    public ItemPedido(String id, ItemCardapio itemCardapio, int quantidade) {
        if (quantidade <= 0) throw new IllegalArgumentException("A quantidade deve ser maior que zero.");
        this.id = id; this.itemCardapio = itemCardapio; this.quantidade = quantidade;
    }
    public String getId() { return id; }
    public ItemCardapio getItemCardapio() { return itemCardapio; }
    public int getQuantidade() { return quantidade; }
    public void alterarQuantidade(int quantidade) {
        if (quantidade <= 0) throw new IllegalArgumentException("A quantidade deve ser maior que zero.");
        this.quantidade = quantidade;
    }
}
