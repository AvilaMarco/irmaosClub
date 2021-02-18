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

@WebServlet(name = "UserDelete", urlPatterns = {"/banearuser"})
@MultipartConfig
public class UserDelete extends HttpServlet {

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Gson gson = new Gson();
        Map<String, Object> respuesta = new HashMap();
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        int id = Integer.parseInt(request.getParameter("id"));
        TablaUsuarios consultas = new TablaUsuarios();
        try {
            consultas.banearUsuario(id);
            respuesta.put("titulo", "Usuario Baneado");
            respuesta.put("mensaje", "El usuario fue baneado correctamente");
        } catch (Exception e) {
            e.printStackTrace();
        }
        out.print(gson.toJson(respuesta));
    }
}
