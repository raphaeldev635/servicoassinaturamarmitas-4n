package br.com.refeicoes.view;

import br.com.refeicoes.app.AppContext;
import br.com.refeicoes.domain.*;
import br.com.refeicoes.util.Formatadores;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/** Fronteira CLI para preferências e composição do pedido. */
public class ViewPedido {
    private final AppContext context;
    private final ConsoleIO io;

    public ViewPedido(AppContext context, Scanner scanner) {
        this.context = context;
        this.io = new ConsoleIO(scanner);
    }

    public Pedido executar(Assinante assinante, Assinatura assinatura, Plano plano) {
        io.titulo("ETAPA 4 DE 11 — PREFERÊNCIAS ALIMENTARES");
        List<PreferenciaAlimentar> opcoes = context.ctlPedido.listarPreferenciasDisponiveis();
        for (int i = 0; i < opcoes.size(); i++) {
            System.out.println((i + 1) + ") " + rotulo(opcoes.get(i).getTipo()));
        }
        System.out.println("Escolha uma ou mais opções separadas por vírgula. Tradicional deve ser usada sozinha.");

        List<String> preferencias;
        while (true) {
            String entrada = io.ler("Preferências (ex.: 2 ou 2,3): ");
            try {
                preferencias = converterPreferencias(entrada, opcoes);
                context.ctlPedido.registrarPreferencia(assinante, preferencias);
                break;
            } catch (IllegalArgumentException e) {
                System.out.println("Preferências inválidas: " + e.getMessage());
            }
        }
        System.out.println("Preferências registradas: "
                + String.join(", ", preferencias.stream().map(this::rotulo).toList()));

        Pedido pedido = context.ctlPedido.criarPedido(assinante, assinatura);
        int limitePlano = plano.getQtdeRefeicoes();
        if (!selecionarItens(assinante, pedido, plano, limitePlano)) {
            System.out.println("Montagem do pedido cancelada. Nenhum pedido foi confirmado.");
            return null;
        }

        io.titulo("ETAPA 8 DE 11 — RESUMO DO PEDIDO");
        System.out.println(context.ctlPedido.obterResumoPedido(pedido));
        System.out.println("\nVALOR DA CONTRATAÇÃO");
        System.out.println("Plano contratado: " + plano.getNomePlano() + " — "
                + plano.getQtdeRefeicoes() + " refeições / " + plano.getPeriodicidadeDeEntrega());
        System.out.println("Total: " + Formatadores.moeda(plano.getValor()));
        System.out.println("Pratos, acompanhamentos e sobremesas já estão incluídos nesse valor.");
        if (!io.confirmar("Confirmar a composição do pedido?")) {
            System.out.println("Pedido não confirmado.");
            return null;
        }

        context.ctlPedido.confirmarPedido(assinante, assinatura, pedido, plano);
        System.out.println("Pedido confirmado. Status: " + pedido.getStatus());
        return pedido;
    }

