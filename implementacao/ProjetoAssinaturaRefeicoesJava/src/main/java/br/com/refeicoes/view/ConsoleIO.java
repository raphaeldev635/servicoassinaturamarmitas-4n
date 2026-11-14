package br.com.refeicoes.view;

import java.math.BigDecimal;
import java.util.Scanner;

public class ConsoleIO {
    private final Scanner scanner;
    public ConsoleIO(Scanner scanner) { this.scanner = scanner; }
    public String ler(String mensagem) {
        System.out.print(mensagem);
        return scanner.nextLine().trim();
    }
    public int lerInteiro(String mensagem, int minimo, int maximo) {
        while (true) {
            String entrada = ler(mensagem);
            try {
                int valor = Integer.parseInt(entrada);
                if (valor >= minimo && valor <= maximo) return valor;
            } catch (NumberFormatException ignored) { }
            System.out.println("Opção inválida. Digite um número entre " + minimo + " e " + maximo + ".");
        }
    }
    public int lerInteiroPositivo(String mensagem) {
        while (true) {
            String entrada = ler(mensagem);
            try {
                int valor = Integer.parseInt(entrada);
                if (valor > 0) return valor;
            } catch (NumberFormatException ignored) { }
            System.out.println("Informe um número inteiro maior que zero.");
        }
    }

    public BigDecimal lerDecimal(String mensagem) {
        while (true) {
            String entrada = ler(mensagem).replace(',', '.');
            try { return new BigDecimal(entrada); }
            catch (NumberFormatException e) { System.out.println("Valor inválido. Tente novamente."); }
        }
    }
    public boolean confirmar(String mensagem) {
        String resposta = ler(mensagem + " (s/n): ");
        return resposta.equalsIgnoreCase("s") || resposta.equalsIgnoreCase("sim");
    }
    public void titulo(String texto) {
        System.out.println("\n============================================================");
        System.out.println("  " + texto);
        System.out.println("============================================================");
    }
}
