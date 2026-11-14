package br.com.refeicoes.domain;

import java.util.UUID;

public class Endereco {
    private final String id;
    private final String logradouro;
    private final String numero;
    private final String complemento;
    private final String bairro;
    private final String cidade;
    private final String cep;

    public Endereco(String logradouro, String numero, String complemento, String bairro, String cidade, String cep) {
        this(UUID.randomUUID().toString(), logradouro, numero, complemento, bairro, cidade, cep);
    }

    public Endereco(String id, String logradouro, String numero, String complemento,
                    String bairro, String cidade, String cep) {
        this.id = id; this.logradouro = logradouro; this.numero = numero;
        this.complemento = complemento; this.bairro = bairro; this.cidade = cidade; this.cep = cep;
    }
    public String getId() { return id; }
    public String getLogradouro() { return logradouro; }
    public String getNumero() { return numero; }
    public String getComplemento() { return complemento; }
    public String getBairro() { return bairro; }
    public String getCidade() { return cidade; }
    public String getCep() { return cep; }
    public String enderecoCompleto() {
        return logradouro + ", " + numero + (complemento.isBlank() ? "" : " - " + complemento)
                + " - " + bairro + " - " + cidade + " - CEP " + cep;
    }
}
