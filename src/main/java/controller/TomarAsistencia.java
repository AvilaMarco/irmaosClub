package controller;

import com.google.gson.Gson;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import jdbc_database.TablaClases;

@WebServlet(name = "TomarAsistencia", urlPatterns = {"/tomarlista"})
@MultipartConfig
public class TomarAsistencia extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String[] presentes = request.getParameterValues("presentes[]");
        int idClase = Integer.parseInt(request.getParameter("id_clase"));
        TablaClases consultas = new TablaClases();
        Map<String, Object> respuesta = new HashMap<>();
        Gson gson = new Gson();
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        try {
            consultas.tomarLista(idClase, presentes);
            respuesta.put("presentes", presentes);
            respuesta.put("idClase", idClase);
        } catch (SQLException error) {
            error.printStackTrace();
        }
        out.print(gson.toJson(respuesta));
        consultas.cerrarConexion();
        out.close();
    }
}
