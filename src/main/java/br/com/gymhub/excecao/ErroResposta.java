package br.com.gymhub.excecao;

import java.time.LocalDateTime;

public record ErroResposta(
        LocalDateTime dataHora,
        int status,
        String mensagem
) {
}
