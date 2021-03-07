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
// SDK de Mercado Pago
import com.mercadopago.MercadoPago;
import com.mercadopago.exceptions.MPConfException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.Preference;
import com.mercadopago.resources.datastructures.preference.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpSession;
import jdbc_database.TablaActividadesMenbresias;
import jdbc_database.TablaPagos;
import jdbc_database.TablaUsuarios;
import models.Usuario;

@WebServlet(name = "CheckoutMercadoPago", urlPatterns = {"/checkout"})
@MultipartConfig
public class CheckoutMercadoPago extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //JSON
        Gson gson = new Gson();
        //datos del front
        int id_titular = Integer.parseInt(request.getParameter("id_usuario"));
        Boolean esAdmin = Boolean.parseBoolean(request.getParameter("isAdmin"));
//        String strPreference = request.getParameter("preference");
        //tablas de consultas
        TablaUsuarios consultasUsuarios = new TablaUsuarios();
        TablaActividadesMenbresias consultasActividades = new TablaActividadesMenbresias();
        TablaPagos consultasPagos = new TablaPagos();
        ArrayList<Map<String, Object>> respuesta = new ArrayList<>();
        //variables
        ArrayList<Integer> menbresias;
//        ArrayList<Item> items = new ArrayList<>();
        int id_menbresia_titular;

        try {
            // Agrega credenciales
//            MercadoPago.SDK.setAccessToken("TEST-5002283706407276-021223-00f2b94570b01f18fdc542a7a8419258-714772071");
            id_menbresia_titular = consultasUsuarios.getMenbresia(id_titular);
            menbresias = consultasActividades.listaMenbresias(id_titular, id_menbresia_titular);
            respuesta = consultasPagos.calcularPago(menbresias, id_menbresia_titular, esAdmin);
        }catch (SQLException ex) {
            ex.printStackTrace();
            Logger.getLogger(CheckoutMercadoPago.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        out.print(gson.toJson(respuesta));
    }

    public Preference completarPreference(Preference preference, Usuario user, int id_menbresia, float precioTotal) {
        //pagador
        Payer payer = new Payer();
        payer.setName(user.getUsuario())
                .setSurname(user.getApellidos())
                .setEmail(user.getEmail())
                .setDateCreated("2018-06-02T12:58:41.425-04:00")
                .setPhone(new Phone()
                        .setAreaCode("")
                        .setNumber(String.valueOf(user.getCelular())))
                .setIdentification(new Identification()
                        .setType("DNI")
                        .setNumber(String.valueOf(user.getDni())))
                .setAddress(new Address()
                        .setStreetName(user.getDireccion())
                        .setStreetNumber(0)
                        .setZipCode("0"));
        preference.setPayer(payer);
        // Back Urls
        BackUrls backUrls = new BackUrls(
                "https://marcoavilaweb.web.app/?id=" + id_menbresia + "&precio=" + precioTotal,
                "https://marcoavilaweb.web.app/",
                "https://marcoavilaweb.web.app/");
        preference.setBackUrls(backUrls);
        // notificacion para pagar
        return preference;
    }

    public float precioTotal(ArrayList<Item> items) {
        float precioTotal = 0;
        for (Item item : items) {
            precioTotal += item.getUnitPrice();
        }
        return precioTotal;
    }

    /* en proceso de creacion */
            //creamos la menbresia
//        int id_menbresia;
        // Crea o usar un objeto de preferencia
//        Preference preference = new Preference();
        //informacion de la session
//        HttpSession session = request.getSession();
//        Usuario user = (Usuario) session.getAttribute("UserData");
    //            if (id_menbresia == 0) {
//                id_menbresia = consultasUsuarios.crearMenbresia(id_usuario);
//            }
//            if (!actividades.isEmpty()) {
//                consultasActividades.anotarUsuarioActividades(id_menbresia, actividades);
//            }
//            id_titular = consultasUsuarios.getIdTitular(id_usuario);
//            if (id_titular == 0) {
//                id_menbresia_titular = id_menbresia;
//                id_titular = id_usuario;
//                if (!actividades.isEmpty()) {
//                    //titular se anota a nueva actividad
//                    menbresiaRegistro = id_menbresia;
//                }
//            } else {
//                id_menbresia_titular = consultasUsuarios.getMenbresia(id_titular);
//                //familiar se anota a nueva actividad
//                menbresiaRegistro = id_menbresia;
//            }
                //definir preferencia -> obtener || crear
//            if (strPreference == null) {
//                //agregar los items a la preferecia
//                preference.setItems(items);
//                preference = completarPreference(preference, user, id_menbresia_titular, precioTotal(items));
//            } else {
//                preference = (Preference) gson.fromJson(strPreference, Map.class);
//                //agregar los items a la preferecia
//                preference.setItems(items);
//            }
//            preference.save();
//    public void primerPago() {
//        StringBuffer itemConDescuento = new StringBuffer();
//
//        for (int i = 0; i < actividades.size(); i++) {
//            Map<String, Object> actividad = actividades.get(i);
//            int id_actividad = Integer.parseInt((String) actividad.get("id_actividad"));
//            int id_horario = Integer.parseInt((String) actividad.get("id_horario"));
//            float precio = (float) (double) actividad.get("precio");
//            //anotamos al usuario a la actividad
//            consultasActividades.anotarUsuario(id_actividad, id_horario, id_menbresia);
//            if (i == actividades.size() - 1) {
//                itemConDescuento.append(actividad.get("nombre actividad"));
//            } else {
//                itemConDescuento.append(actividad.get("nombre actividad") + " + ");
//            }
//        }
//        // Crea un ítem en la preferencia
////        Item item = crearItem((String) actividad.get("nombre actividad"), precio);
////        preference.appendItem(item);
//
////        preference.save();
//    }
}
