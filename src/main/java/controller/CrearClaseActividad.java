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

@WebServlet(name = "CrearClaseActividad", urlPatterns = {"/crearclase"})
public class CrearClaseActividad extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {        
        int idActividad = Integer.parseInt(request.getParameter("id_actividad"));
        int idHorario = Integer.parseInt(request.getParameter("id_horario"));
        TablaClases consultas = new TablaClases();
        Map<String, Object> respuesta = new HashMap<>();
        ArrayList<Map<String, Object>> lista = new ArrayList<>();
        Gson gson = new Gson();
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
//        int idHorario = 0;
        try {
//            idHorario = consultas.obtenerIdHorario(idActividad, dia);
            respuesta.put("id_clase", consultas.crearClase(idActividad, idHorario));
            respuesta.put("alumnos", consultas.alumnosClase(idActividad, idHorario));
            respuesta.put("ausentes", lista);
            respuesta.put("presentes", lista);
        } catch (SQLException error) {
            error.printStackTrace();
        }
        out.print(gson.toJson(respuesta));
        consultas.cerrarConexion();
        out.close();
    }
}
