package controller;

import com.google.gson.Gson;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
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
        if(user.getRol() != 3){
            Map<String, Object> actividad = (Map<String, Object>) session.getAttribute("ActividadData");
            respuesta.put("actividad", actividad);
        }
        out.print(gson.toJson(respuesta));
    }
}
