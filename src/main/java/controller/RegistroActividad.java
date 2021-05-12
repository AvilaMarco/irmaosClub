package controller;

import com.google.gson.Gson;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import jdbc_database.TablaActividadesMenbresias;
import jdbc_database.TablaUsuarios;

@WebServlet(name = "RegistroActividad", urlPatterns = {"/registroactividad"})
@MultipartConfig
public class RegistroActividad extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //JSON
        Gson gson = new Gson();
        //datos del front
        String strActividades = request.getParameter("actividades");
        if (request.getParameter("actividades") == null) {
            strActividades = "";
        }
        int id_usuario = Integer.parseInt(request.getParameter("id_usuario"));
        //tablas de consultas
        TablaUsuarios consultasUsuarios = new TablaUsuarios();
        TablaActividadesMenbresias consultasActividades = new TablaActividadesMenbresias();
        //variables
        int id_menbresia;
        ArrayList<Map<String, Object>> actividades = gson.fromJson(strActividades, ArrayList.class);
        Map<String, Object> respuesta = new HashMap();
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        try {
            id_menbresia = consultasUsuarios.getMenbresia(id_usuario);
            if (id_menbresia == 0) {
                id_menbresia = consultasUsuarios.crearMenbresia(id_usuario);
            }
            if (!actividades.isEmpty()) {
                consultasActividades.anotarUsuarioActividades(id_menbresia, actividades);
                respuesta.put("icon", "success");
                respuesta.put("title", "Felicidades");
                respuesta.put("text", "Registro de actividad correcto");
            } else {
                respuesta.put("icon", "error");
                respuesta.put("title", "Oops...");
                respuesta.put("text", "Hubo un error");
            }
        } catch (SQLException ex) {
            Logger.getLogger(RegistroActividad.class.getName()).log(Level.SEVERE, null, ex);
        }
        out.print(gson.toJson(respuesta));
        consultasActividades.cerrarConexion();
        consultasUsuarios.cerrarConexion();
        out.close();
    }

}
