package br.com.refeicoes.domain;

import java.util.UUID;

public class PreferenciaAlimentar {
    private final String id;
    private final String tipo;
    public PreferenciaAlimentar(String tipo) { this(UUID.randomUUID().toString(), tipo); }
    public PreferenciaAlimentar(String id, String tipo) { this.id = id; this.tipo = tipo; }
    public String getId() { return id; }
    public String getTipo() { return tipo; }
}
