package br.com.plataforma.pedidos.comum;

/** Dígitos e máscaras de CPF, CNPJ e celular usados na tela e na API. */
public final class Mascaras {

    public static final String CELULAR = "\\d{2}9\\d{8}";

    private Mascaras() {
    }

    public static String digitos(String valor) {
        if (valor == null) {
            return null;
        }
        String digitos = valor.replaceAll("\\D", "");
        return digitos.isEmpty() ? null : digitos;
    }

    public static boolean celularInformadoValido(String valor) {
        String digitos = digitos(valor);
        return digitos == null || digitos.matches(CELULAR);
    }

    /** Até 11 dígitos usa máscara de CPF; a partir do 12º, de CNPJ. */
    public static String documento(String valor) {
        String digitos = limitar(digitos(valor), 14);
        if (digitos.isEmpty()) {
            return "";
        }
        return digitos.length() <= 11 ? cpf(digitos) : cnpj(digitos);
    }

    public static String tipoDocumento(String valor) {
        String digitos = digitos(valor);
        if (digitos == null) {
            return "";
        }
        if (digitos.length() == 11) {
            return "CPF";
        }
        return digitos.length() > 11 ? "CNPJ" : "";
    }

    public static String celular(String valor) {
        String digitos = limitar(digitos(valor), 11);
        if (digitos.isEmpty()) {
            return "";
        }
        StringBuilder texto = new StringBuilder("(").append(digitos, 0, Math.min(2, digitos.length()));
        if (digitos.length() > 2) {
            texto.append(") ").append(digitos, 2, Math.min(7, digitos.length()));
        }
        if (digitos.length() > 7) {
            texto.append('-').append(digitos, 7, digitos.length());
        }
        return texto.toString();
    }

    private static String cpf(String digitos) {
        StringBuilder texto = new StringBuilder(digitos.substring(0, Math.min(3, digitos.length())));
        if (digitos.length() > 3) {
            texto.append('.').append(digitos, 3, Math.min(6, digitos.length()));
        }
        if (digitos.length() > 6) {
            texto.append('.').append(digitos, 6, Math.min(9, digitos.length()));
        }
        if (digitos.length() > 9) {
            texto.append('-').append(digitos, 9, Math.min(11, digitos.length()));
        }
        return texto.toString();
    }

    private static String cnpj(String digitos) {
        StringBuilder texto = new StringBuilder(digitos.substring(0, Math.min(2, digitos.length())));
        if (digitos.length() > 2) {
            texto.append('.').append(digitos, 2, Math.min(5, digitos.length()));
        }
        if (digitos.length() > 5) {
            texto.append('.').append(digitos, 5, Math.min(8, digitos.length()));
        }
        if (digitos.length() > 8) {
            texto.append('/').append(digitos, 8, Math.min(12, digitos.length()));
        }
        if (digitos.length() > 12) {
            texto.append('-').append(digitos, 12, Math.min(14, digitos.length()));
        }
        return texto.toString();
    }

    private static String limitar(String digitos, int maximo) {
        if (digitos == null) {
            return "";
        }
        return digitos.length() <= maximo ? digitos : digitos.substring(0, maximo);
    }
}
