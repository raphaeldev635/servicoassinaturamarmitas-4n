package br.com.refeicoes.app;

import br.com.refeicoes.control.*;
import br.com.refeicoes.repository.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.function.Supplier;

/** Monta os repositories, carrega o catálogo em memória e conecta os controllers. */
public class AppContext {
    public final Path pastaDados;
    public final AssinanteRepository assinanteRepository;
    public final PlanoRepository planoRepository;
    public final CodigoConfirmacaoRepository codigoRepository;
    public final AssinaturaRepository assinaturaRepository;
    public final PreferenciaAlimentarRepository preferenciaRepository;
    public final ItemCardapioRepository itemRepository;
    public final CardapioRepository cardapioRepository;
    public final PedidoRepository pedidoRepository;
    public final EnderecoRepository enderecoRepository;
    public final CartaoRepository cartaoRepository;
    public final OperadoraCartaoRepository operadoraRepository;
    public final PagamentoRepository pagamentoRepository;
    public final CtlOnboarding ctlOnboarding;
    public final CtlPedido ctlPedido;
    public final CtlPagamento ctlPagamento;

    public AppContext(Path pastaDados) {
        this(pastaDados, AppContext::gerarCodigo);
    }

    public AppContext(Path pastaDados, Supplier<String> geradorCodigo) {
        this.pastaDados = pastaDados;
        try {
            Files.createDirectories(pastaDados);
        } catch (Exception e) {
            throw new IllegalStateException("Não foi possível criar a pasta de dados.", e);
        }

        // Dados informados pelo assinante: repositories com persistência CSV.
        assinanteRepository = new AssinanteRepository(pastaDados);
        codigoRepository = new CodigoConfirmacaoRepository(pastaDados);
        assinaturaRepository = new AssinaturaRepository(pastaDados);
        preferenciaRepository = new PreferenciaAlimentarRepository(
                pastaDados, CatalogoPreCadastrado.criarPreferencias());
        enderecoRepository = new EnderecoRepository(pastaDados);
        cartaoRepository = new CartaoRepository(pastaDados);
        pagamentoRepository = new PagamentoRepository(pastaDados);

        // Catálogo administrativo pré-cadastrado: objetos mantidos apenas em memória.
        planoRepository = new PlanoRepository();
        itemRepository = new ItemCardapioRepository();
        cardapioRepository = new CardapioRepository();
        operadoraRepository = new OperadoraCartaoRepository();
        CatalogoPreCadastrado.carregar(planoRepository, itemRepository, cardapioRepository, operadoraRepository);

        // O PedidoRepository usa o mesmo catálogo em memória para resolver os itens.
        pedidoRepository = new PedidoRepository(pastaDados, itemRepository);

        ctlOnboarding = new CtlOnboarding(assinanteRepository, codigoRepository, planoRepository,
                assinaturaRepository, geradorCodigo);
        ctlPedido = new CtlPedido(preferenciaRepository, cardapioRepository, pedidoRepository,
                itemRepository, planoRepository);
        ctlPagamento = new CtlPagamento(enderecoRepository, cartaoRepository, operadoraRepository,
                pagamentoRepository, assinaturaRepository, pedidoRepository);
    }

    private static String gerarCodigo() {
        SecureRandom random = new SecureRandom();
        return String.format("%04d", random.nextInt(10_000));
    }
}
