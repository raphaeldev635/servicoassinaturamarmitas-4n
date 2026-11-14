package br.com.refeicoes.domain.state;

import br.com.refeicoes.domain.Plano;

/** Estado de disponibilidade comercial de um plano. */
public interface PlanoEstado {
    String getNome();
    boolean podeSerContratado();
    void ativar(Plano plano);
    void desativar(Plano plano);
}
