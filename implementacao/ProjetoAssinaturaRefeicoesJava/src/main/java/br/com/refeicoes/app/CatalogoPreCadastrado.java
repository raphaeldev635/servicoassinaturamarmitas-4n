package br.com.refeicoes.app;

import br.com.refeicoes.domain.*;
import br.com.refeicoes.repository.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

/** Catálogo inicial do sistema: os objetos são criados na inicialização e ficam em memória. */
public final class CatalogoPreCadastrado {
    private CatalogoPreCadastrado() { }

    public static List<PreferenciaAlimentar> criarPreferencias() {
        return List.of(
                new PreferenciaAlimentar("PREF-TRADICIONAL", "TRADICIONAL"),
                new PreferenciaAlimentar("PREF-VEGETARIANA", "VEGETARIANA"),
                new PreferenciaAlimentar("PREF-SEM-LACTOSE", "SEM_LACTOSE")
        );
    }

    public static void carregar(PlanoRepository planos,
                                ItemCardapioRepository itens,
                                CardapioRepository cardapios,
                                OperadoraCartaoRepository operadoras) {
        // Planos e preços reproduzem os dados apresentados no protótipo Figma.
        planos.salvar(new Plano("PLANO-ESSENCIAL", "Essencial", 8, "Semanal", new BigDecimal("199.00")));
        planos.salvar(new Plano("PLANO-FAMILIA", "Família", 16, "Quinzenal", new BigDecimal("359.00")));
        planos.salvar(new Plano("PLANO-MENSAL", "Mensal", 30, "Mensal", new BigDecimal("629.00")));

        // Os componentes do cardápio já estão incluídos no plano contratado.
        // Por isso, pratos, acompanhamentos e sobremesas não possuem preço próprio.
        itens.salvar(new Prato("PRATO-FRANGO", "Frango grelhado + legumes",
                "Tradicional • 420 kcal", Set.of("ANIMAL", "SEM_LACTOSE")));
        itens.salvar(new Prato("PRATO-SALMAO", "Salmão ao forno",
                "Sem lactose • 480 kcal", Set.of("ANIMAL", "SEM_LACTOSE")));
        itens.salvar(new Prato("PRATO-STROGONOFF", "Strogonoff de cogumelos",
                "Vegetariana • 390 kcal", Set.of("VEGETARIANO", "SEM_LACTOSE")));

        itens.salvar(new Acompanhamento("ACOMP-ARROZ", "Arroz integral",
                "Acompanhamento", Set.of("SEM_LACTOSE")));
        itens.salvar(new Acompanhamento("ACOMP-PURE", "Purê de batata",
                "Acompanhamento", Set.of("LACTOSE")));
        itens.salvar(new Acompanhamento("ACOMP-FAROFA", "Farofa crocante",
                "Acompanhamento", Set.of("SEM_LACTOSE")));

        itens.salvar(new Sobremesa("SOBREMESA-MOUSSE", "Mousse de maracujá",
                "Sobremesa", Set.of("LACTOSE")));
        itens.salvar(new Sobremesa("SOBREMESA-FRUTAS", "Salada de frutas",
                "Sobremesa", Set.of("SEM_LACTOSE")));
        itens.salvar(new Sobremesa("SOBREMESA-BROWNIE", "Brownie sem lactose",
                "Sobremesa", Set.of("SEM_LACTOSE")));

        cardapios.salvar(new Cardapio("CARDAPIO-SEMANAL", "Cardápio de Refeições", itens.listarTodos()));

        // Operadoras simuladas para demonstrar a interação de pagamento.
        operadoras.salvar(new OperadoraCartao("Visa"));
        operadoras.salvar(new OperadoraCartao("Mastercard"));
        operadoras.salvar(new OperadoraCartao("Elo"));
    }
}
