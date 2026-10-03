package br.com.plataforma.pedidos.produto;

import br.com.plataforma.pedidos.produto.ProdutoDtos.ProdutoRequest;
import br.com.plataforma.pedidos.produto.ProdutoDtos.ProdutoResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Tag(name = "Produtos")
@RestController
@RequestMapping("/api/v1/produtos")
public class ProdutoController {

    private final ProdutoService service;

    public ProdutoController(ProdutoService service) {
        this.service = service;
    }

    /** Pesquisa por trecho do código ou da descrição. */
    @GetMapping
    public Page<ProdutoResponse> listar(@RequestParam(required = false) String termo,
                                        @PageableDefault(sort = "descricao") Pageable pageable) {
        return service.listar(termo, pageable).map(ProdutoResponse::de);
    }

    @GetMapping("/{id}")
    public ProdutoResponse buscar(@PathVariable Long id) {
        return ProdutoResponse.de(service.buscar(id));
    }

    @GetMapping("/{id}/imagem")
    public ResponseEntity<byte[]> imagem(@PathVariable Long id) {
        Produto produto = service.buscar(id);
        if (produto.getImagem() == null || produto.getImagemTipo() == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(produto.getImagemTipo()))
                .cacheControl(CacheControl.noCache())
                .body(produto.getImagem());
    }

    @PostMapping
    public ResponseEntity<ProdutoResponse> criar(@Valid @RequestBody ProdutoRequest dados) {
        Produto produto = service.criar(dados);
        var uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").build(produto.getId());
        return ResponseEntity.created(uri).body(ProdutoResponse.de(produto));
    }

    @PutMapping("/{id}")
    public ProdutoResponse atualizar(@PathVariable Long id, @Valid @RequestBody ProdutoRequest dados) {
        return ProdutoResponse.de(service.atualizar(id, dados));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
