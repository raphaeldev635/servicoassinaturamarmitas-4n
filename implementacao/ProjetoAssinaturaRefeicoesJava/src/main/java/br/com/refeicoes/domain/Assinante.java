package br.com.refeicoes.domain;

import br.com.refeicoes.domain.state.AssinanteAtivo;
import br.com.refeicoes.domain.state.AssinanteBloqueado;
import br.com.refeicoes.domain.state.AssinanteInativo;
import br.com.refeicoes.domain.state.EstadoAssinante;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/** Entidade com comportamento dependente do estado de confirmação do cadastro. */
public class Assinante {
    /** Prazo máximo aguardando confirmação antes de passar para INATIVO. */
    public static final Duration PRAZO_BLOQUEIO = Duration.ofHours(24);

    private final String id;
    private final String cpf;
    private String numeroCelular;
    private EstadoAssinante estado;
    private LocalDateTime dataInicioEstado;
    private final List<Endereco> enderecos = new ArrayList<>();
    private final List<PreferenciaAlimentar> preferencias = new ArrayList<>();
    private final List<Assinatura> assinaturas = new ArrayList<>();
    private final List<Cartao> cartoes = new ArrayList<>();

    public Assinante(String cpf, String numeroCelular) {
        this(UUID.randomUUID().toString(), cpf, numeroCelular);
    }

    /** Novo cadastro começa bloqueado até a confirmação do código enviado. */
    public Assinante(String id, String cpf, String numeroCelular) {
        this(id, cpf, numeroCelular, "BLOQUEADO", LocalDateTime.now());
    }

    /** Construtor usado pelo repository para restaurar o estado persistido. */
    public Assinante(String id, String cpf, String numeroCelular, String status, LocalDateTime dataInicioEstado) {
        this.id = id;
        this.cpf = cpf;
        this.numeroCelular = numeroCelular;
        this.estado = criarEstado(status);
        this.dataInicioEstado = dataInicioEstado == null ? LocalDateTime.now() : dataInicioEstado;
    }

    public String getId() { return id; }
    public String getCpf() { return cpf; }
    public String getNumeroCelular() { return numeroCelular; }
    public void atualizarNumeroCelular(String numeroCelular) { this.numeroCelular = numeroCelular; }
    public String getStatus() { return estado.getNome(); }
    public LocalDateTime getDataInicioEstado() { return dataInicioEstado; }

    public void solicitarCodigo(LocalDateTime agora) { estado.solicitarCodigo(this, agora); }
    public void confirmarCodigo(LocalDateTime agora) { estado.confirmarCodigo(this, agora); }
    public void atualizarEstado(LocalDateTime agora) { estado.atualizar(this, agora); }
    public boolean estaAtivo() { return "ATIVO".equals(estado.getNome()); }

    /** Ponto de transição chamado exclusivamente pelos objetos de estado. */
    public void mudarEstadoInterno(EstadoAssinante novoEstado, LocalDateTime agora) {
        this.estado = novoEstado;
        this.dataInicioEstado = agora;
    }

    private static EstadoAssinante criarEstado(String status) {
        if (status == null) return new AssinanteBloqueado();
        return switch (status.toUpperCase()) {
            case "ATIVO" -> new AssinanteAtivo();
            case "INATIVO" -> new AssinanteInativo();
            case "BLOQUEADO" -> new AssinanteBloqueado();
            default -> throw new IllegalArgumentException("Estado de assinante desconhecido: " + status);
        };
    }

    public void adicionarEndereco(Endereco endereco) { enderecos.add(endereco); }
    public void adicionarPreferencia(PreferenciaAlimentar preferencia) { preferencias.add(preferencia); }
    /** Atualiza a associação 1..* entre Assinante e suas PreferenciaAlimentar. */
    public void substituirPreferencias(List<PreferenciaAlimentar> novasPreferencias) {
        preferencias.clear();
        if (novasPreferencias != null) preferencias.addAll(novasPreferencias);
    }
    public void adicionarAssinatura(Assinatura assinatura) { assinaturas.add(assinatura); }
    public void adicionarCartao(Cartao cartao) { cartoes.add(cartao); }
    public List<Endereco> getEnderecos() { return Collections.unmodifiableList(enderecos); }
    public List<PreferenciaAlimentar> getPreferencias() { return Collections.unmodifiableList(preferencias); }
    public List<Assinatura> getAssinaturas() { return Collections.unmodifiableList(assinaturas); }
    public List<Cartao> getCartoes() { return Collections.unmodifiableList(cartoes); }
}
