package br.com.refeicoes.domain;

import java.util.UUID;

public class Cartao {
    private final String id;
    private final String numeroCartao;
    private final String ultimosQuatroDigitos;
    private final String nomeTitular;
    private final String validade;
    private final String codigoSeguranca;
    private final String operadora;

    /** Construtor usado no momento da compra; CVV fica somente em memória. */
    public Cartao(String numeroCartao, String nomeTitular, String validade, String codigoSeguranca, String operadora) {
        this(UUID.randomUUID().toString(), numeroCartao, ultimos(numeroCartao), nomeTitular, validade, codigoSeguranca, operadora);
    }
    public Cartao(String id, String numeroCartao, String ultimosQuatroDigitos, String nomeTitular,
                  String validade, String codigoSeguranca, String operadora) {
        this.id = id; this.numeroCartao = numeroCartao; this.ultimosQuatroDigitos = ultimosQuatroDigitos;
        this.nomeTitular = nomeTitular; this.validade = validade; this.codigoSeguranca = codigoSeguranca;
        this.operadora = operadora;
    }
    private static String ultimos(String numero) {
        String digits = numero == null ? "" : numero.replaceAll("\\D", "");
        return digits.length() <= 4 ? digits : digits.substring(digits.length() - 4);
    }
    public String getId() { return id; }
    public String getNumeroCartao() { return numeroCartao; }
    public String getUltimosQuatroDigitos() { return ultimosQuatroDigitos; }
    public String getNomeTitular() { return nomeTitular; }
    public String getValidade() { return validade; }
    public String getCodigoSeguranca() { return codigoSeguranca; }
    public String getOperadora() { return operadora; }
    public String numeroMascarado() { return "**** **** **** " + ultimosQuatroDigitos; }
}
