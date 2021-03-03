package controller;

import com.google.gson.Gson;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
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
import jdbc_database.TablaUsuarios;

@WebServlet(name = "ResetPassword", urlPatterns = {"/resetpassword"})
@MultipartConfig
public class ResetPassword extends HttpServlet {

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Gson gson = new Gson();
        String pass = request.getParameter("password");
        int id = Integer.parseInt(request.getParameter("id"));
        TablaUsuarios consultas = new TablaUsuarios();
        Map<String, Object> respuesta = new HashMap();
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        try {
            consultas.resetearContraseña(pass, id);
            respuesta.put("mensaje", "contraseña cambiada");
        } catch (SQLException ex) {
            Logger.getLogger(ResetPassword.class.getName()).log(Level.SEVERE, null, ex);
        }
        out.print(gson.toJson(respuesta));
    }

}
