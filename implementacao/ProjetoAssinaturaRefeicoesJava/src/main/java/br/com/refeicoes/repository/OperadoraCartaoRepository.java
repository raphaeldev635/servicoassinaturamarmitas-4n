package br.com.refeicoes.repository;

import br.com.refeicoes.domain.OperadoraCartao;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Catálogo em memória das operadoras aceitas pelo sistema de demonstração. */
public class OperadoraCartaoRepository {
    private final Map<String, OperadoraCartao> operadoras = new LinkedHashMap<>();

    public void salvar(OperadoraCartao operadora) {
        operadoras.put(operadora.getNome().toUpperCase(), operadora);
    }

    public List<OperadoraCartao> listarTodos() {
        return new ArrayList<>(operadoras.values());
    }
}
