package controller;

import com.google.gson.Gson;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.Date;
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

@WebServlet(name = "PagoListo", urlPatterns = {"/pagolisto"})
@MultipartConfig
public class PagoListo extends HttpServlet {

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Gson gson = new Gson();
        //datos del front
        String strActividades = request.getParameter("actividades");
        Map<String, Object> respuesta = new HashMap();
        ArrayList<Map<String, Object>> actividades = gson.fromJson(strActividades, ArrayList.class);
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        TablaActividadesMenbresias consultasMenbresias = new TablaActividadesMenbresias();

        try {
            for (Map<String, Object> actividad : actividades) {
                int id_menbresia = Integer.parseInt((String) actividad.get("id"));
                String nombreActividad = (String) actividad.get("title");
                if (nombreActividad.contains("+")) {
                    String[] items = nombreActividad.split(" +");
                    for (String nombre : items) {
                        if (!nombre.equals("+")) {
                            consultasMenbresias.updatePagoListo(false, id_menbresia, nombre);
                        }
                    }
                } else {
                    consultasMenbresias.updatePagoListo(false, id_menbresia, nombreActividad);
                }
            }
            respuesta.put("icon", "success");
            respuesta.put("title", "Felicidades");
            respuesta.put("text", "Actividades en proceso de pago, Quedamos a la espera del comprobante");
        } catch (SQLException ex) {
            Logger.getLogger(PagoListo.class.getName()).log(Level.SEVERE, null, ex);
        }
        out.print(gson.toJson(respuesta));
    }
}
