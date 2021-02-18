package jdbc_database;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class TablaActividadesMenbresias {

    private Connection connection = null;
    //consultas
    private String usuariosporactividad = "SELECT nombre, COUNT(*) cantidad_usuarios FROM usuarios natural join menbresias natural join actividades_menbresias natural join actividades where baneado = 0 group BY id_actividad;";
    //cantidad de usuarios por horario-actividad
    private String usuarios_horarios = "SELECT horarios.id_actividad, horarios.id_horario, COUNT(actividades_menbresias.id_horario) AS cantidad_usuarios, hora FROM horarios LEFT JOIN actividades_menbresias natural join menbresias natural join (select * from usuarios where usuarios.baneado = 0) as a ON horarios.id_horario = actividades_menbresias.id_horario GROUP BY horarios.id_horario";
    private String cantidad_usuarios_horarios = "SELECT nombre, hora, GROUP_CONCAT(dia SEPARATOR ', ') AS dias, cantidad_usuarios FROM ((" + usuarios_horarios + ") AS a NATURAL JOIN actividades) NATURAL JOIN dias_horarios GROUP BY id_horario;";
    private String actvidades_horarios = "SELECT nickname, id_actividad, id_horario, nombre, hora, GROUP_CONCAT(dia SEPARATOR ', ') AS dias, precio FROM actividades NATURAL JOIN horarios NATURAL JOIN dias_horarios where nickname not like \"%especial\" GROUP BY id_horario";
    private String anotarUsuarioActividad = "INSERT INTO actividades_menbresias (id_actividad, id_menbresia, id_horario) VALUE (?, ?, ?)";
    private String listaemenbresias = "SELECT id_menbresia FROM actividades_menbresias NATURAL JOIN menbresias WHERE id_usuario = ANY (SELECT id_usuario FROM usuarios WHERE id_titular = ?) OR id_menbresia = ? GROUP BY id_menbresia ORDER BY fecha_creacion;";
    private String listadescuentos = "SELECT * FROM descuentos_bjj";
    private String comboActividad = "SELECT * FROM combos_actividades_bjj WHERE nombre = ?;";

    public TablaActividadesMenbresias() {
        connection = new Mysql().getConexion();
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
        hojaVirtual.setInt(1, id_menbresia);
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
