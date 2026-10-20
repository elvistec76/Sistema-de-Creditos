package com.mycompany.inicio;

import java.sql.*;

/** Orden comun: cliente, solicitud, credito, cuota. Serializa operaciones del mismo cliente. */
final class BloqueoCliente {
    private BloqueoCliente() {}
    static void solicitud(Connection c,int solicitud)throws SQLException{
        try(PreparedStatement p=c.prepareStatement("SELECT id_cliente FROM solicitud_credito WHERE id_solicitud=?")){
            p.setInt(1,solicitud);try(ResultSet r=p.executeQuery()){
                if(!r.next())throw new IllegalArgumentException("Solicitud inexistente.");
                try(PreparedStatement b=c.prepareStatement("SELECT id_cliente FROM cliente WHERE id_cliente=? FOR UPDATE")){
                    b.setInt(1,r.getInt(1));try(ResultSet br=b.executeQuery()){if(!br.next())throw new IllegalArgumentException("Cliente inexistente.");}
                }
            }
        }
    }
    static void credito(Connection c,int credito)throws SQLException{
        try(PreparedStatement p=c.prepareStatement("SELECT id_solicitud FROM credito WHERE id_credito=?")){
            p.setInt(1,credito);try(ResultSet r=p.executeQuery()){if(!r.next())throw new IllegalArgumentException("Credito inexistente.");solicitud(c,r.getInt(1));}
        }
    }
}
