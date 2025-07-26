/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import entity.User;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author User
 */
@WebServlet(name = "LoadSellerData", urlPatterns = {"/LoadSellerData"})
public class LoadSellerData extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", false);

        HttpSession httpSession = request.getSession(false);

        if (httpSession != null && httpSession.getAttribute("user") != null) {
            User user = (User) httpSession.getAttribute("user");

            if (user.getUser_Status().getValue().equals("Active") && user.getVerification().equals("VERIFIED!") && user.getUser_Type().getValue().equals("Seller") && user.getDob() != null && user.getUser_Has_Address() != null && user.getLocale() != null) {
                responseObject.addProperty("status", true);
                responseObject.addProperty("name", user.getFname() + " " + user.getLname());
                responseObject.addProperty("country", user.getUser_Has_Address().getCity().getCountry().getName());
            }
        }

        Gson gson = new Gson();
        String responseText = gson.toJson(responseObject);
        response.setContentType("application/json");
        response.getWriter().write(responseText);
    }
}
