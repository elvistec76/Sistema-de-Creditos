package com.mycompany.inicio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Pagos compatibles con pago, aplicacion_pago y movimiento_financiero. */
public class CobranzaDAO {
    public static final String SQL_PRESTAMOS =
            "SELECT cr.numero_credito AS Credito,cr.monto_aprobado AS `Monto aprobado`,"
          + "cr.plazo_meses AS `Plazo meses`,cr.saldo_capital AS `Saldo capital`,cr.estado AS Estado,"
          + "CASE WHEN COUNT(q.id_cuota)=0 THEN 'Sin cuotas generadas' ELSE 'Disponible' END AS Cronograma,"
          + "COUNT(q.id_cuota) AS `Cuotas totales`,COALESCE(SUM(q.saldo>0),0) AS Pendientes,"
          + "COALESCE(SUM(q.saldo),0) AS `Saldo cuotas` "
          + "FROM credito cr JOIN solicitud_credito s ON s.id_solicitud=cr.id_solicitud "
          + "JOIN cliente c ON c.id_cliente=s.id_cliente "
          + "LEFT JOIN cronograma_pago cp ON cp.id_credito=cr.id_credito AND cp.vigente=1 "
          + "LEFT JOIN cuota q ON q.id_cronograma=cp.id_cronograma WHERE c.ci=? "
          + "GROUP BY cr.id_credito,cr.numero_credito,cr.monto_aprobado,cr.plazo_meses,cr.saldo_capital,cr.estado ORDER BY cr.id_credito DESC";
    public static final String SQL_PAGOS_CLIENTE =
            "SELECT p.numero_comprobante AS Comprobante,p.fecha_pago AS Fecha,cr.numero_credito AS Credito,"
          + "p.monto AS Monto,p.forma_pago AS Metodo,p.estado AS Estado,u.login AS Cajero "
          + "FROM pago p JOIN credito cr ON cr.id_credito=p.id_credito "
          + "JOIN solicitud_credito s ON s.id_solicitud=cr.id_solicitud JOIN cliente c ON c.id_cliente=s.id_cliente "
          + "JOIN usuario u ON u.id_usuario=p.id_usuario WHERE c.ci=? ORDER BY p.id_pago DESC";
    private static final String SQL_CUOTAS_CLIENTE =
            "SELECT q.id_cronograma,q.numero,cr.numero_credito,q.fecha_vencimiento,q.monto,q.saldo,"
          + "CASE WHEN q.saldo>0 THEN GREATEST(DATEDIFF(CURDATE(),q.fecha_vencimiento),0) ELSE 0 END,q.estado,cr.estado "
          + "FROM cuota q JOIN cronograma_pago cp ON cp.id_cronograma=q.id_cronograma "
          + "JOIN credito cr ON cr.id_credito=cp.id_credito "
          + "JOIN solicitud_credito s ON s.id_solicitud=cr.id_solicitud JOIN cliente c ON c.id_cliente=s.id_cliente "
          + "WHERE c.ci=? AND cp.vigente=1 AND (?=1 OR q.saldo>0) ORDER BY q.fecha_vencimiento,q.numero";
    private static final Set<String> METODOS = Set.of("EFECTIVO", "TRANSFERENCIA", "QR");
    private static final String SQL_PENDIENTES =
            "SELECT q.id_cronograma, q.numero, cr.numero_credito, q.fecha_vencimiento, "
          + "q.monto, q.saldo, GREATEST(DATEDIFF(CURDATE(), q.fecha_vencimiento), 0), q.estado "
          + "FROM cuota q JOIN cronograma_pago cp ON cp.id_cronograma=q.id_cronograma "
          + "JOIN credito cr ON cr.id_credito=cp.id_credito "
          + "JOIN solicitud_credito s ON s.id_solicitud=cr.id_solicitud "
          + "JOIN cliente cl ON cl.id_cliente=s.id_cliente "
          + "WHERE cl.ci=? AND cp.vigente=1 AND q.saldo>0 "
          + "AND cr.estado IN ('VIGENTE','EN_MORA') ORDER BY q.fecha_vencimiento,q.numero";

    public record ResultadoPago(int idPago, String comprobante, BigDecimal saldoRestante,
                                BigDecimal capital, BigDecimal interes) {}

