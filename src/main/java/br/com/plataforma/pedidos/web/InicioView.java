package br.com.plataforma.pedidos.web;

import br.com.plataforma.pedidos.pedido.PedidoService;
import br.com.plataforma.pedidos.pedido.StatusPedido;
import jakarta.annotation.PostConstruct;
import org.joinfaces.viewscope.ViewScope;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.EnumMap;
import java.util.Map;

@Component
@Scope(ViewScope.SCOPE_VIEW)
public class InicioView implements Serializable {

    private final PedidoService service;
    private final Map<StatusPedido, Long> totais = new EnumMap<>(StatusPedido.class);

    public InicioView(PedidoService service) {
        this.service = service;
    }

    @PostConstruct
    void carregar() {
        for (StatusPedido s : StatusPedido.values()) {
            totais.put(s, service.contarPorStatus(s));
        }
    }

    public long getAbertos() { return totais.get(StatusPedido.ABERTO); }
    public long getConfirmados() { return totais.get(StatusPedido.CONFIRMADO); }
    public long getCancelados() { return totais.get(StatusPedido.CANCELADO); }
}
