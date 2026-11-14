package br.com.refeicoes.control;

import br.com.refeicoes.domain.*;
import br.com.refeicoes.repository.*;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Coordena preferências, consulta ao cardápio e montagem/validação do pedido. */
public class CtlPedido {
    private static final List<String> CATEGORIAS_DISPONIVEIS =
            List.of("PRATO", "ACOMPANHAMENTO", "SOBREMESA");

    private final PreferenciaAlimentarRepository preferenciaRepository;
    private final CardapioRepository cardapioRepository;
    private final PedidoRepository pedidoRepository;
    private final ItemCardapioRepository itemRepository;
    private final PlanoRepository planoRepository;

    public CtlPedido(PreferenciaAlimentarRepository preferenciaRepository,
                     CardapioRepository cardapioRepository,
                     PedidoRepository pedidoRepository,
                     ItemCardapioRepository itemRepository,
                     PlanoRepository planoRepository) {
        this.preferenciaRepository = preferenciaRepository;
        this.cardapioRepository = cardapioRepository;
        this.pedidoRepository = pedidoRepository;
        this.itemRepository = itemRepository;
        this.planoRepository = planoRepository;
    }

    public List<PreferenciaAlimentar> listarPreferenciasDisponiveis() {
        return preferenciaRepository.listarOpcoesDisponiveis();
    }

    public void registrarPreferencia(Assinante assinante, List<String> tipos) {
        if (assinante == null) throw new IllegalArgumentException("Assinante não informado.");
        List<String> normalizadas = new ArrayList<>();
        for (String tipo : tipos) {
            String normalizado = tipo == null ? "" : tipo.trim().toUpperCase().replace(' ', '_');
            if (normalizado.isBlank()) continue;
            if (preferenciaRepository.buscarOpcao(normalizado).isEmpty()) {
                throw new IllegalArgumentException("Preferência não cadastrada: " + tipo);
            }
            if (!normalizadas.contains(normalizado)) normalizadas.add(normalizado);
        }
        if (normalizadas.isEmpty()) normalizadas.add("TRADICIONAL");
        if (normalizadas.contains("TRADICIONAL") && normalizadas.size() > 1) {
            throw new IllegalArgumentException("A preferência Tradicional não pode ser combinada com restrições alimentares.");
        }
        preferenciaRepository.substituirPreferencias(assinante.getId(), normalizadas);
        List<PreferenciaAlimentar> preferenciasDoAssinante = normalizadas.stream()
                .map(tipo -> preferenciaRepository.buscarOpcao(tipo).orElseThrow())
                .toList();
        assinante.substituirPreferencias(preferenciasDoAssinante);
    }

    public List<PreferenciaAlimentar> consultarPreferencias(Assinante assinante) {
        return preferenciaRepository.listarPorAssinante(assinante.getId());
    }

    public List<ItemCardapio> consultarCardapio(Assinante assinante) {
        Set<String> preferencias = new LinkedHashSet<>();
        for (PreferenciaAlimentar preferencia : consultarPreferencias(assinante)) {
            preferencias.add(preferencia.getTipo());
        }
        List<ItemCardapio> resultado = new ArrayList<>();
        for (Cardapio cardapio : cardapioRepository.listarTodos()) {
            resultado.addAll(cardapio.filtrarItens(preferencias));
        }
        return resultado;
    }

    public List<ItemCardapio> consultarCardapioPorCategoria(Assinante assinante, String categoria) {
        return consultarCardapio(assinante).stream()
                .filter(item -> item.getCategoria().equalsIgnoreCase(categoria))
                .toList();
    }

    public Pedido criarPedido(Assinante assinante, Assinatura assinatura) {
        if (assinante == null || !assinante.estaAtivo()) {
            throw new IllegalStateException("Somente um assinante ativo pode criar pedidos.");
        }
        if (assinatura == null || "ENCERRADA".equals(assinatura.getStatus())) {
            throw new IllegalArgumentException("Não há assinatura válida para criar o pedido.");
        }
        return new Pedido();
    }

    /**
     * Cada categoria representa um componente da refeição e possui seu próprio
     * limite, igual à quantidade de refeições contratadas no plano.
     */
    public void validarLimitePlano(Pedido pedido, ItemCardapio item, int quantidade, Plano plano) {
        if (pedido == null || item == null) throw new IllegalArgumentException("Pedido e item são obrigatórios.");
        if (itemRepository.buscarPorId(item.getId()).isEmpty()) {
            throw new IllegalArgumentException("Item de cardápio inexistente.");
        }
        if (plano == null || planoRepository.buscarPorId(plano.getId()).isEmpty() || !plano.estaDisponivel()) {
            throw new IllegalArgumentException("Plano inexistente ou indisponível.");
        }
        if (quantidade <= 0) throw new IllegalArgumentException("A quantidade deve ser positiva.");
        if (!CATEGORIAS_DISPONIVEIS.contains(item.getCategoria())) {
            throw new IllegalArgumentException("Categoria de item não reconhecida.");
        }
        int atualCategoria = pedido.quantidadePorCategoria(item.getCategoria());
        if (atualCategoria + quantidade > plano.getQtdeRefeicoes()) {
            throw new IllegalArgumentException("O plano " + plano.getNomePlano() + " permite até "
                    + plano.getQtdeRefeicoes() + " unidades na categoria " + item.getCategoria() + "."
                    + " Já foram selecionadas " + atualCategoria + " unidade(s) nessa categoria.");
        }
    }

    public void adicionarItem(Pedido pedido, ItemCardapio item, int quantidade, Plano plano) {
        validarLimitePlano(pedido, item, quantidade, plano);
        pedido.adicionarItem(new ItemPedido(item, quantidade));
    }

    public void confirmarPedido(Assinante assinante, Assinatura assinatura, Pedido pedido, Plano plano) {
        if (assinante == null || assinatura == null || pedido == null || plano == null) {
            throw new IllegalArgumentException("Assinante, assinatura, pedido e plano são obrigatórios.");
        }
        if (!plano.estaDisponivel() || !plano.getId().equals(assinatura.getPlanoId())) {
            throw new IllegalStateException("O plano da assinatura precisa estar disponível e corresponder ao pedido.");
        }
        if (pedido.quantidadeTotal() < 1) {
            throw new IllegalStateException("Selecione pelo menos um item para o pedido.");
        }
        for (String categoria : CATEGORIAS_DISPONIVEIS) {
            int quantidadeCategoria = pedido.quantidadePorCategoria(categoria);
            if (quantidadeCategoria > plano.getQtdeRefeicoes()) {
                throw new IllegalStateException("O pedido ultrapassa o limite de "
                        + plano.getQtdeRefeicoes() + " unidades na categoria " + categoria + ".");
            }
        }
        pedido.confirmar();
        pedidoRepository.salvar(assinante.getId(), assinatura.getId(), pedido);
    }

    public void cancelarPedido(Assinante assinante, Assinatura assinatura, Pedido pedido) {
        pedido.cancelar();
        pedidoRepository.salvar(assinante.getId(), assinatura.getId(), pedido);
    }

    public String obterResumoPedido(Pedido pedido) {
        return pedido.obterResumo();
    }

    public List<Pedido> listarPedidos(Assinante assinante) {
        return pedidoRepository.listarPorAssinante(assinante.getId());
    }

    public Plano obterPlanoDaAssinatura(Assinatura assinatura) {
        return planoRepository.buscarPorId(assinatura.getPlanoId())
                .orElseThrow(() -> new IllegalStateException("Plano da assinatura não encontrado."));
    }

}
