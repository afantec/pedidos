package br.com.plataforma.pedidos.pedido;

import br.com.plataforma.pedidos.pedido.PedidoDtos.PedidoRequest;
import br.com.plataforma.pedidos.pedido.PedidoDtos.PedidoResponse;
import br.com.plataforma.pedidos.pedido.PedidoDtos.PedidoResumoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Tag(name = "Pedidos")
@RestController
@RequestMapping("/api/v1/pedidos")
public class PedidoController {

    private final PedidoService service;

    public PedidoController(PedidoService service) {
        this.service = service;
    }

    @GetMapping
    public Page<PedidoResumoResponse> listar(
            @RequestParam(required = false) StatusPedido status,
            @RequestParam(required = false) Long clienteId,
            @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return service.listar(status, clienteId, pageable).map(PedidoResumoResponse::de);
    }

    @GetMapping("/{id}")
    public PedidoResponse buscar(@PathVariable Long id) {
        return PedidoResponse.de(service.buscar(id));
    }

    @Operation(summary = "Cria um pedido ABERTO; o preço de cada item é o preço atual do produto")
    @PostMapping
    public ResponseEntity<PedidoResponse> criar(@Valid @RequestBody PedidoRequest dados) {
        Pedido pedido = service.criar(dados);
        var uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").build(pedido.getId());
        return ResponseEntity.created(uri).body(PedidoResponse.de(pedido));
    }

    @Operation(summary = "Substitui cliente, observação e itens de um pedido ABERTO")
    @PutMapping("/{id}")
    public PedidoResponse atualizar(@PathVariable Long id, @Valid @RequestBody PedidoRequest dados) {
        return PedidoResponse.de(service.atualizar(id, dados));
    }

    @PostMapping("/{id}/confirmacao")
    public PedidoResponse confirmar(@PathVariable Long id) {
        return PedidoResponse.de(service.confirmar(id));
    }

    @PostMapping("/{id}/cancelamento")
    public PedidoResponse cancelar(@PathVariable Long id) {
        return PedidoResponse.de(service.cancelar(id));
    }
}
