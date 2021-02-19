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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpSession;
import jdbc_database.TablaActividadesMenbresias;
import jdbc_database.TablaUsuarios;
import models.Usuario;

@WebServlet(name = "CheckoutMercadoPago", urlPatterns = {"/checkout"})
@MultipartConfig
public class CheckoutMercadoPago extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //JSON
        Gson gson = new Gson();
        //datos del front
        String strActividades = request.getParameter("actividades");
        int id_usuario = Integer.parseInt(request.getParameter("id_usuario"));
        String strPreference = request.getParameter("preference");
        //tablas de consultas
        TablaUsuarios consultasUsuarios = new TablaUsuarios();
        TablaActividadesMenbresias consultasActividades = new TablaActividadesMenbresias();
        //variables
        ArrayList<Map<String, Object>> actividades = gson.fromJson(strActividades, ArrayList.class);
        ArrayList<Integer> menbresias;
        ArrayList<Float> descuentos;
        ArrayList<Item> items = new ArrayList<>();
        int id_titular;
        int id_menbresia_titular;
        //creamos la menbresia
        int id_menbresia;
        // Crea o usar un objeto de preferencia
        Preference preference = new Preference();
        //informacion de la session
        HttpSession session = request.getSession();
        Usuario user = (Usuario) session.getAttribute("UserData");
        try {
            // Agrega credenciales
            MercadoPago.SDK.setAccessToken("TEST-5002283706407276-021223-00f2b94570b01f18fdc542a7a8419258-714772071");
            id_menbresia = consultasUsuarios.getMenbresia(id_usuario);
            if (id_menbresia == 0) {
                id_menbresia = consultasUsuarios.crearMenbresia(id_usuario);
                consultasActividades.anotarUsuarioActividades(id_menbresia, actividades);
            }
            id_titular = consultasUsuarios.getIdTitular(id_usuario);
            if (id_titular == 0) {
                id_menbresia_titular = id_menbresia;
                id_titular = id_usuario;
            } else {
                id_menbresia_titular = consultasUsuarios.getMenbresia(id_titular);
            }
            menbresias = consultasActividades.listaMenbresias(id_titular, id_menbresia_titular);
            System.out.println(menbresias);
            descuentos = consultasActividades.listaDescuentos();
            System.out.println(descuentos);
            for (int i = 0; i < menbresias.size(); i++) {
                int menbresia = menbresias.get(i);
                //solo aplicar con familiares en BJJ
                float descuento;
                if (i < descuentos.size()) {
                    descuento = descuentos.get(i);
                } else {
                    descuento = descuentos.get(descuentos.size() - 1);
                }
                items.addAll(crearItems(menbresia, descuento, consultasUsuarios, consultasActividades));
            }
            System.out.println(items);
            //definir preferencia -> obtener || crear
            if (strPreference == null) {
                System.out.println("nueva preferencia");
                //agregar los items a la preferecia
                preference.setItems(items);
                preference = completarPreference(preference, user, id_menbresia_titular, precioTotal(items));
            } else {
                System.out.println("nunca se ejecuta");
                preference = (Preference) gson.fromJson(strPreference, Map.class);
                //agregar los items a la preferecia
                preference.setItems(items);
            }
            preference.save();
        } catch (MPException ex) {
            ex.printStackTrace();
            Logger.getLogger(CheckoutMercadoPago.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            ex.printStackTrace();
            Logger.getLogger(CheckoutMercadoPago.class.getName()).log(Level.SEVERE, null, ex);
        }

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        out.print(gson.toJson(preference));
    }

    public ArrayList<Item> crearItems(int id_menbresia, float descuento, TablaUsuarios consultasUsuarios, TablaActividadesMenbresias consultasActividades) throws SQLException {
        ArrayList<Item> items = new ArrayList<>();
        //calcular y crear items
        ArrayList<Map<String, Object>> actividades = consultasUsuarios.actividadesPorMenbresia(id_menbresia);
        ArrayList<Map<String, Object>> actividadesConDescuento = new ArrayList<>();
        ArrayList<Map<String, Object>> actividadesSinDescuento = new ArrayList<>();
        //filtrar actividades
        for (Map<String, Object> actividad : actividades) {
            if (actividad.get("grupo_descuento").equals("descuento_bjj")) {
                actividadesConDescuento.add(actividad);
            } else {
                actividadesSinDescuento.add(actividad);
            }
        }
        /* crear items con decuento */
        //fisico bjj
        for (int i = 0; i < actividadesConDescuento.size(); i++) {
            Map<String, Object> actividad = actividadesConDescuento.get(i);
            if (actividad.get("nickname").equals("BJJ especial")) {
                items.add(crearItem(actividad, descuento));
                actividadesConDescuento.remove(i);
            }
        }
        //el resto de actividades de bjj
        if (actividadesConDescuento.size() == 1) {
            Map<String, Object> actividad = actividadesConDescuento.get(0);
            items.add(crearItem(actividad, descuento));
        } else if (actividadesConDescuento.size() > 1) {
            StringBuffer itemConDescuento = new StringBuffer();
            int logitudArr = actividadesConDescuento.size();
            for (int i = 0; i < logitudArr; i++) {
                Map<String, Object> actividad = actividadesConDescuento.get(i);
                String nombre = (String) actividad.get("nickname");
                if (i == logitudArr - 1) {
                    itemConDescuento.append(nombre);
                } else {
                    itemConDescuento.append(nombre).append(" + ");
                }
            }
            Map<String, Object> actividad = consultasActividades.comboActividad(itemConDescuento.toString());//consulta sql
            items.add(crearItem(actividad, descuento));
        }
        /* crear items sin descuento */
        for (Map<String, Object> actividad : actividadesSinDescuento) {
            items.add(crearItem(actividad, 1));
        }
        return items;
    }

    public Item crearItem(Map<String, Object> actividad, float descuento) {
        String nombre = (String) actividad.get("nombre");
        float precio = (float) (int) actividad.get("precio");
        Item item = new Item();
        item.setTitle(nombre)
                .setQuantity(1)
                .setDescription("Deporte")
                .setCurrencyId("ARS")
                .setUnitPrice(precio * descuento);
        return item;
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
                "https://marcoavilaweb.web.app/?id=" + id_menbresia+"&precio="+precioTotal,
                "https://marcoavilaweb.web.app/",
                "https://marcoavilaweb.web.app/");
        preference.setBackUrls(backUrls);
        // notificacion para pagar
        return preference;
    }
    
    public float precioTotal(ArrayList<Item> items){
        float precioTotal = 0;
        for (Item item : items) {
            precioTotal += item.getUnitPrice();
        }
        return precioTotal;
    }

    /* en proceso de creacion */
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
