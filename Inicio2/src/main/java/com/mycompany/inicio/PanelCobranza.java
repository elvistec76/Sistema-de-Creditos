
package com.mycompany.inicio;
 
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
 
public class PanelCobranza extends JPanel {
 
    private static final String[] COLUMNAS = {
        "Cronograma", "Cuota", "Credito", "Vence", "Monto", "Saldo", "Dias atraso", "Estado", "Estado credito"
    };
    // Indices en el MODELO (la columna 0 se oculta en la tabla)
    private static final int COL_CRONOGRAMA = 0;
    private static final int COL_CUOTA = 1;
    private static final int COL_CREDITO = 2;
    private static final int COL_SALDO = 5;
 
    private final CobranzaDAO dao = new CobranzaDAO();
 
    private final JTextField txtCi = new JTextField(12);
    private final JButton btnBuscar = new JButton("Buscar");
    private final JLabel lblCliente = new JLabel("Ingrese el CI del cliente y pulse Buscar");
    private final JLabel lblAlerta = new JLabel("Busca un cliente para consultar sus prestamos y cuotas.");
    private final JCheckBox incluirPagadas = new JCheckBox("Incluir cuotas pagadas");
    private final JButton btnActualizar = new JButton("Actualizar");
    private final JButton btnAvisar = new JButton("Avisar al cliente");
    private final JTable prestamos = new JTable();
    private final JTable pagos = new JTable();
 
    private final DefaultTableModel modelo = new DefaultTableModel(COLUMNAS, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modelo);
 
    private final JLabel lblSeleccion = new JLabel("Seleccione una cuota de la tabla");
    private final JTextField txtMonto = new JTextField(10);
    private final JButton btnSaldo = new JButton("Saldo completo");
    private final JComboBox<String> cmbMetodo =
            new JComboBox<>(new String[]{"EFECTIVO", "TRANSFERENCIA", "QR"});
    private final JButton btnCobrar = new JButton("Cobrar");
 
    private String ciActual;   // CI del cliente cuya lista se esta mostrando
    private final String cajeroLogin;
 
    public PanelCobranza() {
        this(null);
    }

