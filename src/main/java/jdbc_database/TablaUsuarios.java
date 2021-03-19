package jdbc_database;

import com.google.common.collect.HashBiMap;
import java.sql.*;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import models.Usuario;

public class TablaUsuarios {

    private Connection connection = null;
    private final String table = "usuarios";
    //campos de la tabla
    private final String campoId = "id_usuario";
    private final String campoUsuario = "usuario";
    private final String campoEmail = "email";
    private final String campoPassword = "password";
    private final String campoRol = "id_rol";
    private final String campoFechaRegistro = "fecha_registro";
    private final String campoFechaNacimiento = "fecha_nacimiento";
    private final String campoDni = "dni";
    private final String campoCelular = "celular";
    private final String campoCelularEmergencia = "celular_emergencia";
    private final String campoNombres = "nombres";
    private final String campoApellidos = "apellidos";
    private final String campoDireccion = "direccion";
    private final String campoCertificadoSalud = "certificado_salud";
    private final String campoObservacion = "observacion";
    private final String campoIdTitular = "id_titular";
    private final String campoActivo = "activo";
    //consultas
    /*private final String createuser = "INSERT INTO usuarios (usuario,email,password) VALUE (\"nuevoUsuario\",\"correo\",\"1234\")";
    private final String updateuser = "UPDATE usuarios SET email = \"usuario@correo.com\" WHERE id_usuario = 73";
    private final String deleteuser = "DELETE FROM `usuarios` WHERE id_usuario = 73";
    private final String selectUser = "SELECT * FROM usuarios where id_usuario = 1";*/
//    private final String createUser = "INSERT INTO " + table + "(usuario, email, password) VALUE (?, ?, ?)";
//    private final String updateemail = "UPDATE " + table + " SET email = ? WHERE id_usuario = ?";
    private final String createuser = "INSERT INTO " + table + " ";
    private final String updateuser = "UPDATE " + table + " SET ";
    //modificar ondelete cascade
    private final String deleteuser = "DELETE FROM " + table + " WHERE id_usuario = ?";
    private final String banearuser = "UPDATE " + table + " SET baneado = 1 WHERE id_usuario = ?";
    private final String allusers = "SELECT * FROM " + table + " WHERE id_rol = 1 and baneado = 0";
    private final String selectuserbyid = "SELECT * FROM " + table + " where id_usuario = ?";
    private final String login = "SELECT * FROM " + table + " WHERE email = ? and password = ? and baneado = 0;";
    //dashboard
    private final String cantusersactivos = "select count(*) cantidad_usuarios from usuarios where activo = 1;";
    private final String actividadusuario = "SELECT id_actividad, hora, dias, nombre, nickname FROM usuarios NATURAL JOIN menbresias NATURAL JOIN (SELECT id_horario, id_actividad, hora, GROUP_CONCAT(dia SEPARATOR ', ') AS dias FROM (horarios NATURAL JOIN dias_horarios) GROUP BY id_horario) AS tabla1 NATURAL JOIN (actividades_menbresias NATURAL JOIN actividades) WHERE id_usuario = ?";
    private final String crearmenbresia = "INSERT INTO menbresias (id_usuario) value (?)";
    private final String get_menbresia = "SELECT id_menbresia FROM menbresias WHERE id_usuario = ?";
    private final String get_idtitular = "SELECT id_titular FROM usuarios WHERE id_usuario = ?";
//    private final String actividades_menbresia = "SELECT id_actividad, nombre, nickname, precio, grupo_descuento FROM actividades_menbresias NATURAL JOIN actividades WHERE id_menbresia = ? ORDER BY nickname;";
    private final String listaidfamiliares = "select id_usuario from usuarios where id_titular = ?";
    private final String tarjetausuario = "SELECT id_usuario, nombres, apellidos, usuario, concat(apellidos, \" \", nombres) nombre, email, floor( DATEDIFF(NOW(), fecha_nacimiento) / 365) as edad FROM usuarios WHERE id_usuario = ?";
    private final String tarjetausuarioactividades = "SELECT fecha_limite, nombre, hora, GROUP_CONCAT(dia SEPARATOR ', ') AS dias FROM actividades_menbresias natural join actividades NATURAL JOIN horarios NATURAL JOIN dias_horarios where id_menbresia = ? group by id_horario;";
    private final String usuarioPorNombre = "select * from usuarios where usuario = ?;";
    private final String usuarioPorEmailODNI = "select * from usuarios where dni = ? or email = ?;";
    private final String updatePassword = "UPDATE `usuarios` SET password = ? WHERE id_usuario = ?;";

