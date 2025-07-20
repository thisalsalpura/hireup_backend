/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import entity.User;
import hibernate.HibernateUtil;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import model.Util;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

/**
 *
 * @author User
 */
@WebServlet(name = "ChangeForgotPassword", urlPatterns = {"/ChangeForgotPassword"})
public class ChangeForgotPassword extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(request.getReader(), JsonObject.class);

        String fpVerification = jsonObject.get("fpVerification").getAsString();
        String newPassword = jsonObject.get("newPassword").getAsString();
        String confirmPassword = jsonObject.get("confirmPassword").getAsString();

        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", false);

        if (fpVerification.isEmpty()) {
            responseObject.addProperty("message", "Please enter your Verification Code!");
        } else if (newPassword.isEmpty()) {
            responseObject.addProperty("message", "Please enter your New Password!");
        } else if (!Util.isPasswordValid(newPassword)) {
            responseObject.addProperty("message", "Please enter valid New Password! Password must be 8-20 characters long and include Uppercase, Lowercase, Number, and Special Character.");
        } else if (confirmPassword.isEmpty()) {
            responseObject.addProperty("message", "Please enter your Confirm Password!");
        } else if (!newPassword.matches(confirmPassword)) {
            responseObject.addProperty("message", "Doesn't matched New Password and Confirm Password!");
        } else {
            HttpSession httpSession = request.getSession(false);

            if (httpSession != null && httpSession.getAttribute("email") != null && httpSession.getAttribute("verification") != null) {
                if (!fpVerification.equals(httpSession.getAttribute("verification"))) {
                    responseObject.addProperty("message", "Invalid Verification Code!");
                } else {
                    Session session = HibernateUtil.getSessionFactory().openSession();

                    Criteria criteria = session.createCriteria(User.class);
                    criteria.add(Restrictions.eq("email", httpSession.getAttribute("email")));

                    if (!criteria.list().isEmpty()) {
                        User user = (User) criteria.list().get(0);

                        String encryptPassword = Util.encryptPassword(newPassword);
                        user.setPassword(encryptPassword);

                        session.merge(user);
                        session.beginTransaction().commit();

                        Cookie cookie = new Cookie("rememberMe", "");
                        cookie.setMaxAge(0);
                        cookie.setPath("/");
                        response.addCookie(cookie);

                        responseObject.addProperty("status", true);
                        responseObject.addProperty("message", "User password updated Successfully!");
                    }

                    session.close();
                }
            } else {
                responseObject.addProperty("message", "Something went wrong! Please try again later.");
            }
        }

        String responseText = gson.toJson(responseObject);
        response.setContentType("application/json");
        response.getWriter().write(responseText);
    }
}
