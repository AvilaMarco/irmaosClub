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
            consultas.createUser(data);
            respuesta.put("mensaje", "usuario creado");
            System.out.println("creado");
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("creado");
        out.print(gson.toJson(respuesta));
    }
}
