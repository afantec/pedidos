package br.com.plataforma.pedidos.pedido;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public final class PedidoDtos {

    private PedidoDtos() {
    }

    public record PedidoRequest(
            @NotNull Long clienteId,
            @Size(max = 500) String observacao,
            @NotEmpty @Valid List<ItemRequest> itens) {
    }

    public record ItemRequest(
            @NotNull Long produtoId,
            @NotNull @Min(1) @Max(100_000) Integer quantidade) {
    }

    public record ClienteResumo(Long id, String nome) {
    }

    public record ItemResponse(
            Long id, Long produtoId, String codigo, String descricao,
            int quantidade, BigDecimal precoUnitario, BigDecimal subtotal) {

        static ItemResponse de(ItemPedido i) {
            return new ItemResponse(i.getId(), i.getProduto().getId(), i.getProduto().getCodigo(),
                    i.getProduto().getDescricao(), i.getQuantidade(), i.getPrecoUnitario(), i.getSubtotal());
        }
    }

    /** Usado na listagem: sem itens. */
    public record PedidoResumoResponse(
            Long id, ClienteResumo cliente, StatusPedido status, BigDecimal total,
            OffsetDateTime criadoEm, OffsetDateTime atualizadoEm) {

        static PedidoResumoResponse de(Pedido p) {
            return new PedidoResumoResponse(p.getId(),
                    new ClienteResumo(p.getCliente().getId(), p.getCliente().getNome()),
                    p.getStatus(), p.getTotal(), p.getCriadoEm(), p.getAtualizadoEm());
        }
    }

    public record PedidoResponse(
            Long id, ClienteResumo cliente, StatusPedido status, BigDecimal total, String observacao,
            List<ItemResponse> itens, OffsetDateTime criadoEm, OffsetDateTime atualizadoEm) {

        static PedidoResponse de(Pedido p) {
            return new PedidoResponse(p.getId(),
                    new ClienteResumo(p.getCliente().getId(), p.getCliente().getNome()),
                    p.getStatus(), p.getTotal(), p.getObservacao(),
                    p.getItens().stream().map(ItemResponse::de).toList(),
                    p.getCriadoEm(), p.getAtualizadoEm());
        }
    }
}
