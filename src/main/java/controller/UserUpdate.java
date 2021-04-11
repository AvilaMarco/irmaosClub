package controller;

import com.google.gson.Gson;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import jdbc_database.TablaUsuarios;

@WebServlet(name = "UserUpdate", urlPatterns = {"/updateuser"})
@MultipartConfig
public class UserUpdate extends HttpServlet {

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Gson gson = new Gson();
        Map<String, Object> respuesta = new HashMap();
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        Map<String, String[]> data = request.getParameterMap();
        int id = Integer.parseInt(request.getParameter("id_usuario"));
        TablaUsuarios consultas = new TablaUsuarios();
        try {
            consultas.updateUser(data, id);
            respuesta.put("titulo", "Datos Actualizados");
            respuesta.put("mensaje", "La informacion del usuario se actualizo correctamente");
        } catch (Exception e) {
            e.printStackTrace();
        }
        out.print(gson.toJson(respuesta));
        consultas.cerrarConexion();
        out.close();
    }
}
