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
import jdbc_database.TablaActividadesMenbresias;

@WebServlet(name = "UsuariosPorActividad", urlPatterns = {"/usuariosactividades"})
public class UsuariosPorActividad extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Gson gson = new Gson();
        TablaActividadesMenbresias consultas = new TablaActividadesMenbresias();
        Map<String, Object> respuesta = new HashMap<>();
        try {
            respuesta.put("lista_usuarios_por_actividad", consultas.usuariosPorActividad());
        } catch (SQLException error) {
            error.printStackTrace();
        }
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        out.print(gson.toJson(respuesta));
    }

}
