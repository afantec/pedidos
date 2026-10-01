package br.com.plataforma.pedidos.pedido;

import br.com.plataforma.pedidos.cliente.ClienteService;
import br.com.plataforma.pedidos.comum.RecursoNaoEncontradoException;
import br.com.plataforma.pedidos.pedido.Pedido.ItemNovo;
import br.com.plataforma.pedidos.pedido.PedidoDtos.ItemRequest;
import br.com.plataforma.pedidos.pedido.PedidoDtos.PedidoRequest;
import br.com.plataforma.pedidos.produto.ProdutoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Validated
@Transactional(readOnly = true)
public class PedidoService {

    private final PedidoRepository pedidos;
    private final ClienteService clientes;
    private final ProdutoService produtos;

    public PedidoService(PedidoRepository pedidos, ClienteService clientes, ProdutoService produtos) {
        this.pedidos = pedidos;
        this.clientes = clientes;
        this.produtos = produtos;
    }

    public Page<Pedido> listar(StatusPedido status, Long clienteId, Pageable pageable) {
        return pedidos.filtrar(status, clienteId, pageable);
    }

    public Pedido buscar(Long id) {
        return pedidos.buscarCompleto(id).orElseThrow(() -> new RecursoNaoEncontradoException("Pedido", id));
    }

    public long contarPorStatus(StatusPedido status) {
        return pedidos.countByStatus(status);
    }

    @Transactional
    public Pedido criar(@Valid PedidoRequest dados) {
        Pedido pedido = new Pedido(clientes.buscar(dados.clienteId()), observacao(dados));
        pedido.substituirItens(itens(dados.itens()));
        return pedidos.save(pedido);
    }

    @Transactional
    public Pedido atualizar(Long id, @Valid PedidoRequest dados) {
        Pedido pedido = buscar(id);
        pedido.alterarCabecalho(clientes.buscar(dados.clienteId()), observacao(dados));
        pedido.substituirItens(itens(dados.itens()));
        return pedidos.saveAndFlush(pedido);
    }

    @Transactional
    public Pedido confirmar(Long id) {
        Pedido pedido = buscar(id);
        pedido.confirmar();
        return pedidos.saveAndFlush(pedido);   // flush para devolver versão e data atualizadas
    }

    @Transactional
    public Pedido cancelar(Long id) {
        Pedido pedido = buscar(id);
        pedido.cancelar();
        return pedidos.saveAndFlush(pedido);
    }

    /** Agrupa linhas repetidas do mesmo produto, preservando a ordem em que apareceram. */
    private List<ItemNovo> itens(List<ItemRequest> linhas) {
        Map<Long, Integer> quantidades = new LinkedHashMap<>();
        linhas.forEach(l -> quantidades.merge(l.produtoId(), l.quantidade(), Integer::sum));
        return quantidades.entrySet().stream()
                .map(e -> new ItemNovo(produtos.buscar(e.getKey()), e.getValue()))
                .toList();
    }

    private static String observacao(PedidoRequest dados) {
        return StringUtils.hasText(dados.observacao()) ? dados.observacao().trim() : null;
    }
}
