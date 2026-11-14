package br.com.refeicoes.control;

import br.com.refeicoes.domain.*;
import br.com.refeicoes.repository.*;
import br.com.refeicoes.util.Validadores;
import java.math.BigDecimal;
import java.util.List;

/** Coordena endereço, validação do cartão, autorização e fechamento da assinatura. */
public class CtlPagamento {
    private final EnderecoRepository enderecoRepository;
    private final CartaoRepository cartaoRepository;
    private final OperadoraCartaoRepository operadoraRepository;
    private final PagamentoRepository pagamentoRepository;
    private final AssinaturaRepository assinaturaRepository;
    private final PedidoRepository pedidoRepository;

    public CtlPagamento(EnderecoRepository enderecoRepository,
                        CartaoRepository cartaoRepository,
                        OperadoraCartaoRepository operadoraRepository,
                        PagamentoRepository pagamentoRepository,
                        AssinaturaRepository assinaturaRepository,
                        PedidoRepository pedidoRepository) {
        this.enderecoRepository = enderecoRepository;
        this.cartaoRepository = cartaoRepository;
        this.operadoraRepository = operadoraRepository;
        this.pagamentoRepository = pagamentoRepository;
        this.assinaturaRepository = assinaturaRepository;
        this.pedidoRepository = pedidoRepository;
    }

    public Endereco cadastrarEndereco(Assinante assinante, Assinatura assinatura, Endereco endereco) {
        if (assinante == null || assinatura == null) {
            throw new IllegalArgumentException("Assinante e assinatura são obrigatórios.");
        }
        if (endereco == null || endereco.getLogradouro().isBlank()
                || endereco.getNumero().isBlank() || endereco.getCidade().isBlank()) {
            throw new IllegalArgumentException("Logradouro, número e cidade são obrigatórios.");
        }
        String cep = Validadores.somenteDigitos(endereco.getCep());
        if (cep.length() != 8) throw new IllegalArgumentException("CEP deve conter 8 dígitos.");
        // Conforme a sequência UML, valida-se a assinatura antes de gravar o endereço.
        if ("ENCERRADA".equals(assinatura.getStatus())) {
            throw new IllegalStateException("Uma assinatura encerrada não pode receber um novo endereço para cobrança.");
        }
        enderecoRepository.salvar(assinante.getId(), endereco);
        assinante.adicionarEndereco(endereco);

        // Na primeira contratação, a transição ocorre após o endereço. Em pedidos
        // posteriores, uma assinatura já ATIVA conserva esse estado.
        if (!"ATIVA".equals(assinatura.getStatus())) {
            assinatura.aguardarPagamento();
            assinaturaRepository.salvar(assinante.getId(), assinatura);
        }
        return endereco;
    }

    public boolean validarDadosCartao(Cartao cartao) {
        if (cartao == null) return false;
        String numero = Validadores.somenteDigitos(cartao.getNumeroCartao());
        String cvv = Validadores.somenteDigitos(cartao.getCodigoSeguranca());
        return Validadores.cartaoLuhnValido(numero)
                && (cvv.length() == 3 || cvv.length() == 4)
                && Validadores.validadeCartaoFutura(cartao.getValidade())
                && cartao.getNomeTitular() != null && !cartao.getNomeTitular().isBlank();
    }

    public List<OperadoraCartao> consultarOperadoras() {
        return operadoraRepository.listarTodos();
    }

    public Pagamento confirmarPagamento(Assinante assinante, Assinatura assinatura,
                                        Pedido pedido, Plano plano, Cartao cartao) {
        if (assinante == null || assinatura == null || plano == null) {
            throw new IllegalArgumentException("Assinante, assinatura e plano são obrigatórios.");
        }
        if (!plano.estaDisponivel()) {
            throw new IllegalStateException("O plano não está disponível para cobrança.");
        }
        if (!plano.getId().equals(assinatura.getPlanoId())) {
            throw new IllegalArgumentException("O plano informado não corresponde à assinatura.");
        }
        if (!("AGUARDANDO_PAGAMENTO".equals(assinatura.getStatus())
                || "ATIVA".equals(assinatura.getStatus()))) {
            throw new IllegalStateException("A assinatura precisa estar aguardando pagamento ou ativa para receber a cobrança.");
        }
        if (enderecoRepository.listarPorAssinante(assinante.getId()).isEmpty()) {
            throw new IllegalStateException("Cadastre um endereço de entrega antes de pagar.");
        }
        if (pedido == null || !pedido.podeSerPago()) {
            throw new IllegalStateException("O pedido precisa estar confirmado e aguardando autorização.");
        }
        if (!validarDadosCartao(cartao)) {
            throw new IllegalArgumentException("Dados do cartão inválidos ou vencidos.");
        }

        OperadoraCartao operadora = operadoraRepository.listarTodos().stream()
                .filter(op -> op.getNome().equalsIgnoreCase(cartao.getOperadora()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Operadora não cadastrada."));

        // Os componentes do pedido estão incluídos no plano; somente o valor do plano é cobrado.
        BigDecimal valor = plano.getValor();
        if (!Validadores.valorPositivo(valor)) {
            throw new IllegalArgumentException("O valor da assinatura deve ser positivo.");
        }

        Pagamento pagamento = new Pagamento(valor, cartao.getId(), pedido.getId());
        boolean autorizado = operadora.autorizarPagamento(cartao, valor);
        pagamento.registrar(autorizado);

        // Número completo e CVV nunca são persistidos; apenas dados mascarados do cartão.
        cartaoRepository.salvar(assinante.getId(), cartao);
        assinante.adicionarCartao(cartao);
        pagamentoRepository.salvar(assinante.getId(), assinatura.getId(), pagamento);

        if (autorizado) {
            pedido.aprovar();
            pedidoRepository.salvar(assinante.getId(), assinatura.getId(), pedido);
            assinatura.ativar();
        } else if (!"ATIVA".equals(assinatura.getStatus())) {
            // A recusa mantém a contratação pendente, permitindo outra tentativa.
            assinatura.atualizarStatus("AGUARDANDO_PAGAMENTO");
        }
        assinaturaRepository.salvar(assinante.getId(), assinatura);
        return pagamento;
    }

    /** Encerra a tentativa recusada quando o assinante não deseja informar outro cartão. */
    public void encerrarProcesso(Assinante assinante, Assinatura assinatura, Pedido pedido) {
        if (pedido != null && "CONFIRMADO".equals(pedido.getStatus())) {
            pedido.cancelar();
            pedidoRepository.salvar(assinante.getId(), assinatura.getId(), pedido);
        }
        if (assinatura != null && "AGUARDANDO_PAGAMENTO".equals(assinatura.getStatus())) {
            assinatura.encerrar();
            assinaturaRepository.salvar(assinante.getId(), assinatura);
        }
    }

    public List<Pagamento> listarPagamentos(Assinante assinante) {
        return pagamentoRepository.listarPorAssinante(assinante.getId());
    }
}
