package br.com.refeicoes.domain.state;

import br.com.refeicoes.domain.Assinante;
import java.time.LocalDateTime;

/** Estado atingido quando o prazo de confirmação expira sem código válido. */
public class AssinanteInativo implements EstadoAssinante {
    @Override public String getNome() { return "INATIVO"; }

    @Override
    public void solicitarCodigo(Assinante assinante, LocalDateTime agora) {
        // Permite iniciar novamente o processo de verificação.
        assinante.mudarEstadoInterno(new AssinanteBloqueado(), agora);
    }

    @Override
    public void confirmarCodigo(Assinante assinante, LocalDateTime agora) {
        throw new IllegalStateException("Solicite um novo código antes de confirmar o cadastro inativo.");
    }

    @Override public void atualizar(Assinante assinante, LocalDateTime agora) { }
}
