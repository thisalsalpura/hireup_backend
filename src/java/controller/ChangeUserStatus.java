/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import entity.User;
import entity.User_Status;
import hibernate.HibernateUtil;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
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
@WebServlet(name = "ChangeUserStatus", urlPatterns = {"/ChangeUserStatus"})
public class ChangeUserStatus extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(request.getReader(), JsonObject.class);

        int userId = jsonObject.get("id").getAsInt();

        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", false);

        if (userId != 0) {
            Session session = HibernateUtil.getSessionFactory().openSession();

            Criteria criteria = session.createCriteria(User.class);
            criteria.add(Restrictions.eq("id", userId));
            if (!criteria.list().isEmpty()) {
                User user = (User) criteria.list().get(0);
                if (user.getUser_Status().getValue().equals("Active")) {
                    Criteria criteria1 = session.createCriteria(User_Status.class);
                    criteria1.add(Restrictions.eq("value", "Inactive"));
                    if (!criteria1.list().isEmpty()) {
                        User_Status status = (User_Status) criteria1.list().get(0);
                        user.setUser_Status(status);
                        responseObject.addProperty("message", "Change " + user.getFname() + " " + user.getLname() + " status to " + status.getValue());
                    }
                } else if (user.getUser_Status().getValue().equals("Inactive")) {
                    Criteria criteria1 = session.createCriteria(User_Status.class);
                    criteria1.add(Restrictions.eq("value", "Active"));
                    if (!criteria1.list().isEmpty()) {
                        User_Status status = (User_Status) criteria1.list().get(0);
                        user.setUser_Status(status);
                        responseObject.addProperty("message", "Change " + user.getFname() + " " + user.getLname() + " status to " + status.getValue());
                    }
                }

                session.merge(user);
                session.beginTransaction().commit();

                responseObject.addProperty("status", true);
            }

            session.close();
        }

        String responseText = gson.toJson(responseObject);
        response.setContentType("application/json");
        response.getWriter().write(responseText);
    }
}
