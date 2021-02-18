package controller;

import com.google.gson.Gson;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import jdbc_database.TablaUsuarios;
import models.Usuario;

@WebServlet(name = "GetUserById", urlPatterns = {"/getuser"})
public class GetUserById extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int id_usuario = Integer.parseInt(request.getParameter("id"));
        TablaUsuarios consultasUsuarios = new TablaUsuarios();
        Gson gson = new Gson();
        Map<String, Object> result = new HashMap();
        try {
            Usuario user = consultasUsuarios.getUser(id_usuario);
            if (user != null) {
                result = user.getInfo();
            } else {
                result.put("error", "el usuario no se encontro");
            }
        } catch (SQLException error) {
            error.printStackTrace();
        }
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        out.print(gson.toJson(result));
    }
}
