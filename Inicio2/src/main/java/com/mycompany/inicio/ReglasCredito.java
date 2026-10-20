package com.mycompany.inicio;

import java.sql.*;

/** Parametros de la base de datos: no sustituye una politica ausente con valores inventados. */
final class ReglasCredito {
    private ReglasCredito() {}
    static int parametro(Connection c,String clave,int maximo) throws SQLException {
        try(PreparedStatement p=c.prepareStatement("SELECT valor FROM parametro WHERE clave=?")) {
            p.setString(1,clave);try(ResultSet r=p.executeQuery()){
                if(r.next())try{int n=Integer.parseInt(r.getString(1));if(n>=0&&n<=maximo)return n;}catch(NumberFormatException ignorado){}
            }
        }
        throw new IllegalArgumentException("Configura un valor valido para "+clave+" antes de continuar.");
    }
    static int insertar(Connection c,String sql,Object... args) throws SQLException {
        try(PreparedStatement p=c.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)){
            parametros(p,args);p.executeUpdate();try(ResultSet r=p.getGeneratedKeys()){
                if(!r.next())throw new SQLException("No se obtuvo el identificador generado.");return r.getInt(1);
            }
        }
    }
    static int actualizar(Connection c,String sql,Object... args) throws SQLException {
        try(PreparedStatement p=c.prepareStatement(sql)){parametros(p,args);return p.executeUpdate();}
    }
    static void parametros(PreparedStatement p,Object... args)throws SQLException{
        for(int i=0;i<args.length;i++)p.setObject(i+1,args[i]);
    }
    static void texto(String v,int max,String nombre) {
        if(v==null||v.isBlank()||v.trim().length()>max)throw new IllegalArgumentException(nombre+": obligatorio, hasta "+max+" caracteres.");
    }
}
