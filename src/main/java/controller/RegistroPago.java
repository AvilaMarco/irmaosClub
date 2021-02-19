package controller;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
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
import jdbc_database.TablaPagos;

@WebServlet(name = "RegistroPago", urlPatterns = {"/registropago"})
@MultipartConfig
public class RegistroPago extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int idMenbresia = Integer.parseInt(request.getParameter("id"));
        int precio = Integer.parseInt(request.getParameter("precio"));
        TablaPagos consultas = new TablaPagos();
        try {
            consultas.Pagar(idMenbresia, precio);
        } catch (SQLException ex) {
            ex.printStackTrace();
            Logger.getLogger(RegistroPago.class.getName()).log(Level.SEVERE, null, ex);
        }
        response.sendRedirect("/");
    }

    public static String peticionHttpGet(String urlParaVisitar) throws MalformedURLException, ProtocolException, IOException {
        // Esto es lo que vamos a devolver
        StringBuilder resultado = new StringBuilder();
        // Crear un objeto de tipo URL
        URL url = new URL(urlParaVisitar);

        // Abrir la conexión e indicar que será de tipo GET
        HttpURLConnection conexion = (HttpURLConnection) url.openConnection();
        conexion.setRequestMethod("GET");
//        conexion.setRequestProperty("Authorization", "Bearer TEST-5002283706407276-021223-00f2b94570b01f18fdc542a7a8419258-714772071");
//        conexion.setRequestProperty("Content-Type", "application/json");
//        conexion.addRequestProperty("Content-Type", "application/json");
       
        // Búferes para leer
        BufferedReader rd = new BufferedReader(new InputStreamReader(conexion.getInputStream()));
        String linea;
        // Mientras el BufferedReader se pueda leer, agregar contenido a resultado
        while ((linea = rd.readLine()) != null) {
            resultado.append(linea);
        }
        // Cerrar el BufferedReader
        rd.close();
        // Regresar resultado, pero como cadena, no como StringBuilder
        return resultado.toString();
    }
}
