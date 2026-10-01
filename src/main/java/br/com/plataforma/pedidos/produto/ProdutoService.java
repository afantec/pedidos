package br.com.plataforma.pedidos.produto;

import br.com.plataforma.pedidos.comum.ConflitoException;
import br.com.plataforma.pedidos.comum.RecursoNaoEncontradoException;
import br.com.plataforma.pedidos.pedido.PedidoRepository;
import br.com.plataforma.pedidos.produto.ProdutoDtos.ProdutoRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Service
@Validated
@Transactional(readOnly = true)
public class ProdutoService {

    private final ProdutoRepository produtos;
    private final PedidoRepository pedidos;

    public ProdutoService(ProdutoRepository produtos, PedidoRepository pedidos) {
        this.produtos = produtos;
        this.pedidos = pedidos;
    }

    public Page<Produto> listar(String termo, Pageable pageable) {
        return StringUtils.hasText(termo)
                ? produtos.pesquisar(termo.trim(), pageable)
                : produtos.findAll(pageable);
    }

    public List<Produto> listarTodos() {
        return produtos.findAll(Sort.by("descricao"));
    }

    public List<Produto> listarAtivos() {
        return produtos.findByAtivoTrue(Sort.by("descricao"));
    }

    public Produto buscar(Long id) {
        return produtos.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Produto", id));
    }

    @Transactional
    public Produto criar(@Valid ProdutoRequest dados) {
        if (produtos.existsByCodigoIgnoreCase(dados.codigo().trim())) {
            throw new ConflitoException("Já existe produto com o código " + dados.codigo());
        }
        Produto produto = new Produto();
        preencher(produto, dados);
        return produtos.save(produto);
    }

    /** O novo preço vale só para pedidos futuros: itens já gravados guardam o preço da época. */
    @Transactional
    public Produto atualizar(Long id, @Valid ProdutoRequest dados) {
        Produto produto = buscar(id);
        if (produtos.existsByCodigoIgnoreCaseAndIdNot(dados.codigo().trim(), id)) {
            throw new ConflitoException("Já existe produto com o código " + dados.codigo());
        }
        preencher(produto, dados);
        return produto;
    }

    @Transactional
    public void excluir(Long id) {
        Produto produto = buscar(id);
        if (pedidos.existeItemComProduto(id)) {
            throw new ConflitoException("Produto usado em pedidos não pode ser excluído; inative-o.");
        }
        produtos.delete(produto);
    }

    private static void preencher(Produto produto, ProdutoRequest dados) {
        produto.setCodigo(dados.codigo().trim().toUpperCase());
        produto.setDescricao(dados.descricao().trim());
        produto.setPreco(dados.preco());
        produto.setAtivo(dados.ativo() == null || dados.ativo());
    }
}
