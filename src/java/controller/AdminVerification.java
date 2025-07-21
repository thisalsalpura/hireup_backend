/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import entity.Admin;
import hibernate.HibernateUtil;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

/**
 *
 * @author User
 */
@WebServlet(name = "AdminVerification", urlPatterns = {"/AdminVerification"})
public class AdminVerification extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(request.getReader(), JsonObject.class);

        String verification = jsonObject.get("verification").getAsString();

        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", false);

        if (verification.isEmpty()) {
            responseObject.addProperty("message", "Please enter your Verification Code!");
        } else {
            HttpSession httpSession = request.getSession(false);

            if (httpSession != null && httpSession.getAttribute("admin") != null) {
                Session session = HibernateUtil.getSessionFactory().openSession();

                Criteria criteria = session.createCriteria(Admin.class);
                criteria.add(Restrictions.eq("verification", verification));

                if (!criteria.list().isEmpty()) {
                    Admin admin = (Admin) httpSession.getAttribute("admin");
                    admin.setVerification("VERIFIED!");

                    session.merge(admin);
                    session.beginTransaction().commit();

                    httpSession.setAttribute("admin", admin);
                    Cookie cookie = new Cookie("JSESSIONID", httpSession.getId());
                    cookie.setHttpOnly(true);
                    cookie.setPath("/");
                    cookie.setSecure(true);
                    response.addCookie(cookie);

                    responseObject.addProperty("status", true);
                    responseObject.addProperty("message", "Admin Verification Successfull!");
                } else {
                    responseObject.addProperty("message", "Invalid Verification Code!");
                }

                session.close();
            } else {
                responseObject.addProperty("message", "You're Session is Timeout.");
            }
        }

        String responseText = gson.toJson(responseObject);
        response.setContentType("application/json");
        response.getWriter().write(responseText);
    }
}
