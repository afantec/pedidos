package br.com.plataforma.pedidos.web;

import br.com.plataforma.pedidos.produto.Produto;
import br.com.plataforma.pedidos.produto.ProdutoDtos.ProdutoRequest;
import br.com.plataforma.pedidos.produto.ProdutoService;
import jakarta.annotation.PostConstruct;
import org.joinfaces.viewscope.ViewScope;
import org.primefaces.PrimeFaces;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;

@Component
@Scope(ViewScope.SCOPE_VIEW)
public class ProdutoView implements Serializable {

    private final ProdutoService service;

    private List<Produto> produtos;
    private Produto edicao = new Produto();

    public ProdutoView(ProdutoService service) {
        this.service = service;
    }

    @PostConstruct
    void carregar() {
        produtos = service.listarTodos();
    }

    public void novo() {
        edicao = new Produto();
    }

    /** Edita uma cópia, para não alterar a linha da tabela se o usuário cancelar. */
    public void editar(Produto p) {
        edicao = new Produto();
        edicao.setId(p.getId());
        edicao.setCodigo(p.getCodigo());
        edicao.setDescricao(p.getDescricao());
        edicao.setPreco(p.getPreco());
        edicao.setAtivo(p.isAtivo());
    }

    public void salvar() {
        var dados = new ProdutoRequest(edicao.getCodigo(), edicao.getDescricao(), edicao.getPreco(), edicao.isAtivo());
        boolean ok = Mensagens.executar(() -> {
            if (edicao.getId() == null) {
                service.criar(dados);
            } else {
                service.atualizar(edicao.getId(), dados);
            }
        });
        if (ok) {
            Mensagens.info("Produto salvo.");
            carregar();
            PrimeFaces.current().executeScript("PF('dlgProduto').hide()");
        }
    }

    public void excluir(Produto p) {
        if (Mensagens.executar(() -> service.excluir(p.getId()))) {
            Mensagens.info("Produto excluído.");
            carregar();
        }
    }

    public List<Produto> getProdutos() { return produtos; }
    public Produto getEdicao() { return edicao; }
}
