package br.com.plataforma.pedidos.produto;

import br.com.plataforma.pedidos.comum.RegraNegocioException;

public record ImagemProduto(byte[] conteudo, String tipo) {

    public static final int MAXIMO = 2 * 1024 * 1024;

    public static ImagemProduto de(byte[] dados) {
        if (dados == null || dados.length == 0) {
            return null;
        }
        if (dados.length > MAXIMO) {
            throw new RegraNegocioException("A imagem deve ter no máximo 2 MB.");
        }
        String tipo = tipoDe(dados);
        if (tipo == null) {
            throw new RegraNegocioException("Envie uma imagem PNG, JPEG, WEBP ou GIF.");
        }
        return new ImagemProduto(dados, tipo);
    }

    static String tipoDe(byte[] dados) {
        if (comeca(dados, new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF})) {
            return "image/jpeg";
        }
        if (comeca(dados, new byte[]{(byte) 0x89, 'P', 'N', 'G'})) {
            return "image/png";
        }
        if (comeca(dados, new byte[]{'G', 'I', 'F'})) {
            return "image/gif";
        }
        if (dados.length >= 12
                && comeca(dados, new byte[]{'R', 'I', 'F', 'F'})
                && dados[8] == 'W' && dados[9] == 'E' && dados[10] == 'B' && dados[11] == 'P') {
            return "image/webp";
        }
        return null;
    }

    private static boolean comeca(byte[] dados, byte[] assinatura) {
        if (dados.length < assinatura.length) {
            return false;
        }
        for (int i = 0; i < assinatura.length; i++) {
            if (dados[i] != assinatura[i]) {
                return false;
            }
        }
        return true;
    }
}
