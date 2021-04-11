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
import jdbc_database.TablaClases;

@WebServlet(name = "RecuperarUltimaClase", urlPatterns = {"/ultimaclase"})
public class RecuperarUltimaClase extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int idActividad = Integer.parseInt(request.getParameter("id_actividad"));
        TablaClases consultas = new TablaClases();
        Map<String, Object> respuesta = null;
        ArrayList<Map<String, Object>> alumnos;
        ArrayList<Map<String, Object>> ausentes = new ArrayList<>();
        Gson gson = new Gson();
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        try {
            respuesta = consultas.recuperarUltimaClase(idActividad);
            System.out.println(respuesta);
            if (respuesta != null) {
                alumnos = consultas.alumnosClase(idActividad, (int) respuesta.get("id_horario"));
                respuesta.put("alumnos", alumnos);
                respuesta.put("ausentes", ausentes);
            }
        } catch (SQLException error) {
            error.printStackTrace();
        }
        out.print(gson.toJson(respuesta));
        consultas.cerrarConexion();
        out.close();
    }

}
