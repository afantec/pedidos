function prepararMascara(el) {
    el.dataset.digitos = somenteDigitos(el.value);
    el.dataset.texto = el.value;
}

function mascaraDocumento(el) {
    aplicarMascara(el, 14, formatarDocumento);
    marcarTipoDocumento(el);
}

function mascaraCelular(el) {
    aplicarMascara(el, 11, formatarCelular);
}

function somenteDigitos(valor) {
    return (valor || "").replace(/\D/g, "");
}

function aplicarMascara(el, maximo, formatar) {
    const inicio = el.selectionStart ?? el.value.length;
    const textoAtual = el.value;
    let digitosAteCursor = somenteDigitos(textoAtual.slice(0, inicio)).length;
    let digitos = somenteDigitos(textoAtual).slice(0, maximo);
    const anteriores = el.dataset.digitos || "";
    const textoAnterior = el.dataset.texto || "";
    if (digitos === anteriores && textoAtual.length < textoAnterior.length && digitosAteCursor > 0) {
        const corte = digitosAteCursor - 1;
        digitos = digitos.slice(0, corte) + digitos.slice(corte + 1);
        digitosAteCursor = corte;
    }
    const texto = formatar(digitos);
    el.dataset.digitos = digitos;
    el.dataset.texto = texto;
    el.value = texto;
    const cursor = posicaoCursor(texto, digitosAteCursor);
    el.setSelectionRange(cursor, cursor);
}

function posicaoCursor(texto, digitosAteCursor) {
    if (digitosAteCursor <= 0) {
        return 0;
    }
    let vistos = 0;
    for (let i = 0; i < texto.length; i++) {
        if (/\d/.test(texto.charAt(i))) {
            vistos++;
            if (vistos === digitosAteCursor) {
                return i + 1;
            }
        }
    }
    return texto.length;
}

function formatarDocumento(digitos) {
    return digitos.length <= 11 ? formatarCpf(digitos) : formatarCnpj(digitos);
}

function formatarCpf(digitos) {
    let texto = digitos.slice(0, 3);
    if (digitos.length > 3) {
        texto += "." + digitos.slice(3, 6);
    }
    if (digitos.length > 6) {
        texto += "." + digitos.slice(6, 9);
    }
    if (digitos.length > 9) {
        texto += "-" + digitos.slice(9, 11);
    }
    return texto;
}

function formatarCnpj(digitos) {
    let texto = digitos.slice(0, 2);
    if (digitos.length > 2) {
        texto += "." + digitos.slice(2, 5);
    }
    if (digitos.length > 5) {
        texto += "." + digitos.slice(5, 8);
    }
    if (digitos.length > 8) {
        texto += "/" + digitos.slice(8, 12);
    }
    if (digitos.length > 12) {
        texto += "-" + digitos.slice(12, 14);
    }
    return texto;
}

function formatarCelular(digitos) {
    if (!digitos) {
        return "";
    }
    let texto = "(" + digitos.slice(0, 2);
    if (digitos.length > 2) {
        texto += ") " + digitos.slice(2, 7);
    }
    if (digitos.length > 7) {
        texto += "-" + digitos.slice(7, 11);
    }
    return texto;
}

function marcarTipoDocumento(el) {
    const rotulo = el.closest(".campo")?.querySelector(".tipo-doc");
    if (!rotulo) {
        return;
    }
    const digitos = el.dataset.digitos || "";
    if (digitos.length === 11) {
        rotulo.textContent = "CPF";
    } else if (digitos.length > 11) {
        rotulo.textContent = "CNPJ";
    } else {
        rotulo.textContent = "";
    }
}
