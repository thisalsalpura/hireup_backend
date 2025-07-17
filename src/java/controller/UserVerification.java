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
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

/**
 *
 * @author User
 */
@WebServlet(name = "UserVerification", urlPatterns = {"/UserVerification"})
public class UserVerification extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(request.getReader(), JsonObject.class);

        String verificationCode = jsonObject.get("verificationCode").getAsString();

        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", false);

        if (verificationCode.isEmpty()) {
            responseObject.addProperty("message", "Please enter your Verification Code!");
        } else {
            HttpSession httpSession = request.getSession(false);

            if ((httpSession == null) || (httpSession.getAttribute("email") == null)) {
                responseObject.addProperty("message", "ENULL");
            } else {
                String email = httpSession.getAttribute("email").toString();

                Session session = HibernateUtil.getSessionFactory().openSession();

                Criteria criteria = session.createCriteria(User.class);
                criteria.add(Restrictions.eq("email", email));
                criteria.add(Restrictions.eq("verification", verificationCode));

                if (criteria.list().isEmpty()) {
                    responseObject.addProperty("message", "Invalid Verification Code!");
                } else {
                    User user = (User) criteria.list().get(0);
                    user.setVerification("VERIFIED!");

                    session.update(user);
                    session.beginTransaction().commit();

                    httpSession.setAttribute("user", user);
                    Cookie cookie = new Cookie("JSESSIONID", httpSession.getId());
                    cookie.setHttpOnly(true);
                    cookie.setPath("/");
                    cookie.setSecure(true);
                    response.addCookie(cookie);

                    responseObject.addProperty("status", true);
                    responseObject.addProperty("message", "User verification Successful!");
                }

                session.close();
            }
        }

        String responseText = gson.toJson(responseObject);
        response.setContentType("application/json");
        response.getWriter().write(responseText);
    }
}
