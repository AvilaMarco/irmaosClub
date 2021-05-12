package controller;

import com.google.gson.Gson;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import jdbc_database.TablaUsuarios;
import models.Usuario;

@WebServlet(name = "InfoUser", urlPatterns = {"/getinfouser"})
public class InfoUser extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        Gson gson = new Gson();
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        Map<String, Object> respuesta;
        Usuario user = (Usuario) session.getAttribute("UserData");
        respuesta = user.getCompleteData();
        TablaUsuarios consultas = new TablaUsuarios();
        if (user.getRol() == 2) {
            Map<String, Object> actividad = (Map<String, Object>) session.getAttribute("ActividadData");
            respuesta.put("actividad", actividad);
        } else if (user.getRol() == 1) {
            try {
                int menbresia = consultas.getMenbresia(user.getId_usuario());
                ArrayList<Map<String, Object>> actividades = consultas.tarjetaActividades(menbresia);
                respuesta.put("actividades", actividades);
            } catch (SQLException ex) {
                Logger.getLogger(InfoUser.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        out.print(gson.toJson(respuesta));
        consultas.cerrarConexion();
        out.close();
    }
}
