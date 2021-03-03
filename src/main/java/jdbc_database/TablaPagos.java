package jdbc_database;

import com.mercadopago.resources.datastructures.preference.Item;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class TablaPagos {

    private Connection connection = null;
    //sin uso
    private final String cantidadusuariospagos = "SELECT tiene_pago, count(*) as cantidad_usuarios FROM (usuarios natural join menbresias) left join pagos on pagos.id_menbresia = menbresias.id_menbresia WHERE id_titular IS NULL group by fecha_pago";
    private final String usuariosPagosData = "SELECT apellidos, nombres, email, tiene_pago, fecha_pago FROM (usuarios natural join menbresias) left join pagos on pagos.id_menbresia = menbresias.id_menbresia WHERE id_titular IS NULL and (month(fecha_pago) = month(now()) or fecha_pago is null)";
//    private final String actividades = "SELECT nombre, nickname, precio, count(id_actividad) as cantidad FROM actividades natural join (" + id_actividades + ") as a group by id_actividad";
    private final String tienepago = "select * from pagos where id_menbresia = ?;";
//    private String fechalimite = "select year(now()) as año, month(now()) as mes, day(fecha_limite) as dia, fecha_pago, fecha_limite from pagos where id_menbresia = ? and datediff(now(),fecha_pago) between 0 and 80 group by month(fecha_limite) order by fecha_pago desc;";
    private final String fechalimite = "select date_add(fecha_limite, INTERVAL 1 month) as fecha from pagos where id_menbresia = ? and precio = ? and item_actividad = ? order by fecha_limite;";
//    private final String id_actividades = "select id_actividad from actividades_menbresias where id_menbresia = any (" + menbresias_hijos + ") or id_menbresia = ?";
    private final String pagosItems = "select * from pagos where id_menbresia = ? and item_actividad = ?";
    private final String pagoultimosdias = "select * from pagos where id_menbresia = ? and item_actividad = ? and precio = ? and datediff(now(),fecha_pago) between 0 and 30;";
//    private final String menbresias_hijos = "select id_menbresia from menbresias where id_usuario = any (" + id_hijos + ")";
//    private final String id_hijos = "select id_usuario from usuarios where id_titular = (" + id_usuario + ")";
//    private final String id_usuario = "select id_usuario from menbresias where id_menbresia = ?";
    //consultas
    //pagos
    private final String actividadessinpagos = "select year(fecha_limite) as year, month(fecha_limite) as month, day(fecha_limite) as day, id_actividad, nombre, nickname, precio, grupo_descuento from actividades_menbresias natural join actividades where pago = 0 and id_menbresia = ? order by nickname";
    private final String actividadesconpagos = "select year(fecha_limite) as year, month(fecha_limite) as month, day(fecha_limite) as day, id_actividad, nombre, nickname, precio, grupo_descuento from actividades_menbresias natural join actividades where pago = 1 and id_menbresia = ? order by nickname";
//    private String pagodeudca = "INSERT INTO `pagos`(`id_menbresia`, `precio`, `fecha_pago`, `fecha_limite`, `item_actividad`) VALUES (?, ?, ?, ?, ?)";
    //pagos 2
    //actividades 1
    private final String actividadPorNombre = "select id_actividad, date_add(fecha_limite, interval 1 month) as fecha from actividades_menbresias natural join actividades where id_menbresia = ? and nombre = ?;";
    private final String actividadPorNickname = "select id_actividad, date_add(fecha_limite, interval 1 month) as fecha from actividades_menbresias natural join actividades where id_menbresia = ? and nickname = ?;";
    private final String actualizarFechaLimite = "update actividades_menbresias set pago = 1, fecha_limite = ? where id_actividad = ? and id_menbresia = ?";
    private final String registropago = "INSERT INTO `pagos`(`id_menbresia`, `precio`, `fecha_limite`, `item_actividad`) VALUE (?, ?, ?, ?);";
    //actividades 0
    private final String setFechaLimite = "update actividades_menbresias set pago = 1, fecha_limite = date_add(now(), interval 1 month) where id_actividad = ? and id_menbresia = ?;";
    private final String registroprimerpago = "INSERT INTO `pagos`(`id_menbresia`, `precio`, `item_actividad`, `fecha_limite`) VALUE (?, ?, ?, date_add(now(), interval 1 month));";
    private final String actividadSinPagoPorNombre = "select id_actividad from actividades_menbresias natural join actividades where id_menbresia = ? and nombre = ?;";
    private final String actividadSinPagoPorNickname = "select id_actividad from actividades_menbresias natural join actividades where id_menbresia = ? and nickname = ?;";

    public TablaPagos() {
        connection = new Mysql().getConexion();
    }

    public void Pagar(int id_menbresia_titular, int id_menbresia, int precio, String nombreActividad) throws SQLException {
        if (nombreActividad.contains("+")) {
            String[] items = nombreActividad.split(" +");
            String fecha_limite = "";
            PreparedStatement ps;
            for (String actividad : items) {
                if (!actividad.equals("+")) {
                    //paso 1
                    ps = connection.prepareStatement(actividadPorNickname);
                    ps.setInt(1, id_menbresia);
                    ps.setString(2, actividad);
                    ResultSet rs = ps.executeQuery();
                    int id_actividad = 0;
                    if (rs.next()) {
                        id_actividad = rs.getInt("id_actividad");
                        fecha_limite = rs.getString("fecha");
                    }
                    ps.close();
                    //paso 2
                    ps = connection.prepareStatement(actualizarFechaLimite);
                    ps.setString(1, fecha_limite);
                    ps.setInt(2, id_actividad);
                    ps.setInt(3, id_menbresia);
                    ps.executeUpdate();
                    ps.close();
                }
            }
            //paso 3
            ps = connection.prepareStatement(registropago);
            ps.setInt(1, id_menbresia_titular);
            ps.setInt(2, precio);
            ps.setString(3, fecha_limite);
            ps.setString(4, nombreActividad);
        } else {
            //paso 1
            PreparedStatement ps = connection.prepareStatement(actividadPorNombre);
            ps.setInt(1, id_menbresia);
            ps.setString(2, nombreActividad);
            ResultSet rs = ps.executeQuery();
            int id_actividad = 0;
            String fecha_limite = "";
            if (rs.next()) {
                id_actividad = rs.getInt("id_actividad");
                fecha_limite = rs.getString("fecha");
            }
            ps.close();
            //paso 2
            ps = connection.prepareStatement(actualizarFechaLimite);
            ps.setString(1, fecha_limite);
            ps.setInt(2, id_actividad);
            ps.setInt(3, id_menbresia);
            ps.executeUpdate();
            ps.close();
            //paso 3
            ps = connection.prepareStatement(registropago);
            ps.setInt(1, id_menbresia_titular);
            ps.setInt(2, precio);
            ps.setString(3, fecha_limite);
            ps.setString(4, nombreActividad);
        }
    }

    public void PagarRegistro(int id_menbresia_titular, int id_menbresia, int precio, String nombreActividad) throws SQLException {
        if (nombreActividad.contains("+")) {
            String[] items = nombreActividad.split(" +");
            PreparedStatement ps;
            for (String actividad : items) {
                if (!actividad.equals("+")) {
                    //paso 1
                    ps = connection.prepareStatement(actividadSinPagoPorNickname);
                    ps.setInt(1, id_menbresia);
                    ps.setString(2, actividad);
                    ResultSet rs = ps.executeQuery();
                    int id_actividad = 0;
                    if (rs.next()) {
                        id_actividad = rs.getInt("id_actividad");
                    }
                    ps.close();
                    //paso 0
                    ps = connection.prepareStatement(setFechaLimite);
                    ps.setInt(1, id_actividad);
                    ps.setInt(2, id_menbresia);
                    ps.executeUpdate();
                }
            }
            //paso 3
            ps = connection.prepareStatement(registroprimerpago);
            ps.setInt(1, id_menbresia_titular);
            ps.setInt(2, precio);
            ps.setString(3, nombreActividad);
        } else {
            //paso 1
            PreparedStatement ps = connection.prepareStatement(actividadSinPagoPorNombre);
            ps.setInt(1, id_menbresia);
            ps.setString(2, nombreActividad);
            ResultSet rs = ps.executeQuery();
            int id_actividad = 0;
            if (rs.next()) {
                id_actividad = rs.getInt("id_actividad");
            }
            ps.close();
            //paso 0
            ps = connection.prepareStatement(setFechaLimite);
            ps.setInt(1, id_actividad);
            ps.setInt(2, id_menbresia);
            ps.executeUpdate();
            //paso 3
            ps = connection.prepareStatement(registroprimerpago);
            ps.setInt(1, id_menbresia_titular);
            ps.setInt(2, precio);
            ps.setString(3, nombreActividad);
        }
//        PreparedStatement hojaVirtual = connection.prepareStatement(fechalimite);
//        hojaVirtual.setInt(1, id_menbresia);
//        hojaVirtual.setInt(2, precio);
//        hojaVirtual.setString(3, nombreActividad);
//        ResultSet data = hojaVirtual.executeQuery();
//        if (data.next()) {
//            PreparedStatement ps = connection.prepareStatement(pagodeuda);
//            ps.setInt(1, id_menbresia);
//            ps.setInt(2, precio);
//            ps.setString(3, data.getString("fecha"));
//            ps.setString(4, data.getString("fecha"));
//            ps.setString(5, nombreActividad);
//            ps.executeQuery();
//        }
    }

    public Map<String, Object> calcularPago(ArrayList<Integer> menbresias, int id_menbresia_titular) throws SQLException {
        TablaActividadesMenbresias consultas = new TablaActividadesMenbresias();
        consultas.actulizarActividadesPagas();
        ArrayList<Item> itemSinPago = consultas.crearItemsConDescuento(menbresias, actividadessinpagos);
        ArrayList<Item> itemConPago = consultas.crearItemsConDescuento(menbresias, actividadesconpagos);
        Map<String, Object> pagos = new LinkedHashMap<>();
        pagos.put("itemsRegistro", itemSinPago);
        pagos.put("itemsMensual", itemConPago);
        return pagos;
//        ArrayList<Item> itemsRegistro = new ArrayList<>();
//        ArrayList<Item> itemsMensual = new ArrayList<>();
//        for (Item item : itemSinPago) {
//            PreparedStatement ps = connection.prepareStatement(pagosItems);
//            ps.setInt(1, id_menbresia_titular);
//            ps.setString(2, item.getTitle());
//            ResultSet rs = ps.executeQuery();
//            if (rs.next()) {
//                itemsDeuda.add(item);
//            } else {
//                itemsRegistro.add(item);
//            }
//        }
//        for (Item item : itemConPago) {
//            PreparedStatement ps = connection.prepareStatement(pagoultimosdias);
//            int precio = (int) (float) item.getUnitPrice();
//            ps.setInt(1, id_menbresia_titular);
//            ps.setString(2, item.getTitle());
//            ps.setInt(3, precio);
//            ResultSet rs = ps.executeQuery();
//            if (!rs.next()) {
//                itemsMensual.add(item);
//            }
//        }
//        pagos.put("itemsDeuda", itemsDeuda);
    }

//    public String getFechaLimite(int id_menbresia) throws SQLException {
//        PreparedStatement hojaVirtual = connection.prepareStatement(fechalimite);
//        hojaVirtual.setInt(1, id_menbresia);
//        ResultSet data = hojaVirtual.executeQuery();
//        String fechaLimite = "";
//        if (data.next()) {
//            String año = data.getString("año");
//            String mes = data.getString("mes");
//            String dia = data.getString("dia");
//            fechaLimite = año + "-" + mes + "-" + dia;
//        } else {
//            PreparedStatement ps = connection.prepareStatement(tienepago);
//            ps.setInt(1, id_menbresia);
//            ResultSet rs = hojaVirtual.executeQuery();
//            if (rs.next()) {
//                // calcular deuda para pagar correctamente
//            }
//        }
//        return fechaLimite;
//    }
//    public ArrayList<Map<String, Object>> cantidadUsuariosPagos() throws SQLException {
//        Statement hojaVirtual = connection.createStatement();
//        ResultSet data = hojaVirtual.executeQuery(cantidadusuariospagos);
//        ArrayList<Map<String, Object>> usuariosPagos = new ArrayList<>();
//        while (data.next()) {
//            Map<String, Object> cantidadUsuariosConPagos = new HashMap<>();
//            cantidadUsuariosConPagos.put("tiene_pago", data.getString("tiene_pago"));
//            cantidadUsuariosConPagos.put("cantidad_usuarios", data.getInt("cantidad_usuarios"));
//            usuariosPagos.add(cantidadUsuariosConPagos);
//        }
//        return usuariosPagos;
//    }

//    public ArrayList<Map<String, Object>> usuariosPagos() throws SQLException {
//        Statement hojaVirtual = connection.createStatement();
//        ResultSet data = hojaVirtual.executeQuery(usuariosPagosData);
//        ArrayList<Map<String, Object>> usuariosPagos = new ArrayList<>();
//        while (data.next()) {
//            Map<String, Object> UsuarioPago = new HashMap<>();
//            UsuarioPago.put("apellidos", data.getString("apellidos"));
//            UsuarioPago.put("nombres", data.getString("nombres"));
//            UsuarioPago.put("email", data.getString("email"));
//            UsuarioPago.put("tiene_pago", data.getBoolean("tiene_pago"));
//            UsuarioPago.put("fecha_pago", data.getString("fecha_pago"));
//            usuariosPagos.add(UsuarioPago);
//        }
//        return usuariosPagos;
//    }
}
