package com.carmotors.modelo;

public class EstadoServicio {
    private final int id;
    private final String nombre;

    public EstadoServicio(int id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }
}
