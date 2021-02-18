package models;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class Usuario {

    //propiedades
    private int id_usuario;
    private String usuario;
    private String email;
    private String password;
    private int id_rol;
    private String fecha_registro;
    private String nacimiento;
    private int dni;
    private Long celular;
    private Long celularEmergencia;
    private String nombres;
    private String apellidos;
    private String direccion;
    private boolean certificadoSalud;
    private String observacion;
    private int idTitular;
    private boolean activo;

    //metodo constructor
    public Usuario(int id_usuario, String usuario, String email, String password, int id_rol, String fecha_registro) {
        this.id_usuario = id_usuario;
        this.usuario = usuario;
        this.email = email;
        this.password = password;
        this.id_rol = id_rol;
        this.fecha_registro = fecha_registro;
    }

    public Usuario(int id_usuario, String usuario, String email, String password, int id_rol, String fecha_registro, String nacimiento, int dni, Long celular, Long celularEmergencia, String nombres, String apellidos, String direccion, boolean certificadoSalud, String observacion, int idTitular, boolean activo) {
        this.id_usuario = id_usuario;
        this.usuario = usuario;
        this.email = email;
        this.password = password;
        this.id_rol = id_rol;
        this.fecha_registro = fecha_registro;
        this.nacimiento = nacimiento;
        this.dni = dni;
        this.celular = celular;
        this.celularEmergencia = celularEmergencia;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.direccion = direccion;
        this.certificadoSalud = certificadoSalud;
        this.observacion = observacion;
        this.idTitular = idTitular;
        this.activo = activo;
    }

    //getters

    public int getDni() {
        return dni;
    }

    public String getDireccion() {
        return direccion;
    }

    public Long getCelular() {
        return celular;
    }
    
    public int getId_usuario() {
        return id_usuario;
    }

    public String getApellidos(){
        return apellidos;
    }
    
    public String getUsuario() {
        return usuario;
    }

    public String getEmail() {
        return email;
    }

    public int getRol() {
        return id_rol;
    }

    public String getFecha_registro() {
        return fecha_registro;
    }

    public String getData() {
        return "usuario: " + usuario + "\n" + "email: " + email;
    }

    public Map<String, Object> getInfo() {
        Map<String, Object> result = new HashMap();
        result.put("usuario", usuario);
        result.put("email", email);
        result.put("id_rol", id_rol);
        result.put("fecha_registro", fecha_registro);
        return result;
    }

    public Map<String, Object> getCompleteData() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", id_usuario);
        result.put("usuario", usuario);
        result.put("nombres", nombres);
        result.put("apellidos", apellidos);
        result.put("email", email);
        result.put("celular", celular);
        result.put("celular_emergencia", celularEmergencia);
        result.put("direccion", direccion);
        result.put("certificado_salud", certificadoSalud);
        result.put("observacion", observacion);
        result.put("fecha_registro", fecha_registro);
        result.put("activo", activo);
        return result;
    }

    //setters
    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
