package br.com.refeicoes.repository;

import br.com.refeicoes.domain.ItemCardapio;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Repositório em memória dos pratos, acompanhamentos e sobremesas pré-cadastrados. */
public class ItemCardapioRepository {
    private final Map<String, ItemCardapio> itens = new LinkedHashMap<>();

    public void salvar(ItemCardapio item) {
        itens.put(item.getId(), item);
    }

    public List<ItemCardapio> listarTodos() {
        return new ArrayList<>(itens.values());
    }

    public Optional<ItemCardapio> buscarPorId(String id) {
        return Optional.ofNullable(itens.get(id));
    }
}
