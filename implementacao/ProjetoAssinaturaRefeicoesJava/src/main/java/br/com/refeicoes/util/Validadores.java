package br.com.refeicoes.util;

import java.math.BigDecimal;
import java.time.YearMonth;

public final class Validadores {
    private Validadores() { }

    public static String somenteDigitos(String texto) {
        return texto == null ? "" : texto.replaceAll("\\D", "");
    }

    public static boolean cpfValido(String cpfInformado) {
        String cpf = somenteDigitos(cpfInformado);
        if (cpf.length() != 11 || cpf.matches("(\\d)\\1{10}")) return false;
        int soma = 0;
        for (int i = 0; i < 9; i++) soma += (cpf.charAt(i) - '0') * (10 - i);
        int digito1 = 11 - (soma % 11);
        if (digito1 >= 10) digito1 = 0;
        soma = 0;
        for (int i = 0; i < 10; i++) soma += (cpf.charAt(i) - '0') * (11 - i);
        int digito2 = 11 - (soma % 11);
        if (digito2 >= 10) digito2 = 0;
        return cpf.charAt(9) - '0' == digito1 && cpf.charAt(10) - '0' == digito2;
    }

    public static boolean cartaoLuhnValido(String numero) {
        String digits = somenteDigitos(numero);
        if (digits.length() < 13 || digits.length() > 19) return false;
        int soma = 0;
        boolean dobrar = false;
        for (int i = digits.length() - 1; i >= 0; i--) {
            int n = digits.charAt(i) - '0';
            if (dobrar) {
                n *= 2;
                if (n > 9) n -= 9;
            }
            soma += n;
            dobrar = !dobrar;
        }
        return soma % 10 == 0;
    }

    public static boolean validadeCartaoFutura(String validade) {
        try {
            String[] p = validade.trim().split("[/]");
            if (p.length != 2) return false;
            int mes = Integer.parseInt(p[0]);
            int ano = Integer.parseInt(p[1]);
            if (ano < 100) ano += 2000;
            if (mes < 1 || mes > 12) return false;
            return !YearMonth.of(ano, mes).isBefore(YearMonth.now());
        } catch (RuntimeException e) {
            return false;
        }
    }

    public static boolean valorPositivo(BigDecimal valor) {
        return valor != null && valor.compareTo(BigDecimal.ZERO) > 0;
    }
}
