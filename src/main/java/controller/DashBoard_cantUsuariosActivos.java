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

@WebServlet(name = "DashBoard_cantUsuariosActivos", urlPatterns = {"/cantidad_usuarios_on"})
public class DashBoard_cantUsuariosActivos extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Gson gson = new Gson();
        TablaUsuarios consultas = new TablaUsuarios();
        int cantidadUsuariosOn = 0;
        Map<String, Object> respuesta = new HashMap<>();
        try {
            cantidadUsuariosOn = consultas.cantUsuariosActivos();
        } catch (SQLException error) {
            error.printStackTrace();
        }
        respuesta.put("cantidad_usuarios_activos", cantidadUsuariosOn);
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        out.print(gson.toJson(respuesta));
        consultas.cerrarConexion();
        out.close();
    }
}
