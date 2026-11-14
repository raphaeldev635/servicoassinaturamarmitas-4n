package br.com.refeicoes.control;

import br.com.refeicoes.domain.Assinante;
import br.com.refeicoes.domain.Assinatura;
import br.com.refeicoes.domain.CodigoConfirmacao;
import br.com.refeicoes.domain.Plano;
import br.com.refeicoes.repository.AssinanteRepository;
import br.com.refeicoes.repository.AssinaturaRepository;
import br.com.refeicoes.repository.CodigoConfirmacaoRepository;
import br.com.refeicoes.repository.PlanoRepository;
import br.com.refeicoes.util.Validadores;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class CtlOnboarding {
    private final AssinanteRepository assinanteRepository;
    private final CodigoConfirmacaoRepository codigoRepository;
    private final PlanoRepository planoRepository;
    private final AssinaturaRepository assinaturaRepository;
    private final Supplier<String> geradorCodigo;

    public CtlOnboarding(AssinanteRepository assinanteRepository, CodigoConfirmacaoRepository codigoRepository,
                         PlanoRepository planoRepository, AssinaturaRepository assinaturaRepository,
                         Supplier<String> geradorCodigo) {
        this.assinanteRepository = assinanteRepository; this.codigoRepository = codigoRepository;
        this.planoRepository = planoRepository; this.assinaturaRepository = assinaturaRepository;
        this.geradorCodigo = geradorCodigo;
    }

    /** Localiza o assinante pelo CPF ou cadastra um novo; CPF é a chave de consulta do diagrama. */
    public Assinante informarDadosAcesso(String cpfInformado, String celularInformado) {
        String cpf = Validadores.somenteDigitos(cpfInformado);
        String celular = Validadores.somenteDigitos(celularInformado);
        if (!Validadores.cpfValido(cpf)) throw new IllegalArgumentException("CPF inválido.");
        // Aceita DDD + celular nacional (11 dígitos) ou o mesmo número com DDI 55.
        if (celular.length() == 13 && celular.startsWith("55")) {
            celular = celular.substring(2);
        }
        if (!celular.matches("\\d{2}9\\d{8}")) {
            throw new IllegalArgumentException(
                    "Informe um celular com DDD e 9 dígitos (11 números ao todo; DDI 55 opcional).");
        }
        Optional<Assinante> existente = assinanteRepository.buscarPorCpf(cpf);
        if (existente.isPresent()) {
            Assinante assinante = existente.get();
            if (!assinante.getNumeroCelular().equals(celular))
                throw new IllegalArgumentException("Esse CPF já está cadastrado com outro celular.");
            String estadoAnterior = assinante.getStatus();
            assinante.atualizarEstado(LocalDateTime.now());
            if (!estadoAnterior.equals(assinante.getStatus())) assinanteRepository.salvar(assinante);
            return assinante;
        }
        Assinante novo = new Assinante(cpf, celular);
        assinanteRepository.salvar(novo);
        return novo;
    }

    public CodigoConfirmacao solicitarCodigo(Assinante assinante) {
        LocalDateTime agora = LocalDateTime.now();
        assinante.atualizarEstado(agora);
        assinante.solicitarCodigo(agora);
        String codigo = geradorCodigo.get();
        if (codigo == null || !codigo.matches("\\d{4}")) throw new IllegalStateException("O gerador deve retornar quatro dígitos.");
        CodigoConfirmacao novo = new CodigoConfirmacao(codigo, agora, agora.plusMinutes(10));
        codigoRepository.salvar(assinante.getId(), novo);
        assinanteRepository.salvar(assinante);
        return novo;
    }

    public boolean informarCodigo(Assinante assinante, String codigoInformado) {
        LocalDateTime agora = LocalDateTime.now();
        String estadoAnterior = assinante.getStatus();
        assinante.atualizarEstado(agora);
        if (!estadoAnterior.equals(assinante.getStatus())) assinanteRepository.salvar(assinante);
        if (!"BLOQUEADO".equals(assinante.getStatus()))
            throw new IllegalStateException("O cadastro não está aguardando confirmação. Solicite um novo código se estiver inativo.");
        CodigoConfirmacao codigo = codigoRepository.buscarMaisRecente(assinante.getId())
                .orElseThrow(() -> new IllegalStateException("Nenhum código foi solicitado."));
        boolean valido = codigo.validarCodigo(codigoInformado, agora);
        if (valido) assinante.confirmarCodigo(agora);
        codigoRepository.salvar(assinante.getId(), codigo);
        assinanteRepository.salvar(assinante);
        return valido;
    }

    public List<Plano> exibirPlanos() {
        return planoRepository.listarTodos().stream().filter(Plano::estaDisponivel).toList();
    }

    public Assinatura selecionarPlano(Assinante assinante, Plano plano) {
        LocalDateTime agora = LocalDateTime.now();
        assinante.atualizarEstado(agora);
        assinanteRepository.salvar(assinante);
        if (!assinante.estaAtivo())
            throw new IllegalStateException("Confirme o código de acesso antes de escolher um plano.");
        if (plano == null || planoRepository.buscarPorId(plano.getId()).filter(Plano::estaDisponivel).isEmpty())
            throw new IllegalArgumentException("Plano não encontrado ou indisponível para contratação.");
        Assinatura assinatura = new Assinatura(plano.getId(), LocalDate.now().plusDays(3));
        assinaturaRepository.salvar(assinante.getId(), assinatura);
        assinante.adicionarAssinatura(assinatura);
        return assinatura;
    }

    public List<Assinatura> listarAssinaturas(Assinante assinante) {
        return assinaturaRepository.listarPorAssinante(assinante.getId());
    }
}
