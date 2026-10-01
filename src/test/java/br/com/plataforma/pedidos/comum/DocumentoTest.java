package br.com.plataforma.pedidos.comum;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentoTest {

    @ParameterizedTest
    @ValueSource(strings = {"52998224725", "11144477735", "12345678000195", "11222333000181"})
    void aceitaCpfECnpjValidos(String doc) {
        assertThat(Documento.Validador.valido(doc)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"52998224724", "11111111111", "12345678000190", "123", "529.982.247-25", ""})
    void rejeitaDocumentosInvalidos(String doc) {
        assertThat(Documento.Validador.valido(doc)).isFalse();
    }
}
