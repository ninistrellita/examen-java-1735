package com.carmotors.modelo;

public class Vehiculo {
    private final String documentoCliente;
    private final String placa;
    private final String marca;
    private final String modelo;

    public Vehiculo(String documentoCliente, String placa, String marca, String modelo) {
        this.documentoCliente = documentoCliente;
        this.placa = placa;
        this.marca = marca;
        this.modelo = modelo;
    }

    public String getDocumentoCliente() {
        return documentoCliente;
    }

    public String getPlaca() {
        return placa;
    }

    public String getMarca() {
        return marca;
    }

    public String getModelo() {
        return modelo;
    }
}
