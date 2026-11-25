package com.carmotors.modelo;

import java.time.LocalDate;
import java.util.List;

public class HistorialVehiculo {
    private final String nombre;
    private final String email;
    private final String placa;
    private final String marca;
    private final String modelo;
    private final List<Visita> visitas;

    public HistorialVehiculo(String nombre, String email, String placa, String marca, String modelo, List<Visita> visitas) {
        this.nombre = nombre;
        this.email = email;
        this.placa = placa;
        this.marca = marca;
        this.modelo = modelo;
        this.visitas = visitas;
    }

    public String getNombre() {
        return nombre;
    }

    public String getEmail() {
        return email;
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

    public List<Visita> getVisitas() {
        return visitas;
    }

    public static class Visita {
        private final LocalDate recepcion;
        private final LocalDate entrega;
        private final String tipo;
        private final String estado;
        private final String descripcion;

        public Visita(LocalDate recepcion, LocalDate entrega, String tipo, String estado, String descripcion) {
            this.recepcion = recepcion;
            this.entrega = entrega;
            this.tipo = tipo;
            this.estado = estado;
            this.descripcion = descripcion;
        }

        public LocalDate getRecepcion() {
            return recepcion;
        }

        public LocalDate getEntrega() {
            return entrega;
        }

        public String getTipo() {
            return tipo;
        }

        public String getEstado() {
            return estado;
        }

        public String getDescripcion() {
            return descripcion;
        }
    }
}
