package br.com.refeicoes.domain;
import java.util.Set;
public class Prato extends ItemCardapio {
    public Prato(String nome, String descricao, Set<String> caracteristicas) { super(nome, descricao, caracteristicas); }
    public Prato(String id, String nome, String descricao, Set<String> caracteristicas) { super(id, nome, descricao, caracteristicas); }
    @Override public String getCategoria() { return "PRATO"; }
}
