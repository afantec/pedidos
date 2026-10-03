package br.com.plataforma.pedidos.web;

import br.com.plataforma.pedidos.cliente.Cliente;
import br.com.plataforma.pedidos.cliente.ClienteDtos.ClienteRequest;
import br.com.plataforma.pedidos.cliente.ClienteService;
import br.com.plataforma.pedidos.comum.Mascaras;
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
        edicao.setDocumento(Mascaras.documento(c.getDocumento()));
        edicao.setFone(Mascaras.celular(c.getFone()));
        edicao.setEmail(c.getEmail());
        edicao.setAtivo(c.isAtivo());
    }

    public void salvar() {
        if (!Mascaras.celularInformadoValido(edicao.getFone())) {
            Mensagens.erro("Informe um celular com DDD.");
            return;
        }
        var dados = new ClienteRequest(edicao.getNome(), edicao.getDocumento(), edicao.getFone(),
                edicao.getEmail(), edicao.isAtivo());
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

    public String documentoFormatado(String documento) {
        return Mascaras.documento(documento);
    }

    public String foneFormatado(String fone) {
        return Mascaras.celular(fone);
    }

    public String getTipoDocumento() {
        return Mascaras.tipoDocumento(edicao.getDocumento());
    }

    public List<Cliente> getClientes() { return clientes; }
    public Cliente getEdicao() { return edicao; }
}
