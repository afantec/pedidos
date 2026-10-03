package br.com.plataforma.pedidos.web;

import br.com.plataforma.pedidos.comum.RegraNegocioException;
import br.com.plataforma.pedidos.produto.ImagemProduto;
import br.com.plataforma.pedidos.produto.Produto;
import br.com.plataforma.pedidos.produto.ProdutoDtos.ProdutoRequest;
import br.com.plataforma.pedidos.produto.ProdutoService;
import jakarta.annotation.PostConstruct;
import org.joinfaces.viewscope.ViewScope;
import org.primefaces.PrimeFaces;
import org.primefaces.model.file.UploadedFile;
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
    private byte[] imagemNova;
    private long imagemVersao;

    public ProdutoView(ProdutoService service) {
        this.service = service;
    }

    @PostConstruct
    void carregar() {
        produtos = service.listarTodos();
    }

    public void novo() {
        edicao = new Produto();
        imagemNova = null;
    }

    /** Edita uma cópia, para não alterar a linha da tabela se o usuário cancelar. */
    public void editar(Produto p) {
        edicao = new Produto();
        edicao.setId(p.getId());
        edicao.setCodigo(p.getCodigo());
        edicao.setDescricao(p.getDescricao());
        edicao.setPreco(p.getPreco());
        edicao.setImagemTipo(p.getImagemTipo());
        edicao.setAtivo(p.isAtivo());
        imagemNova = null;
    }

    public void salvar() {
        ImagemProduto imagem;
        try {
            imagem = ImagemProduto.de(imagemNova);
        } catch (RegraNegocioException e) {
            imagemNova = null;
            Mensagens.erro(e.getMessage());
            return;
        }
        var dados = new ProdutoRequest(edicao.getCodigo(), edicao.getDescricao(), edicao.getPreco(), edicao.isAtivo());
        boolean ok = Mensagens.executar(() -> {
            if (edicao.getId() == null) {
                service.criar(dados, imagem);
            } else {
                service.atualizar(edicao.getId(), dados, imagem);
            }
        });
        if (ok) {
            imagemNova = null;
            imagemVersao = System.currentTimeMillis();
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

    public void setArquivo(UploadedFile arquivo) {
        imagemNova = arquivo == null || arquivo.getSize() <= 0 ? null : arquivo.getContent();
    }

    public UploadedFile getArquivo() {
        return null;
    }

    public long getImagemVersao() {
        return imagemVersao;
    }

    public boolean isTemImagemEdicao() {
        return edicao.getId() != null && edicao.getImagemTipo() != null;
    }

    public String getUrlImagemEdicao() {
        if (!isTemImagemEdicao()) {
            return "";
        }
        return "/api/v1/produtos/" + edicao.getId() + "/imagem?v=" + imagemVersao;
    }

    public List<Produto> getProdutos() { return produtos; }
    public Produto getEdicao() { return edicao; }
}
