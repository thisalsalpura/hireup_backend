/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.Util;

/**
 *
 * @author User
 */
@WebServlet(name = "UserProfileUpdate", urlPatterns = {"/UserProfileUpdate"})
public class UserProfileUpdate extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(request.getReader(), JsonObject.class);

        String fname = jsonObject.get("fname").getAsString();
        String lname = jsonObject.get("lname").getAsString();
        String dob = jsonObject.get("dob").getAsString();
        String line1 = jsonObject.get("line1").getAsString();
        String line2 = jsonObject.get("line2").getAsString();
        String pcode = jsonObject.get("pcode").getAsString();
        int countryId = jsonObject.get("countryId").getAsInt();
        int cityId = jsonObject.get("cityId").getAsInt();
        int localeId = jsonObject.get("localeId").getAsInt();

        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", false);
        
        if (fname.isEmpty()) {
            responseObject.addProperty("message", "Please enter your First Name!");
        } else if (lname.isEmpty()) {
            responseObject.addProperty("message", "Please enter your Last Name!");
        } else if (dob.isEmpty()) {
            responseObject.addProperty("message", "Please enter your Date of Birth!");
        } else if (line1.isEmpty()) {
            responseObject.addProperty("message", "Please enter your Address Line 1!");
        } else if (line2.isEmpty()) {
            responseObject.addProperty("message", "Please enter your Address Line 2!");
        } else if (pcode.isEmpty()) {
            responseObject.addProperty("message", "Please enter your Postal Code!");
        } else if (!Util.isPostalCodeValid(pcode)) {
            responseObject.addProperty("message", "Invalid Postal Code!");
        } else if (countryId == 0) {
            responseObject.addProperty("message", "Please select a Country!");
        } else if (cityId == 0) {
            responseObject.addProperty("message", "Please select a City!");
        } else if (localeId == 0) {
            responseObject.addProperty("message", "Please select a Locale!");
        } else {
            
        }
    }
}
