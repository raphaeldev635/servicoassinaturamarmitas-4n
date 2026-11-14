package br.com.refeicoes.domain;
import java.util.Set;
public class Sobremesa extends ItemCardapio {
    public Sobremesa(String nome, String descricao, Set<String> caracteristicas) { super(nome, descricao, caracteristicas); }
    public Sobremesa(String id, String nome, String descricao, Set<String> caracteristicas) { super(id, nome, descricao, caracteristicas); }
    @Override public String getCategoria() { return "SOBREMESA"; }
}
