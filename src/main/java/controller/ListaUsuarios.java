package controller;

import com.google.gson.Gson;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import jdbc_database.TablaUsuarios;
import models.Usuario;

@WebServlet(name = "ListaUsuarios", urlPatterns = {"/usuarios"})
public class ListaUsuarios extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        TablaUsuarios consultas = new TablaUsuarios();
        Gson gson = new Gson();
        Map<String, Object> result = new HashMap();
        ArrayList<Usuario> usuarios = null;
        ArrayList<Map<String, Object>> listaUsuarios = new ArrayList<>();
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        try {
            usuarios = consultas.getUsers();
            for (Usuario usuario : usuarios) {
                listaUsuarios.add(usuario.getCompleteData());
            }
            result.put("usuarios", listaUsuarios);
        } catch (SQLException error) {
            error.printStackTrace();
        }
        out.print(gson.toJson(result));
        consultas.cerrarConexion();
        out.close();
    }
}
