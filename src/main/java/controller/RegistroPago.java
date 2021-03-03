package controller;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
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
import jdbc_database.TablaPagos;
import jdbc_database.TablaUsuarios;

@WebServlet(name = "RegistroPago", urlPatterns = {"/registropago"})
@MultipartConfig
public class RegistroPago extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //JSON
        Gson gson = new Gson();
        //datos del front
        String strActividades = request.getParameter("actividades");
        String strActividadesRegistro = request.getParameter("actividadesRegistro");
        int id_usuario = Integer.parseInt(request.getParameter("id_usuario"));
        //tablas
        TablaPagos consultasPagos = new TablaPagos();
        TablaUsuarios consultasUsuarios = new TablaUsuarios();
        int id_menbresia_titular;
        ArrayList<Map<String, Object>> actividades = gson.fromJson(strActividades, ArrayList.class);
        ArrayList<Map<String, Object>> actividadesRegistro = gson.fromJson(strActividadesRegistro, ArrayList.class);
        Map<String, Object> respuesta = new HashMap();
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        try {
            id_menbresia_titular = consultasUsuarios.getMenbresia(id_usuario);
            for (Map<String, Object> actividad : actividades) {
                int precio = (int) (double) actividad.get("unitPrice");
                String nombreActividad = (String) actividad.get("title");
                int id_menbresia = Integer.parseInt((String) actividad.get("id"));
                consultasPagos.Pagar(id_menbresia_titular, id_menbresia, precio, nombreActividad);
            }
            for (Map<String, Object> actividadeRegistro : actividadesRegistro) {
                int precio = (int) (double) actividadeRegistro.get("unitPrice");
                String nombreActividad = (String) actividadeRegistro.get("title");
                int id_menbresia = Integer.parseInt((String) actividadeRegistro.get("id"));
                consultasPagos.PagarRegistro(id_menbresia_titular, id_menbresia, precio, nombreActividad);
            }
            respuesta.put("pago", "el pago se realizo correctamente");
        } catch (SQLException ex) {
            ex.printStackTrace();
            Logger.getLogger(RegistroPago.class.getName()).log(Level.SEVERE, null, ex);
        }
        out.print(gson.toJson(respuesta));
    }
}
