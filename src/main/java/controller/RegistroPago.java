package controller;

import com.google.gson.Gson;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "RegistroPago", urlPatterns = {"/registropago"})
@MultipartConfig
public class RegistroPago extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int idMenbresia = Integer.parseInt(request.getParameter("id"));
        int idPago = Integer.parseInt(request.getParameter("payment_id"));
//        hacer un fetch para saber cuanto pago
//https://api.mercadopago.com/v1/payments/idPago
        Gson gson = new Gson();
        System.out.println("get:");
//        System.out.println(id);
        System.out.println(request.getParameterMap());
//        Map<String, Object> respuesta = new HashMap();
//        response.setContentType("application/json");
//        PrintWriter out = response.getWriter();
//        respuesta.put("nice", id);
//        out.print(gson.toJson(respuesta));
        response.sendRedirect("/");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Gson gson = new Gson();
        String id = request.getParameter("id");
        System.out.println("post:");
        System.out.println(id);
        System.out.println(request.getParameterMap());
//        Map<String, Object> respuesta = new HashMap();
//        response.setContentType("application/json");
//        PrintWriter out = response.getWriter();
//        respuesta.put("nice", id);
//        out.print(gson.toJson(respuesta));
        response.sendRedirect("/");
    }
}
