package br.com.plataforma.pedidos.comum;

/** Operação válida sintaticamente, mas proibida pelas regras do domínio: vira HTTP 422. */
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
