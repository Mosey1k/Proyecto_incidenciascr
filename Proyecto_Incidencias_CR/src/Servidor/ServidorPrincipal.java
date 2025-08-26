package Servidor;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;

public class ServidorPrincipal {
    public static final int PUERTO = 5000;

    public static void main(String[] args) {
        GestorBD gestor = new GestorBD();
        gestor.conectar("jdbc:mysql://localhost:3306/incidencia_cr", "root", "Brack2908"); 

        try (ServerSocket serverSocket = new ServerSocket(PUERTO)) {
            System.out.println("SERVIDOR|INICIO|Puerto " + PUERTO);
            while (true) {
                Socket clienteSocket = serverSocket.accept();
                System.out.println("SERVIDOR|NUEVA_CONEXION|" + clienteSocket.getRemoteSocketAddress());
                ManejadorCliente manejador = new ManejadorCliente(clienteSocket, gestor);
                new Thread(manejador).start();
            }
        } catch (IOException e) {
            System.err.println("SERVIDOR|ERROR|" + e.getMessage());
        } finally {
            gestor.cerrar();
        }
    }
}
