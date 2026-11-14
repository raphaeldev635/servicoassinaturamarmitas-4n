package br.com.refeicoes.repository;

import br.com.refeicoes.domain.Cardapio;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Mantém os cardápios em memória durante a execução da aplicação. */
public class CardapioRepository {
    private final Map<String, Cardapio> cardapios = new LinkedHashMap<>();

    public void salvar(Cardapio cardapio) {
        cardapios.put(cardapio.getId(), cardapio);
    }

    public List<Cardapio> listarTodos() {
        return new ArrayList<>(cardapios.values());
    }
}
