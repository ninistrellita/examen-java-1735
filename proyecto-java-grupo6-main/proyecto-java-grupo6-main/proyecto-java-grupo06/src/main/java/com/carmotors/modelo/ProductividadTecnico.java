package com.carmotors.modelo;

/** Resultado del reporte de productividad: órdenes asignadas y terminadas por técnico. */
public class ProductividadTecnico {
    private final int tecnicoId;
    private final String tecnico;
    private final long asignadas;
    private final long completadas;

    public ProductividadTecnico(int tecnicoId, String tecnico, long asignadas, long completadas) {
        this.tecnicoId = tecnicoId;
        this.tecnico = tecnico;
        this.asignadas = asignadas;
        this.completadas = completadas;
    }

    public int getTecnicoId() { return tecnicoId; }
    public String getTecnico() { return tecnico; }
    public long getAsignadas() { return asignadas; }
    public long getCompletadas() { return completadas; }

    public int getPorcentajeCompletadas() {
        return asignadas == 0 ? 0 : (int) Math.round(completadas * 100.0 / asignadas);
    }

    @Override
    public String toString() {
        return tecnico + ": " + completadas + "/" + asignadas + " (" + getPorcentajeCompletadas() + "%)";
    }
}
