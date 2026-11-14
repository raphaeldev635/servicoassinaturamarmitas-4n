package br.com.refeicoes.domain;

import java.time.LocalDateTime;
import java.util.UUID;

public class CodigoConfirmacao {
    public static final int LIMITE_TENTATIVAS = 3;
    private final String id;
    private final String codigo;
    private final LocalDateTime dataEnvio;
    private final LocalDateTime dataExpiracao;
    private String status;
    private int tentativas;

    public CodigoConfirmacao(String codigo, LocalDateTime dataEnvio, LocalDateTime dataExpiracao) {
        this(UUID.randomUUID().toString(), codigo, dataEnvio, dataExpiracao, "PENDENTE", 0);
    }
    public CodigoConfirmacao(String id, String codigo, LocalDateTime dataEnvio, LocalDateTime dataExpiracao,
                             String status, int tentativas) {
        this.id = id; this.codigo = codigo; this.dataEnvio = dataEnvio; this.dataExpiracao = dataExpiracao;
        this.status = status; this.tentativas = tentativas;
    }
    public String getId() { return id; }
    public String getCodigo() { return codigo; }
    public LocalDateTime getDataEnvio() { return dataEnvio; }
    public LocalDateTime getDataExpiracao() { return dataExpiracao; }
    public String getStatus() { return status; }
    public int getTentativas() { return tentativas; }

    public boolean validarCodigo(String informado, LocalDateTime agora) {
        if (!"PENDENTE".equals(status)) return false;
        if (agora.isAfter(dataExpiracao)) { status = "EXPIRADO"; return false; }
        if (codigo.equals(informado == null ? "" : informado.trim())) {
            status = "CONFIRMADO";
            return true;
        }
        registrarTentativaInvalida();
        return false;
    }

    public void registrarTentativaInvalida() {
        tentativas++;
        if (tentativas >= LIMITE_TENTATIVAS) status = "BLOQUEADO";
    }
}
