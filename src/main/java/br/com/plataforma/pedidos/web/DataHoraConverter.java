package br.com.plataforma.pedidos.web;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Exibe datas no horário de Brasília. O banco devolve TIMESTAMPTZ em UTC e o
 * f:convertDateTime do Mojarra ignora timeZone para OffsetDateTime. Somente saída.
 */
@FacesConverter("dataHora")
public class DataHoraConverter implements Converter<OffsetDateTime> {

    private static final DateTimeFormatter FORMATO =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneId.of("America/Sao_Paulo"));

    @Override
    public String getAsString(FacesContext context, UIComponent component, OffsetDateTime valor) {
        return valor == null ? "" : FORMATO.format(valor);
    }

    @Override
    public OffsetDateTime getAsObject(FacesContext context, UIComponent component, String valor) {
        throw new UnsupportedOperationException("dataHora é somente para exibição");
    }
}
