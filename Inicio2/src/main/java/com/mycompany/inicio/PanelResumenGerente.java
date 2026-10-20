package com.mycompany.inicio;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/** Tarjetas con los indicadores principales de la cartera. */
public class PanelResumenGerente extends JPanel {

    // {titulo de la tarjeta, consulta que devuelve UN valor}
    private static final String[][] INDICADORES = {
        {"Clientes registrados",
         "SELECT COUNT(*) FROM cliente"},
        {"Solicitudes en tramite",
         "SELECT COUNT(*) FROM solicitud_credito "
       + "WHERE estado IN ('REGISTRADA','PENDIENTE','OBSERVADA','EN_EVALUACION')"},
        {"Creditos otorgados",
         "SELECT COUNT(*) FROM credito WHERE estado IN ('VIGENTE','EN_MORA','CANCELADO')"},
        {"Saldo de capital (cartera)",
         "SELECT COALESCE(SUM(saldo_capital), 0) FROM credito WHERE estado IN ('VIGENTE','EN_MORA')"},
        {"Cuotas vencidas",
         "SELECT COUNT(*) FROM cuota q JOIN cronograma_pago cp USING(id_cronograma) JOIN credito cr USING(id_credito) WHERE cp.vigente=1 AND cr.estado IN ('VIGENTE','EN_MORA') AND q.saldo>0 AND q.fecha_vencimiento<CURDATE()"},
        {"Monto vencido",
         "SELECT COALESCE(SUM(q.saldo),0) FROM cuota q JOIN cronograma_pago cp USING(id_cronograma) JOIN credito cr USING(id_credito) WHERE cp.vigente=1 AND cr.estado IN ('VIGENTE','EN_MORA') AND q.saldo>0 AND q.fecha_vencimiento<CURDATE()"}
    };

    private final JLabel[] valores = new JLabel[INDICADORES.length];
    private final JLabel corte = new JLabel();
    private final javax.swing.JTable estados = new javax.swing.JTable();
    private final JLabel ingresos = new JLabel("-"), egresos = new JLabel("-");
    private static final String SQL_ESTADOS="SELECT estado AS Estado,COUNT(*) AS Solicitudes,COALESCE(SUM(monto_solicitado),0) AS `Monto solicitado Bs` FROM solicitud_credito GROUP BY estado ORDER BY estado";

