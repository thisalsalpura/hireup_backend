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
@WebServlet(name = "ChangePassword", urlPatterns = {"/ChangePassword"})
public class ChangePassword extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(request.getReader(), JsonObject.class);

        String currentPassword = jsonObject.get("currentPassword").getAsString();
        String newPassword = jsonObject.get("newPassword").getAsString();
        String confirmPassword = jsonObject.get("confirmPassword").getAsString();

        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", false);

        if (currentPassword.isEmpty()) {
            responseObject.addProperty("message", "Something went wrong! Please try again later.");
        } else if (newPassword.isEmpty()) {
            responseObject.addProperty("message", "Please enter your New Password!");
        } else if (!Util.isPasswordValid(newPassword)) {
            responseObject.addProperty("message", "Please enter valid New Password! Password must be 8-20 characters long and include Uppercase, Lowercase, Number, and Special Character.");
        } else if (confirmPassword.isEmpty()) {
            responseObject.addProperty("message", "Please enter your Confirm Password!");
        } else if (!newPassword.matches(confirmPassword)) {
            responseObject.addProperty("message", "Doesn't matched New Password and Confirm Password!");
        } else {

            Session session = HibernateUtil.getSessionFactory().openSession();

            HttpSession httpSession = request.getSession(false);

            if (httpSession != null && httpSession.getAttribute("user") != null) {
                User user = (User) httpSession.getAttribute("user");

                Criteria criteria = session.createCriteria(User.class);
                criteria.add(Restrictions.eq("email", user.getEmail()));

                if (!criteria.list().isEmpty()) {
                    User u = (User) criteria.list().get(0);

                    String encryptPassword = Util.encryptPassword(newPassword);
                    u.setPassword(encryptPassword);

                    httpSession.setAttribute("user", u);
                    Cookie cookie = new Cookie("JSESSIONID", httpSession.getId());
                    cookie.setHttpOnly(true);
                    cookie.setPath("/");
                    cookie.setSecure(true);
                    response.addCookie(cookie);

                    session.merge(u);
                    session.beginTransaction().commit();

                    responseObject.addProperty("status", true);
                    responseObject.addProperty("message", "User password updated Successfully!");
                }
            }

            session.close();

        }

        String responseText = gson.toJson(responseObject);
        response.setContentType("application/json");
        response.getWriter().write(responseText);
    }
}
