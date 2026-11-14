package br.com.refeicoes.domain.state;

import br.com.refeicoes.domain.Assinante;
import java.time.LocalDateTime;

/** Comportamentos disponíveis em cada estado do ciclo de vida do assinante. */
public interface EstadoAssinante {
    String getNome();
    void solicitarCodigo(Assinante assinante, LocalDateTime agora);
    void confirmarCodigo(Assinante assinante, LocalDateTime agora);
    void atualizar(Assinante assinante, LocalDateTime agora);
}
