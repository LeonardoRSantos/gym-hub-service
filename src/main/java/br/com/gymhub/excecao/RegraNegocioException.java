package br.com.gymhub.excecao;

public class RegraNegocioException extends RuntimeException{

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
