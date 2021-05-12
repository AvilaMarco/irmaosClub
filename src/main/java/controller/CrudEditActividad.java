package controller;

import com.google.gson.Gson;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import jdbc_database.TablaActividadesMenbresias;

@MultipartConfig
@WebServlet(name = "CrudEditActividad", urlPatterns = {"/crudactividades/editActividad"})
public class CrudEditActividad extends HttpServlet {

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Gson gson = new Gson();
        String strActividad = request.getParameter("actividad");
        Map<String, Object> actividad = gson.fromJson(strActividad, Map.class);
        Map<String, Object> respuesta = new HashMap<>();
        int idActividad = Integer.parseInt((String) actividad.get("idActividad"));
        int idHorario = Integer.parseInt((String) actividad.get("idHorario"));
        int idInstructor = Integer.parseInt((String) actividad.get("idInstructor"));
        String nombre = (String) actividad.get("actividad");
        int precio = Integer.parseInt((String) actividad.get("precio"));
        int hora = Integer.parseInt((String) actividad.get("hora"));
        ArrayList<String> dias = (ArrayList<String>) actividad.get("dias");
        System.out.println(actividad);
        TablaActividadesMenbresias consultas = new TablaActividadesMenbresias();
        consultas.editActividad(nombre, precio, idActividad, idHorario, idInstructor, hora, dias);
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        respuesta.put("icon", "success");
        respuesta.put("title", "Felicidades");
        respuesta.put("text", "Actividad Editada Correctamente");
        out.print(gson.toJson(respuesta));
        consultas.cerrarConexion();
        out.close();
    }
}
