package controller;

import com.google.gson.Gson;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import jdbc_database.TablaUsuarios;
import models.Usuario;

@WebServlet(name = "LoginUser", urlPatterns = {"/login"})
//@MultipartConfig
public class LoginUser extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        Usuario usuario = null;
        TablaUsuarios consultas = new TablaUsuarios();
        Map<String, Object> respuesta = new HashMap();
        Map<String, Object> infoActividad = null;
        HttpSession session;
        Gson gson = new Gson();
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        try {
            usuario = consultas.loginUser(email, password);
            if (usuario != null) {
                infoActividad = consultas.actividadUsuario(usuario.getId_usuario());
            }
        } catch (SQLException error) {
            error.printStackTrace();
        }
        if (usuario == null) {
            respuesta.put("icon", "error");
            respuesta.put("title", "Oops...");
            respuesta.put("text", "datos erroneos o el usuario no existe");
            out.print(gson.toJson(respuesta));
        } else {
            session = request.getSession();
            session.setAttribute("UserData", usuario);
            switch (usuario.getRol()) {
                case 1:
                    session.setAttribute("ActividadData", infoActividad);
                    session.setMaxInactiveInterval(600);
                    response.sendRedirect("html/dashboard_cliente.html");
                    break;
                case 2:
                    session.setAttribute("ActividadData", infoActividad);
                    response.sendRedirect("html/dashboard_instructor.html");
                    break;
                default:
                    response.sendRedirect("html/jguXjo04aghX.html");
                    break;
            }
        }
        consultas.cerrarConexion();
        out.close();
    }
}
