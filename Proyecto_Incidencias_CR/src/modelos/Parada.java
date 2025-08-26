package modelos;

public class Parada {
    private int id;
    private String nombre;
    private String canton;
    private double lat;
    private double lon;

    public Parada() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCanton() { return canton; }
    public void setCanton(String canton) { this.canton = canton; }

    public double getLat() { return lat; }
    public void setLat(double lat) { this.lat = lat; }

    public double getLon() { return lon; }
    public void setLon(double lon) { this.lon = lon; }

    @Override
    public String toString() {
        return "Parada[id=" + id + ", nombre=" + nombre + ", canton=" + canton + "]";
    }
}

