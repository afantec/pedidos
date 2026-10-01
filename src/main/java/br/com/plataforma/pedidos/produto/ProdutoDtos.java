package br.com.plataforma.pedidos.produto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public final class ProdutoDtos {

    private ProdutoDtos() {
    }

    public record ProdutoRequest(
            @NotBlank @Size(max = 30) String codigo,
            @NotBlank @Size(max = 200) String descricao,
            @NotNull @DecimalMin("0.00") @Digits(integer = 10, fraction = 2) BigDecimal preco,
            Boolean ativo) {
    }

    public record ProdutoResponse(
            Long id, String codigo, String descricao, BigDecimal preco, boolean ativo, OffsetDateTime criadoEm) {

        static ProdutoResponse de(Produto p) {
            return new ProdutoResponse(p.getId(), p.getCodigo(), p.getDescricao(), p.getPreco(),
                    p.isAtivo(), p.getCriadoEm());
        }
    }
}
