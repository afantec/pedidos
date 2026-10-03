package br.com.plataforma.pedidos.produto;

import br.com.plataforma.pedidos.comum.RegraNegocioException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ImagemProdutoTest {

    @Test
    void reconhecePngJpegGifEWebp() {
        assertThat(ImagemProduto.de(new byte[]{(byte) 0x89, 'P', 'N', 'G', 1}).tipo()).isEqualTo("image/png");
        assertThat(ImagemProduto.de(new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 1}).tipo()).isEqualTo("image/jpeg");
        assertThat(ImagemProduto.de("GIF89a".getBytes()).tipo()).isEqualTo("image/gif");
        byte[] webp = new byte[12];
        System.arraycopy("RIFF".getBytes(), 0, webp, 0, 4);
        System.arraycopy("WEBP".getBytes(), 0, webp, 8, 4);
        assertThat(ImagemProduto.de(webp).tipo()).isEqualTo("image/webp");
    }

    @Test
    void rejeitaArquivoQueNaoEImagemOuPassaDe2Mb() {
        assertThat(ImagemProduto.de(null)).isNull();
        assertThatThrownBy(() -> ImagemProduto.de("texto".getBytes()))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("PNG");

        byte[] grande = new byte[ImagemProduto.MAXIMO + 1];
        grande[0] = (byte) 0x89;
        grande[1] = 'P';
        grande[2] = 'N';
        grande[3] = 'G';
        assertThatThrownBy(() -> ImagemProduto.de(grande))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("2 MB");
    }
}
