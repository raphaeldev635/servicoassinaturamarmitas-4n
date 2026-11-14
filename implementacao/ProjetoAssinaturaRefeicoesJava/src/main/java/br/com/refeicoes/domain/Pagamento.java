package br.com.refeicoes.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class Pagamento {
    private final String id;
    private final BigDecimal valor;
    private final LocalDateTime dataPagamento;
    private String status;
    private final String cartaoId;
    private final String pedidoId;

    public Pagamento(BigDecimal valor, String cartaoId, String pedidoId) {
        this(UUID.randomUUID().toString(), valor, LocalDateTime.now(), "EM_PROCESSAMENTO", cartaoId, pedidoId);
    }
    public Pagamento(String id, BigDecimal valor, LocalDateTime dataPagamento, String status,
                     String cartaoId, String pedidoId) {
        this.id = id; this.valor = valor; this.dataPagamento = dataPagamento; this.status = status;
        this.cartaoId = cartaoId; this.pedidoId = pedidoId;
    }
    public String getId() { return id; }
    public BigDecimal getValor() { return valor; }
    public LocalDateTime getDataPagamento() { return dataPagamento; }
    public String getStatus() { return status; }
    public String getCartaoId() { return cartaoId; }
    public String getPedidoId() { return pedidoId; }
    public void registrar(boolean autorizado) { status = autorizado ? "APROVADO" : "RECUSADO"; }
}
