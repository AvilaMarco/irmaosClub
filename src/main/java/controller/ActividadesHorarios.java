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

@WebServlet(name = "ActividadesHorarios", urlPatterns = {"/actividadeshorarios"})
@MultipartConfig
public class ActividadesHorarios extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Gson gson = new Gson();
        ArrayList<Map<String, Object>> respuesta = new ArrayList<>();
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        TablaActividadesMenbresias consultas = new TablaActividadesMenbresias();
        try {
            respuesta = consultas.actividadesHorarios();
        } catch (Exception e) {
            e.printStackTrace();
        }
        out.print(gson.toJson(respuesta));
    }
}
