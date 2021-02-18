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
import jdbc_database.TablaPagos;

@WebServlet(name = "UsuariosPagosData", urlPatterns = {"/usuariospagos"})
public class UsuariosPagosData extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        TablaPagos consultas = new TablaPagos();
        ArrayList<Map<String, Object>> usuariosPagos = null;
        Map<String, Object> respuesta = new HashMap<>();
        Gson gson = new Gson();
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        try{
           usuariosPagos = consultas.usuariosPagos();
           respuesta.put("lista_usuarios_pagos", usuariosPagos);
           out.print(gson.toJson(respuesta));
        }catch(SQLException error){
            error.printStackTrace();
        }
    }
}
