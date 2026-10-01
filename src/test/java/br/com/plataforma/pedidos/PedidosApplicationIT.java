package br.com.plataforma.pedidos;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Sobe a aplicação inteira contra um PostgreSQL real (Flyway aplicado do zero).
 * Sem Docker disponível o teste é ignorado; no pipeline ele é obrigatório (decisão D1 da análise).
 */
@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.flyway.create-schemas=true")
class PedidosApplicationIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired
    TestRestTemplate http;

    @Test
    @SuppressWarnings("unchecked")
    void fluxoCompletoDePedido() {
        var cliente = http.postForEntity("/api/v1/clientes",
                Map.of("nome", "Cliente IT", "documento", "11144477735"), Map.class);
        assertThat(cliente.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        var produto = http.postForEntity("/api/v1/produtos",
                Map.of("codigo", "it-01", "descricao", "Produto IT", "preco", "12.50"), Map.class);
        assertThat(produto.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(produto.getBody()).containsEntry("codigo", "IT-01");

        var pedido = http.postForEntity("/api/v1/pedidos", Map.of(
                "clienteId", cliente.getBody().get("id"),
                "itens", List.of(
                        Map.of("produtoId", produto.getBody().get("id"), "quantidade", 2),
                        Map.of("produtoId", produto.getBody().get("id"), "quantidade", 1))), Map.class);
        assertThat(pedido.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(pedido.getBody()).containsEntry("status", "ABERTO").containsEntry("total", 37.5);
        assertThat((List<?>) pedido.getBody().get("itens")).hasSize(1);   // linhas do mesmo produto somadas

        Object id = pedido.getBody().get("id");
        var confirmado = http.postForEntity("/api/v1/pedidos/{id}/confirmacao", null, Map.class, id);
        assertThat(confirmado.getBody()).containsEntry("status", "CONFIRMADO");

        var denovo = http.postForEntity("/api/v1/pedidos/{id}/confirmacao", null, Map.class, id);
        assertThat(denovo.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);

        var lista = http.getForEntity("/api/v1/pedidos?status=CONFIRMADO", Map.class);
        assertThat((Map<String, Object>) lista.getBody().get("page")).containsEntry("totalElements", 1);

        var exclusao = http.exchange("/api/v1/clientes/{id}", org.springframework.http.HttpMethod.DELETE,
                null, Map.class, cliente.getBody().get("id"));
        assertThat(exclusao.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void healthUpEInterfaceWebNoAr() {
        ResponseEntity<String> health = http.getForEntity("/actuator/health", String.class);
        assertThat(health.getBody()).contains("\"status\":\"UP\"");

        for (String pagina : List.of("/index.xhtml", "/clientes.xhtml", "/produtos.xhtml",
                "/pedidos.xhtml", "/pedido.xhtml")) {
            ResponseEntity<String> resposta = http.getForEntity(pagina, String.class);
            assertThat(resposta.getStatusCode()).as(pagina).isEqualTo(HttpStatus.OK);
            assertThat(resposta.getBody()).as(pagina).contains("ui-menubar");
        }
    }
}
