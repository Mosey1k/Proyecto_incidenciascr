package Cliente;

import java.net.Socket;
import java.io.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import util.Protocolo;
import java.util.ArrayList;
import java.util.List;



public class ClientePrincipal extends JFrame {
    private static final String HOST = "localhost";
    private static final int PUERTO = 5000;

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    
    private JTextField txtUsername;
    private JButton btnLogin;
    private JLabel lblUsuario;

    private JComboBox<String> cboParadas; 
    private JTextField txtTipo, txtDescripcion;
    private JButton btnReportar;

    private JTextField txtLimite;
    private JButton btnConsultar;

    private JTextArea txtAreaResultados;

    private JTextField txtIncidenciaId;
    private JComboBox<String> cboEstado;
    private JButton btnActualizarEstado;

   
    private int usuarioIdLogueado = -1;

    public ClientePrincipal() {
        initUI();
        conectarServidorEnBackground();
    }

    private void initUI() {
        setTitle("Cliente de Incidencias CR");
        setSize(800, 520);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8,8));

        
        JPanel panelLogin = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelLogin.setBorder(BorderFactory.createTitledBorder("Login"));
        txtUsername = new JTextField(12);
        btnLogin = new JButton("Iniciar sesión");
        lblUsuario = new JLabel("No logueado");
        panelLogin.add(new JLabel("Usuario:"));
        panelLogin.add(txtUsername);
        panelLogin.add(btnLogin);
        panelLogin.add(lblUsuario);

        
        JPanel panelReporte = new JPanel(new GridBagLayout());
        panelReporte.setBorder(BorderFactory.createTitledBorder("Reportar incidencia"));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4,4,4,4);
        g.fill = GridBagConstraints.HORIZONTAL;

        cboParadas = new JComboBox<>();
        txtTipo = new JTextField();
        txtDescripcion = new JTextField();
        btnReportar = new JButton("Enviar Reporte");
        btnReportar.setEnabled(false);

        g.gridx=0; g.gridy=0; panelReporte.add(new JLabel("Parada:"), g);
        g.gridx=1; g.gridy=0; g.weightx=1; panelReporte.add(cboParadas, g);
        g.gridx=0; g.gridy=1; panelReporte.add(new JLabel("Tipo:"), g);
        g.gridx=1; g.gridy=1; panelReporte.add(txtTipo, g);
        g.gridx=0; g.gridy=2; panelReporte.add(new JLabel("Descripción:"), g);
        g.gridx=1; g.gridy=2; panelReporte.add(txtDescripcion, g);
        g.gridx=0; g.gridy=3; g.gridwidth=2; panelReporte.add(btnReportar, g);

        
        JPanel panelConsulta = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelConsulta.setBorder(BorderFactory.createTitledBorder("Consultar por parada"));
        txtLimite = new JTextField("5", 4);
        btnConsultar = new JButton("Consultar");
        btnConsultar.setEnabled(false);
        panelConsulta.add(new JLabel("Límite:"));
        panelConsulta.add(txtLimite);
        panelConsulta.add(btnConsultar);

        
        JPanel panelActualizar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelActualizar.setBorder(BorderFactory.createTitledBorder("Actualizar estado"));
        txtIncidenciaId = new JTextField(6);
        cboEstado = new JComboBox<>(new String[]{"PENDING","RESOLVED","INVALID"});
        btnActualizarEstado = new JButton("Actualizar");
        btnActualizarEstado.setEnabled(false);
        panelActualizar.add(new JLabel("ID Incidencia:"));
        panelActualizar.add(txtIncidenciaId);
        panelActualizar.add(new JLabel("Estado:"));
        panelActualizar.add(cboEstado);
        panelActualizar.add(btnActualizarEstado);

       
        txtAreaResultados = new JTextArea();
        txtAreaResultados.setEditable(false);
        JScrollPane scroll = new JScrollPane(txtAreaResultados);

        
        JPanel left = new JPanel(new BorderLayout(6,6));
        left.add(panelLogin, BorderLayout.NORTH);
        left.add(panelReporte, BorderLayout.CENTER);
        left.add(panelConsulta, BorderLayout.SOUTH);

        JPanel rightTop = new JPanel(new BorderLayout(6,6));
        rightTop.add(panelActualizar, BorderLayout.NORTH);

        JPanel right = new JPanel(new BorderLayout(6,6));
        right.add(rightTop, BorderLayout.NORTH);
        right.add(scroll, BorderLayout.CENTER);

        add(left, BorderLayout.WEST);
        add(right, BorderLayout.CENTER);

        
        btnLogin.addActionListener(e -> ejecutarLogin());
        btnReportar.addActionListener(e -> ejecutarReportar());
        btnConsultar.addActionListener(e -> ejecutarConsultar());
        btnActualizarEstado.addActionListener(e -> ejecutarActualizarEstado());

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cerrarRecursos();
                dispose();
                System.exit(0);
            }
        });
    }

    private void conectarServidorEnBackground() {
        txtAreaResultados.append("Conectando a servidor...\n");
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() {
                try {
                    socket = new Socket(HOST, PUERTO);
                    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    out = new PrintWriter(socket.getOutputStream(), true);
                    return "OK";
                } catch (IOException ex) {
                    return "ERROR: " + ex.getMessage();
                }
            }
            @Override
            protected void done() {
                try {
                    String r = get();
                    if ("OK".equals(r)) {
                        txtAreaResultados.append("Conectado a " + HOST + ":" + PUERTO + "\n");
                       
                        cargarParadas();
                        btnLogin.setEnabled(true);
                        btnReportar.setEnabled(false); 
                        btnConsultar.setEnabled(true);
                        btnActualizarEstado.setEnabled(true);
                    } else {
                        txtAreaResultados.append("No se pudo conectar: " + r + "\n");
                    }
                } catch (Exception ex) {
                    txtAreaResultados.append("Error al conectar: " + ex.getMessage() + "\n");
                }
            }
        }.execute();
    }

    private void cargarParadas() {
        new SwingWorker<List<String>, Void>() {
            @Override
            protected List<String> doInBackground() {
                try {
                    out.println(Protocolo.formarMensajeListStops());
                    String resp = in.readLine();
                    List<String> items = new ArrayList<>();
                    if (resp == null || resp.trim().isEmpty() || resp.equals("[]")) return items;
                    String[] partes = resp.split(" ;; ");
                    for (String p : partes) {
                       
                        String[] f = p.split("\\|");
                        if (f.length >= 2) {
                            items.add(f[0] + " - " + f[1]);
                        }
                    }
                    return items;
                } catch (IOException ex) {
                    txtAreaResultados.append("Error al cargar paradas: " + ex.getMessage() + "\n");
                    return new ArrayList<>();
                }
            }
            @Override
            protected void done() {
                try {
                    List<String> items = get();
                    cboParadas.removeAllItems();
                    for (String it : items) cboParadas.addItem(it);
                    txtAreaResultados.append("Paradas cargadas: " + items.size() + "\n");
                } catch (Exception ex) {
                    txtAreaResultados.append("Error al poblar combo: " + ex.getMessage() + "\n");
                }
            }
        }.execute();
    }

    private void ejecutarLogin() {
        String usuario = txtUsername.getText().trim();
        if (usuario.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingresa nombre de usuario.", "Login", JOptionPane.WARNING_MESSAGE);
            return;
        }
        btnLogin.setEnabled(false);
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() {
                out.println(Protocolo.formarMensajeLogin(usuario));
                try { return in.readLine(); } catch (IOException ex) { return "ERROR|" + ex.getMessage(); }
            }
            @Override
            protected void done() {
                try {
                    String r = get();
                    if (r != null && r.startsWith("OK|USERID|")) {
                        String[] parts = r.split("\\|");
                        usuarioIdLogueado = Integer.parseInt(parts[2]);
                        lblUsuario.setText("Usuario: " + usuario + " (id:" + usuarioIdLogueado + ")");
                        btnReportar.setEnabled(true);
                        txtAreaResultados.append("[LOGIN] OK id=" + usuarioIdLogueado + "\n");
                    } else {
                        txtAreaResultados.append("[LOGIN] " + r + "\n");
                        JOptionPane.showMessageDialog(ClientePrincipal.this, "Login falló: " + r, "Login", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    txtAreaResultados.append("[LOGIN] Error: " + ex.getMessage() + "\n");
                } finally {
                    btnLogin.setEnabled(true);
                }
            }
        }.execute();
    }

    private void ejecutarReportar() {
        if (usuarioIdLogueado <= 0) {
            JOptionPane.showMessageDialog(this, "Inicia sesión antes de reportar.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String paradaItem = (String) cboParadas.getSelectedItem();
        if (paradaItem == null) {
            JOptionPane.showMessageDialog(this, "Selecciona una parada.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String paradaId = paradaItem.split(" - ")[0];
        String tipo = txtTipo.getText().trim();
        String desc = txtDescripcion.getText().trim();
        if (tipo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Indica tipo de incidencia.", "Faltan datos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String msg = Protocolo.formarMensajeReport(String.valueOf(usuarioIdLogueado), paradaId, tipo, desc);
        btnReportar.setEnabled(false);
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() {
                out.println(msg);
                try { return in.readLine(); } catch (IOException ex) { return "ERROR|" + ex.getMessage(); }
            }
            @Override
            protected void done() {
                try {
                    String r = get();
                    txtAreaResultados.append("[REPORTE] " + r + "\n");
                } catch (Exception ex) {
                    txtAreaResultados.append("[REPORTE] Error: " + ex.getMessage() + "\n");
                } finally { btnReportar.setEnabled(true); }
            }
        }.execute();
    }

    private void ejecutarConsultar() {
   
    String paradaItem = null;
    try {
        paradaItem = (String) cboParadas.getSelectedItem();
    } catch (Exception ex) {
        txtAreaResultados.append("[CONSULTA] Error: componente cboParadas no encontrado o tipo incorrecto\n");
        return;
    }
    if (paradaItem == null || paradaItem.trim().isEmpty()) {
        javax.swing.JOptionPane.showMessageDialog(this, "Selecciona una parada para consultar.", "Error", javax.swing.JOptionPane.WARNING_MESSAGE);
        return;
    }

    
    String[] partesPar = paradaItem.split(" - ", 2);
    final String paradaIdFinal = partesPar.length > 0 ? partesPar[0].trim() : paradaItem.trim();

    
    String limTxt = txtLimite.getText().trim();
    int lim = 5;
    try {
        lim = Integer.parseInt(limTxt);
        if (lim <= 0) throw new NumberFormatException();
    } catch (NumberFormatException ex) {
        javax.swing.JOptionPane.showMessageDialog(this, "El límite debe ser un número entero positivo. Usando 5.", "Límite inválido", javax.swing.JOptionPane.WARNING_MESSAGE);
        lim = 5;
        txtLimite.setText("5");
    }
    final int limiteFinal = lim;

    final String msg = util.Protocolo.formarMensajeGetRecent(paradaIdFinal, String.valueOf(limiteFinal));
    btnConsultar.setEnabled(false);

    new javax.swing.SwingWorker<String, Void>() {
        @Override
        protected String doInBackground() {
            try {
                out.println(msg);
                String respuesta = in.readLine();
                return respuesta != null ? respuesta : "ERROR|Sin respuesta del servidor";
            } catch (IOException ex) {
                return "ERROR|IOException: " + ex.getMessage();
            }
        }

        @Override
        protected void done() {
            try {
                String r = get();
                if (r == null) {
                    txtAreaResultados.append("[CONSULTA] Respuesta nula\n");
                    return;
                }
                if (r.startsWith("ERROR|")) {
                    txtAreaResultados.append("[CONSULTA] " + r + "\n");
                    return;
                }
                if ("[]".equals(r) || r.trim().isEmpty()) {
                    txtAreaResultados.append("[CONSULTA] Sin incidencias.\n");
                    return;
                }

               
                String[] items = r.split(" ;; ");
                int n = Math.min(items.length, limiteFinal);

                String[] columns = new String[] {"ID","Usuario","Parada","Tipo","Descripción","Fecha","Estado"};
                Object[][] data = new Object[n][columns.length];

                for (int i = 0; i < n; i++) {
                    String it = items[i];
                    int start = it.indexOf('[');
                    int end = it.lastIndexOf(']');
                    String core = (start >= 0 && end > start) ? it.substring(start+1, end) : it;
                    String[] parts = core.split(", ");
                    
                    String id = "", user = "", stop = "", tipo = "", desc = "", fecha = "", estado = "";
                    for (String p : parts) {
                        if (!p.contains("=")) continue;
                        String[] kv = p.split("=", 2);
                        String key = kv[0].trim();
                        String val = kv.length > 1 ? kv[1].trim() : "";
                        switch (key) {
                            case "id": id = val; break;
                            case "usuario": user = val; break;
                            case "parada": stop = val; break;
                            case "tipo": tipo = val; break;
                            case "descripcion": desc = val; break;
                            case "fecha": fecha = val; break;
                            case "estado": estado = val; break;
                            default: break;
                        }
                    }
                    data[i][0] = id;
                    data[i][1] = user;
                    data[i][2] = stop;
                    data[i][3] = tipo;
                    data[i][4] = desc;
                    data[i][5] = fecha;
                    data[i][6] = estado;
                }

                javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(data, columns) {
                    @Override public boolean isCellEditable(int row, int col) { return false; }
                };
                javax.swing.JTable table = new javax.swing.JTable(model);
                table.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
                javax.swing.JScrollPane sp = new javax.swing.JScrollPane(table);
                sp.setPreferredSize(new java.awt.Dimension(760, Math.min(240, 30 + n * 26)));

                javax.swing.JOptionPane.showMessageDialog(ClientePrincipal.this, sp, "Incidencias (parada " + paradaIdFinal + ")", javax.swing.JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception ex) {
                txtAreaResultados.append("[CONSULTA] Error al procesar respuesta: " + ex.getMessage() + "\n");
            } finally {
                btnConsultar.setEnabled(true);
            }
        }
    }.execute();
}


    private void ejecutarActualizarEstado() {
        String incId = txtIncidenciaId.getText().trim();
        String estado = (String) cboEstado.getSelectedItem();
        if (incId.isEmpty()) { JOptionPane.showMessageDialog(this, "Ingresa ID de incidencia.", "Error", JOptionPane.WARNING_MESSAGE); return; }
        String msg = Protocolo.formarMensajeUpdateStatus(incId, estado);
        btnActualizarEstado.setEnabled(false);
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() {
                out.println(msg);
                try { return in.readLine(); } catch (IOException ex) { return "ERROR|" + ex.getMessage(); }
            }
            @Override
            protected void done() {
                try {
                    String r = get();
                    txtAreaResultados.append("[UPDATE] " + r + "\n");
                } catch (Exception ex) {
                    txtAreaResultados.append("[UPDATE] Error: " + ex.getMessage() + "\n");
                } finally { btnActualizarEstado.setEnabled(true); }
            }
        }.execute();
    }

    private void cerrarRecursos() {
        try { if (out != null) out.println("DISCONNECT|bye"); } catch (Exception ignored) {}
        try { if (in != null) in.close(); } catch (IOException ignored) {}
        try { if (out != null) out.close(); } catch (Exception ignored) {}
        try { if (socket != null && !socket.isClosed()) socket.close(); } catch (IOException ignored) {}
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ClientePrincipal c = new ClientePrincipal();
            c.setVisible(true);
        });
    }
}
