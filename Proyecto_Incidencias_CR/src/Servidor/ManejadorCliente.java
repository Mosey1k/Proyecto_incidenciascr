package Servidor;

import java.net.Socket;
import java.io.*;
import util.Protocolo;

public class ManejadorCliente implements Runnable {
    private Socket socket;
    private GestorBD gestor;

    public ManejadorCliente(Socket socket, GestorBD gestor) {
        this.socket = socket;
        this.gestor = gestor;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            String linea;
            while ((linea = in.readLine()) != null) {
                System.out.println("SERVIDOR|RECIBIDO|" + linea);
               
                String respuesta = Protocolo.procesarMensaje(linea, gestor);
                out.println(respuesta);
                System.out.println("SERVIDOR|RESPONDIDO|" + respuesta);
            }
        } catch (IOException ex) {
            System.err.println("MANEJADOR|ERROR|" + ex.getMessage());
        } finally {
            try { socket.close(); } catch (IOException e) { /* ignorar */ }
            System.out.println("MANEJADOR|CERRADO|" + socket.getRemoteSocketAddress());
        }
    }
}
