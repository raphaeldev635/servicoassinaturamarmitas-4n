package br.com.refeicoes.domain.state;

import br.com.refeicoes.domain.Assinante;
import java.time.LocalDateTime;

/** Estado inicial: o assinante ainda não confirmou o código recebido. */
public class AssinanteBloqueado implements EstadoAssinante {
    @Override public String getNome() { return "BLOQUEADO"; }

    @Override
    public void solicitarCodigo(Assinante assinante, LocalDateTime agora) {
        // Uma nova solicitação reinicia a janela de confirmação.
        assinante.mudarEstadoInterno(this, agora);
    }

    @Override
    public void confirmarCodigo(Assinante assinante, LocalDateTime agora) {
        assinante.mudarEstadoInterno(new AssinanteAtivo(), agora);
    }

    @Override
    public void atualizar(Assinante assinante, LocalDateTime agora) {
        if (!agora.isBefore(assinante.getDataInicioEstado().plus(Assinante.PRAZO_BLOQUEIO))) {
            assinante.mudarEstadoInterno(new AssinanteInativo(), agora);
        }
    }
}
