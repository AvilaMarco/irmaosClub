package controller;

import com.google.gson.Gson;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
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

@MultipartConfig
@WebServlet(name = "CrudListaInstructores", urlPatterns = {"/crudactividades/listaInstructores"})
public class CrudListaInstructores extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            Gson gson = new Gson();
            TablaActividadesMenbresias consultas = new TablaActividadesMenbresias();
            response.setContentType("application/json");
            PrintWriter out = response.getWriter();
            List<Map<String, Object>> actividades;
            actividades = consultas.listaCrudInstructores();
            out.print(gson.toJson(actividades));
            consultas.cerrarConexion();
            out.close();
        } catch (SQLException ex) {
            Logger.getLogger(CrudListaActividadesData.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
