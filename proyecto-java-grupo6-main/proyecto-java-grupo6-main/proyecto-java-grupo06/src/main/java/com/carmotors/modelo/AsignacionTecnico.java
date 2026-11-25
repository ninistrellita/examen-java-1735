package com.carmotors.modelo;

/** Un técnico asignado a una orden de servicio (fila de servicios_tecnicos). */
public class AsignacionTecnico {
    private final int servicioId;
    private final int tecnicoId;
    private final String tecnico;

    public AsignacionTecnico(int servicioId, int tecnicoId, String tecnico) {
        this.servicioId = servicioId;
        this.tecnicoId = tecnicoId;
        this.tecnico = tecnico;
    }

    public int getServicioId() { return servicioId; }
    public int getTecnicoId() { return tecnicoId; }
    public String getTecnico() { return tecnico; }
}
