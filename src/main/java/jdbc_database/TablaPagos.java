package jdbc_database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TablaPagos {

    private Connection connection = null;
    //consultas
    private String cantidadusuariospagos = "SELECT tiene_pago, count(*) as cantidad_usuarios FROM (usuarios natural join menbresias) left join pagos on pagos.id_menbresia = menbresias.id_menbresia WHERE id_titular IS NULL group by fecha_pago";
    private String usuariosPagosData = "SELECT apellidos, nombres, email, tiene_pago, fecha_pago FROM (usuarios natural join menbresias) left join pagos on pagos.id_menbresia = menbresias.id_menbresia WHERE id_titular IS NULL and (month(fecha_pago) = month(now()) or fecha_pago is null)";
    private String id_usuario = "select id_usuario from menbresias where id_menbresia = ?";
    private String id_hijos = "select id_usuario from usuarios where id_titular = (" + id_usuario + ")";
    private String menbresias_hijos = "select id_menbresia from menbresias where id_usuario = any (" + id_hijos + ")";
    private String id_actividades = "select id_actividad from actividades_menbresias where id_menbresia = any (" + menbresias_hijos + ") or id_menbresia = ?";
    private String actividades = "SELECT nombre, nickname, precio, count(id_actividad) as cantidad FROM actividades natural join (" + id_actividades + ") as a group by id_actividad";
    private String registropago = "INSERT INTO `pagos`(`id_menbresia`, `precio`, `fecha_limite`) VALUE (?, ?, ?);";
    private String registroprimerpago = "INSERT INTO `pagos`(`id_menbresia`, `precio`) VALUE (?, ?);";
    private String fechalimite = "select year(now()) as año, month(now()) as mes, day(fecha_limite) as dia, fecha_pago, fecha_limite from pagos where id_menbresia = ? and datediff(now(),fecha_pago) between 0 and 80 group by month(fecha_limite) order by fecha_pago desc;";
    private String tienepago = "select * from pagos where id_menbresia = ?;";

    public TablaPagos() {
        connection = new Mysql().getConexion();
    }

    public void Pagar(int id_menbresia, int precio) throws SQLException {
        String fechaLimite = getFechaLimite(id_menbresia);
        PreparedStatement hojaVirtual;
        if(fechaLimite.equals("")){
            hojaVirtual = connection.prepareStatement(registroprimerpago);
            hojaVirtual.setInt(1, id_menbresia);
            hojaVirtual.setInt(2, precio);
        }else{
            hojaVirtual = connection.prepareStatement(registropago);
            hojaVirtual.setInt(1, id_menbresia);
            hojaVirtual.setInt(2, precio);
            hojaVirtual.setString(3, fechaLimite);
        }
        hojaVirtual.executeUpdate();
    }

    public String getFechaLimite(int id_menbresia) throws SQLException {
        PreparedStatement hojaVirtual = connection.prepareStatement(fechalimite);
        hojaVirtual.setInt(1, id_menbresia);
        ResultSet data = hojaVirtual.executeQuery();
        String fechaLimite = "";
        if (data.next()) {
            String año = data.getString("año");
            String mes = data.getString("mes");
            String dia = data.getString("dia");
            fechaLimite = año + "-" + mes + "-" + dia;
        } else {
            PreparedStatement ps = connection.prepareStatement(tienepago);
            ps.setInt(1, id_menbresia);
            ResultSet rs = hojaVirtual.executeQuery();
            if(rs.next()){
                // calcular deuda para pagar correctamente
            }
        }
        return fechaLimite;
    }

    public ArrayList<Map<String, Object>> cantidadUsuariosPagos() throws SQLException {
        Statement hojaVirtual = connection.createStatement();
        ResultSet data = hojaVirtual.executeQuery(cantidadusuariospagos);
        ArrayList<Map<String, Object>> usuariosPagos = new ArrayList<>();
        while (data.next()) {
            Map<String, Object> cantidadUsuariosConPagos = new HashMap<>();
            cantidadUsuariosConPagos.put("tiene_pago", data.getString("tiene_pago"));
            cantidadUsuariosConPagos.put("cantidad_usuarios", data.getInt("cantidad_usuarios"));
            usuariosPagos.add(cantidadUsuariosConPagos);
        }
        return usuariosPagos;
    }

    public ArrayList<Map<String, Object>> usuariosPagos() throws SQLException {
        Statement hojaVirtual = connection.createStatement();
        ResultSet data = hojaVirtual.executeQuery(usuariosPagosData);
        ArrayList<Map<String, Object>> usuariosPagos = new ArrayList<>();
        while (data.next()) {
            Map<String, Object> UsuarioPago = new HashMap<>();
            UsuarioPago.put("apellidos", data.getString("apellidos"));
            UsuarioPago.put("nombres", data.getString("nombres"));
            UsuarioPago.put("email", data.getString("email"));
            UsuarioPago.put("tiene_pago", data.getBoolean("tiene_pago"));
            UsuarioPago.put("fecha_pago", data.getString("fecha_pago"));
            usuariosPagos.add(UsuarioPago);
        }
        return usuariosPagos;
    }
}
