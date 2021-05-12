package controller;

import com.google.gson.Gson;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import jdbc_database.TablaClases;
import models.Usuario;

@WebServlet(name = "FinalizarClase", urlPatterns = {"/terminarclase"})
public class FinalizarClase extends HttpServlet {

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Gson gson = new Gson();
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        Map<String, Object> respuesta = new HashMap<>();
        int idClase = Integer.parseInt(request.getParameter("id_clase"));
        TablaClases consultas = new TablaClases();
        try {
            consultas.finalizarClase(idClase);
            respuesta.put("finalizada", true);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        out.print(gson.toJson(respuesta));
        consultas.cerrarConexion();
        out.close();
    }
}
