package br.com.refeicoes.domain;

import br.com.refeicoes.domain.state.PlanoDisponivel;
import br.com.refeicoes.domain.state.PlanoEstado;
import br.com.refeicoes.domain.state.PlanoIndisponivel;
import java.math.BigDecimal;
import java.util.UUID;

/** Plano cuja contratação depende do seu estado de disponibilidade. */
public class Plano {
    private final String id;
    private final String nomePlano;
    private final int qtdeRefeicoes;
    private final String periodicidadeDeEntrega;
    private final BigDecimal valor;
    private PlanoEstado estado;

    public Plano(String nomePlano, int qtdeRefeicoes, String periodicidadeDeEntrega, BigDecimal valor) {
        this(UUID.randomUUID().toString(), nomePlano, qtdeRefeicoes, periodicidadeDeEntrega, valor, "DISPONIVEL");
    }

    public Plano(String id, String nomePlano, int qtdeRefeicoes, String periodicidadeDeEntrega, BigDecimal valor) {
        this(id, nomePlano, qtdeRefeicoes, periodicidadeDeEntrega, valor, "DISPONIVEL");
    }

    /** Construtor usado pelo repository para restaurar o estado persistido. */
    public Plano(String id, String nomePlano, int qtdeRefeicoes, String periodicidadeDeEntrega,
                 BigDecimal valor, String status) {
        this.id = id;
        this.nomePlano = nomePlano;
        this.qtdeRefeicoes = qtdeRefeicoes;
        this.periodicidadeDeEntrega = periodicidadeDeEntrega;
        this.valor = valor;
        this.estado = criarEstado(status);
    }

    public String getId() { return id; }
    public String getNomePlano() { return nomePlano; }
    public int getQtdeRefeicoes() { return qtdeRefeicoes; }
    public String getPeriodicidadeDeEntrega() { return periodicidadeDeEntrega; }
    public BigDecimal getValor() { return valor; }
    public String getStatus() { return estado.getNome(); }
    public boolean estaDisponivel() { return estado.podeSerContratado(); }
    public void ativar() { estado.ativar(this); }
    public void desativar() { estado.desativar(this); }

    /** Ponto de transição chamado pelos objetos de estado. */
    public void mudarEstadoInterno(PlanoEstado novoEstado) { this.estado = novoEstado; }

    private static PlanoEstado criarEstado(String status) {
        if (status == null) return new PlanoDisponivel();
        return switch (status.toUpperCase()) {
            case "DISPONIVEL" -> new PlanoDisponivel();
            case "INDISPONIVEL" -> new PlanoIndisponivel();
            default -> throw new IllegalArgumentException("Estado de plano desconhecido: " + status);
        };
    }

    @Override public String toString() {
        return nomePlano + " | " + qtdeRefeicoes + " refeições | " + periodicidadeDeEntrega
                + " | R$ " + valor + " | " + getStatus();
    }
}
