package br.com.plataforma.pedidos.pedido;

import br.com.plataforma.pedidos.cliente.Cliente;
import br.com.plataforma.pedidos.comum.RegraNegocioException;
import br.com.plataforma.pedidos.produto.Produto;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "pedido")
public class Pedido implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusPedido status = StatusPedido.ABERTO;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @Column(length = 500)
    private String observacao;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private List<ItemPedido> itens = new ArrayList<>();

    @Column(name = "criado_em", nullable = false, updatable = false)
    private OffsetDateTime criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private OffsetDateTime atualizadoEm;

    @Version
    private Long versao;

    protected Pedido() {
    }

    public Pedido(Cliente cliente, String observacao) {
        definirCliente(cliente);
        this.observacao = observacao;
    }

    @PrePersist
    void aoCriar() {
        criadoEm = atualizadoEm = OffsetDateTime.now();
    }

    @PreUpdate
    void aoAtualizar() {
        atualizadoEm = OffsetDateTime.now();
    }

    // ---- regras de negócio ----

    void alterarCabecalho(Cliente cliente, String observacao) {
        exigirAberto("alterado");
        definirCliente(cliente);
        this.observacao = observacao;
    }

    /** Substitui todos os itens; quantidades do mesmo produto já chegam somadas. */
    void substituirItens(List<ItemNovo> novos) {
        exigirAberto("alterado");
        if (novos.isEmpty()) {
            throw new RegraNegocioException("O pedido precisa ter ao menos um item.");
        }
        itens.clear();
        for (ItemNovo novo : novos) {
            if (!novo.produto().isAtivo()) {
                throw new RegraNegocioException("Produto " + novo.produto().getCodigo() + " está inativo.");
            }
            itens.add(new ItemPedido(this, novo.produto(), novo.quantidade()));
        }
        total = itens.stream().map(ItemPedido::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    void confirmar() {
        exigirAberto("confirmado");
        if (itens.isEmpty()) {
            throw new RegraNegocioException("Pedido sem itens não pode ser confirmado.");
        }
        status = StatusPedido.CONFIRMADO;
    }

    void cancelar() {
        if (status == StatusPedido.CANCELADO) {
            throw new RegraNegocioException("Pedido " + id + " já está cancelado.");
        }
        status = StatusPedido.CANCELADO;
    }

    private void definirCliente(Cliente cliente) {
        if (!cliente.isAtivo()) {
            throw new RegraNegocioException("Cliente " + cliente.getNome() + " está inativo.");
        }
        this.cliente = cliente;
    }

    private void exigirAberto(String acao) {
        if (status != StatusPedido.ABERTO) {
            throw new RegraNegocioException(
                    "Pedido " + id + " está " + status.getDescricao().toLowerCase() + " e não pode ser " + acao + ".");
        }
    }

    record ItemNovo(Produto produto, int quantidade) {
    }

    // ---- leitura ----

    public boolean isEditavel() {
        return status == StatusPedido.ABERTO;
    }

    public Long getId() { return id; }
    public Cliente getCliente() { return cliente; }
    public StatusPedido getStatus() { return status; }
    public BigDecimal getTotal() { return total; }
    public String getObservacao() { return observacao; }
    public List<ItemPedido> getItens() { return Collections.unmodifiableList(itens); }
    public OffsetDateTime getCriadoEm() { return criadoEm; }
    public OffsetDateTime getAtualizadoEm() { return atualizadoEm; }
    public Long getVersao() { return versao; }
}
