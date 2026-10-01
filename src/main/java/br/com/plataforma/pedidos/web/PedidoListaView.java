package br.com.plataforma.pedidos.web;

import br.com.plataforma.pedidos.cliente.Cliente;
import br.com.plataforma.pedidos.cliente.ClienteService;
import br.com.plataforma.pedidos.pedido.Pedido;
import br.com.plataforma.pedidos.pedido.PedidoService;
import br.com.plataforma.pedidos.pedido.StatusPedido;
import jakarta.annotation.PostConstruct;
import org.joinfaces.viewscope.ViewScope;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Component
@Scope(ViewScope.SCOPE_VIEW)
public class PedidoListaView implements Serializable {

    private final PedidoService service;
    private final ClienteService clienteService;

    private StatusPedido status;
    private Long clienteId;
    private List<Cliente> clientes;
    private final LazyDataModel<Pedido> pedidos = new Paginador();

    public PedidoListaView(PedidoService service, ClienteService clienteService) {
        this.service = service;
        this.clienteService = clienteService;
    }

    @PostConstruct
    void iniciar() {
        clientes = clienteService.listarTodos();
    }

    public void confirmar(Pedido p) {
        if (Mensagens.executar(() -> service.confirmar(p.getId()))) {
            Mensagens.info("Pedido " + p.getId() + " confirmado.");
        }
    }

    public void cancelar(Pedido p) {
        if (Mensagens.executar(() -> service.cancelar(p.getId()))) {
            Mensagens.info("Pedido " + p.getId() + " cancelado.");
        }
    }

    public StatusPedido[] getStatusDisponiveis() { return StatusPedido.values(); }

    public LazyDataModel<Pedido> getPedidos() { return pedidos; }
    public List<Cliente> getClientes() { return clientes; }

    public StatusPedido getStatus() { return status; }
    public void setStatus(StatusPedido status) { this.status = status; }

    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }

    /** Paginação no banco, com os filtros do formulário (os filtros por coluna não são usados). */
    private class Paginador extends LazyDataModel<Pedido> {

        @Override
        public int count(Map<String, FilterMeta> filterBy) {
            return (int) service.listar(status, clienteId, PageRequest.of(0, 1)).getTotalElements();
        }

        @Override
        public List<Pedido> load(int first, int pageSize, Map<String, SortMeta> sortBy, Map<String, FilterMeta> filterBy) {
            var pagina = PageRequest.of(first / pageSize, pageSize, Sort.by(Sort.Direction.DESC, "id"));
            return service.listar(status, clienteId, pagina).getContent();
        }

        @Override
        public String getRowKey(Pedido pedido) {
            return String.valueOf(pedido.getId());
        }
    }
}
