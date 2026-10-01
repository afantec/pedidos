package br.com.plataforma.pedidos.pedido;

/**
 * Ciclo de vida: ABERTO (editável) -> CONFIRMADO -> CANCELADO.
 * Um pedido ABERTO também pode ser cancelado diretamente.
 */
public enum StatusPedido {
    ABERTO("Aberto"),
    CONFIRMADO("Confirmado"),
    CANCELADO("Cancelado");

    private final String descricao;

    StatusPedido(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
