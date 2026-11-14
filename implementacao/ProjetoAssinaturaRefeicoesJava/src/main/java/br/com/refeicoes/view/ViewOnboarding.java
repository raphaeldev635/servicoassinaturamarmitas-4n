package br.com.refeicoes.view;

import br.com.refeicoes.app.AppContext;
import br.com.refeicoes.domain.*;
import br.com.refeicoes.util.Formatadores;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/** Fronteira do onboarding: coleta dados, confirma o celular e apresenta os planos. */
public class ViewOnboarding {
    private final AppContext context;
    private final ConsoleIO io;

    public ViewOnboarding(AppContext context, Scanner scanner) {
        this.context = context;
        this.io = new ConsoleIO(scanner);
    }

    public Optional<FluxoAssinatura> executar() {
        io.titulo("ETAPA 1 DE 11 — IDENTIFICAÇÃO");

        System.out.println("Bem-vindo! Informe seu CPF e celular para começar.");
        System.out.println("CPF válido de exemplo: 529.982.247-25.");
        System.out.println("Atenção: esse CPF pode já estar cadastrado. Se isso ocorrer, informe outro CPF válido ainda não utilizado.");

        String cpf = io.ler("CPF: ");
        String celular = io.ler("Celular com DDD (11 dígitos; DDI 55 opcional): ");

        Assinante assinante;
        try {
            assinante = context.ctlOnboarding.informarDadosAcesso(cpf, celular);
        } catch (IllegalArgumentException e) {
            System.out.println("Não foi possível continuar: " + e.getMessage());
            return Optional.empty();
        }

        if (assinante.estaAtivo()) {
            System.out.println("\nCadastro localizado e celular já confirmado. Estado: ATIVO.");
        } else {
            boolean confirmado = false;
            io.titulo("ETAPA 2 DE 11 — CONFIRMAÇÃO SMS");
            while (!confirmado) {
                CodigoConfirmacao codigo = context.ctlOnboarding.solicitarCodigo(assinante);
                System.out.println("\n[SIMULAÇÃO DE SMS] Código enviado para "
                        + assinante.getNumeroCelular() + ": " + codigo.getCodigo());

                for (int tentativa = 1; tentativa <= CodigoConfirmacao.LIMITE_TENTATIVAS; tentativa++) {
                    String informado = io.ler("Digite o código recebido: ");
                    confirmado = context.ctlOnboarding.informarCodigo(assinante, informado);
                    if (confirmado) break;

                    CodigoConfirmacao atual = context.codigoRepository
                            .buscarMaisRecente(assinante.getId()).orElseThrow();
                    if ("BLOQUEADO".equals(atual.getStatus())) {
                        System.out.println("As três tentativas foram utilizadas. O código foi bloqueado.");
                        break;
                    }
                    if ("EXPIRADO".equals(atual.getStatus())) {
                        System.out.println("O código expirou.");
                        break;
                    }
                    System.out.println("Código inválido. Tentativa " + atual.getTentativas()
                            + " de " + CodigoConfirmacao.LIMITE_TENTATIVAS + ".");
                }

                if (confirmado) break;
                if (!io.confirmar("Deseja solicitar o envio de um novo código por SMS?")) {
                    System.out.println("Validação interrompida. O cadastro permanece aguardando confirmação.");
                    return Optional.empty();
                }
            }
            System.out.println("Celular confirmado com sucesso. Estado do assinante: " + assinante.getStatus() + ".");
        }

        List<Plano> planos = context.ctlOnboarding.exibirPlanos();
        if (planos.isEmpty()) {
            System.out.println("Não há planos disponíveis para contratação no momento.");
            return Optional.empty();
        }

        io.titulo("ETAPA 3 DE 11 — ESCOLHA DO PLANO");
        for (int i = 0; i < planos.size(); i++) {
            Plano plano = planos.get(i);
            System.out.printf("%d) %s — %d refeições — %s — R$ %s%n",
                    i + 1, plano.getNomePlano(), plano.getQtdeRefeicoes(),
                    plano.getPeriodicidadeDeEntrega(), Formatadores.moeda(plano.getValor()).replace("R$ ", ""));
        }
        int escolha = io.lerInteiro("Plano desejado: ", 1, planos.size());
        Plano plano = planos.get(escolha - 1);
        System.out.println("\nPlano escolhido: " + plano.getNomePlano());
        if (!io.confirmar("Confirmar contratação deste plano?")) {
            System.out.println("Contratação cancelada pelo usuário.");
            return Optional.empty();
        }

        Assinatura assinatura = context.ctlOnboarding.selecionarPlano(assinante, plano);
        System.out.println("Assinatura criada. Protocolo: " + assinatura.getNumeroProtocolo());
        System.out.println("Status da assinatura: " + assinatura.getStatus());
        return Optional.of(new FluxoAssinatura(assinante, assinatura, plano));
    }
}
