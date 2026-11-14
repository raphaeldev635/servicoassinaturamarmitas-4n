package br.com.refeicoes.domain.state;

import br.com.refeicoes.domain.Plano;

public class PlanoIndisponivel implements PlanoEstado {
    @Override public String getNome() { return "INDISPONIVEL"; }
    @Override public boolean podeSerContratado() { return false; }
    @Override public void ativar(Plano plano) { plano.mudarEstadoInterno(new PlanoDisponivel()); }
    @Override public void desativar(Plano plano) { }
}
