package br.com.refeicoes.view;

import br.com.refeicoes.domain.Assinante;
import br.com.refeicoes.domain.Assinatura;
import br.com.refeicoes.domain.Plano;

public record FluxoAssinatura(Assinante assinante, Assinatura assinatura, Plano plano) { }
