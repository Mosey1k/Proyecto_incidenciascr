/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelos;


import java.sql.Timestamp;

public class Incidencia {
    private int id;
    private int usuarioId;
    private int paradaId;
    private String tipo;
    private String descripcion;
    private Timestamp fecha;
    private String estado;

    public Incidencia() {}

    
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUsuarioId() { return usuarioId; }
    public void setUsuarioId(int usuarioId) { this.usuarioId = usuarioId; }

    public int getParadaId() { return paradaId; }
    public void setParadaId(int paradaId) { this.paradaId = paradaId; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Timestamp getFecha() { return fecha; }
    public void setFecha(Timestamp fecha) { this.fecha = fecha; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    @Override
    public String toString() {
        return "Incidencia[id=" + id + ", usuario=" + usuarioId + ", parada=" + paradaId
            + ", tipo=" + tipo + ", descripcion=" + descripcion + ", fecha=" + fecha + ", estado=" + estado + "]";
    }
}
