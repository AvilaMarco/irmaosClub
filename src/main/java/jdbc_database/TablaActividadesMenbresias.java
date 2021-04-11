package jdbc_database;

import com.mercadopago.resources.datastructures.preference.Item;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TablaActividadesMenbresias implements DB {

    private Connection connection = null;
    /* Consultas Select */
    private final String usuariosporactividad = "SELECT nombre, COUNT(*) cantidad_usuarios FROM " + TactividadesConUsuarios + " natural join actividades where baneado = 0 group BY id_actividad;";
    private final String usuarios_horarios = "SELECT horarios.id_actividad, horarios.id_horario, COUNT(actividades_menbresias.id_horario) AS cantidad_usuarios, hora FROM horarios LEFT JOIN " + TActividadesConMenbresias + " ON horarios.id_horario = actividades_menbresias.id_horario WHERE pago = 1 GROUP BY horarios.id_horario";
    private final String cantidad_usuarios_horarios = "SELECT nombre, hora, " + CGroupDias + ", cantidad_usuarios FROM ((" + usuarios_horarios + ") AS a NATURAL JOIN actividades) NATURAL JOIN dias_horarios GROUP BY id_horario;";
    private final String actvidades_horarios = "SELECT nickname, id_actividad, id_horario, nombre, hora, " + CGroupDias + ", precio FROM " + TActividadesConDias + " where nickname not like '%especial' GROUP BY id_horario";
    private final String listaemenbresias = "SELECT id_menbresia FROM " + TActividadesConMenbresias + " WHERE id_usuario = ANY (SELECT id_usuario FROM usuarios WHERE id_titular = ?) OR id_menbresia = ? GROUP BY id_menbresia ORDER BY fecha_creacion;";
    private final String listadescuentos = "SELECT * FROM descuentos_bjj";
    private final String comboActividad = "SELECT * FROM combos_actividades_bjj WHERE nombre = ?;";
    private final String listaActividadesInstructores = "SELECT nombre, a.usuario as instructor, hora, " + CGroupDias + ", precio, a.id_menbresia, horarios.id_horario, actividades.id_actividad FROM " + TActividadesConDias + " left join ( select * from " + TactividadesConUsuarios + " where usuarios.id_rol = 2 ) as a on horarios.id_horario = a.id_horario GROUP BY horarios.id_horario;";
    //cambiar consulta id_rool = 2
    private final String listaInstructores = "select usuario as nombre, id_menbresia from usuarios natural join menbresias where id_rol = 2;";
    private final String listaActividades = "select nombre, id_actividad from actividades where precio > 0;";
    private final String existeInstructor = "select * from " + TactividadesConUsuarios + " where id_actividad = ? and id_horario = ? and usuarios.id_rol = 2;";
    /* Consultas Update */
    private final String updateActividadesPagas = "update actividades_menbresias set pago = 0 where pago = 1 and date(fecha_limite) < now();";
    private final String set_fecha_pago = "update actividades_menbresias set fecha_limite = date(?) where id_menbresia = ? and id_actividad = ? and pago = 1";
    private final String update_pago_listo = "update actividades_menbresias natural join actividades set pago_listo = ?  where datediff(fecha_limite, now()) < 8 and id_menbresia = ? and (nombre = ? || nickname = ?);";
    private final String updateNombrePrecioAcitividad = "update actividades set nombre = ?, precio = ? where id_actividad = ?;";
    private final String updateInstructorActividad = "update " + TactividadesConUsuarios + " set actividades_menbresias.id_menbresia = ? where id_actividad = ? and id_horario = ? and usuarios.id_rol = 2;";
    private final String updateHora = "update horarios set hora = ? where id_horario = ?;";
    /* Consultas Create */
    private final String anotarUsuarioActividad = "INSERT INTO actividades_menbresias (id_actividad, id_menbresia, id_horario) VALUE (?, ?, ?)";
    private final String createActividad = "insert into actividades(nombre, nickname, precio) value (?, ?, ?);";
    private final String crearHorario = "insert into horarios(id_actividad, hora) value (?, ?);";
    private final String creaDiaHoraio = "insert into dias_horarios(id_horario, dia) values(?,?);";
    private final String crearInstructorActividad = "insert into actividades_menbresias(id_actividad, id_menbresia, id_horario) value (?, ?, ?);";
    /* Consultas Delete */
    private final String deleteDiasHorario = "delete from dias_horarios where id_horario = ?;";

    public void editActividad(String nombre, int precio, int idActividad, int idHorario, int idInstructor, int hora, ArrayList<String> dias) {
        PreparedStatement psActividad;
        PreparedStatement ps;
        PreparedStatement psInstructor;
        PreparedStatement psDiasHorarios;
        PreparedStatement psHorarios;
        ResultSet rs;
        try {
            /* Transaccion Start */
            connection.setAutoCommit(false);
            /* Actividad */
            psActividad = connection.prepareStatement(updateNombrePrecioAcitividad);
            psActividad.setString(1, nombre);
            psActividad.setInt(2, precio);
            psActividad.setInt(3, idActividad);
            psActividad.executeUpdate();
            psActividad.close();
            /* Instructor */
            if (idInstructor != 0) {
                ps = connection.prepareStatement(existeInstructor);
                ps.setInt(1, idActividad);
                ps.setInt(2, idHorario);
                rs = ps.executeQuery();
                if (rs.next()) {
                    psInstructor = connection.prepareStatement(updateInstructorActividad);
                    psInstructor.setInt(1, idInstructor);
                    psInstructor.setInt(2, idActividad);
                    psInstructor.setInt(3, idHorario);
                } else {
                    psInstructor = connection.prepareStatement(crearInstructorActividad);
                    psInstructor.setInt(1, idActividad);
                    psInstructor.setInt(2, idInstructor);
                    psInstructor.setInt(3, idHorario);
                }
                ps.close();
                rs.close();
                psInstructor.executeUpdate();
                psInstructor.close();
            }
            /* Horario */
            psHorarios = connection.prepareStatement(updateHora);
            psHorarios.setInt(1, hora);
            psHorarios.setInt(2, idHorario);
            psHorarios.executeUpdate();
            psHorarios.close();
            /* Dias Horarios */
            psDiasHorarios = connection.prepareStatement(deleteDiasHorario);
            psDiasHorarios.setInt(1, idHorario);
            psDiasHorarios.executeUpdate();
            psDiasHorarios.close();
            for (String dia : dias) {
                psDiasHorarios = connection.prepareStatement(creaDiaHoraio);
                psDiasHorarios.setInt(1, idHorario);
                psDiasHorarios.setString(2, dia);
                psDiasHorarios.executeUpdate();
                psDiasHorarios.close();
            }
            connection.commit();
            /* Transaccion End */
        } catch (SQLException e) {
            Logger.getLogger(TablaActividadesMenbresias.class.getName()).log(Level.SEVERE, null, e);
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    System.out.println(ex.toString());
                }
            }
        }
    }

    public void crearActividad(int id_Actividad, String nombre, int precio, int hora, ArrayList<String> dias) {
        int idActividad = 0;
        int idHorario = 0;
        PreparedStatement psActividad;
        PreparedStatement psHorario;
        PreparedStatement psDiasHorarios;
        try {
            /* Transaccion Start */
            connection.setAutoCommit(false);
            /* Actividad */
            if (id_Actividad == 0) {
                psActividad = connection.prepareStatement(createActividad, Statement.RETURN_GENERATED_KEYS);
                psActividad.setString(1, nombre);
                psActividad.setString(2, nombre);
                psActividad.setInt(3, precio);
                psActividad.executeUpdate();
                ResultSet rsActividad = psActividad.getGeneratedKeys();
                if (rsActividad.next()) {
                    idActividad = rsActividad.getInt(1);
                }
            } else {
                idActividad = id_Actividad;
            }
            /* Horario */
            psHorario = connection.prepareStatement(crearHorario, Statement.RETURN_GENERATED_KEYS);
            psHorario.setInt(1, idActividad);
            psHorario.setInt(2, hora);
            psHorario.executeUpdate();
            ResultSet rsHorario = psHorario.getGeneratedKeys();
            if (rsHorario.next()) {
                idHorario = rsHorario.getInt(1);
            }
            /* Dias Horarios */
            for (String dia : dias) {
                psDiasHorarios = connection.prepareStatement(creaDiaHoraio);
                psDiasHorarios.setInt(1, idHorario);
                psDiasHorarios.setString(2, dia);
                psDiasHorarios.executeUpdate();
            }
            connection.commit();
            /* Transaccion End */
        } catch (SQLException e) {
            Logger.getLogger(TablaActividadesMenbresias.class.getName()).log(Level.SEVERE, null, e);
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    System.out.println(ex.toString());
                }
            }
        }
    }

    public List<Map<String, Object>> listaCrudActividades() throws SQLException {
        ArrayList<Map<String, Object>> actividades = new ArrayList<>();
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(listaActividades);
        while (rs.next()) {
            Map<String, Object> actividad = new LinkedHashMap<>();
            actividad.put("nombre", rs.getString("nombre"));
            actividad.put("id_actividad", rs.getInt("id_actividad"));
            actividades.add(actividad);
        }
        cerrarRecursos(null, st, rs);
        return actividades;
    }

    public List<Map<String, Object>> listaCrudInstructores() throws SQLException {
        ArrayList<Map<String, Object>> instructores = new ArrayList<>();
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(listaInstructores);
        while (rs.next()) {
            Map<String, Object> instructor = new LinkedHashMap<>();
            instructor.put("nombre", rs.getString("nombre"));
            instructor.put("id_menbresia", rs.getInt("id_menbresia"));
            instructores.add(instructor);
        }
        cerrarRecursos(null, st, rs);
        return instructores;
    }

    public List<Map<String, Object>> listaCrudActividadesData() throws SQLException {
        ArrayList<Map<String, Object>> actividades = new ArrayList<>();
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(listaActividadesInstructores);
        while (rs.next()) {
            Map<String, Object> actividad = new LinkedHashMap<>();
            String instructor = rs.getString("instructor");
            if (instructor == null) {
                instructor = "Sin Instructor";
            }
            actividad.put("nombre", rs.getString("nombre"));
            actividad.put("instructor", instructor);
            actividad.put("hora", rs.getInt("hora"));
            actividad.put("dias", rs.getString("dias"));
            actividad.put("precio", rs.getInt("precio"));
            actividad.put("id_menbresia", rs.getInt("id_menbresia"));
            actividad.put("id_horario", rs.getInt("id_horario"));
            actividad.put("id_actividad", rs.getInt("id_actividad"));
            actividades.add(actividad);
        }
        cerrarRecursos(null, st, rs);
        return actividades;
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
        cerrarRecursos(ps, null, null);
    }

    public void actulizarActividadesPagas() throws SQLException {
        Statement s = connection.createStatement();
        s.executeUpdate(updateActividadesPagas);
        cerrarRecursos(null, s, null);
    }

    public ArrayList<Map<String, Object>> crearItemsConDescuento(ArrayList<Integer> menbresias, String consultaActividades) throws SQLException {
        ArrayList<Float> descuentos = listaDescuentos();
        ArrayList<Map<String, Object>> items = new ArrayList<>();
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
            System.out.println("menbresia: " + menbresia);
            System.out.println(actividades);
            items.addAll(crearItems(actividades, descuento, menbresia));
        }
        return items;
    }

    public ArrayList<Map<String, Object>> crearItems(ArrayList<Map<String, Object>> actividades, float descuento, int id_menbresia) throws SQLException {
        ArrayList<Map<String, Object>> items = new ArrayList<>();
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
            Map<String, Object> actividad = comboActividad(itemConDescuento.toString(), fechaLimite);
            System.out.println("antes de crear item");
            System.out.println("menbresia: " + id_menbresia);
            System.out.println(actividad);
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
        cerrarRecursos(ps, null, null);
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

    public Map<String, Object> crearItem(Map<String, Object> actividad, float descuento, int id_menbresia) {
        String nombre = (String) actividad.get("nombre");
        float precio = (float) (int) actividad.get("precio");
        String menbresia = id_menbresia + "";
        String fecha = (String) actividad.get("fecha_limite");
        Map<String, Object> item =  new LinkedHashMap<>();
        System.out.println(actividad);
        item.put("id", menbresia);
        item.put("nombre", nombre);
        item.put("precio", precio * descuento);
        item.put("fecha vencimiento", fecha);
        return item;
//        Item item = new Item();
//        item.setTitle(nombre)
//                .setId(menbresia)
//                .setQuantity(1)
//                //                .setDescription("Deporte")
//                //                .setCurrencyId("ARS")
//                .setUnitPrice(precio * descuento);
//        return item;
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
            actividad.put("fecha_limite", data.getString("fecha_limite"));
            if (data.getString("nombre").equals("BJJ Adultos Avanzados")) {
                Boolean esta = false;
                for (Map<String, Object> actividadFor : actividades) {
                    if (actividadFor.get("nombre").equals("BJJ Adultos Avanzados")) {
                        esta = true;
                        actividadFor.replace("precio", 2000);
                        break;
                    }
                }
                if (!esta) {
                    actividades.add(actividad);
                }
            } else {
                actividades.add(actividad);
            }
        }
        cerrarRecursos(hojaVirtual, null, data);
        return actividades;
    }

    public Map<String, Object> comboActividad(String nombre, String fecha_limite) throws SQLException {
        PreparedStatement hojaVirtual = connection.prepareStatement(comboActividad);
        hojaVirtual.setString(1, nombre);
        ResultSet data = hojaVirtual.executeQuery();
        Map<String, Object> actividad = new HashMap<>();
        if (data.next()) {
            actividad.put("nombre", data.getString("nombre"));
            actividad.put("precio", data.getInt("precio"));
            actividad.put("fecha_limite", fecha_limite);
        }
        cerrarRecursos(hojaVirtual, null, data);
        return actividad;
    }

    public ArrayList<Float> listaDescuentos() throws SQLException {
        Statement hojaVirtual = connection.createStatement();
        ResultSet data = hojaVirtual.executeQuery(listadescuentos);
        ArrayList<Float> descuentos = new ArrayList<>();
        while (data.next()) {
            descuentos.add(data.getFloat("descuento"));
        }
        cerrarRecursos(null, hojaVirtual, data);
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
        cerrarRecursos(hojaVirtual, null, data);
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
        cerrarRecursos(hojaVirtual, null, null);
    }

    public ArrayList<Map<String, Object>> actividadesHorarios() throws SQLException {
        Statement hojaVirtual = connection.createStatement();
        ResultSet data = hojaVirtual.executeQuery(actvidades_horarios);
        ArrayList<Map<String, Object>> listaActividades = new ArrayList<>();
        while (data.next()) {
            Map<String, Object> actividad = new LinkedHashMap<>();
            actividad.put("nombre", data.getString("nombre"));
            actividad.put("hora", data.getInt("hora"));
            actividad.put("dias", data.getString("dias"));
            actividad.put("precio", data.getInt("precio"));
            actividad.put("nickname", data.getString("nickname"));
            actividad.put("id_actividad", data.getString("id_actividad"));
            actividad.put("id_horario", data.getString("id_horario"));
            listaActividades.add(actividad);
        }
        cerrarRecursos(null, hojaVirtual, data);
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
        cerrarRecursos(null, hojaVirtual, data);
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
        cerrarRecursos(null, hojaVirtual, data);
        return usuariosHorarios;
    }

    public TablaActividadesMenbresias() {
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

}
