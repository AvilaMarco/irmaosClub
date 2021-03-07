package jdbc_database;

import com.mercadopago.resources.datastructures.preference.Item;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class TablaActividadesMenbresias {

    private Connection connection = null;
    //consultas
    private final String usuariosporactividad = "SELECT nombre, COUNT(*) cantidad_usuarios FROM usuarios natural join menbresias natural join actividades_menbresias natural join actividades where baneado = 0 group BY id_actividad;";
    //cantidad de usuarios por horario-actividad
    private final String usuarios_horarios = "SELECT horarios.id_actividad, horarios.id_horario, COUNT(actividades_menbresias.id_horario) AS cantidad_usuarios, hora FROM horarios LEFT JOIN actividades_menbresias natural join menbresias ON horarios.id_horario = actividades_menbresias.id_horario WHERE pago = 1 GROUP BY horarios.id_horario";
    private final String cantidad_usuarios_horarios = "SELECT nombre, hora, GROUP_CONCAT(dia SEPARATOR ', ') AS dias, cantidad_usuarios FROM ((" + usuarios_horarios + ") AS a NATURAL JOIN actividades) NATURAL JOIN dias_horarios GROUP BY id_horario;";
    private final String actvidades_horarios = "SELECT nickname, id_actividad, id_horario, nombre, hora, GROUP_CONCAT(dia SEPARATOR ', ') AS dias, precio FROM actividades NATURAL JOIN horarios NATURAL JOIN dias_horarios where nickname not like \"%especial\" GROUP BY id_horario";
    private final String anotarUsuarioActividad = "INSERT INTO actividades_menbresias (id_actividad, id_menbresia, id_horario) VALUE (?, ?, ?)";
    private final String listaemenbresias = "SELECT id_menbresia FROM actividades_menbresias NATURAL JOIN menbresias WHERE id_usuario = ANY (SELECT id_usuario FROM usuarios WHERE id_titular = ?) OR id_menbresia = ? GROUP BY id_menbresia ORDER BY fecha_creacion;";
    private final String listadescuentos = "SELECT * FROM descuentos_bjj";
    private final String comboActividad = "SELECT * FROM combos_actividades_bjj WHERE nombre = ?;";
    private final String updateActividadesPagas = "update actividades_menbresias set pago = 0 where pago = 1 and date(fecha_limite) < now();";
    private final String set_fecha_pago = "update actividades_menbresias set fecha_limite = date(?) where id_menbresia = ? and id_actividad = ?";
    private final String update_pago_listo = "update actividades_menbresias natural join actividades set pago_listo = ?  where id_menbresia = ? and (nombre = ? || nickname = ?);";

    public TablaActividadesMenbresias() {
        connection = new Mysql().getConexion();
    }

    public void updatePagoListo(Boolean esAdmin, int id_menbresia, String nombre) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(update_pago_listo);
        if (esAdmin) {
            ps.setInt(1, 0);
        } else {
            ps.setInt(1, 1);
        }
        ps.setInt(2, id_menbresia);
        ps.setString(3, nombre);
        ps.setString(4, nombre);
        ps.executeUpdate();
        ps.close();
    }

    public void actulizarActividadesPagas() throws SQLException {
        Statement s = connection.createStatement();
        s.executeUpdate(updateActividadesPagas);
        s.close();
    }

    public ArrayList<Item> crearItemsConDescuento(ArrayList<Integer> menbresias, String consultaActividades) throws SQLException {
        ArrayList<Float> descuentos = listaDescuentos();
        ArrayList<Item> items = new ArrayList<>();
        for (int i = 0; i < menbresias.size(); i++) {
            int menbresia = menbresias.get(i);
            //solo aplicar con familiares en BJJ
            float descuento;
            if (i < descuentos.size()) {
                descuento = descuentos.get(i);
            } else {
                descuento = descuentos.get(descuentos.size() - 1);
            }

            ArrayList<Map<String, Object>> actividades = actividadesPorMenbresia(menbresia, consultaActividades);
            items.addAll(crearItems(actividades, descuento, menbresia));
        }
        return items;
    }

    public ArrayList<Item> crearItems(ArrayList<Map<String, Object>> actividades, float descuento, int id_menbresia) throws SQLException {
        ArrayList<Item> items = new ArrayList<>();
        ArrayList<Map<String, Object>> actividadesConDescuento = new ArrayList<>();
        ArrayList<Map<String, Object>> actividadesSinDescuento = new ArrayList<>();
        //filtrar actividades
        for (Map<String, Object> actividad : actividades) {
            if (actividad.get("grupo_descuento").equals("descuento_bjj")) {
                actividadesConDescuento.add(actividad);
            } else {
                actividadesSinDescuento.add(actividad);
            }
        }
        /* crear items con decuento */
        //fisico bjj
        for (int i = 0; i < actividadesConDescuento.size(); i++) {
            Map<String, Object> actividad = actividadesConDescuento.get(i);
            if (actividad.get("nickname").equals("BJJ especial")) {
                items.add(crearItem(actividad, descuento, id_menbresia));
                actividadesConDescuento.remove(i);
            }
        }
        //el resto de actividades de bjj
        if (actividadesConDescuento.size() == 1) {
            Map<String, Object> actividad = actividadesConDescuento.get(0);
            items.add(crearItem(actividad, descuento, id_menbresia));
        } else if (actividadesConDescuento.size() > 1) {
            //nivelar fechas de vencimiento
            StringBuffer itemConDescuento = new StringBuffer();
            int logitudArr = actividadesConDescuento.size();
            String fechaLimite = minimaFechaLimite(actividadesConDescuento);
            for (int i = 0; i < logitudArr; i++) {
                Map<String, Object> actividad = actividadesConDescuento.get(i);
                String nombre = (String) actividad.get("nickname");
                int id_actividad = Integer.parseInt((String) actividad.get("id_actividad"));
                setFechaLimite(fechaLimite, id_menbresia, id_actividad);
                if (i == logitudArr - 1) {
                    itemConDescuento.append(nombre);
                } else {
                    itemConDescuento.append(nombre).append(" + ");
                }
            }
            Map<String, Object> actividad = comboActividad(itemConDescuento.toString());

            items.add(crearItem(actividad, descuento, id_menbresia));
        }
        /* crear items sin descuento */
        for (Map<String, Object> actividad : actividadesSinDescuento) {
            items.add(crearItem(actividad, 1, id_menbresia));
        }
        return items;
    }

    public void setFechaLimite(String fecha, int id_menbresia, int id_actividad) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(set_fecha_pago);
        ps.setString(1, fecha);
        ps.setInt(2, id_menbresia);
        ps.setInt(3, id_actividad);
        ps.executeUpdate();
    }

    public LocalDate getFecha(Map<String, Object> a) {
        int y = (int) a.get("year");
        int m = (int) a.get("month");
        int d = (int) a.get("day");
        LocalDate f = LocalDate.of(y, m, d);
        return f;
    }

    public String minimaFechaLimite(ArrayList<Map<String, Object>> actividades) {
        String fecha = "";
        if (actividades.size() == 2) {
            LocalDate f1 = getFecha(actividades.get(0));
            LocalDate f2 = getFecha(actividades.get(1));
            if (f1.isBefore(f2)) {
                fecha = f1.toString();
            } else {
                fecha = f2.toString();
            }
        } else {
            LocalDate f1 = getFecha(actividades.get(0));
            LocalDate f2 = getFecha(actividades.get(1));
            LocalDate f3 = getFecha(actividades.get(2));
            if (f1.isBefore(f2) && f1.isBefore(f3)) {
                fecha = f1.toString();
            } else if (f2.isBefore(f3)) {
                fecha = f2.toString();
            } else {
                fecha = f3.toString();
            }
        }
        return fecha;
    }

    public Item crearItem(Map<String, Object> actividad, float descuento, int id_menbresia) {
        String nombre = (String) actividad.get("nombre");
        float precio = (float) (int) actividad.get("precio");
        String menbresia = id_menbresia + "";
        Item item = new Item();
        item.setTitle(nombre)
                .setId(menbresia)
                .setQuantity(1)
                //                .setDescription("Deporte")
                //                .setCurrencyId("ARS")
                .setUnitPrice(precio * descuento);
        return item;
    }

    public ArrayList<Map<String, Object>> actividadesPorMenbresia(int id_menbresia, String consultaActividades) throws SQLException {
        PreparedStatement hojaVirtual = connection.prepareStatement(consultaActividades);
        hojaVirtual.setInt(1, id_menbresia);
        ResultSet data = hojaVirtual.executeQuery();
        ArrayList<Map<String, Object>> actividades = new ArrayList<>();
        while (data.next()) {
            Map<String, Object> actividad = new HashMap<>();
            actividad.put("id_actividad", data.getString("id_actividad"));
            actividad.put("nombre", data.getString("nombre"));
            actividad.put("nickname", data.getString("nickname"));
            actividad.put("precio", data.getInt("precio"));
            actividad.put("grupo_descuento", data.getString("grupo_descuento"));
            actividad.put("year", data.getInt("year"));
            actividad.put("month", data.getInt("month"));
            actividad.put("day", data.getInt("day"));
            actividades.add(actividad);
        }
        return actividades;
    }

    public Map<String, Object> comboActividad(String nombre) throws SQLException {
        PreparedStatement hojaVirtual = connection.prepareStatement(comboActividad);
        hojaVirtual.setString(1, nombre);
        ResultSet data = hojaVirtual.executeQuery();
        Map<String, Object> actividad = new HashMap<>();
        if (data.next()) {
            actividad.put("nombre", data.getString("nombre"));
            actividad.put("precio", data.getInt("precio"));
        }
        return actividad;
    }

    public ArrayList<Float> listaDescuentos() throws SQLException {
        Statement hojaVirtual = connection.createStatement();
        ResultSet data = hojaVirtual.executeQuery(listadescuentos);
        ArrayList<Float> descuentos = new ArrayList<>();
        while (data.next()) {
            descuentos.add(data.getFloat("descuento"));
        }
        return descuentos;
    }

    public ArrayList<Integer> listaMenbresias(int id_usuario, int id_menbresia) throws SQLException {
        PreparedStatement hojaVirtual = connection.prepareStatement(listaemenbresias);
        hojaVirtual.setInt(1, id_usuario);
        hojaVirtual.setInt(2, id_menbresia);
        ResultSet data = hojaVirtual.executeQuery();
        ArrayList<Integer> menbresias = new ArrayList<>();
        while (data.next()) {
            menbresias.add(data.getInt("id_menbresia"));
        }
        return menbresias;
    }

    public void anotarUsuarioActividades(int id_menbresia, ArrayList<Map<String, Object>> actividades) throws SQLException {
        for (int i = 0; i < actividades.size(); i++) {
            Map<String, Object> actividad = actividades.get(i);
            int id_actividad = Integer.parseInt((String) actividad.get("id_actividad"));
            int id_horario = Integer.parseInt((String) actividad.get("id_horario"));
            //anotamos al usuario a la actividad
            anotarUsuario(id_actividad, id_horario, id_menbresia);
        }
    }

    public void anotarUsuario(int id_actividad, int id_horario, int id_menbresia) throws SQLException {
        PreparedStatement hojaVirtual = connection.prepareStatement(anotarUsuarioActividad);
        hojaVirtual.setInt(1, id_actividad);
        hojaVirtual.setInt(2, id_menbresia);
        hojaVirtual.setInt(3, id_horario);
        hojaVirtual.executeUpdate();
    }

    public ArrayList<Map<String, Object>> actividadesHorarios() throws SQLException {
        Statement hojaVirtual = connection.createStatement();
        ResultSet data = hojaVirtual.executeQuery(actvidades_horarios);
        ArrayList<Map<String, Object>> listaActividades = new ArrayList<>();
        while (data.next()) {
            Map<String, Object> actividad = new LinkedHashMap<>();
            actividad.put("nombre actividad", data.getString("nombre"));
            actividad.put("hora", data.getInt("hora"));
            actividad.put("dias", data.getString("dias"));
            actividad.put("precio", data.getInt("precio"));
            actividad.put("nickname", data.getString("nickname"));
            actividad.put("id_actividad", data.getString("id_actividad"));
            actividad.put("id_horario", data.getString("id_horario"));
            listaActividades.add(actividad);
        }
        return listaActividades;
    }

    public ArrayList<Map<String, Object>> usuariosPorActividad() throws SQLException {
        Statement hojaVirtual = connection.createStatement();
        ResultSet data = hojaVirtual.executeQuery(usuariosporactividad);
        ArrayList<Map<String, Object>> usuariosActividades = new ArrayList<>();
        while (data.next()) {
            Map<String, Object> usuarioActividad = new HashMap<>();
            usuarioActividad.put("nombre_actividad", data.getString("nombre"));
            usuarioActividad.put("cantidad_usuarios", data.getInt("cantidad_usuarios"));
            usuariosActividades.add(usuarioActividad);
        }
        return usuariosActividades;
    }

    public ArrayList<Map<String, Object>> cantidadUsuariosPorHorarios() throws SQLException {
        Statement hojaVirtual = connection.createStatement();
        ResultSet data = hojaVirtual.executeQuery(cantidad_usuarios_horarios);
        ArrayList<Map<String, Object>> usuariosHorarios = new ArrayList<>();
        while (data.next()) {
            Map<String, Object> usuarioActividad = new HashMap<>();
            usuarioActividad.put("nombre_actividad", data.getString("nombre"));
            usuarioActividad.put("hora", data.getInt("hora"));
            usuarioActividad.put("dias", data.getString("dias"));
            usuarioActividad.put("cantidad_usuarios", data.getInt("cantidad_usuarios"));
            usuariosHorarios.add(usuarioActividad);
        }
        return usuariosHorarios;
    }

}
