package br.com.plataforma.pedidos.cliente;

import br.com.plataforma.pedidos.comum.Documento;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

public final class ClienteDtos {

    private ClienteDtos() {
    }

    /** Dados de entrada para criar ou alterar um cliente. Documento só com dígitos. */
    public record ClienteRequest(
            @NotBlank @Size(max = 150) String nome,
            @NotBlank @Documento String documento,
            @Email @Size(max = 150) String email,
            Boolean ativo) {
    }

    public record ClienteResponse(
            Long id, String nome, String documento, String email, boolean ativo, OffsetDateTime criadoEm) {

        static ClienteResponse de(Cliente c) {
            return new ClienteResponse(c.getId(), c.getNome(), c.getDocumento(), c.getEmail(),
                    c.isAtivo(), c.getCriadoEm());
        }
    }
}
