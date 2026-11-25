package com.carmotors.servicio;

import com.carmotors.excepcion.CampoInvalidoException;

final class ValidacionRegistro {
    private ValidacionRegistro() {}

    static String obligatorio(String valor, int limite, String campo) {
        String limpio = opcional(valor, limite, campo);
        if (limpio == null) {
            throw new CampoInvalidoException(campo, campo + " es obligatorio.");
        }
        return limpio;
    }

    static String opcional(String valor, int limite, String campo) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        String limpio = valor.strip();
        if (limpio.length() > limite) {
            throw new CampoInvalidoException(campo, campo + " admite hasta " + limite + " caracteres.");
        }
        return limpio;
    }

    static String nombrePersona(String valor, String campo) {
        String limpio = obligatorio(valor, 255, campo);
        if (!limpio.matches("[\\p{L}\\p{M}]+(?:[ '-][\\p{L}\\p{M}]+)*")) {
            throw new CampoInvalidoException(campo, campo + " solo admite letras y espacios.");
        }
        return limpio;
    }

    static String digitos(String valor, int limite, String campo, boolean opcional) {
        String limpio = opcional ? opcional(valor, limite, campo) : obligatorio(valor, limite, campo);
        if (limpio != null && !limpio.matches("[0-9]+")) {
            throw new CampoInvalidoException(campo, campo + " solo admite números.");
        }
        return limpio;
    }

    static String nombreServicio(String valor) {
        String limpio = obligatorio(valor, 100, "Nombre del servicio");
        if (!limpio.matches("[\\p{L}\\p{M}]+(?:[ '-][\\p{L}\\p{M}]+)*")) {
            throw new CampoInvalidoException("Nombre del servicio", "El servicio solo admite letras y espacios.");
        }
        return limpio;
    }

    static String textoConLetras(String valor, int limite, String campo) {
        String limpio = obligatorio(valor, limite, campo);
        if (limpio.codePoints().noneMatch(Character::isLetter)) {
            throw new CampoInvalidoException(campo, campo + " debe contener letras.");
        }
        return limpio;
    }
}
