/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import entity.User;
import java.io.IOException;
import java.text.SimpleDateFormat;
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
@WebServlet(name = "LoadUserData", urlPatterns = {"/LoadUserData"})
public class LoadUserData extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession httpSession = request.getSession(false);

        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", false);

        if (httpSession != null && httpSession.getAttribute("user") != null) {
            User user = (User) httpSession.getAttribute("user");

            if (user.getUser_Status().getValue().equals("Active") && user.getVerification().equals("VERIFIED!")) {
                responseObject.addProperty("status", true);

                responseObject.addProperty("fname", user.getFname());
                responseObject.addProperty("lname", user.getLname());
                responseObject.addProperty("email", user.getEmail());
                responseObject.addProperty("password", user.getPassword());

                SimpleDateFormat sdf = new SimpleDateFormat("MMM yyyy");
                responseObject.addProperty("regDate", sdf.format(user.getJoined_date()));

                if (user.getDob() == null || user.getUser_Has_Address() == null || user.getLocale() == null) {
                    responseObject.addProperty("message", "INCOMPLETE");
                } else {
                    responseObject.addProperty("message", "UPDATED");

                    SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
                    responseObject.addProperty("dob", sdf1.format(user.getDob()));

                    responseObject.addProperty("line1", user.getUser_Has_Address().getLine_1());
                    responseObject.addProperty("line2", user.getUser_Has_Address().getLine_2());
                    responseObject.addProperty("pcode", user.getUser_Has_Address().getPostal_code());
                    responseObject.addProperty("countryId", user.getUser_Has_Address().getCity().getCountry().getId());
                    responseObject.addProperty("cityId", user.getUser_Has_Address().getCity().getId());
                    responseObject.addProperty("localeId", user.getLocale().getId());
                }
            }
        }

        Gson gson = new Gson();

        String responseText = gson.toJson(responseObject);
        response.setContentType("application/json");
        response.getWriter().write(responseText);
    }
}
