package com.mycompany.inicio;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.sql.SQLException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

/** Solicitudes por decidir, con botones Aprobar / Rechazar / Observar. */
public class PanelDecisiones extends JPanel {

    private static final String SQL_POR_DECIDIR =
            "SELECT s.id_solicitud AS Solicitud, c.ci AS CI, "
          + "CONCAT(c.nombres, ' ', c.apellidos) AS Cliente, "
          + "c.ingresos_mensuales AS Ingresos, s.destino AS Destino, "
          + "s.monto_solicitado AS Monto, s.plazo_meses AS Plazo, s.estado AS Estado, (SELECT COALESCE(e.resultado,'PENDIENTE') FROM evaluacion_credito e WHERE e.id_solicitud=s.id_solicitud AND e.vigente=1 ORDER BY e.numero_version DESC LIMIT 1) AS Evaluacion "
          + "FROM solicitud_credito s "
          + "JOIN cliente c ON c.id_cliente = s.id_cliente "
          + "WHERE s.estado IN ('PENDIENTE','EN_EVALUACION') "
          + "ORDER BY s.fecha_solicitud, s.id_solicitud";

    private final String jefeLogin;
    private final DecisionDAO dao = new DecisionDAO();
    private final JTable tabla = new JTable();

    public PanelDecisiones(String jefeLogin) {
        this.jefeLogin = jefeLogin;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel titulo = new JLabel("Solicitudes por decidir");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 22f));
        add(titulo, BorderLayout.NORTH);

        tabla.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel abajo = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JButton btnAprobar = new JButton("Aprobar");
        JButton btnRechazar = new JButton("Rechazar");
        JButton btnObservar = new JButton("Observar");
        JButton btnActualizar = new JButton("Actualizar");
        abajo.add(btnAprobar);
        abajo.add(btnRechazar);
        abajo.add(btnObservar);
        JButton evaluacion=new JButton("Ver evaluacion del analista");
        evaluacion.addActionListener(e->{try{HistorialEvaluacionUI.mostrar(this,InterfazFormulario.id(tabla));}catch(Exception ex){InterfazFormulario.error(this,ex);}});
        abajo.add(evaluacion);
        abajo.add(btnActualizar);
        add(abajo, BorderLayout.SOUTH);

        btnAprobar.addActionListener(e -> decidir("APROBADA", false));
        btnRechazar.addActionListener(e -> decidir("RECHAZADA", true));
        btnObservar.addActionListener(e -> decidir("OBSERVADA", true));
        btnActualizar.addActionListener(e -> cargar());

        cargar();
    }

    public final void cargar() {
        try {
            TablaUtil.cargarAutorizada(tabla, "CREDITO_DECIDIR", SQL_POR_DECIDIR);
        } catch (SQLException | IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo cargar:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void decidir(String nuevoEstado, boolean motivoObligatorio) {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona una solicitud de la tabla.");
            return;
        }
        int m = tabla.convertRowIndexToModel(fila);
        int idSolicitud = Integer.parseInt(tabla.getModel().getValueAt(m, 0).toString());

        String motivo = JOptionPane.showInputDialog(this,
                "Motivo de la decision" + (motivoObligatorio ? "" : " (opcional)") + ":",
                nuevoEstado + " - solicitud " + idSolicitud, JOptionPane.QUESTION_MESSAGE);
        if (motivo == null) {
            return;                                  // cancelo
        }
        motivo = motivo.trim();
        if (motivoObligatorio && motivo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Escribe el motivo.");
            return;
        }
        if (motivo.length() > 300) {
            motivo = motivo.substring(0, 300);       // la columna admite 300 caracteres
        }

        try {
            dao.decidir(idSolicitud, jefeLogin, nuevoEstado, motivo.isEmpty() ? null : motivo);
            JOptionPane.showMessageDialog(this, "Solicitud " + idSolicitud + ": " + nuevoEstado);
            cargar();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "No se pudo decidir",
                    JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}