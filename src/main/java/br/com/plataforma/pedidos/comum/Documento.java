package br.com.plataforma.pedidos.comum;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** CPF (11 dígitos) ou CNPJ (14 dígitos), somente números e com dígitos verificadores válidos. */
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = Documento.Validador.class)
public @interface Documento {

    String message() default "CPF ou CNPJ inválido";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class Validador implements ConstraintValidator<Documento, String> {

        @Override
        public boolean isValid(String valor, ConstraintValidatorContext contexto) {
            return valor == null || valido(valor);   // obrigatoriedade fica com @NotBlank
        }

        public static boolean valido(String doc) {
            if (!doc.matches("\\d{11}|\\d{14}") || doc.chars().distinct().count() == 1) {
                return false;
            }
            return doc.length() == 11
                    ? confere(doc, new int[]{10, 9, 8, 7, 6, 5, 4, 3, 2})
                      && confere(doc, new int[]{11, 10, 9, 8, 7, 6, 5, 4, 3, 2})
                    : confere(doc, new int[]{5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2})
                      && confere(doc, new int[]{6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2});
        }

        /** Calcula o dígito verificador sobre os primeiros pesos.length dígitos e compara com o seguinte. */
        private static boolean confere(String doc, int[] pesos) {
            int soma = 0;
            for (int i = 0; i < pesos.length; i++) {
                soma += (doc.charAt(i) - '0') * pesos[i];
            }
            int resto = soma % 11;
            int digito = resto < 2 ? 0 : 11 - resto;
            return digito == doc.charAt(pesos.length) - '0';
        }
    }
}
