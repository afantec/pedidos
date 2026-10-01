package br.com.plataforma.pedidos.comum;

/** Registro inexistente: vira HTTP 404 na API. */
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String recurso, Object id) {
        super(recurso + " " + id + " não encontrado");
    }
}
