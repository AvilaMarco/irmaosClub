package controller;

import com.google.gson.Gson;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import jdbc_database.TablaUsuarios;

@WebServlet(name = "UserCreate", urlPatterns = {"/createuser"})
@MultipartConfig
public class UserCreate extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Gson gson = new Gson();
        Map<String, Object> respuesta = new HashMap();
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        Map<String, String[]> data = request.getParameterMap();
        TablaUsuarios consultas = new TablaUsuarios();
        try {
            if (consultas.usuarioExiste(data)) {
                respuesta.put("icon", "error");
                respuesta.put("title", "Oops...");
                respuesta.put("text", "El nombre de usuario ya existe");
            } else if (consultas.emailODniExiste(data)) {
                respuesta.put("icon", "error");
                respuesta.put("title", "Oops...");
                respuesta.put("text", "El DNI o el Email ya existen");
            } else {
                consultas.createUser(data);
                respuesta.put("icon", "success");
                respuesta.put("title", "Felicidades");
                respuesta.put("text", "Usuario creado Correctamente");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        out.print(gson.toJson(respuesta));
        consultas.cerrarConexion();
        out.close();
    }
}
