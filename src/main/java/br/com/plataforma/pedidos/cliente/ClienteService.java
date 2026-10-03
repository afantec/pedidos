package br.com.plataforma.pedidos.cliente;

import br.com.plataforma.pedidos.cliente.ClienteDtos.ClienteRequest;
import br.com.plataforma.pedidos.comum.ConflitoException;
import br.com.plataforma.pedidos.comum.RecursoNaoEncontradoException;
import br.com.plataforma.pedidos.pedido.PedidoRepository;
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
public class ClienteService {

    private final ClienteRepository clientes;
    private final PedidoRepository pedidos;

    public ClienteService(ClienteRepository clientes, PedidoRepository pedidos) {
        this.clientes = clientes;
        this.pedidos = pedidos;
    }

    public Page<Cliente> listar(String nome, Pageable pageable) {
        return StringUtils.hasText(nome)
                ? clientes.findByNomeContainingIgnoreCase(nome.trim(), pageable)
                : clientes.findAll(pageable);
    }

    public List<Cliente> listarTodos() {
        return clientes.findAll(Sort.by("nome"));
    }

    public List<Cliente> listarAtivos() {
        return clientes.findByAtivoTrue(Sort.by("nome"));
    }

    public Cliente buscar(Long id) {
        return clientes.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Cliente", id));
    }

    @Transactional
    public Cliente criar(@Valid ClienteRequest dados) {
        if (clientes.existsByDocumento(dados.documento())) {
            throw new ConflitoException("Já existe cliente com o documento " + dados.documento());
        }
        Cliente cliente = new Cliente();
        preencher(cliente, dados);
        return clientes.save(cliente);
    }

    @Transactional
    public Cliente atualizar(Long id, @Valid ClienteRequest dados) {
        Cliente cliente = buscar(id);
        if (clientes.existsByDocumentoAndIdNot(dados.documento(), id)) {
            throw new ConflitoException("Já existe cliente com o documento " + dados.documento());
        }
        preencher(cliente, dados);
        return cliente;
    }

    /** Clientes com pedidos não podem ser excluídos; devem ser inativados. */
    @Transactional
    public void excluir(Long id) {
        Cliente cliente = buscar(id);
        if (pedidos.existsByClienteId(id)) {
            throw new ConflitoException("Cliente possui pedidos e não pode ser excluído; inative-o.");
        }
        clientes.delete(cliente);
    }

    private static void preencher(Cliente cliente, ClienteRequest dados) {
        cliente.setNome(dados.nome().trim());
        cliente.setDocumento(dados.documento());
        cliente.setFone(dados.fone());
        cliente.setEmail(StringUtils.hasText(dados.email()) ? dados.email().trim() : null);
        cliente.setAtivo(dados.ativo() == null || dados.ativo());
    }
}
