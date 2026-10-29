package br.com.gymhub.controlador.resposta;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RespostaMensagem(String message) {

}
