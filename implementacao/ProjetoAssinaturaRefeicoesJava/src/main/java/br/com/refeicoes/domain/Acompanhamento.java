package br.com.refeicoes.domain;
import java.util.Set;
public class Acompanhamento extends ItemCardapio {
    public Acompanhamento(String nome, String descricao, Set<String> caracteristicas) { super(nome, descricao, caracteristicas); }
    public Acompanhamento(String id, String nome, String descricao, Set<String> caracteristicas) { super(id, nome, descricao, caracteristicas); }
    @Override public String getCategoria() { return "ACOMPANHAMENTO"; }
}
