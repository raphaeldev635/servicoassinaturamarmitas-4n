package br.com.refeicoes.view;

import br.com.refeicoes.app.AppContext;
import br.com.refeicoes.domain.*;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.NoSuchElementException;

/** Fronteira principal: mantém a aplicação em execução até o usuário escolher sair. */
public class ConsoleApp {
    private final AppContext context;
    private final Scanner scanner = new Scanner(System.in);
    private final ConsoleIO io = new ConsoleIO(scanner);

    public ConsoleApp(AppContext context) { this.context = context; }

    public void iniciar() {
        io.titulo("SISTEMA DE ASSINATURA DE REFEIÇÕES");
        boolean executando = true;
        while (executando) {
            exibirMenuPrincipal();

            final int opcao;
            try {
                opcao = io.lerInteiro("Escolha uma opção: ", 0, 5);
            } catch (NoSuchElementException e) {
                // EOF (por exemplo, Ctrl+D) encerra a aplicação sem exigir uma opção.
                System.out.println("\nEntrada encerrada. Obrigado por utilizar o sistema. Até logo!");
                break;
            }

            try {
                switch (opcao) {
                    case 1 -> contratarAssinatura();
                    case 2 -> novoPedido();
                    case 3 -> consultarCadastro();
                    case 4 -> consultarCardapio();
                    case 5 -> retomarPagamento();
                    case 0 -> {
                        executando = false;
                        System.out.println("Obrigado por utilizar o sistema. Até logo!");
                    }
                    default -> System.out.println("Opção não implementada.");
                }
            } catch (RuntimeException e) {
                System.out.println("\nOcorreu um problema: " + e.getMessage());
                System.out.println("A operação foi interrompida sem encerrar o sistema.");
            }
            // Ao terminar qualquer operação, o loop volta diretamente ao menu.
            // Não há pausa nem necessidade de pressionar Enter.
        }
    }

    private void exibirMenuPrincipal() {
        System.out.println("\nMENU PRINCIPAL");
        System.out.println("1) Contratar assinatura e fazer primeiro pedido");
        System.out.println("2) Fazer novo pedido (assinante existente)");
        System.out.println("3) Consultar cadastro e assinatura");
        System.out.println("4) Consultar cardápio geral");
        System.out.println("5) Retomar contratação/pagamento pendente");
        System.out.println("0) Sair");
    }

    private void contratarAssinatura() {
        Optional<FluxoAssinatura> fluxo = new ViewOnboarding(context, scanner).executar();
        if (fluxo.isEmpty()) return;
        FluxoAssinatura f = fluxo.get();
        Pedido pedido = new ViewPedido(context, scanner).executar(f.assinante(), f.assinatura(), f.plano());
        if (pedido == null) {
            System.out.println("Assinatura permanece com status " + f.assinatura().getStatus() + ".");
            return;
        }
        new ViewPagamento(context, scanner).executar(f.assinante(), f.assinatura(), f.plano(), pedido);
    }

    private void novoPedido() {
        io.titulo("NOVO PEDIDO");
        String cpf = io.ler("CPF cadastrado: ").replaceAll("\\D", "");
        Assinante assinante = context.assinanteRepository.buscarPorCpf(cpf).orElse(null);
        if (assinante == null) { System.out.println("Assinante não encontrado."); return; }
        List<Assinatura> assinaturas = context.assinaturaRepository.listarPorAssinante(assinante.getId());
        Assinatura assinatura = assinaturas.stream().filter(a -> "ATIVA".equals(a.getStatus())).findFirst().orElse(null);
        if (assinatura == null) { System.out.println("Não há assinatura ativa para este CPF."); return; }
        Plano plano = context.planoRepository.buscarPorId(assinatura.getPlanoId()).orElseThrow();
        Pedido pedido = new ViewPedido(context, scanner).executar(assinante, assinatura, plano);
        if (pedido != null) new ViewPagamento(context, scanner).executar(assinante, assinatura, plano, pedido);
    }


    private void retomarPagamento() {
        io.titulo("RETOMAR CONTRATAÇÃO/PAGAMENTO PENDENTE");
        String cpf = io.ler("CPF cadastrado: ").replaceAll("\\D", "");
        Assinante assinante = context.assinanteRepository.buscarPorCpf(cpf).orElse(null);
        if (assinante == null) { System.out.println("Assinante não encontrado."); return; }
        List<Assinatura> pendentes = context.assinaturaRepository.listarPorAssinante(assinante.getId()).stream()
                .filter(a -> "PENDENTE".equals(a.getStatus())
                        || "AGUARDANDO_PAGAMENTO".equals(a.getStatus()))
                .toList();
        Assinatura assinatura = pendentes.isEmpty() ? null : pendentes.get(pendentes.size() - 1);
        if (assinatura == null) {
            System.out.println("Não há contratação ou pagamento pendente para retomar.");
            return;
        }
        Pedido pedido = context.pedidoRepository.listarPorAssinatura(assinatura.getId()).stream()
                .filter(p -> "CONFIRMADO".equals(p.getStatus()))
                .max((a, b) -> a.getDataPedido().compareTo(b.getDataPedido())).orElse(null);
        if (pedido == null) { System.out.println("Não há pedido confirmado associado para cobrar."); return; }
        Plano plano = context.planoRepository.buscarPorId(assinatura.getPlanoId()).orElse(null);
        if (plano == null) { System.out.println("O plano associado não foi encontrado no catálogo atual."); return; }
        new ViewPagamento(context, scanner).executar(assinante, assinatura, plano, pedido);
    }

    private void consultarCadastro() {
        io.titulo("CONSULTA DE ASSINANTE");
        String cpf = io.ler("CPF: ").replaceAll("\\D", "");
        Assinante a = context.assinanteRepository.buscarPorCpf(cpf).orElse(null);
        if (a == null) { System.out.println("Nenhum cadastro encontrado."); return; }
        a.atualizarEstado(java.time.LocalDateTime.now());
        context.assinanteRepository.salvar(a);
        System.out.println("CPF: " + a.getCpf());
        System.out.println("Celular: " + a.getNumeroCelular());
        System.out.println("Estado do assinante: " + a.getStatus());
        List<Assinatura> assinaturas = context.assinaturaRepository.listarPorAssinante(a.getId());
        if (assinaturas.isEmpty()) System.out.println("Nenhuma assinatura cadastrada.");
        for (Assinatura s : assinaturas) {
            Plano p = context.planoRepository.buscarPorId(s.getPlanoId()).orElse(null);
            System.out.println("- Protocolo " + s.getNumeroProtocolo() + " | " + (p == null ? "Plano removido" : p.getNomePlano()) + " | " + s.getStatus());
        }
        List<Endereco> enderecos = context.enderecoRepository.listarPorAssinante(a.getId());
        if (!enderecos.isEmpty()) System.out.println("Último endereço: " + enderecos.get(enderecos.size() - 1).enderecoCompleto());
        List<Pagamento> pagamentos = context.pagamentoRepository.listarPorAssinante(a.getId());
        System.out.println("Pagamentos registrados: " + pagamentos.size());
    }

    private void consultarCardapio() {
        io.titulo("CARDÁPIO GERAL");
        for (Cardapio c : context.cardapioRepository.listarTodos()) {
            System.out.println(c.getNomeCardapio());
            for (ItemCardapio item : c.obterItens()) System.out.println(" - " + item);
        }
    }
}