    public List<Object[]> listarCuotasPendientes(String ci) throws SQLException {
        Seguridad.comprobar("CRONOGRAMA_CONSULTAR");
        List<Object[]> filas = new ArrayList<>();
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(SQL_PENDIENTES)) {
            ps.setString(1, ci);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    filas.add(new Object[]{rs.getInt(1), rs.getInt(2), rs.getString(3),
                        rs.getDate(4), rs.getBigDecimal(5), rs.getBigDecimal(6),
                        rs.getInt(7), rs.getString(8)});
                }
            }
        }
        return filas;
    }

    public List<Object[]> listarCuotasCliente(String ci,boolean incluirPagadas) throws SQLException {
        Seguridad.comprobar("CRONOGRAMA_CONSULTAR");
        List<Object[]> filas=new ArrayList<>();
        try(Connection con=Conexion.getConexion();PreparedStatement ps=con.prepareStatement(SQL_CUOTAS_CLIENTE)) {
            ps.setString(1,ci);ps.setBoolean(2,incluirPagadas);
            try(ResultSet rs=ps.executeQuery()) {
                while(rs.next())filas.add(new Object[]{rs.getInt(1),rs.getInt(2),rs.getString(3),rs.getDate(4),rs.getBigDecimal(5),rs.getBigDecimal(6),rs.getInt(7),rs.getString(8),rs.getString(9)});
            }
        }
        return filas;
    }

    /** Aplica primero el interes pendiente y luego el capital de la cuota elegida. */
    public ResultadoPago registrarPago(int idCronograma, int numeroCuota,
            BigDecimal monto, String metodo, String cajeroLogin) throws SQLException {
        if (monto == null || monto.signum() <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor a cero.");
        }
        try {
            monto = monto.setScale(2, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("El monto admite hasta dos decimales.");
        }
        if (monto.precision() > 14 || metodo == null || !METODOS.contains(metodo)) {
            throw new IllegalArgumentException("Monto o metodo de pago no valido.");
        }
        try (Connection con = Conexion.getConexion()) {
            // Tras esperar el bloqueo del credito, leer los pagos ya confirmados por otra caja.
            con.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            con.setAutoCommit(false);
            try {
                int idUsuario = UsuarioDAO.exigirRol(con, cajeroLogin, "CAJERO");
                Seguridad.exigir(con, cajeroLogin, "PAGO_REGISTRAR");
                int idCredito;
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT id_credito FROM cronograma_pago WHERE id_cronograma=?")) {
                    ps.setInt(1, idCronograma);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new IllegalArgumentException("El cronograma no existe.");
                        idCredito = rs.getInt(1);
                    }
                }
                // Todos los cobros de un credito toman primero este bloqueo.
                BloqueoCliente.credito(con,idCredito);
                BigDecimal saldoCapital;
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT saldo_capital, estado FROM credito WHERE id_credito=? FOR UPDATE")) {
                    ps.setInt(1, idCredito);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new IllegalArgumentException("El credito no existe.");
                        String estado = rs.getString(2);
                        if (!"VIGENTE".equals(estado) && !"EN_MORA".equals(estado)) {
                            throw new IllegalArgumentException("Solo se cobran creditos vigentes o en mora.");
                        }
                        saldoCapital = rs.getBigDecimal(1);
                    }
                }
                int idCuota;
                BigDecimal saldo, capitalPendiente, interesPendiente;
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT q.id_cuota,q.saldo,q.capital-q.capital_pagado,"
                      + "q.interes-q.interes_pagado,q.estado,cp.vigente "
                      + "FROM cuota q JOIN cronograma_pago cp ON cp.id_cronograma=q.id_cronograma "
                      + "WHERE q.id_cronograma=? AND q.numero=? FOR UPDATE")) {
                    ps.setInt(1, idCronograma);
                    ps.setInt(2, numeroCuota);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new IllegalArgumentException("La cuota no existe.");
                        idCuota = rs.getInt(1);
                        saldo = rs.getBigDecimal(2);
                        capitalPendiente = rs.getBigDecimal(3);
                        interesPendiente = rs.getBigDecimal(4);
                        if (!rs.getBoolean(6)) throw new IllegalArgumentException("El cronograma ya no esta vigente.");
                        if (saldo.signum() <= 0 || "PAGADA".equals(rs.getString(5))) {
                            throw new IllegalArgumentException("La cuota ya esta pagada.");
                        }
                    }
                }
                if (capitalPendiente.signum() < 0 || interesPendiente.signum() < 0
                        || saldo.compareTo(capitalPendiente.add(interesPendiente)) != 0) {
                    throw new IllegalArgumentException("La cuota tiene saldos inconsistentes; revise sus pagos anteriores.");
                }
                if (monto.compareTo(saldo) > 0) {
                    throw new IllegalArgumentException("El monto supera el saldo actual de la cuota: " + saldo);
                }
                BigDecimal interes = monto.min(interesPendiente);
                BigDecimal capital = monto.subtract(interes);
                BigDecimal nuevoSaldo = saldo.subtract(monto);
                BigDecimal nuevoCapital = saldoCapital.subtract(capital);
                if (nuevoCapital.signum() < 0) {
                    throw new IllegalArgumentException("El saldo de capital del credito es inconsistente.");
                }
                String comprobante = "P-" + UUID.randomUUID().toString().replace("-", "").substring(0, 26);
                int idPago;
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO pago(id_credito,id_usuario,monto,forma_pago,referencia,numero_comprobante) "
                      + "VALUES(?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, idCredito); ps.setInt(2, idUsuario); ps.setBigDecimal(3, monto);
                    ps.setString(4, metodo); ps.setString(5, comprobante); ps.setString(6, comprobante);
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (!rs.next()) throw new SQLException("No se obtuvo el identificador del pago.");
                        idPago = rs.getInt(1);
                    }
                }
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO aplicacion_pago(id_pago,id_cuota,monto_interes,monto_capital) VALUES(?,?,?,?)")) {
                    ps.setInt(1, idPago); ps.setInt(2, idCuota);
                    ps.setBigDecimal(3, interes); ps.setBigDecimal(4, capital); ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE cuota SET capital_pagado=capital_pagado+?,interes_pagado=interes_pagado+?,"
                      + "saldo=?,estado=CASE WHEN ?=0 THEN 'PAGADA' "
                      + "WHEN fecha_vencimiento<CURDATE() THEN 'VENCIDA' ELSE 'PENDIENTE' END,"
                      + "dias_atraso=CASE WHEN ?=0 THEN 0 ELSE GREATEST(DATEDIFF(CURDATE(),fecha_vencimiento),0) END "
                      + "WHERE id_cuota=?")) {
                    ps.setBigDecimal(1, capital); ps.setBigDecimal(2, interes);
                    ps.setBigDecimal(3, nuevoSaldo); ps.setBigDecimal(4, nuevoSaldo);
                    ps.setBigDecimal(5, nuevoSaldo); ps.setInt(6, idCuota); ps.executeUpdate();
                }
                int pendientes, vencidas;
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT COUNT(*),COALESCE(SUM(q.fecha_vencimiento<CURDATE()),0) "
                      + "FROM cuota q JOIN cronograma_pago cp ON cp.id_cronograma=q.id_cronograma "
                      + "WHERE cp.id_credito=? AND cp.vigente=1 AND q.saldo>0")) {
                    ps.setInt(1, idCredito);
                    try (ResultSet rs = ps.executeQuery()) { rs.next(); pendientes=rs.getInt(1); vencidas=rs.getInt(2); }
                }
                if (pendientes == 0 && nuevoCapital.signum() != 0) {
                    throw new IllegalArgumentException("El capital no coincide con el cronograma; no se puede cerrar el credito.");
                }
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE credito SET saldo_capital=?,estado=? WHERE id_credito=?")) {
                    ps.setBigDecimal(1, nuevoCapital);
                    ps.setString(2, pendientes == 0 ? "CANCELADO" : vencidas > 0 ? "EN_MORA" : "VIGENTE");
                    ps.setInt(3, idCredito); ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO movimiento_financiero(id_usuario,id_pago,tipo,concepto,monto) "
                      + "VALUES(?,?,'INGRESO',?,?)")) {
                    ps.setInt(1, idUsuario); ps.setInt(2, idPago);
                    ps.setString(3, "Pago cuota " + numeroCuota + " - " + comprobante);
                    ps.setBigDecimal(4, monto); ps.executeUpdate();
                }
                AvisosClienteDAO.confirmarPago(con,idCredito,numeroCuota,monto,nuevoSaldo,comprobante,idUsuario);
                con.commit();
                return new ResultadoPago(idPago, comprobante, nuevoSaldo, capital, interes);
            } catch (SQLException | RuntimeException ex) {
                con.rollback();
                throw ex;
            }
        }
    }
}