    public PanelCobranza(String cajeroLogin) {
        this.cajeroLogin = cajeroLogin;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
 
        // --- Arriba: busqueda ---
        JPanel arriba = new JPanel(new BorderLayout(5, 5));
        JPanel fila = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        fila.add(new JLabel("CI del cliente:"));
        fila.add(txtCi);
        fila.add(btnBuscar);
        fila.add(btnActualizar);
        fila.add(incluirPagadas);
        arriba.add(fila, BorderLayout.NORTH);
        JPanel informacion = new JPanel(new java.awt.GridLayout(2,1,0,5));
        informacion.add(lblCliente);
        lblAlerta.setOpaque(true);
        lblAlerta.setBorder(BorderFactory.createEmptyBorder(7,8,7,8));
        informacion.add(lblAlerta);
        arriba.add(informacion, BorderLayout.SOUTH);
        add(arriba, BorderLayout.NORTH);
 
        // --- Centro: tabla de cuotas pendientes ---
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setRowHeight(24);
        tabla.removeColumn(tabla.getColumnModel().getColumn(COL_CRONOGRAMA)); // oculta id interno
        JTabbedPane secciones = new JTabbedPane();
        secciones.addTab("Cuotas del cliente",new JScrollPane(tabla));
        secciones.addTab("Prestamos del cliente",new JScrollPane(prestamos));
        secciones.addTab("Pagos del cliente",new JScrollPane(pagos));
        add(secciones, BorderLayout.CENTER);
 
        // --- Abajo: cobro ---
        JPanel abajo = new JPanel(new BorderLayout(5, 5));
        abajo.add(lblSeleccion, BorderLayout.NORTH);
        JPanel filaCobro = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filaCobro.add(new JLabel("Monto a cobrar:"));
        filaCobro.add(txtMonto);
        filaCobro.add(btnSaldo);
        filaCobro.add(new JLabel("Metodo:"));
        filaCobro.add(cmbMetodo);
        filaCobro.add(btnCobrar);
        abajo.add(filaCobro, BorderLayout.CENTER);
        JPanel avisos = new JPanel(new FlowLayout(FlowLayout.LEFT,8,4));
        avisos.add(btnAvisar);
        avisos.add(new JLabel("Aviso interno: el cliente lo vera al ingresar o actualizar su ventana."));
        abajo.add(avisos,BorderLayout.SOUTH);
        add(abajo, BorderLayout.SOUTH);
 
        setCobroHabilitado(false);
 
        // --- Eventos ---
        btnBuscar.addActionListener(e -> buscar());
        btnActualizar.addActionListener(e -> recargar());
        incluirPagadas.addActionListener(e -> recargar());
        btnAvisar.addActionListener(e -> avisarCliente());
        txtCi.addActionListener(e -> buscar());          // Enter tambien busca
        btnSaldo.addActionListener(e -> usarSaldoCompleto());
        btnCobrar.addActionListener(e -> cobrar());
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                actualizarSeleccion();
            }
        });
    }
 
    private void buscar() {
        String ci = txtCi.getText().trim();
        if (ci.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingrese el CI del cliente.");
            return;
        }
        ciActual = null;
        modelo.setRowCount(0);
        prestamos.setModel(new DefaultTableModel());
        pagos.setModel(new DefaultTableModel());
        lblAlerta.setText("Buscando prestamos y cuotas...");
        lblCliente.setText("Buscando cliente...");
        actualizarSeleccion();
        try {
            Cliente c = new ClienteDAO().buscarPorCi(ci);
            if (c == null) {
                ciActual = null;
                modelo.setRowCount(0);
                lblCliente.setText("No existe un cliente con CI " + ci);
                lblAlerta.setText("Revisa el CI ingresado para consultar prestamos y cuotas.");
                actualizarSeleccion();
                return;
            }
            ciActual = ci;
            lblCliente.setText(c.getNombreCompleto() + "  (CI " + c.getCi() + ")");
            recargar();
        } catch (SQLException | IllegalArgumentException ex) {
            lblAlerta.setText("No se pudieron consultar los datos del cliente.");
            error("No se pudo buscar al cliente", ex);
        }
    }
 
    private void recargar() {
        if (ciActual == null) {
            return;
        }
        modelo.setRowCount(0);
        actualizarSeleccion();
        try {
            TablaUtil.cargarAutorizada(prestamos,"CRONOGRAMA_CONSULTAR",CobranzaDAO.SQL_PRESTAMOS,ciActual);
            TablaUtil.cargarAutorizada(pagos,"PAGO_REGISTRAR",CobranzaDAO.SQL_PAGOS_CLIENTE,ciActual);
            List<Object[]> filas = dao.listarCuotasCliente(ciActual,incluirPagadas.isSelected());
            modelo.setRowCount(0);
            int pendientes=0,vencidas=0;
            BigDecimal saldoVencido=BigDecimal.ZERO;
            for (Object[] f : filas) {
                modelo.addRow(f);
                if (((BigDecimal)f[5]).signum()>0 && ("VIGENTE".equals(f[8])||"EN_MORA".equals(f[8]))) {
                    pendientes++;
                    if (((Number)f[6]).intValue()>0) {vencidas++;saldoVencido=saldoVencido.add((BigDecimal)f[5]);}
                }
            }
            lblAlerta.setBackground(vencidas>0?new java.awt.Color(255,231,210):new java.awt.Color(225,242,230));
            lblAlerta.setText(vencidas>0?"ATENCION: "+vencidas+" cuota(s) vencida(s). Saldo vencido Bs "+saldoVencido+". Pendientes: "+pendientes
                    : "Sin cuotas vencidas cobrables. Cuotas pendientes: "+pendientes+". Consulta los prestamos en la segunda pestaña.");
            if (filas.isEmpty()) {
                lblSeleccion.setText("Este cliente no tiene cuotas pendientes.");
            } else {
                lblSeleccion.setText("Selecciona una cuota pendiente. Las cuotas pagadas son solo de consulta.");
                for(int f=0;f<modelo.getRowCount();f++) {
                    if(((BigDecimal)modelo.getValueAt(f,COL_SALDO)).signum()>0) {tabla.setRowSelectionInterval(f,f);break;}
                }
            }
            actualizarSeleccion();
        } catch (SQLException | IllegalArgumentException ex) {
            error("No se pudieron cargar las cuotas", ex);
        }
    }
 
    private void actualizarSeleccion() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            if (modelo.getRowCount() > 0) {
                lblSeleccion.setText("Seleccione una cuota de la tabla");
            }
            txtMonto.setText("");
            setCobroHabilitado(false);
            return;
        }
        int m = tabla.convertRowIndexToModel(fila);
        lblSeleccion.setText("Credito " + modelo.getValueAt(m, COL_CREDITO)
                + "  |  Cuota " + modelo.getValueAt(m, COL_CUOTA)
                + "  |  Saldo: " + modelo.getValueAt(m, COL_SALDO));
        txtMonto.setText(((BigDecimal) modelo.getValueAt(m, COL_SALDO)).toPlainString());
        String estadoCredito=String.valueOf(modelo.getValueAt(m,8));
        boolean cobrable=((BigDecimal)modelo.getValueAt(m,COL_SALDO)).signum()>0
                && ("VIGENTE".equals(estadoCredito)||"EN_MORA".equals(estadoCredito));
        setCobroHabilitado(cobrable);
        if(!cobrable)lblSeleccion.setText("Cuota de consulta: ya pagada o credito fuera de VIGENTE / EN_MORA.");
    }
 
    private void usarSaldoCompleto() {
        int fila = tabla.getSelectedRow();
        if (fila >= 0) {
            int m = tabla.convertRowIndexToModel(fila);
            txtMonto.setText(((BigDecimal) modelo.getValueAt(m, COL_SALDO)).toPlainString());
        }
    }
 
    private void cobrar() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione la cuota a cobrar.");
            return;
        }
 
        BigDecimal monto;
        try {
            monto = new BigDecimal(txtMonto.getText().trim().replace(',', '.'))
                    .setScale(2, RoundingMode.UNNECESSARY);
        } catch (NumberFormatException | ArithmeticException ex) {
            JOptionPane.showMessageDialog(this, "Ingrese un monto valido con hasta dos decimales.",
                    "Monto invalido", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (monto.signum() <= 0) {
            JOptionPane.showMessageDialog(this, "El monto debe ser mayor a 0.",
                    "Monto invalido", JOptionPane.WARNING_MESSAGE);
            return;
        }
 
        int m = tabla.convertRowIndexToModel(fila);
        BigDecimal saldo = (BigDecimal) modelo.getValueAt(m, COL_SALDO);
        if (monto.compareTo(saldo) > 0) {
            JOptionPane.showMessageDialog(this,
                    "El monto no puede superar el saldo de la cuota (" + saldo + ").",
                    "Monto invalido", JOptionPane.WARNING_MESSAGE);
            return;
        }
 
        int idCronograma = ((Number) modelo.getValueAt(m, COL_CRONOGRAMA)).intValue();
        int numeroCuota = ((Number) modelo.getValueAt(m, COL_CUOTA)).intValue();
        String metodo = (String) cmbMetodo.getSelectedItem();
 
        int ok = JOptionPane.showConfirmDialog(this,
                lblCliente.getText()+"\nCredito "+modelo.getValueAt(m,COL_CREDITO)
                        +"\nCobrar " + monto + " (" + metodo + ") de la cuota " + numeroCuota + "?",
                "Confirmar cobro", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) {
            return;
        }
 
        try {
            CobranzaDAO.ResultadoPago pago = dao.registrarPago(
                    idCronograma, numeroCuota, monto, metodo, cajeroLogin);
            BigDecimal restante = pago.saldoRestante();
            String msg = restante.signum() == 0
                    ? "Cuota pagada completamente."
                    : "Pago registrado. Saldo restante de la cuota: " + restante;
            msg += "\nComprobante: " + pago.comprobante()
                    + "\nCapital aplicado: " + pago.capital()
                    + "\nInteres aplicado: " + pago.interes()
                    + "\nRecibido por: " + cajeroLogin;
            JOptionPane.showMessageDialog(this, msg, "Cobro registrado",
                    JOptionPane.INFORMATION_MESSAGE);
            recargar();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "No se pudo cobrar", JOptionPane.WARNING_MESSAGE);
            recargar();   // el saldo pudo haber cambiado
        } catch (SQLException ex) {
            error("No se pudo registrar el pago", ex);
        }
    }
 
    private void setCobroHabilitado(boolean b) {
        txtMonto.setEnabled(b);
        btnSaldo.setEnabled(b);
        cmbMetodo.setEnabled(b);
        btnCobrar.setEnabled(b);
        btnAvisar.setEnabled(b);
    }

    private void avisarCliente() {
        int fila=tabla.getSelectedRow();
        if(fila<0) {JOptionPane.showMessageDialog(this,"Selecciona una cuota pendiente.");return;}
        if(JOptionPane.showConfirmDialog(this,"Registrar un recordatorio interno de esta cuota para el cliente?",
                "Avisar al cliente",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;
        int m=tabla.convertRowIndexToModel(fila);
        try {
            var aviso=new AvisosClienteDAO().recordarCuota(((Number)modelo.getValueAt(m,COL_CRONOGRAMA)).intValue(),
                    ((Number)modelo.getValueAt(m,COL_CUOTA)).intValue(),cajeroLogin);
            JOptionPane.showMessageDialog(this,aviso.creado()?"Aviso registrado. El cliente lo vera en Mis avisos al ingresar o actualizar."
                    :"Ya se registro hoy un recordatorio con estos mismos datos.");
        }catch(SQLException | IllegalArgumentException ex){JOptionPane.showMessageDialog(this,ex.getMessage(),"No se pudo registrar el aviso",JOptionPane.WARNING_MESSAGE);}
    }
 
    private void error(String titulo, Exception ex) {
        JOptionPane.showMessageDialog(this, titulo + ":\n" + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
    }
}

