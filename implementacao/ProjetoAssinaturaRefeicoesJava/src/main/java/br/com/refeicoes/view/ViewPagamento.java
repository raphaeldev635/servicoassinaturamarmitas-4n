package br.com.refeicoes.view;

import br.com.refeicoes.app.AppContext;
import br.com.refeicoes.domain.*;
import br.com.refeicoes.util.Formatadores;
import java.util.List;
import java.util.Scanner;

/** Fronteira para endereço, dados do cartão e retorno da autorização simulada. */
public class ViewPagamento {
    private final AppContext context;
    private final ConsoleIO io;

    public ViewPagamento(AppContext context, Scanner scanner) {
        this.context = context;
        this.io = new ConsoleIO(scanner);
    }

    public Pagamento executar(Assinante assinante, Assinatura assinatura, Plano plano, Pedido pedido) {
        io.titulo("ETAPA 9 DE 11 — ENDEREÇO DE ENTREGA");

        Endereco endereco = null;
        boolean enderecoValido = false;

        for (int tentativa = 1; tentativa <= 5; tentativa++) {
            System.out.println("\nTentativa " + tentativa + " de 5");

            try {
                // A construção também fica dentro do try para que uma validação
                // lançada pelo construtor não interrompa o ciclo de tentativas.
                Endereco candidato = new Endereco(
                        io.ler("Logradouro: "),
                        io.ler("Número: "),
                        io.ler("Complemento (opcional): "),
                        io.ler("Bairro: "),
                        io.ler("Cidade: "),
                        io.ler("CEP (8 dígitos): "));

                context.ctlPagamento.cadastrarEndereco(assinante, assinatura, candidato);
                endereco = candidato;
                enderecoValido = true;
                break;

            } catch (IllegalArgumentException e) {
                System.out.println("\nCEP/endereço inválido: " + e.getMessage());
                if (tentativa < 5) {
                    System.out.println("Tentativas restantes: " + (5 - tentativa) + ".");
                    System.out.println("Por favor, informe novamente o endereço.");
                }
            }
        }

        if (!enderecoValido) {
            System.out.println("\nNúmero máximo de tentativas atingido.");
            System.out.println("O endereço não foi cadastrado.");
            return null;
        }

        System.out.println("Endereço armazenado: " + endereco.enderecoCompleto());

        List<OperadoraCartao> operadoras = context.ctlPagamento.consultarOperadoras();
        io.titulo("ETAPA 10 DE 11 — PAGAMENTO");
        System.out.println("Status da assinatura: " + assinatura.getStatus());
        System.out.println("Plano: " + plano.getNomePlano());
        System.out.println("Periodicidade: " + plano.getPeriodicidadeDeEntrega());
        System.out.println("Quantidade de refeições: " + plano.getQtdeRefeicoes());
        System.out.println("Total a pagar: " + Formatadores.moeda(plano.getValor()));
        System.out.println("A cobrança enviada à operadora corresponde ao valor do plano.");
        System.out.println("\nOperadoras disponíveis:");
        for (int i = 0; i < operadoras.size(); i++) {
            System.out.println((i + 1) + ") " + operadoras.get(i).getNome());
        }

        while (true) {
            io.titulo("DADOS DO CARTÃO");
            String numero = io.ler("Número do cartão (13 a 19 dígitos, com ou sem espaços): ");
            String titular = io.ler("Nome do titular: ");
            String validade = io.ler("Validade (MM/AAAA): ");
            String cvv = io.ler("Código de segurança: ");
            int indiceOperadora = io.lerInteiro("Operadora: ", 1, operadoras.size());
            Cartao cartao = new Cartao(numero, titular, validade, cvv,
                    operadoras.get(indiceOperadora - 1).getNome());

            if (!context.ctlPagamento.validarDadosCartao(cartao)) {
                System.out.println("Dados do cartão inválidos ou vencidos. Nenhuma cobrança foi enviada à operadora.");
                if (io.confirmar("Deseja informar outro cartão?")) continue;
                return null;
            }

            Pagamento pagamento = context.ctlPagamento.confirmarPagamento(assinante, assinatura, pedido, plano, cartao);
            if ("APROVADO".equals(pagamento.getStatus())) {
                System.out.println("\nPagamento autorizado pela operadora " + cartao.getOperadora() + ".");
                System.out.println("Cartão utilizado: " + cartao.numeroMascarado());
                exibirConfirmacao(assinatura, plano, pedido, endereco);
                return pagamento;
            }

            System.out.println("Pagamento não autorizado. Status da assinatura: " + assinatura.getStatus() + ".");
            if (io.confirmar("Deseja tentar novamente com outro cartão?")) {
                continue;
            }

            context.ctlPagamento.encerrarProcesso(assinante, assinatura, pedido);
            System.out.println("Processo encerrado sem ativação da assinatura.");
            return pagamento;
        }
    }

    private void exibirConfirmacao(Assinatura assinatura, Plano plano, Pedido pedido, Endereco endereco) {
        io.titulo("ETAPA 11 DE 11 — ASSINATURA CONFIRMADA");
        if ("ATIVA".equals(assinatura.getStatus())) System.out.println("Assinatura Ativa!");
        System.out.println("Plano contratado: " + plano.getNomePlano());
        System.out.println("Quantidade de refeições: " + plano.getQtdeRefeicoes());
        System.out.println("Periodicidade: " + plano.getPeriodicidadeDeEntrega());
        System.out.println("\nRefeições selecionadas:");
        for (ItemPedido item : pedido.getItens()) {
            System.out.println("- " + item.getItemCardapio().getCategoria() + ": "
                    + item.getQuantidade() + " x " + item.getItemCardapio().getNomeItem());
        }
        System.out.println("\nEndereço de entrega: " + endereco.enderecoCompleto());
        System.out.println("Previsão da primeira entrega: " + assinatura.getPrevisaoPrimeiraEntrega());
        System.out.println("Número do protocolo: " + assinatura.getNumeroProtocolo());
        System.out.println("Status da assinatura: " + assinatura.getStatus());
        System.out.println("Status do pedido: " + pedido.getStatus());
    }
}
