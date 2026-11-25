package com.carmotors.excepcion;

public class CampoInvalidoException extends IllegalArgumentException {
    private final String campo;

    public CampoInvalidoException(String campo, String mensaje) {
        super(mensaje);
        this.campo = campo;
    }

    public String getCampo() {
        return campo;
    }
}