    /**
     * Laço de seleção com limite independente por categoria.
     * Cada refeição do plano pode conter um prato, um acompanhamento e uma sobremesa;
     * por isso, cada categoria admite até N unidades, sem consumir o limite das outras.
     */
    private boolean selecionarItens(Assinante assinante, Pedido pedido, Plano plano, int limite) {
        System.out.println("Limite do plano: até " + limite + " unidades por categoria.");
        while (temCategoriaComSaldo(pedido, limite)) {
            io.titulo("SELEÇÃO DE ITENS DO PEDIDO");
            exibirContadoresCategorias(pedido, limite);
            System.out.println("Escolha uma categoria para consultar o cardápio:");
            System.out.println("1) Pratos principais");
            System.out.println("2) Acompanhamentos");
            System.out.println("3) Sobremesas");
            System.out.println("0) Concluir seleção");
            int categoriaOpcao = io.lerInteiro("Categoria: ", 0, 3);
            if (categoriaOpcao == 0) break;

            String categoria = switch (categoriaOpcao) {
                case 1 -> "PRATO";
                case 2 -> "ACOMPANHAMENTO";
                case 3 -> "SOBREMESA";
                default -> throw new IllegalStateException("Categoria inválida.");
            };
            String titulo = switch (categoriaOpcao) {
                case 1 -> "ETAPA 5 DE 11 — PRATOS PRINCIPAIS";
                case 2 -> "ETAPA 6 DE 11 — ACOMPANHAMENTOS";
                case 3 -> "ETAPA 7 DE 11 — SOBREMESAS";
                default -> "CARDÁPIO";
            };

            int quantidadeAtualCategoria = pedido.quantidadePorCategoria(categoria);
            if (quantidadeAtualCategoria >= limite) {
                System.out.println("O limite de " + limite + " unidades para "
                        + rotuloCategoria(categoria) + " já foi atingido.");
                continue;
            }

            List<ItemCardapio> itens = context.ctlPedido.consultarCardapioPorCategoria(assinante, categoria);
            io.titulo(titulo);
            if (itens.isEmpty()) {
                System.out.println("Não há itens compatíveis com as preferências selecionadas.");
                continue;
            }

            for (int i = 0; i < itens.size(); i++) {
                ItemCardapio item = itens.get(i);
                System.out.printf("%d) %s — %s%n", i + 1, item.getNomeItem(), item.getDescricaoItem());
            }
            System.out.println("Digite 0 para voltar à seleção de categorias.");
            int opcao = io.lerInteiro("Item: ", 0, itens.size());
            if (opcao == 0) continue;

            int quantidadeDisponivel = limite - quantidadeAtualCategoria;
            System.out.println("Disponível nesta categoria: " + quantidadeDisponivel + " unidade(s).");
            int quantidade = io.lerInteiroPositivo("Quantidade desejada: ");
            try {
                context.ctlPedido.adicionarItem(pedido, itens.get(opcao - 1), quantidade, plano);
                System.out.println("Item adicionado.");
                exibirContadoresCategorias(pedido, limite);
            } catch (IllegalArgumentException | IllegalStateException e) {
                System.out.println("Não foi possível adicionar: " + e.getMessage());
            }
        }

        if (pedido.quantidadeTotal() == 0) {
            System.out.println("Selecione pelo menos um item antes de concluir.");
            return false;
        }
        if (!temCategoriaComSaldo(pedido, limite)) {
            System.out.println("Limite atingido nas três categorias.");
        }
        System.out.println("Seleção concluída.");
        exibirContadoresCategorias(pedido, limite);
        return true;
    }

    private boolean temCategoriaComSaldo(Pedido pedido, int limite) {
        return pedido.quantidadePorCategoria("PRATO") < limite
                || pedido.quantidadePorCategoria("ACOMPANHAMENTO") < limite
                || pedido.quantidadePorCategoria("SOBREMESA") < limite;
    }

    private void exibirContadoresCategorias(Pedido pedido, int limite) {
        System.out.println("Pratos: " + pedido.quantidadePorCategoria("PRATO") + "/" + limite
                + " | Acompanhamentos: " + pedido.quantidadePorCategoria("ACOMPANHAMENTO") + "/" + limite
                + " | Sobremesas: " + pedido.quantidadePorCategoria("SOBREMESA") + "/" + limite);
    }

    private String rotuloCategoria(String categoria) {
        return switch (categoria) {
            case "PRATO" -> "pratos";
            case "ACOMPANHAMENTO" -> "acompanhamentos";
            case "SOBREMESA" -> "sobremesas";
            default -> categoria;
        };
    }

    private List<String> converterPreferencias(String entrada, List<PreferenciaAlimentar> opcoes) {
        List<String> selecionadas = new ArrayList<>();
        if (entrada == null || entrada.isBlank()) return List.of("TRADICIONAL");
        for (String parte : entrada.split(",")) {
            int indice;
            try {
                indice = Integer.parseInt(parte.trim());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("informe os números das opções separados por vírgula.");
            }
            if (indice < 1 || indice > opcoes.size()) {
                throw new IllegalArgumentException("opção fora da lista.");
            }
            String tipo = opcoes.get(indice - 1).getTipo();
            if (!selecionadas.contains(tipo)) selecionadas.add(tipo);
        }
        if (selecionadas.isEmpty()) selecionadas.add("TRADICIONAL");
        return selecionadas;
    }

    private String rotulo(String tipo) {
        return switch (tipo) {
            case "TRADICIONAL" -> "Tradicional";
            case "VEGETARIANA" -> "Vegetariana";
            case "SEM_LACTOSE" -> "Sem lactose";
            default -> tipo;
        };
    }
}