    public TablaUsuarios() {
        connection = new Mysql().getConexion();
    }

    public ArrayList<Integer> getIdFamiliares(int id_usuario) throws SQLException {
        ArrayList<Integer> idFamiliares = new ArrayList<>();
        PreparedStatement hojaVirtual = connection.prepareStatement(listaidfamiliares);
        hojaVirtual.setInt(1, id_usuario);
        ResultSet data = hojaVirtual.executeQuery();
        while (data.next()) {
            idFamiliares.add(data.getInt(campoId));
        }
        return idFamiliares;
    }

    public void resetearContraseña(String pass, int id_usuario) throws SQLException{
        PreparedStatement ps = connection.prepareStatement(updatePassword);
        System.out.println(pass);
        System.out.println(id_usuario);
        ps.setString(1, pass);
        ps.setInt(2, id_usuario);
        ps.executeUpdate();
    }
    
    public Map<String, Object> tarjetaUsuario(int id_usuario) throws SQLException {
        Map<String, Object> usuario = new HashMap<>();
        PreparedStatement hojaVirtual = connection.prepareStatement(tarjetausuario);
        hojaVirtual.setInt(1, id_usuario);
        ResultSet data = hojaVirtual.executeQuery();
        if (data.next()) {
            usuario.put("id", data.getInt("id_usuario"));
            usuario.put("usuario", data.getString("usuario"));
            usuario.put("nombres", data.getString("nombres"));
            usuario.put("apellidos", data.getString("apellidos"));
            usuario.put("email", data.getString("email"));
            usuario.put("edad", data.getInt("edad"));
        }
        int menbresia = getMenbresia(id_usuario);
        if (menbresia != 0) {
            usuario.put("actividades", tarjetaActividades(menbresia));
        }
        return usuario;
    }

