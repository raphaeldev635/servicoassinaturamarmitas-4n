package br.com.refeicoes.domain;

import java.math.BigDecimal;

/** Simulação local da operadora. Não realiza transações bancárias reais. */
public class OperadoraCartao {
    private final String nome;
    public OperadoraCartao(String nome) { this.nome = nome; }
    public String getNome() { return nome; }

    public boolean autorizarPagamento(Cartao cartao, BigDecimal valor) {
        if (cartao == null || valor == null || valor.signum() <= 0) return false;
        // Cartão de teste terminado em 0002 simula uma recusa da operadora.
        return !"0002".equals(cartao.getUltimosQuatroDigitos());
    }
}
