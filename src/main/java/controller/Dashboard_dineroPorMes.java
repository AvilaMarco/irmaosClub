package controller;

import com.google.gson.Gson;
import java.io.IOException;
import java.io.PrintWriter;
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
import jdbc_database.TablaPagos;

@WebServlet(name = "Dashboard_dineroPorMes", urlPatterns = {"/dineropormes"})
@MultipartConfig
public class Dashboard_dineroPorMes extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Gson gson = new Gson();
        ArrayList<Map<String, Object>> respuesta = new ArrayList<>();
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        TablaPagos consultas = new TablaPagos();
        try {
            respuesta = consultas.dineroPorMes();
        } catch (SQLException ex) {
            Logger.getLogger(Dashboard_dineroPorMes.class.getName()).log(Level.SEVERE, null, ex);
        }
        out.print(gson.toJson(respuesta));
        consultas.cerrarConexion();
        out.close();
    }

}
