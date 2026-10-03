package br.com.plataforma.pedidos.comum;

import br.com.plataforma.pedidos.cliente.ClienteDtos.ClienteRequest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MascarasTest {

    @Test
    void formataCpfAteODecimoPrimeiroDigito() {
        assertThat(Mascaras.documento("529")).isEqualTo("529");
        assertThat(Mascaras.documento("5299")).isEqualTo("529.9");
        assertThat(Mascaras.documento("52998224725")).isEqualTo("529.982.247-25");
        assertThat(Mascaras.tipoDocumento("52998224725")).isEqualTo("CPF");
    }

    @Test
    void formataCnpjAPartirDoDecimoSegundoDigito() {
        assertThat(Mascaras.documento("112223330001")).isEqualTo("11.222.333/0001");
        assertThat(Mascaras.documento("11.222.333/0001-81")).isEqualTo("11.222.333/0001-81");
        assertThat(Mascaras.tipoDocumento("11222333000181")).isEqualTo("CNPJ");
        assertThat(Mascaras.tipoDocumento("112223330001")).isEqualTo("CNPJ");
    }

    @Test
    void formataCelular() {
        assertThat(Mascaras.celular("11")).isEqualTo("(11");
        assertThat(Mascaras.celular("119")).isEqualTo("(11) 9");
        assertThat(Mascaras.celular("1198765")).isEqualTo("(11) 98765");
        assertThat(Mascaras.celular("(11) 98765-4321")).isEqualTo("(11) 98765-4321");
    }

    @Test
    void normalizaDocumentoEFoneNoPedidoDeCadastro() {
        var dados = new ClienteRequest("Ana", "529.982.247-25", "(11) 98765-4321", "ana@example.com", true);

        assertThat(dados.documento()).isEqualTo("52998224725");
        assertThat(dados.fone()).isEqualTo("11987654321");
        assertThat(new ClienteRequest("Ana", "52998224725", "  ", null, null).fone()).isNull();
    }

    @Test
    void celularVazioEValidoEIncompletoNao() {
        assertThat(Mascaras.celularInformadoValido(null)).isTrue();
        assertThat(Mascaras.celularInformadoValido("(11) 98765-4321")).isTrue();
        assertThat(Mascaras.celularInformadoValido("1133334444")).isFalse();
        assertThat(Mascaras.tipoDocumento("529982247")).isEmpty();
    }
}
