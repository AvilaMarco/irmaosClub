package jdbc_database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TablaPagos {

    private Connection connection = null;
    //consultas auxiliares
    private final String fechaLimite = "fecha_limite, year(fecha_limite) as year, month(fecha_limite) as month, day(fecha_limite) as day";
    //pagos
    private final String act_registro = "select " + fechaLimite + ", id_actividad, nombre, nickname, grupo_descuento, precio from actividades_menbresias natural join actividades where pago = 0 and id_menbresia = ? and pago_listo = 0 order by nickname";
    private final String act_procesando_sin_pago = "select " + fechaLimite + ", id_actividad, nombre, nickname, grupo_descuento, precio from actividades_menbresias natural join actividades where pago = 0 and id_menbresia = ? and pago_listo = 1 order by nickname";
    private final String act_procesando_con_pago = "select " + fechaLimite + ", id_actividad, nombre, nickname, grupo_descuento, precio from actividades_menbresias natural join actividades where pago = 1 and id_menbresia = ? and pago_listo = 1 and datediff(fecha_limite, now()) between 0 and 7 order by nickname";
    private final String act_con_vencimiento = "select " + fechaLimite + ", id_actividad, nombre, nickname, grupo_descuento, precio from actividades_menbresias natural join actividades where pago = 1 and id_menbresia = ? and datediff(fecha_limite, now()) between 0 and 7 and pago_listo = 0 order by nickname";
    private final String act_sin_vencimiento = "select " + fechaLimite + ", id_actividad, nombre, nickname, grupo_descuento, precio from actividades_menbresias natural join actividades where pago = 1 and id_menbresia = ? and datediff(fecha_limite, now()) > 7 and pago_listo = 0 order by nickname";
    //pagos final
    private final String estaPago = "select pago from actividades_menbresias natural join actividades where id_menbresia = ? and (nombre = ? or nickname = ?) order by fecha_limite";
    private final String actividad_con_pago = "select id_actividad, date_add(fecha_limite, interval 1 month) as fecha from actividades_menbresias natural join actividades where id_menbresia = ? and (nombre = ? or nickname = ?); ";
    private final String actividad_sin_pago = "select id_actividad, date_add(now(), interval 1 month) as fecha from actividades_menbresias natural join actividades where id_menbresia = ? and (nombre = ? or nickname = ?); ";
    private final String update_fecha_limite = "update actividades_menbresias set pago = 1, fecha_limite = date(?) where id_actividad = ? and id_menbresia = ?;";
    private final String registrar_pago = "INSERT INTO `pagos`(`id_menbresia`, `precio`, `fecha_limite`, `item_actividad`) VALUE (?, ?, date(?), ?);";
    //Dashboard
    private final String dinero_por_mes = "select fecha_pago, month(fecha_pago) as mes, year(fecha_pago) as year,  SUM(precio) as dinero_toltal from pagos group by MONTH(fecha_pago) order by fecha_pago desc limit 12;";

    public TablaPagos() {
        connection = new Mysql().getConexion();
    }

    private void cerrarRecursos(PreparedStatement ps, Statement s, ResultSet rs) {
        try {
            if (ps != null) {
                ps.close();
            }
            if (s != null) {
                s.close();
            }
            if (rs != null) {
                rs.close();
            }
//            connection.close();
        } catch (SQLException ex) {
            Logger.getLogger(TablaActividadesMenbresias.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void cerrarConexion() {
        try {
            connection.close();
        } catch (SQLException ex) {
            Logger.getLogger(TablaActividadesMenbresias.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public ArrayList<Map<String, Object>> dineroPorMes() throws SQLException {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(dinero_por_mes);
        ArrayList<Map<String, Object>> mensualidad = new ArrayList<>();
        while (rs.next()) {
            Map<String, Object> dineroMes = new HashMap<>();
            dineroMes.put("dinero_total", rs.getInt("dinero_toltal"));
            dineroMes.put("month", rs.getInt("mes"));
            dineroMes.put("year", rs.getInt("year"));
            mensualidad.add(dineroMes);
        }
        cerrarRecursos(null, st, rs);
        return mensualidad;
    }

    public void Pagar(int id_menbresia_titular, int id_menbresia, int precio, String nombreActividad) throws SQLException {
        TablaActividadesMenbresias consultas = new TablaActividadesMenbresias();
        String fecha_limite = "";
        int id_actividad = 0;
        ResultSet rs;
        PreparedStatement ps;
        if (nombreActividad.contains("+")) {
            String[] items = nombreActividad.split(" +");
            for (String actividad : items) {
                if (!actividad.equals("+")) {
                    consultas.updatePagoListo(true, id_menbresia, actividad);
                    //paso final 0 - obtener datos
                    ps = connection.prepareStatement(estaPago);
                    ps.setInt(1, id_menbresia);
                    ps.setString(2, actividad);
                    ps.setString(3, actividad);
                    rs = ps.executeQuery();
                    if (rs.next()) {
                        Boolean estaPago = rs.getBoolean("pago");
                        cerrarRecursos(ps, null, rs);
                        if (estaPago) {
                            ps = connection.prepareStatement(actividad_con_pago);
                        } else {
                            ps = connection.prepareStatement(actividad_sin_pago);
                        }
                        ps.setInt(1, id_menbresia);
                        ps.setString(2, actividad);
                        ps.setString(3, actividad);
                        rs = ps.executeQuery();
                        if (rs.next()) {
                            fecha_limite = rs.getString("fecha");
                            id_actividad = rs.getInt("id_actividad");
                        }
                        cerrarRecursos(ps, null, rs);
                    }
                    //paso final 1 - actualizar fecha
                    ps = connection.prepareStatement(update_fecha_limite);
                    ps.setString(1, fecha_limite);
                    ps.setInt(2, id_actividad);
                    ps.setInt(3, id_menbresia);
                    ps.executeUpdate();
                    cerrarRecursos(ps, null, null);
                }
            }
        } else {
            consultas.updatePagoListo(true, id_menbresia, nombreActividad);
            //paso final 0 - obtener datos
            ps = connection.prepareStatement(estaPago);
            ps.setInt(1, id_menbresia);
            ps.setString(2, nombreActividad);
            ps.setString(3, nombreActividad);
            rs = ps.executeQuery();
            if (rs.next()) {
                Boolean estaPago = rs.getBoolean("pago");
                cerrarRecursos(ps, null, rs);
                if (estaPago) {
                    ps = connection.prepareStatement(actividad_con_pago);
                } else {
                    ps = connection.prepareStatement(actividad_sin_pago);
                }
                ps.setInt(1, id_menbresia);
                ps.setString(2, nombreActividad);
                ps.setString(3, nombreActividad);
                rs = ps.executeQuery();
                if (rs.next()) {
                    fecha_limite = rs.getString("fecha");
                    id_actividad = rs.getInt("id_actividad");
                }
                cerrarRecursos(ps, null, rs);
            }
            //paso final 1 - actualizar fecha
            ps = connection.prepareStatement(update_fecha_limite);
            ps.setString(1, fecha_limite);
            ps.setInt(2, id_actividad);
            ps.setInt(3, id_menbresia);
            ps.executeUpdate();
            cerrarRecursos(ps, null, null);
        }
        //paso final 2 - registro de pago
        ps = connection.prepareStatement(registrar_pago);
        ps.setInt(1, id_menbresia);
        ps.setInt(2, precio);
        ps.setString(3, fecha_limite);
        ps.setString(4, nombreActividad);
        ps.executeUpdate();
        cerrarRecursos(ps, null, null);
    }

    public ArrayList<Map<String, Object>> calcularPago(ArrayList<Integer> menbresias, int id_menbresia_titular, Boolean esAdmin) throws SQLException {
        TablaActividadesMenbresias consultas = new TablaActividadesMenbresias();
        consultas.actulizarActividadesPagas();
        ArrayList<Map<String, Object>> itemsSinPago = consultas.crearItemsConDescuento(menbresias, act_registro);
        ArrayList<Map<String, Object>> itemsSinVencimiento = consultas.crearItemsConDescuento(menbresias, act_sin_vencimiento);
        ArrayList<Map<String, Object>> itemsConVencimiento = consultas.crearItemsConDescuento(menbresias, act_con_vencimiento);
        ArrayList<Map<String, Object>> itemsProcensadoSinPago = consultas.crearItemsConDescuento(menbresias, act_procesando_sin_pago);
        ArrayList<Map<String, Object>> itemsProcensadoConPago = consultas.crearItemsConDescuento(menbresias, act_procesando_con_pago);
        ArrayList<Map<String, Object>> itemsProcensadoPago = new ArrayList<>();
        itemsProcensadoPago.addAll(itemsProcensadoSinPago);
        itemsProcensadoPago.addAll(itemsProcensadoConPago);
        ArrayList<Map<String, Object>> tablasActividades = new ArrayList<>();
        String[] checkbox1 = {"pago-registro"};
        String[] checkbox2 = {"pago"};
        String[] checkbox3 = {"pago-proceso"};
        if (esAdmin) {
            tablasActividades.add(crearTablaPago("Actividades al Dia", "success", itemsSinVencimiento, null));
            tablasActividades.add(crearTablaPago("Actividades Proximas a vencer", "danger", itemsConVencimiento, null));
            tablasActividades.add(crearTablaPago("Actividades Para Pagar", "warning", itemsSinPago, null));
            tablasActividades.add(crearTablaPago("Procesando Pago", "info", itemsProcensadoPago, checkbox3));
        } else {
            tablasActividades.add(crearTablaPago("Actividades al Dia", "success", itemsSinVencimiento, null));
            tablasActividades.add(crearTablaPago("Actividades Proximas a vencer", "danger", itemsConVencimiento, checkbox2));
            tablasActividades.add(crearTablaPago("Actividades Para Pagar", "warning", itemsSinPago, checkbox1));
            tablasActividades.add(crearTablaPago("Procesando Pago", "info", itemsProcensadoPago, null));
        }
        return tablasActividades;

    }

    public Map<String, Object> crearTablaPago(String title, String color, ArrayList<Map<String, Object>> actividades, String[] checkbox) {
        Map<String, Object> tablaPago = new HashMap<>();
        tablaPago.put("titulo", title);
        tablaPago.put("color", color);
        tablaPago.put("actividades", actividades);
        if (checkbox != null) {
            tablaPago.put("checkbox", checkbox);
        }
        return tablaPago;
    }

    
    
   /*     //sin uso
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
//    private String pagodeudca = "INSERT INTO `pagos`(`id_menbresia`, `precio`, `fecha_pago`, `fecha_limite`, `item_actividad`) VALUES (?, ?, ?, ?, ?)";
        //actividades 1
    private final String actividadPorNombre = "select id_actividad, date_add(fecha_limite, interval 1 month) as fecha from actividades_menbresias natural join actividades where id_menbresia = ? and nombre = ?;";
    private final String actividadPorNickname = "select id_actividad, date_add(fecha_limite, interval 1 month) as fecha from actividades_menbresias natural join actividades where id_menbresia = ? and nickname = ?;";
    private final String actualizarFechaLimite = "update actividades_menbresias set pago = 1, fecha_limite = ? where id_actividad = ? and id_menbresia = ?";
    private final String registropago = "INSERT INTO `pagos`(`id_menbresia`, `precio`, `fecha_limite`, `item_actividad`) VALUE (?, ?, ?, ?);";
    //actividades 0
    private final String setFechaLimite = "update actividades_menbresias set pago = 1, fecha_limite = date_add(now(), interval 1 month) where id_actividad = ? and id_menbresia = ?;";
    private final String registroprimerpago = "INSERT INTO `pagos`(`id_menbresia`, `precio`, `item_actividad`, `fecha_limite`) VALUE (?, ?, ?, date_add(now(), interval 1 month));";
    private final String actividadSinPagoPorNombre = "select id_actividad from actividades_menbresias natural join actividades where id_menbresia = ? and nombre = ?;";
    private final String actividadSinPagoPorNickname = "select id_actividad from actividades_menbresias natural join actividades where id_menbresia = ? and nickname = ?;";*/
//        public void Pagar(int id_menbresia_titular, int id_menbresia, int precio, String nombreActividad) throws SQLException {
//        TablaActividadesMenbresias consultas = new TablaActividadesMenbresias();
//        if (nombreActividad.contains("+")) {
//            String[] items = nombreActividad.split(" +");
//            String fecha_limite = "";
//            PreparedStatement ps;
//            for (String actividad : items) {
//                if (!actividad.equals("+")) {
//                    consultas.updatePagoListo(true, id_menbresia, actividad);
//                    //paso 1
//                    ps = connection.prepareStatement(actividadPorNickname);
//                    ps.setInt(1, id_menbresia);
//                    ps.setString(2, actividad);
//                    ResultSet rs = ps.executeQuery();
//                    int id_actividad = 0;
//                    if (rs.next()) {
//                        id_actividad = rs.getInt("id_actividad");
//                        fecha_limite = rs.getString("fecha");
//                    }
//                    ps.close();
//                    //paso 2
//                    ps = connection.prepareStatement(actualizarFechaLimite);
//                    ps.setString(1, fecha_limite);
//                    ps.setInt(2, id_actividad);
//                    ps.setInt(3, id_menbresia);
//                    ps.executeUpdate();
//                    ps.close();
//                }
//            }
//            //paso 3
//            ps = connection.prepareStatement(registropago);
//            ps.setInt(1, id_menbresia_titular);
//            ps.setInt(2, precio);
//            ps.setString(3, fecha_limite);
//            ps.setString(4, nombreActividad);
//        } else {
//            consultas.updatePagoListo(true, id_menbresia, nombreActividad);
//            //paso 1
//            PreparedStatement ps = connection.prepareStatement(actividadPorNombre);
//            ps.setInt(1, id_menbresia);
//            ps.setString(2, nombreActividad);
//            ResultSet rs = ps.executeQuery();
//            int id_actividad = 0;
//            String fecha_limite = "";
//            if (rs.next()) {
//                id_actividad = rs.getInt("id_actividad");
//                fecha_limite = rs.getString("fecha");
//            }
//            ps.close();
//            //paso 2
//            ps = connection.prepareStatement(actualizarFechaLimite);
//            ps.setString(1, fecha_limite);
//            ps.setInt(2, id_actividad);
//            ps.setInt(3, id_menbresia);
//            ps.executeUpdate();
//            ps.close();
//            //paso 3
//            ps = connection.prepareStatement(registropago);
//            ps.setInt(1, id_menbresia_titular);
//            ps.setInt(2, precio);
//            ps.setString(3, fecha_limite);
//            ps.setString(4, nombreActividad);
//        }
//    }
//
//    public void PagarRegistro(int id_menbresia_titular, int id_menbresia, int precio, String nombreActividad) throws SQLException {
//        TablaActividadesMenbresias consultas = new TablaActividadesMenbresias();
//        if (nombreActividad.contains("+")) {
//            String[] items = nombreActividad.split(" +");
//            PreparedStatement ps;
//            for (String actividad : items) {
//                if (!actividad.equals("+")) {
//                    consultas.updatePagoListo(true, id_menbresia, actividad);
//                    //paso 1
//                    ps = connection.prepareStatement(actividadSinPagoPorNickname);
//                    ps.setInt(1, id_menbresia);
//                    ps.setString(2, actividad);
//                    ResultSet rs = ps.executeQuery();
//                    int id_actividad = 0;
//                    if (rs.next()) {
//                        id_actividad = rs.getInt("id_actividad");
//                    }
//                    ps.close();
//                    //paso 0
//                    ps = connection.prepareStatement(setFechaLimite);
//                    ps.setInt(1, id_actividad);
//                    ps.setInt(2, id_menbresia);
//                    ps.executeUpdate();
//                }
//            }
//            //paso 3
//            ps = connection.prepareStatement(registroprimerpago);
//            ps.setInt(1, id_menbresia_titular);
//            ps.setInt(2, precio);
//            ps.setString(3, nombreActividad);
//        } else {
//            consultas.updatePagoListo(true, id_menbresia, nombreActividad);
//            //paso 1
//            PreparedStatement ps = connection.prepareStatement(actividadSinPagoPorNombre);
//            ps.setInt(1, id_menbresia);
//            ps.setString(2, nombreActividad);
//            ResultSet rs = ps.executeQuery();
//            int id_actividad = 0;
//            if (rs.next()) {
//                id_actividad = rs.getInt("id_actividad");
//            }
//            ps.close();
//            //paso 0
//            ps = connection.prepareStatement(setFechaLimite);
//            ps.setInt(1, id_actividad);
//            ps.setInt(2, id_menbresia);
//            ps.executeUpdate();
//            //paso 3
//            ps = connection.prepareStatement(registroprimerpago);
//            ps.setInt(1, id_menbresia_titular);
//            ps.setInt(2, precio);
//            ps.setString(3, nombreActividad);
//        }
//    }
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
//        Map<String, Object> p = new LinkedHashMap<>();
//        pagos.put("itemsRegistro", itemSinPago);
//        pagos.put("itemsMensual", itemConPago);
//        ArrayList<Map<String, Object>> itemsRegistro = new ArrayList<>();
//        ArrayList<Map<String, Object>> itemsMensual = new ArrayList<>();
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
