package br.com.plataforma.pedidos.web;

import br.com.plataforma.pedidos.cliente.Cliente;
import br.com.plataforma.pedidos.cliente.ClienteDtos.ClienteRequest;
import br.com.plataforma.pedidos.cliente.ClienteService;
import jakarta.annotation.PostConstruct;
import org.joinfaces.viewscope.ViewScope;
import org.primefaces.PrimeFaces;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;

@Component
@Scope(ViewScope.SCOPE_VIEW)
public class ClienteView implements Serializable {

    private final ClienteService service;

    private List<Cliente> clientes;
    private Cliente edicao = new Cliente();

    public ClienteView(ClienteService service) {
        this.service = service;
    }

    @PostConstruct
    void carregar() {
        clientes = service.listarTodos();
    }

    public void novo() {
        edicao = new Cliente();
    }

    /** Edita uma cópia, para não alterar a linha da tabela se o usuário cancelar. */
    public void editar(Cliente c) {
        edicao = new Cliente();
        edicao.setId(c.getId());
        edicao.setNome(c.getNome());
        edicao.setDocumento(c.getDocumento());
        edicao.setEmail(c.getEmail());
        edicao.setAtivo(c.isAtivo());
    }

    public void salvar() {
        String documento = edicao.getDocumento() == null ? null : edicao.getDocumento().replaceAll("\\D", "");
        var dados = new ClienteRequest(edicao.getNome(), documento, edicao.getEmail(), edicao.isAtivo());
        boolean ok = Mensagens.executar(() -> {
            if (edicao.getId() == null) {
                service.criar(dados);
            } else {
                service.atualizar(edicao.getId(), dados);
            }
        });
        if (ok) {
            Mensagens.info("Cliente salvo.");
            carregar();
            PrimeFaces.current().executeScript("PF('dlgCliente').hide()");
        }
    }

    public void excluir(Cliente c) {
        if (Mensagens.executar(() -> service.excluir(c.getId()))) {
            Mensagens.info("Cliente excluído.");
            carregar();
        }
    }

    public List<Cliente> getClientes() { return clientes; }
    public Cliente getEdicao() { return edicao; }
}
