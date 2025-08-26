package Servidor;

import modelos.Incidencia;
import modelos.Parada;
import modelos.Usuario;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GestorBD {
    private Connection conexion;

    public void conectar(String url, String usuario, String clave) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conexion = DriverManager.getConnection(url, usuario, clave);
            System.out.println("BD|CONECTADA");
        } catch (SQLException e) {
            System.err.println("BD|ERROR_CONEXION|" + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.err.println("BD|ERROR_DRIVER|" + e.getMessage());
        }
    }

    public void cerrar() {
        if (conexion != null) {
            try { conexion.close(); System.out.println("BD|CERRADA"); } catch (SQLException e) {}
        }
    }

    private boolean conectado() {
        try {
            return conexion != null && !conexion.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    
    public int autenticarUsuarioPorNombre(String username) {
        if (!conectado()) return -1;
        String sql = "SELECT id, username, role, rol FROM users WHERE username = ? LIMIT 1";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        } catch (SQLException e) {
           
            try {
                String sql2 = "SELECT id, username FROM users WHERE username = ? LIMIT 1";
                try (PreparedStatement ps2 = conexion.prepareStatement(sql2)) {
                    ps2.setString(1, username);
                    try (ResultSet rs2 = ps2.executeQuery()) {
                        if (rs2.next()) return rs2.getInt("id");
                    }
                }
            } catch (SQLException ex) {
                System.err.println("BD|ERROR_AUTENTICAR|" + ex.getMessage());
            }
        }
        return -1;
    }

    public boolean existeUsuario(int idUsuario) {
        if (!conectado()) return false;
        String sql = "SELECT 1 FROM users WHERE id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("BD|ERROR_EXISTE_USUARIO|" + e.getMessage());
        }
        return false;
    }

    
    public List<Parada> listarParadas() {
        List<Parada> lista = new ArrayList<>();
        if (!conectado()) return lista;
        String sql = "SELECT * FROM stops";
        try (PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Parada p = new Parada();
                p.setId(rs.getInt(getColumnLabel(rs, "id", "id")));
                p.setNombre(getColumnString(rs, "nombre", "name"));
                p.setCanton(getColumnString(rs, "canton", "canton"));
                p.setLat(getColumnDouble(rs, "lat", "lat"));
                p.setLon(getColumnDouble(rs, "lon", "lon"));
                lista.add(p);
            }
        } catch (SQLException e) {
            System.err.println("BD|ERROR_LISTAR_PARADAS|" + e.getMessage());
        }
        return lista;
    }

    public Parada obtenerParadaPorId(int idParada) {
        if (!conectado()) return null;
        String sql = "SELECT * FROM stops WHERE id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idParada);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Parada p = new Parada();
                    p.setId(rs.getInt(getColumnLabel(rs, "id", "id")));
                    p.setNombre(getColumnString(rs, "nombre", "name"));
                    p.setCanton(getColumnString(rs, "canton", "canton"));
                    p.setLat(getColumnDouble(rs, "lat", "lat"));
                    p.setLon(getColumnDouble(rs, "lon", "lon"));
                    return p;
                }
            }
        } catch (SQLException e) {
            System.err.println("BD|ERROR_OBT_PARADA|" + e.getMessage());
        }
        return null;
    }

  

    public int insertarIncidencia(Incidencia i) {
        if (!conectado()) return -1;
        String sql = "INSERT INTO incidents (user_id, stop_id, type, description) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, i.getUsuarioId());
            ps.setInt(2, i.getParadaId());
            ps.setString(3, i.getTipo());
            ps.setString(4, i.getDescripcion());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("BD|ERROR_INSERT|" + e.getMessage());
        }
        return -1;
    }

    public List<Incidencia> obtenerRecientesPorParada(int paradaId, int limite) {
        List<Incidencia> lista = new ArrayList<>();
        if (!conectado()) return lista;
        String sql = "SELECT id, user_id, stop_id, type, description, created_at, status FROM incidents WHERE stop_id = ? ORDER BY created_at DESC LIMIT ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, paradaId);
            ps.setInt(2, limite);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Incidencia inc = new Incidencia();
                    inc.setId(rs.getInt("id"));
                    inc.setUsuarioId(rs.getInt("user_id"));
                    inc.setParadaId(rs.getInt("stop_id"));
                    inc.setTipo(rs.getString("type"));
                    inc.setDescripcion(rs.getString("description"));
                    inc.setEstado(getColumnString(rs, "status", "status"));
                    inc.setFecha(rs.getTimestamp("created_at"));
                    lista.add(inc);
                }
            }
        } catch (SQLException e) {
            System.err.println("BD|ERROR_SELECT|" + e.getMessage());
        }
        return lista;
    }

  
    public String obtenerRecientesComoString(int paradaId, int limite) {
        List<Incidencia> lista = obtenerRecientesPorParada(paradaId, limite);
        if (lista.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Incidencia i : lista) {
            if (!first) sb.append(" ;; ");
            sb.append(i.toString());
            first = false;
        }
        return sb.toString();
    }

    public boolean actualizarEstadoIncidencia(int incidenciaId, String nuevoEstado) {
        if (!conectado()) return false;
        String sql = "UPDATE incidents SET status = ? WHERE id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, nuevoEstado);
            ps.setInt(2, incidenciaId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("BD|ERROR_UPDATE|" + e.getMessage());
        }
        return false;
    }

 

    private String getColumnString(ResultSet rs, String primary, String fallback) {
        try {
            return rs.getString(primary);
        } catch (SQLException e) {
            try { return rs.getString(fallback); } catch (SQLException ex) { return null; }
        }
    }

    private double getColumnDouble(ResultSet rs, String primary, String fallback) {
        try { return rs.getDouble(primary); } catch (SQLException e) {
            try { return rs.getDouble(fallback); } catch (SQLException ex) { return 0.0; }
        }
    }

    private String getColumnLabel(ResultSet rs, String primary, String fallback) {
        try {
            rs.findColumn(primary);
            return primary;
        } catch (SQLException e) {
            try { rs.findColumn(fallback); return fallback; } catch (SQLException ex) { return primary; }
        }
    }
}