    public ArrayList<Map<String, Object>> tarjetaActividades(int menbresia) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(tarjetausuarioactividades);
        ps.setInt(1, menbresia);
        ResultSet rs = ps.executeQuery();
        ArrayList<Map<String, Object>> actividades = new ArrayList<>();
        while (rs.next()) {
            Map<String, Object> actividad = new HashMap<>();
            actividad.put("nombre", rs.getString("nombre"));
            actividad.put("hora", rs.getString("hora"));
            actividad.put("dias", rs.getString("dias"));
            actividad.put("fecha_limite", rs.getString("fecha_limite"));
            actividades.add(actividad);
        }
        return actividades;
    }

    public boolean usuarioExiste(Map<String, String[]> data) throws SQLException {
        String usuario = data.get("usuario")[0];
        PreparedStatement ps = connection.prepareStatement(usuarioPorNombre);
        ps.setString(1, usuario);
        ResultSet rs = ps.executeQuery();
        return rs.next();
    }

    public boolean emailODniExiste(Map<String, String[]> data) throws SQLException {
        String email = data.get("email")[0];
        int dni = Integer.parseInt(data.get("dni")[0]);
        PreparedStatement ps = connection.prepareStatement(usuarioPorEmailODNI);
        ps.setInt(1, dni);
        ps.setString(2, email);
        ResultSet rs = ps.executeQuery();
        return rs.next();
    }

    public int getMenbresia(int id_usuario) throws SQLException {
        PreparedStatement hojaVirtual = connection.prepareStatement(get_menbresia);
        hojaVirtual.setInt(1, id_usuario);
        ResultSet data = hojaVirtual.executeQuery();
        int idMenbresia = 0;
        if (data.next()) {
            idMenbresia = data.getInt(1);
        }
        return idMenbresia;
    }

    public int getIdTitular(int id_usuario) throws SQLException {
        PreparedStatement hojaVirtual = connection.prepareStatement(get_idtitular);
        hojaVirtual.setInt(1, id_usuario);
        ResultSet data = hojaVirtual.executeQuery();
        int idTitular = 0;
        if (data.next()) {
            idTitular = data.getInt(1);
        }
        return idTitular;
    }

    public int crearMenbresia(int id_usuario) throws SQLException {
        PreparedStatement hojaVirtual = connection.prepareStatement(crearmenbresia, Statement.RETURN_GENERATED_KEYS);
        hojaVirtual.setInt(1, id_usuario);
        hojaVirtual.executeUpdate();
        ResultSet data = hojaVirtual.getGeneratedKeys();
        int idMenbresia = 0;
        if (data.next()) {
            idMenbresia = data.getInt(1);
        }
        return idMenbresia;
    }

    public void banearUsuario(int id_usuario) throws SQLException {
        PreparedStatement hojaVirtual = connection.prepareStatement(banearuser);
        hojaVirtual.setInt(1, id_usuario);
        hojaVirtual.executeUpdate();
    }

    public void updateUser(Map<String, String[]> data, int id_usuario) throws SQLException {
        String consultasql = consultaUpdate(data);
        PreparedStatement hojaVirtual = connection.prepareStatement(consultasql);
        hojaVirtual.setInt(1, id_usuario);
        hojaVirtual.executeUpdate();
    }

    public void createUser(Map<String, String[]> data) throws SQLException {
        Statement hojaVirtual = connection.createStatement();
        String consultasql = consultaCreate(data);
        hojaVirtual.executeUpdate(consultasql);
    }

    public String consultaUpdate(Map<String, String[]> data) {
        Set<String> setCampos = data.keySet();
        String[] campos = new String[setCampos.size()];
        setCampos.toArray(campos);
        StringBuffer claves = new StringBuffer();
        for (int i = 0; i < campos.length; i++) {
            String campo = campos[i];
            Object valor = tryParse(data.get(campo)[0]);
            if (i == campos.length - 1) {
                claves.append(campo + " = " + valor);
            } else {
                claves.append(campo + " = " + valor + ", ");
            }
        }
        String consultasql = updateuser + claves.toString() + " WHERE id_usuario = ?;";
        return consultasql;
    }

    public String consultaCreate(Map<String, String[]> data) {
        Set<String> setCampos = data.keySet();
        String[] campos = new String[setCampos.size()];
        setCampos.toArray(campos);
        StringBuffer claves = new StringBuffer();
        StringBuffer valores = new StringBuffer();
        claves.append("(");
        valores.append("(");
        System.out.println("valores");
        for (int i = 0; i < campos.length; i++) {
            String campo = campos[i];
            Object valor = tryParse(data.get(campos[i])[0]);
            System.out.println(valor);
            if (i == campos.length - 1) {
                claves.append(campo + ")");
                valores.append(valor + ")");
            } else {
                claves.append(campos[i] + ", ");
                valores.append(valor + ", ");
            }
        }
        String consultasql = createuser + claves.toString() + " VALUE " + valores.toString();
        return consultasql;
    }

    //para poder convertir a texto cuando es necesario
    public static Object tryParse(String valor) {
        try {
            return Long.parseLong(valor);
        } catch (NumberFormatException e) {
            return "\"" + valor + "\"";
        }
    }

    public Map<String, Object> actividadUsuario(int idUsuario) throws SQLException {
        PreparedStatement hojaVirtual = connection.prepareStatement(actividadusuario);
        hojaVirtual.setInt(1, idUsuario);
        ResultSet data = hojaVirtual.executeQuery();
        Map<String, Object> infoActividad = new HashMap<>();
        if (data.next()) {
            infoActividad.put("id_actividad", data.getInt("id_actividad"));
            infoActividad.put("hora", data.getInt("hora"));
            infoActividad.put("dias", data.getString("dias"));
            infoActividad.put("nombre", data.getString("nombre"));
            infoActividad.put("nickname", data.getString("nickname"));
        }
        return infoActividad;
    }

    public int cantUsuariosActivos() throws SQLException {
        Statement hojaVirtual = connection.createStatement();
        ResultSet data = hojaVirtual.executeQuery(cantusersactivos);
        int result = 0;
        if (data.next()) {
            result = data.getInt("cantidad_usuarios");
        }
        return result;
    }

    public Usuario loginUser(String email, String password) throws SQLException {
        PreparedStatement hojaVirtual = connection.prepareStatement(login);
        hojaVirtual.setString(1, email);
        hojaVirtual.setString(2, password);
        ResultSet data = hojaVirtual.executeQuery();
        Usuario usuario = null;
        if (data.next()) {
            int id = data.getInt(campoId);
            String usuarioNombre = data.getString(campoUsuario);
            int id_rol = data.getInt(campoRol);
            String registro = data.getString(campoFechaRegistro);
            String nacimiento = data.getString(campoFechaNacimiento);
            int dni = data.getInt(campoDni);
            Long celular = data.getLong(campoCelular);
            Long celularEmergencia = data.getLong(campoCelularEmergencia);
            String nombres = data.getString(campoNombres);
            String apellidos = data.getString(campoApellidos);
            String direccion = data.getString(campoDireccion);
            boolean certificadoSalud = data.getBoolean(campoCertificadoSalud);
            String observacion = data.getString(campoObservacion);
            int idTitular = data.getInt(campoIdTitular);
            boolean activo = data.getBoolean(campoActivo);
            usuario = new Usuario(id, usuarioNombre, email, password, id_rol, registro, nacimiento, dni, celular, celularEmergencia, nombres, apellidos, direccion, certificadoSalud, observacion, idTitular, activo);
        }
        return usuario;
    }

