package controller;

import com.google.gson.Gson;
import java.io.IOException;
import java.io.PrintWriter;
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
import jdbc_database.TablaUsuarios;

@WebServlet(name = "Familiares", urlPatterns = {"/familiares"})
@MultipartConfig
public class Familiares extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Gson gson = new Gson();
        int id = Integer.parseInt(request.getParameter("id_usuario"));
        TablaUsuarios consultas = new TablaUsuarios();
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        ArrayList<Integer> idFamiliares = new ArrayList<>();
        ArrayList<Map<String, Object>> familiares = new ArrayList<>();
        try {
            idFamiliares = consultas.getIdFamiliares(id);
            for (Integer idFamiliar : idFamiliares) {
                Map<String, Object> familiar = consultas.tarjetaUsuario(idFamiliar);
                familiares.add(familiar);
            }
        } catch (SQLException ex) {
            Logger.getLogger(Familiares.class.getName()).log(Level.SEVERE, null, ex);
        }
        out.print(gson.toJson(familiares));
        consultas.cerrarConexion();
        out.close();
    }
}
