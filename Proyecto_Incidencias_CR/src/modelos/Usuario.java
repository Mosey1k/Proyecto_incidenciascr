
package modelos;



public class Usuario {
    private int id;
    private String nombre;
    private String rol; 

    public Usuario() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    @Override
    public String toString() {
        return "Usuario[id=" + id + ", nombre=" + nombre + ", rol=" + rol + "]";
    }
}
