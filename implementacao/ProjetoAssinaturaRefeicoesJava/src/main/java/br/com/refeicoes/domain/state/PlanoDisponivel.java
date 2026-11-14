package br.com.refeicoes.domain.state;

import br.com.refeicoes.domain.Plano;

public class PlanoDisponivel implements PlanoEstado {
    @Override public String getNome() { return "DISPONIVEL"; }
    @Override public boolean podeSerContratado() { return true; }
    @Override public void ativar(Plano plano) { }
    @Override public void desativar(Plano plano) { plano.mudarEstadoInterno(new PlanoIndisponivel()); }
}