    public PanelResumenGerente(String gerente) {
        setLayout(new BorderLayout(12,12));setBorder(BorderFactory.createEmptyBorder(18,18,18,18));
        JPanel cabecera=InterfazFormulario.encabezado("Resumen de gestion","Vista general de clientes, solicitudes y cartera. Sesion: "+gerente);
        cabecera.add(corte,BorderLayout.SOUTH);add(cabecera,BorderLayout.NORTH);
        JPanel tarjetas=new JPanel(new GridLayout(2,3,14,14));
        String[] ayuda={"Clientes registrados en el sistema","Registradas, observadas o en evaluacion","Desembolsados, incluidos los cancelados","Capital pendiente de creditos activos","Cuotas con saldo y fecha anterior a hoy","Saldo de cuotas vencidas; incluye interes"};
        for(int i=0;i<INDICADORES.length;i++){
            JPanel t=new JPanel(new BorderLayout(8,10));
            t.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(),BorderFactory.createEmptyBorder(14,14,14,14)));
            JLabel nombre=new JLabel(INDICADORES[i][0]);nombre.setFont(nombre.getFont().deriveFont(Font.BOLD,14f));t.add(nombre,BorderLayout.NORTH);
            valores[i]=new JLabel("-");valores[i].setFont(valores[i].getFont().deriveFont(Font.BOLD,28f));t.add(valores[i],BorderLayout.CENTER);
            t.add(new JLabel("<html>"+ayuda[i]+"</html>"),BorderLayout.SOUTH);tarjetas.add(t);
        }
        tarjetas.setPreferredSize(new java.awt.Dimension(100,290));
        JPanel datos=new JPanel(new GridLayout(1,2,14,0));
        JPanel solicitudes=new JPanel(new BorderLayout(4,8));solicitudes.setBorder(BorderFactory.createTitledBorder("Solicitudes por estado"));
        estados.setRowHeight(25);solicitudes.add(new javax.swing.JScrollPane(estados));datos.add(solicitudes);
        JPanel caja=new JPanel(new java.awt.GridBagLayout());caja.setBorder(BorderFactory.createTitledBorder("Movimientos de caja de hoy"));
        InterfazFormulario.campo(caja,0,"Ingresos por pagos:",ingresos);InterfazFormulario.campo(caja,1,"Egresos por desembolsos:",egresos);
        InterfazFormulario.campo(caja,2,"Criterio:",new JLabel("<html>Movimientos registrados en el sistema.<br>Consulta los reportes para ver cada operacion.</html>"));datos.add(caja);
        JPanel contenido=new JPanel(new BorderLayout(12,14));contenido.add(tarjetas,BorderLayout.NORTH);contenido.add(datos,BorderLayout.CENTER);
        javax.swing.JScrollPane scroll=new javax.swing.JScrollPane(contenido);scroll.setBorder(null);contenido.setPreferredSize(new java.awt.Dimension(950,520));add(scroll,BorderLayout.CENTER);
        JPanel abajo=new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));JButton actualizar=new JButton("Actualizar resumen");actualizar.addActionListener(e->cargar());abajo.add(actualizar);add(abajo,BorderLayout.SOUTH);
        cargar();
    }
    private static String moneda(java.math.BigDecimal n){java.text.NumberFormat f=java.text.NumberFormat.getNumberInstance(java.util.Locale.forLanguageTag("es-BO"));f.setMinimumFractionDigits(2);f.setMaximumFractionDigits(2);return "Bs "+f.format(n);}
    public final void cargar(){
        try(Connection con=Conexion.getConexion()){
            con.setAutoCommit(false);con.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
            try{
                Seguridad.exigir(con,Sesion.login(),"REPORTE_GENERAR");
                java.math.BigDecimal[] datos=new java.math.BigDecimal[INDICADORES.length];
                for(int i=0;i<INDICADORES.length;i++)try(PreparedStatement p=con.prepareStatement(INDICADORES[i][1]);ResultSet r=p.executeQuery()){
                    datos[i]=r.next()?r.getBigDecimal(1):java.math.BigDecimal.ZERO;
                }
                String fecha;try(PreparedStatement p=con.prepareStatement("SELECT NOW()");ResultSet r=p.executeQuery()){r.next();fecha=r.getString(1);}
                java.math.BigDecimal in=java.math.BigDecimal.ZERO,out=java.math.BigDecimal.ZERO;
                try(PreparedStatement p=con.prepareStatement("SELECT tipo,COALESCE(SUM(monto),0) FROM movimiento_financiero WHERE DATE(fecha_hora)=CURDATE() GROUP BY tipo");ResultSet r=p.executeQuery()){
                    while(r.next()){if("INGRESO".equals(r.getString(1)))in=r.getBigDecimal(2);if("EGRESO".equals(r.getString(1)))out=r.getBigDecimal(2);}
                }
                javax.swing.table.DefaultTableModel modelo=new javax.swing.table.DefaultTableModel(new String[]{"Estado","Solicitudes","Monto solicitado Bs"},0){@Override public boolean isCellEditable(int r,int c){return false;}};
                try(PreparedStatement p=con.prepareStatement(SQL_ESTADOS);ResultSet r=p.executeQuery()){while(r.next())modelo.addRow(new Object[]{r.getString(1),r.getInt(2),moneda(r.getBigDecimal(3))});}
                con.commit();
                for(int i=0;i<datos.length;i++)valores[i].setText(i==3||i==5?moneda(datos[i]):datos[i].toBigInteger().toString());
                ingresos.setText(moneda(in));egresos.setText(moneda(out));estados.setModel(modelo);corte.setText("Actualizado: "+fecha+"  |  Moneda: bolivianos (Bs)");
            }catch(SQLException|RuntimeException e){con.rollback();throw e;}
        }catch(SQLException|IllegalArgumentException e){InterfazFormulario.error(this,e);}
    }
}
