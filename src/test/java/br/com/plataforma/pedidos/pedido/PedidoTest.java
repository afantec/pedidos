package br.com.plataforma.pedidos.pedido;

import br.com.plataforma.pedidos.cliente.Cliente;
import br.com.plataforma.pedidos.comum.RegraNegocioException;
import br.com.plataforma.pedidos.pedido.Pedido.ItemNovo;
import br.com.plataforma.pedidos.produto.Produto;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PedidoTest {

    private final Cliente cliente = cliente(true);
    private final Produto cafe = produto("CAF", "18.90", true);
    private final Produto leite = produto("LEI", "4.99", true);

    @Test
    void calculaTotalComPrecoAtualDoProduto() {
        Pedido pedido = new Pedido(cliente, null);
        pedido.substituirItens(List.of(new ItemNovo(cafe, 2), new ItemNovo(leite, 3)));

        assertThat(pedido.getTotal()).isEqualByComparingTo("52.77");
        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.ABERTO);

        cafe.setPreco(new BigDecimal("99.00"));   // mudar o produto não altera o item já gravado
        assertThat(pedido.getItens().get(0).getPrecoUnitario()).isEqualByComparingTo("18.90");
    }

    @Test
    void naoAceitaPedidoSemItens() {
        Pedido pedido = new Pedido(cliente, null);
        assertThatThrownBy(() -> pedido.substituirItens(List.of()))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void naoAceitaClienteOuProdutoInativo() {
        assertThatThrownBy(() -> new Pedido(cliente(false), null))
                .isInstanceOf(RegraNegocioException.class).hasMessageContaining("inativo");

        Pedido pedido = new Pedido(cliente, null);
        assertThatThrownBy(() -> pedido.substituirItens(List.of(new ItemNovo(produto("X", "1.00", false), 1))))
                .isInstanceOf(RegraNegocioException.class).hasMessageContaining("inativo");
    }

    @Test
    void pedidoConfirmadoNaoPodeSerAlterado() {
        Pedido pedido = new Pedido(cliente, null);
        pedido.substituirItens(List.of(new ItemNovo(cafe, 1)));
        pedido.confirmar();

        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.CONFIRMADO);
        assertThat(pedido.isEditavel()).isFalse();
        assertThatThrownBy(() -> pedido.substituirItens(List.of(new ItemNovo(leite, 1))))
                .isInstanceOf(RegraNegocioException.class);
        assertThatThrownBy(pedido::confirmar).isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void cancelaUmaUnicaVez() {
        Pedido pedido = new Pedido(cliente, null);
        pedido.substituirItens(List.of(new ItemNovo(cafe, 1)));
        pedido.confirmar();
        pedido.cancelar();

        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.CANCELADO);
        assertThatThrownBy(pedido::cancelar).isInstanceOf(RegraNegocioException.class);
    }

    private static Cliente cliente(boolean ativo) {
        Cliente c = new Cliente();
        c.setNome("Cliente teste");
        c.setDocumento("52998224725");
        c.setAtivo(ativo);
        return c;
    }

    private static Produto produto(String codigo, String preco, boolean ativo) {
        Produto p = new Produto();
        p.setCodigo(codigo);
        p.setDescricao("Produto " + codigo);
        p.setPreco(new BigDecimal(preco));
        p.setAtivo(ativo);
        return p;
    }
}
