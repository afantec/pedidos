package br.com.plataforma.pedidos.web;

import br.com.plataforma.pedidos.cliente.Cliente;
import br.com.plataforma.pedidos.cliente.ClienteService;
import br.com.plataforma.pedidos.pedido.Pedido;
import br.com.plataforma.pedidos.pedido.PedidoDtos.ItemRequest;
import br.com.plataforma.pedidos.pedido.PedidoDtos.PedidoRequest;
import br.com.plataforma.pedidos.pedido.PedidoService;
import br.com.plataforma.pedidos.pedido.StatusPedido;
import br.com.plataforma.pedidos.produto.Produto;
import br.com.plataforma.pedidos.produto.ProdutoService;
import jakarta.faces.context.FacesContext;
import org.joinfaces.viewscope.ViewScope;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Criação e edição de um pedido (pedido.xhtml?id=...). */
@Component
@Scope(ViewScope.SCOPE_VIEW)
public class PedidoFormView implements Serializable {

    private final PedidoService service;
    private final ClienteService clienteService;
    private final ProdutoService produtoService;

    private Long id;
    private StatusPedido status;
    private Long clienteId;
    private String observacao;
    private final List<Linha> itens = new ArrayList<>();

    private List<Cliente> clientes;
    private List<Produto> produtos;
    private Long produtoId;
    private Integer quantidade = 1;

    public PedidoFormView(PedidoService service, ClienteService clienteService, ProdutoService produtoService) {
        this.service = service;
        this.clienteService = clienteService;
        this.produtoService = produtoService;
    }

    /** f:viewAction: carrega o pedido informado em ?id=, ou prepara um novo. */
    public void carregar() {
        if (FacesContext.getCurrentInstance().isPostback()) {
            return;
        }
        clientes = new ArrayList<>(clienteService.listarAtivos());
        produtos = produtoService.listarAtivos();
        if (id == null) {
            return;
        }
        Mensagens.executar(() -> {
            Pedido p = service.buscar(id);
            status = p.getStatus();
            clienteId = p.getCliente().getId();
            observacao = p.getObservacao();
            p.getItens().forEach(i -> itens.add(new Linha(i.getProduto().getId(), i.getProduto().getCodigo(),
                    i.getProduto().getDescricao(), i.getPrecoUnitario(), i.getQuantidade())));
            if (clientes.stream().noneMatch(c -> c.getId().equals(clienteId))) {
                clientes.add(0, p.getCliente());   // cliente inativado depois do pedido
            }
        });
    }

    public void adicionarItem() {
        if (produtoId == null || quantidade == null || quantidade < 1) {
            Mensagens.erro("Informe o produto e uma quantidade maior que zero.");
            return;
        }
        itens.stream().filter(l -> l.getProdutoId().equals(produtoId)).findFirst().ifPresentOrElse(
                l -> l.setQuantidade(l.getQuantidade() + quantidade),
                () -> produtos.stream().filter(p -> p.getId().equals(produtoId)).findFirst()
                        .ifPresent(p -> itens.add(new Linha(p.getId(), p.getCodigo(), p.getDescricao(),
                                p.getPreco(), quantidade))));
        produtoId = null;
        quantidade = 1;
    }

    public void removerItem(Linha linha) {
        itens.remove(linha);
    }

    public void salvar() throws IOException {
        var dados = new PedidoRequest(clienteId, observacao,
                itens.stream().map(l -> new ItemRequest(l.getProdutoId(), l.getQuantidade())).toList());
        Long[] salvo = new Long[1];
        boolean ok = Mensagens.executar(() -> salvo[0] = (id == null
                ? service.criar(dados)
                : service.atualizar(id, dados)).getId());
        if (ok) {
            Mensagens.infoAposRedirect("Pedido " + salvo[0] + " salvo.");
            FacesContext.getCurrentInstance().getExternalContext().redirect("pedidos.xhtml");
        }
    }

    public BigDecimal getTotal() {
        return itens.stream().map(Linha::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean isEditavel() {
        return status == null || status == StatusPedido.ABERTO;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public StatusPedido getStatus() { return status; }

    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }

    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }

    public List<Linha> getItens() { return itens; }
    public List<Cliente> getClientes() { return clientes; }
    public List<Produto> getProdutos() { return produtos; }

    public Long getProdutoId() { return produtoId; }
    public void setProdutoId(Long produtoId) { this.produtoId = produtoId; }

    public Integer getQuantidade() { return quantidade; }
    public void setQuantidade(Integer quantidade) { this.quantidade = quantidade; }

    /** Linha editável da tabela de itens. O preço exibido é o do pedido ou, para itens novos, o atual. */
    public static class Linha implements Serializable {

        private final Long produtoId;
        private final String codigo;
        private final String descricao;
        private final BigDecimal preco;
        private Integer quantidade;

        Linha(Long produtoId, String codigo, String descricao, BigDecimal preco, Integer quantidade) {
            this.produtoId = produtoId;
            this.codigo = codigo;
            this.descricao = descricao;
            this.preco = preco;
            this.quantidade = quantidade;
        }

        public BigDecimal getSubtotal() {
            return quantidade == null ? BigDecimal.ZERO : preco.multiply(BigDecimal.valueOf(quantidade));
        }

        public Long getProdutoId() { return produtoId; }
        public String getCodigo() { return codigo; }
        public String getDescricao() { return descricao; }
        public BigDecimal getPreco() { return preco; }
        public Integer getQuantidade() { return quantidade; }
        public void setQuantidade(Integer quantidade) { this.quantidade = quantidade; }
    }
}
