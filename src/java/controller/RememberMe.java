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
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

/**
 *
 * @author User
 */
@WebServlet(name = "RememberMe", urlPatterns = {"/RememberMe"})
public class RememberMe extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", false);

        Cookie cookies[] = request.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("rememberMe")) {
                    String email = cookie.getValue();

                    Session session = HibernateUtil.getSessionFactory().openSession();

                    Criteria criteria = session.createCriteria(User.class);
                    criteria.add(Restrictions.eq("email", email));

                    User user;
                    if (!criteria.list().isEmpty()) {
                        user = (User) criteria.list().get(0);

                        if (user.getUser_Status().getValue().equals("Active") && user.getVerification().equals("VERIFIED!")) {
                            responseObject.addProperty("email", email);

                            responseObject.addProperty("status", true);
                            break;
                        }
                    }
                }
            }
        }

        Gson gson = new Gson();
        String responseText = gson.toJson(responseObject);
        response.setContentType("application/json");
        response.getWriter().write(responseText);
    }
}
