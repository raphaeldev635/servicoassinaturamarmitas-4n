package br.com.refeicoes.domain;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

public abstract class ItemCardapio {
    private final String id;
    private final String nomeItem;
    private final String descricaoItem;
    private final Set<String> caracteristicas;

    protected ItemCardapio(String nomeItem, String descricaoItem, Set<String> caracteristicas) {
        this(UUID.randomUUID().toString(), nomeItem, descricaoItem, caracteristicas);
    }
    protected ItemCardapio(String id, String nomeItem, String descricaoItem, Set<String> caracteristicas) {
        this.id = id; this.nomeItem = nomeItem; this.descricaoItem = descricaoItem;
        this.caracteristicas = new LinkedHashSet<>(caracteristicas == null ? Set.of() : caracteristicas);
    }
    public String getId() { return id; }
    public String getNomeItem() { return nomeItem; }
    public String getDescricaoItem() { return descricaoItem; }
    public Set<String> getCaracteristicas() { return Collections.unmodifiableSet(caracteristicas); }
    public abstract String getCategoria();
    @Override public String toString() {
        return "[" + getCategoria() + "] " + nomeItem + " — " + descricaoItem;
    }
}
