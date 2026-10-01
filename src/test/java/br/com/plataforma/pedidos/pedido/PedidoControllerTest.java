package br.com.plataforma.pedidos.pedido;

import br.com.plataforma.pedidos.comum.RecursoNaoEncontradoException;
import br.com.plataforma.pedidos.comum.RegraNegocioException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PedidoController.class)
class PedidoControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    PedidoService service;

    @Test
    void rejeitaPedidoSemItensComErroPorCampo() throws Exception {
        mvc.perform(post("/api/v1/pedidos").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"clienteId": 1, "itens": []}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros.itens").exists());
        verifyNoInteractions(service);
    }

    @Test
    void pedidoInexistenteRetorna404() throws Exception {
        given(service.buscar(99L)).willThrow(new RecursoNaoEncontradoException("Pedido", 99L));

        mvc.perform(get("/api/v1/pedidos/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Pedido 99 não encontrado"));
    }

    @Test
    void regraDeNegocioRetorna422() throws Exception {
        given(service.confirmar(eq(5L))).willThrow(new RegraNegocioException("Pedido 5 está cancelado"));

        mvc.perform(post("/api/v1/pedidos/5/confirmacao"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.title").value("Regra de negócio violada"));
    }

    @Test
    void quantidadeZeroEhInvalida() throws Exception {
        mvc.perform(post("/api/v1/pedidos").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"clienteId": 1, "itens": [{"produtoId": 1, "quantidade": 0}]}
                                """))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }
}
