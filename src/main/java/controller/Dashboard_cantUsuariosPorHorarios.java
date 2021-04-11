package controller;

import com.google.gson.Gson;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import jdbc_database.TablaActividadesMenbresias;

@WebServlet(name = "Dashboard_cantUsuariosPorHorarios", urlPatterns = {"/usuariosporhorarios"})
public class Dashboard_cantUsuariosPorHorarios extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        TablaActividadesMenbresias consultas = new TablaActividadesMenbresias();
        ArrayList<Map<String, Object>> usuariosHorarios = null;
        Map<String, Object> respuesta = new HashMap<>();
        Gson gson = new Gson();
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        try{
            usuariosHorarios = consultas.cantidadUsuariosPorHorarios();
            respuesta.put("lista_usuarios_horarios", usuariosHorarios);
            out.print(gson.toJson(respuesta));
            consultas.cerrarConexion();
            out.close();
        }catch(SQLException error){
            error.printStackTrace();
        }
    }
}
