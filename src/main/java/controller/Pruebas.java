package controller;

import com.google.gson.Gson;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "Pruebas", urlPatterns = {"/pruebas"})
@MultipartConfig
public class Pruebas extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Gson gson = new Gson();
        String id = request.getParameter("id");
        Map<String, Object> respuesta = new HashMap();
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        out.print(gson.toJson(respuesta));
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String strPreference = request.getParameter("preference");
        System.out.println(strPreference);
        System.out.println(strPreference == null);
    }
}
