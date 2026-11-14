package br.com.refeicoes.domain;

import java.time.LocalDate;
import java.util.UUID;

public class Assinatura {
    private final String id;
    private final String numeroProtocolo;
    private final String planoId;
    private String status;
    private final LocalDate previsaoPrimeiraEntrega;

    public Assinatura(String planoId, LocalDate previsaoPrimeiraEntrega) {
        this(UUID.randomUUID().toString(), "AS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                planoId, "PENDENTE", previsaoPrimeiraEntrega);
    }
    public Assinatura(String id, String numeroProtocolo, String planoId, String status,
                      LocalDate previsaoPrimeiraEntrega) {
        this.id = id; this.numeroProtocolo = numeroProtocolo; this.planoId = planoId;
        this.status = status; this.previsaoPrimeiraEntrega = previsaoPrimeiraEntrega;
    }
    public String getId() { return id; }
    public String getNumeroProtocolo() { return numeroProtocolo; }
    public String getPlanoId() { return planoId; }
    public String getStatus() { return status; }
    public LocalDate getPrevisaoPrimeiraEntrega() { return previsaoPrimeiraEntrega; }
    public void atualizarStatus(String novoStatus) { this.status = novoStatus; }
    /** Transição realizada após o cadastro do endereço de entrega. */
    public void aguardarPagamento() { this.status = "AGUARDANDO_PAGAMENTO"; }
    public void ativar() { this.status = "ATIVA"; }
    public void encerrar() { this.status = "ENCERRADA"; }
}
