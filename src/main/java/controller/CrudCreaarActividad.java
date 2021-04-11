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
@WebServlet(name = "CrudCreaarActividad", urlPatterns = {"/crudactividades/creaarActividad"})
public class CrudCreaarActividad extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Gson gson = new Gson();
        String strActividad = request.getParameter("actividad");
        Map<String, Object> actividad = gson.fromJson(strActividad, Map.class);
        Map<String, Object> respuesta = new HashMap<>();
        int idActividad = (int) ((double) actividad.get("idActividad"));
        String nombre = (String) actividad.get("nombre");
        int precio = (int) ((double) actividad.get("precio"));
        int hora = (int) ((double) actividad.get("hora"));
        ArrayList<String> dias = (ArrayList<String>) actividad.get("dias");
        TablaActividadesMenbresias consultas = new TablaActividadesMenbresias();
        consultas.crearActividad(idActividad, nombre, precio, hora, dias);
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        respuesta.put("icon", "success");
        respuesta.put("title", "Felicidades");
        respuesta.put("text", "Actividad Creada Correctamente");
        out.print(gson.toJson(respuesta));
        consultas.cerrarConexion();
        out.close();
    }
}
