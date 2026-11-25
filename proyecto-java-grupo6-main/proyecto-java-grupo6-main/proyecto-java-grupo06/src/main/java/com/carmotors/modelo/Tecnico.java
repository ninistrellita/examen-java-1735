package com.carmotors.modelo;

public class Tecnico {
    private final int id;
    private final String nombre;

    public Tecnico(int id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }
}