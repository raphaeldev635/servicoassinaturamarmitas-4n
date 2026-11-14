package br.com.refeicoes.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

/** Formatação de valores monetários para a apresentação em português do Brasil. */
public final class Formatadores {
    private Formatadores() { }

    public static String moeda(BigDecimal valor) {
        NumberFormat formato = NumberFormat.getNumberInstance(Locale.forLanguageTag("pt-BR"));
        formato.setMinimumFractionDigits(2);
        formato.setMaximumFractionDigits(2);
        return "R$ " + formato.format(valor);
    }
}
