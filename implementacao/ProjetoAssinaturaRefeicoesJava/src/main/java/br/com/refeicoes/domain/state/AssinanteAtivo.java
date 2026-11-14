package br.com.refeicoes.domain.state;

import br.com.refeicoes.domain.Assinante;
import java.time.LocalDateTime;

/** Estado alcançado quando o código de confirmação é validado. */
public class AssinanteAtivo implements EstadoAssinante {
    @Override public String getNome() { return "ATIVO"; }

    @Override
    public void solicitarCodigo(Assinante assinante, LocalDateTime agora) {
        throw new IllegalStateException("O assinante já está ativo e confirmado.");
    }

    @Override
    public void confirmarCodigo(Assinante assinante, LocalDateTime agora) {
        throw new IllegalStateException("O código já foi confirmado.");
    }

    @Override public void atualizar(Assinante assinante, LocalDateTime agora) { }
}
