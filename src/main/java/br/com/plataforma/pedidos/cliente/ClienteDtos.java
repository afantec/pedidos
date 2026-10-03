package br.com.plataforma.pedidos.cliente;

import br.com.plataforma.pedidos.comum.Documento;
import br.com.plataforma.pedidos.comum.Mascaras;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

public final class ClienteDtos {

    private ClienteDtos() {
    }

    /** Dados de entrada para criar ou alterar um cliente. Documento e fone ficam só com dígitos. */
    public record ClienteRequest(
            @NotBlank @Size(max = 150) String nome,
            @NotBlank @Documento String documento,
            @Pattern(regexp = Mascaras.CELULAR, message = "Informe um celular com DDD") String fone,
            @Email @Size(max = 150) String email,
            Boolean ativo) {

        public ClienteRequest {
            documento = Mascaras.digitos(documento);
            fone = Mascaras.digitos(fone);
        }
    }

    public record ClienteResponse(
            Long id, String nome, String documento, String fone, String email, boolean ativo,
            OffsetDateTime criadoEm) {

        static ClienteResponse de(Cliente c) {
            return new ClienteResponse(c.getId(), c.getNome(), c.getDocumento(), c.getFone(), c.getEmail(),
                    c.isAtivo(), c.getCriadoEm());
        }
    }
}
