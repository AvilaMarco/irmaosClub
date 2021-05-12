package jdbc_database;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TablaClases {

    private Connection connection = null;
    //consultas
    private final String crearclase = "insert into clases(id_actividad, id_horario) value (?, ?)";
    //agregar luego: and abs(hour(now()) - hora) between 0 and 1 and abs(minute(now()) - minute(hora)) between 0 and 15
    private final String gethorario = "select id_horario from horarios natural join dias_horarios where id_actividad = ? and dia = ?";
    private final String alumnosclase = "SELECT id_usuario, apellidos, nombres FROM actividades_menbresias NATURAL JOIN (menbresias NATURAL JOIN usuarios) WHERE id_actividad = ? AND id_horario = ? and id_rol= 1 AND pago = 1;";
    private final String idultimaclase = "SELECT id_clase, id_horario, dayname(fecha) as dia FROM clases WHERE id_actividad = ? AND finalizada = 0";
    private final String datosultimaclase = "SELECT id_usuario, apellidos, nombres FROM clases_usuarios natural join usuarios WHERE id_clase = ? AND baneado = 0";
    private final String finalizarclase = "update clases set finalizada = 1 where id_clase = ?";

    public TablaClases() {
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

    public void finalizarClase(int idClase) throws SQLException {
        PreparedStatement hojaVirtual = connection.prepareStatement(finalizarclase);
        hojaVirtual.setInt(1, idClase);
        hojaVirtual.executeUpdate();
        cerrarRecursos(hojaVirtual, null, null);
    }

    public Map<String, Object> recuperarUltimaClase(int idActividad) throws SQLException {
        PreparedStatement consulta1 = connection.prepareStatement(idultimaclase);
        consulta1.setInt(1, idActividad);
        ResultSet idUltimaClase = consulta1.executeQuery();
        ArrayList<Map<String, Object>> presentes;
        Map<String, Object> respuesta = null;
        if (idUltimaClase.next()) {
            PreparedStatement consulta2 = connection.prepareStatement(datosultimaclase);
            int idUltimaclase = idUltimaClase.getInt("id_clase");
            int idHorario = idUltimaClase.getInt("id_horario");
            String dia = idUltimaClase.getString("dia");
            consulta2.setInt(1, idUltimaclase);
            cerrarRecursos(consulta1, null, idUltimaClase);
            ResultSet data = consulta2.executeQuery();
            respuesta = new LinkedHashMap<>();
            presentes = new ArrayList<>();
            while (data.next()) {
                Map<String, Object> usuario = new LinkedHashMap<>();
                usuario.put("id_usuario", data.getInt("id_usuario"));
                usuario.put("apellidos", data.getString("apellidos"));
                usuario.put("nombres", data.getString("nombres"));
                presentes.add(usuario);
            }
            respuesta.put("id_clase", idUltimaclase);
            respuesta.put("presentes", presentes);
            respuesta.put("id_horario", idHorario);
            respuesta.put("dia", dia);
            cerrarRecursos(consulta2, null, data);
        }
        return respuesta;
    }

    public void tomarLista(int id_clase, String[] presentes) throws SQLException {
        StringBuffer tomarLista = new StringBuffer();
        tomarLista.append("insert into clases_usuarios(id_clase, id_usuario) values ");
        for (int i = 0; i < presentes.length; i++) {
            String addPresente;
            if (i == presentes.length - 1) {
                addPresente = "(" + id_clase + ", " + Integer.parseInt(presentes[i]) + ");";
            } else {
                addPresente = "(" + id_clase + ", " + Integer.parseInt(presentes[i]) + "),";
            }
            tomarLista.append(addPresente);
        }
        Statement hojaVirtual = connection.createStatement();
        hojaVirtual.executeUpdate(tomarLista.toString());
        cerrarRecursos(null, hojaVirtual, null);
    }

    public int crearClase(int id_actividad, int id_horario) throws SQLException {
        PreparedStatement hojaVirtual = connection.prepareStatement(crearclase, Statement.RETURN_GENERATED_KEYS);
        hojaVirtual.setInt(1, id_actividad);
        hojaVirtual.setInt(2, id_horario);
        hojaVirtual.executeUpdate();
        ResultSet data = hojaVirtual.getGeneratedKeys();
        int idClase = 0;
        if (data.next()) {
            idClase = data.getInt(1);
        }
        cerrarRecursos(hojaVirtual, null, data);
        return idClase;
    }

    public int obtenerIdHorario(int idActividad, String dia) throws SQLException {
        PreparedStatement hojaVirtual = connection.prepareStatement(gethorario);
        hojaVirtual.setInt(1, idActividad);
        hojaVirtual.setString(2, dia);
        ResultSet data = hojaVirtual.executeQuery();
        int idHorario = 0;
        if (data.next()) {
            idHorario = data.getInt("id_horario");
        }
        cerrarRecursos(hojaVirtual, null, data);
        return idHorario;
    }

    public ArrayList<Map<String, Object>> alumnosClase(int idActividad, int id_horario) throws SQLException {
        PreparedStatement hojaVirtual = connection.prepareStatement(alumnosclase);
        hojaVirtual.setInt(1, idActividad);
        hojaVirtual.setInt(2, id_horario);
        ResultSet data = hojaVirtual.executeQuery();
        ArrayList<Map<String, Object>> alumnos = new ArrayList<>();
        while (data.next()) {
            Map<String, Object> alumno = new LinkedHashMap<>();
            alumno.put("id_usuario", data.getInt("id_usuario"));
            alumno.put("apellidos", data.getString("apellidos"));
            alumno.put("nombres", data.getString("nombres"));
            alumnos.add(alumno);
        }
        cerrarRecursos(hojaVirtual, null, data);
        return alumnos;
    }
}
