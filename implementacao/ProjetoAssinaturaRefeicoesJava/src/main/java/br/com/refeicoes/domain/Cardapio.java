package br.com.refeicoes.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class Cardapio {
    private final String id;
    private final String nomeCardapio;
    private final List<ItemCardapio> itens;

    public Cardapio(String nomeCardapio, List<ItemCardapio> itens) {
        this(UUID.randomUUID().toString(), nomeCardapio, itens);
    }
    public Cardapio(String id, String nomeCardapio, List<ItemCardapio> itens) {
        this.id = id; this.nomeCardapio = nomeCardapio; this.itens = new ArrayList<>(itens);
    }
    public String getId() { return id; }
    public String getNomeCardapio() { return nomeCardapio; }
    public List<ItemCardapio> obterItens() { return Collections.unmodifiableList(itens); }

    /** Retira do cardápio itens incompatíveis com as preferências registradas. */
    public List<ItemCardapio> filtrarItens(Set<String> preferencias) {
        List<ItemCardapio> resultado = new ArrayList<>();
        for (ItemCardapio item : itens) {
            Set<String> tags = item.getCaracteristicas();
            boolean compativel = true;
            for (String preferencia : preferencias) {
                String p = preferencia.toUpperCase(Locale.ROOT);
                if (p.equals("VEGETARIANA") && tags.contains("ANIMAL")) compativel = false;
                if (p.equals("SEM_GLUTEN") && tags.contains("GLUTEN")) compativel = false;
                if (p.equals("SEM_LACTOSE") && tags.contains("LACTOSE")) compativel = false;
                if (!compativel) break;
            }
            if (compativel) resultado.add(item);
        }
        return resultado;
    }
}
