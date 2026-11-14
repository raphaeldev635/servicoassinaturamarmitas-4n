package br.com.refeicoes.repository;

import br.com.refeicoes.domain.Plano;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Repositório em memória para o catálogo pré-cadastrado de planos. */
public class PlanoRepository {
    private final Map<String, Plano> planos = new LinkedHashMap<>();

    public void salvar(Plano plano) {
        planos.put(plano.getId(), plano);
    }

    public List<Plano> listarTodos() {
        return new ArrayList<>(planos.values());
    }

    public Optional<Plano> buscarPorId(String id) {
        return Optional.ofNullable(planos.get(id));
    }

    public Optional<Plano> buscarPorNome(String nome) {
        return planos.values().stream()
                .filter(p -> p.getNomePlano().equalsIgnoreCase(nome))
                .findFirst();
    }
}
