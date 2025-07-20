/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import entity.User;
import entity.User_Type;
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
@WebServlet(name = "SwitchToSeller", urlPatterns = {"/SwitchToSeller"})
public class SwitchToSeller extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession httpSession = request.getSession(false);

        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", false);

        if (httpSession != null && httpSession.getAttribute("user") != null) {
            User user = (User) httpSession.getAttribute("user");
            User_Type type = user.getUser_Type();

            if (type.getValue().equals("Buyer")) {
                responseObject.addProperty("message", "BUYER");
            } else if (type.getValue().equals("Seller")) {
                responseObject.addProperty("message", "SELLER");
            }
        } else {
            responseObject.addProperty("message", "NSESSION");
        }

        responseObject.addProperty("status", true);

        Gson gson = new Gson();
        String responseText = gson.toJson(responseObject);
        response.setContentType("application/json");
        response.getWriter().write(responseText);
    }
}
