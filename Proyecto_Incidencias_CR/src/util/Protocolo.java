package util;

import modelos.Incidencia;
import Servidor.GestorBD;
import java.util.List;

public class Protocolo {

    public static String obtenerComando(String linea) {
        if (linea == null || !linea.contains("|")) return linea == null ? "DESCONOCIDO" : (linea.split("\\|")[0]);
        return linea.split("\\|")[0];
    }

    public static String formarMensajeReport(String usuarioId, String paradaId, String tipo, String descripcion) {
        descripcion = descripcion.replace("|", " ");
        return "REPORT|" + usuarioId + "|" + paradaId + "|" + tipo + "|" + descripcion;
    }

    public static String formarMensajeGetRecent(String paradaId, String limite) {
        return "GET_RECENT|" + paradaId + "|" + limite;
    }

    public static String formarMensajeLogin(String username) {
        return "LOGIN|" + username;
    }

    public static String formarMensajeListStops() {
        return "LIST_STOPS|";
    }

    public static String formarMensajeUpdateStatus(String incidenciaId, String estado) {
        return "UPDATE_STATUS|" + incidenciaId + "|" + estado;
    }

    public static Incidencia parsearIncidencia(String linea) {
        String[] partes = linea.split("\\|", 5);
        Incidencia i = new Incidencia();
        try {
            i.setUsuarioId(Integer.parseInt(partes[1]));
            i.setParadaId(Integer.parseInt(partes[2]));
            i.setTipo(partes[3]);
            i.setDescripcion(partes[4]);
        } catch (Exception e) { /* manejo simple */ }
        return i;
    }

    public static int parsearParadaId(String linea) {
        String[] p = linea.split("\\|");
        try { return Integer.parseInt(p[1]); } catch (Exception e) { return -1; }
    }

    public static int parsearLimite(String linea) {
        String[] p = linea.split("\\|");
        try { return Integer.parseInt(p[2]); } catch (Exception e) { return 5; }
    }

    public static int parsearIncidenciaId(String linea) {
        String[] p = linea.split("\\|");
        try { return Integer.parseInt(p[1]); } catch (Exception e) { return -1; }
    }

    
    public static String procesarMensaje(String linea, GestorBD gestor) {
        if (linea == null || linea.trim().isEmpty()) return "ERROR|Mensaje vacío";

        String comando = obtenerComando(linea);

        switch (comando) {
            case "REPORT":
                Incidencia inc = parsearIncidencia(linea);
                if (!gestor.existeUsuario(inc.getUsuarioId())) return "ERROR|Usuario no existe";
                if (gestor.obtenerParadaPorId(inc.getParadaId()) == null) return "ERROR|Parada no existe";
                int id = gestor.insertarIncidencia(inc);
                return id > 0 ? "OK|Incidencia registrada|" + id : "ERROR|No se pudo insertar incidencia";

            case "GET_RECENT":
                int paradaId = parsearParadaId(linea);
                int limite = parsearLimite(linea);
                if (paradaId <= 0) return "ERROR|ID de parada inválido";
                return gestor.obtenerRecientesComoString(paradaId, limite);

            case "LOGIN":
                String[] p = linea.split("\\|", 2);
                if (p.length < 2) return "ERROR|Falta nombre de usuario";
                String username = p[1];
                int uid = gestor.autenticarUsuarioPorNombre(username);
                if (uid > 0) return "OK|USERID|" + uid;
                else return "ERROR|Usuario no encontrado";

            case "LIST_STOPS":
                List<modelos.Parada> paradas = gestor.listarParadas();
                if (paradas.isEmpty()) return "[]";
                StringBuilder sb = new StringBuilder();
                boolean first = true;
                for (modelos.Parada par : paradas) {
                    if (!first) sb.append(" ;; ");
                    sb.append(par.getId()).append("|").append(par.getNombre()).append("|").append(par.getCanton());
                    first = false;
                }
                return sb.toString();

            case "UPDATE_STATUS":
                String[] parts = linea.split("\\|");
                if (parts.length < 3) return "ERROR|Formato UPDATE_STATUS incorrecto";
                int incId;
                try { incId = Integer.parseInt(parts[1]); } catch (Exception ex) { return "ERROR|ID inválido"; }
                String estado = parts[2];
                boolean ok = gestor.actualizarEstadoIncidencia(incId, estado);
                return ok ? "OK|Estado actualizado" : "ERROR|No se actualizó";

            default:
                return "ERROR|Comando desconocido";
        }
    }
}
