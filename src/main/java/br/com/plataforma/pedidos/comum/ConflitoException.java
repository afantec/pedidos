package br.com.plataforma.pedidos.comum;

/** Conflito com o estado atual dos dados (duplicidade, registro em uso): vira HTTP 409. */
public class ConflitoException extends RuntimeException {

    public ConflitoException(String mensagem) {
        super(mensagem);
    }
}