//    public void createUser(String usuario, String email, String password) throws SQLException {
//        //Statement hojaVirtual = connection.createStatement();
//        PreparedStatement hojaVirtual = connection.prepareStatement(createUser);
//        hojaVirtual.setString(1, usuario);
//        hojaVirtual.setString(2, email);
//        hojaVirtual.setString(3, password);
//        hojaVirtual.executeUpdate();
//        System.out.println("usuario creado");
//    }
//
//    public void updateEmailById(String email, int id_usuario) throws SQLException {
//        //Statement hojaVirtual = connection.createStatement();
//        PreparedStatement hojaVirtual = connection.prepareStatement(updateemail);
//        hojaVirtual.setString(1, email);
//        hojaVirtual.setInt(2, id_usuario);
//        hojaVirtual.executeUpdate();
//        System.out.println("actualizacion del correo correcta");
//    }
//
//    public void deleteUserById(int id_usuario) throws SQLException {
//        //Statement hojaVirtual = connection.createStatement();
//        PreparedStatement hojaVirtual = connection.prepareStatement(deleteuserbyid);
//        hojaVirtual.setInt(1, id_usuario);
//        hojaVirtual.executeUpdate();
//        System.out.println("usuario borrado");
//    }
    public ArrayList<Usuario> getUsers() throws SQLException {
        Statement hojaVirtual = connection.createStatement();
        ResultSet data = hojaVirtual.executeQuery(allusers);
        ArrayList<Usuario> usuarios = new ArrayList();
        while (data.next()) {
            int id = data.getInt(campoId);
            String usuario = data.getString(campoUsuario);
            String email = data.getString(campoEmail);
            String password = data.getString(campoPassword);
            int id_rol = data.getInt(campoRol);
            String registro = data.getString(campoFechaRegistro);
            String nacimiento = data.getString(campoFechaNacimiento);
            int dni = data.getInt(campoDni);
            Long celular = data.getLong(campoCelular);
            Long celularEmergencia = data.getLong(campoCelularEmergencia);
            String nombres = data.getString(campoNombres);
            String apellidos = data.getString(campoApellidos);
            String direccion = data.getString(campoDireccion);
            boolean certificadoSalud = data.getBoolean(campoCertificadoSalud);
            String observacion = data.getString(campoObservacion);
            int idTitular = data.getInt(campoIdTitular);
            boolean activo = data.getBoolean(campoActivo);
            Usuario user = new Usuario(id, usuario, email, password, id_rol, registro, nacimiento, dni, celular, celularEmergencia, nombres, apellidos, direccion, certificadoSalud, observacion, idTitular, activo);
            usuarios.add(user);
        }
        return usuarios;
    }

    public Usuario getUser(int id_usuario) throws SQLException {
        Usuario user = null;
        PreparedStatement hojaVirtual = connection.prepareStatement(selectuserbyid);
        hojaVirtual.setInt(1, id_usuario);
        ResultSet data = hojaVirtual.executeQuery();
        while (data.next()) {
            int id = data.getInt(campoId);
            String nombre = data.getString(campoUsuario);
            String email = data.getString(campoEmail);
            String password = data.getString(campoPassword);
            int id_rol = data.getInt(campoRol);
            String registro = data.getString(campoFechaRegistro);
            user = new Usuario(id, nombre, email, password, id_rol, registro);
        }
        return user;
    }
}
