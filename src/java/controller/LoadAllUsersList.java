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
import java.util.ArrayList;
import java.util.List;
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
@WebServlet(name = "LoadAllUsersList", urlPatterns = {"/LoadAllUsersList"})
public class LoadAllUsersList extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Gson gson = new Gson();

        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", false);

        Session session = HibernateUtil.getSessionFactory().openSession();

        Criteria criteria = session.createCriteria(User.class);
        criteria.add(Restrictions.eq("verification", "VERIFIED!"));
        if (!criteria.list().isEmpty()) {
            List<User> users = criteria.list();
            List<User> userList = new ArrayList<>();
            List<String> statusList = new ArrayList<>();
            for (User user : users) {
                userList.add(user);
                statusList.add(user.getUser_Status().getValue());
            }

            if (!userList.isEmpty() && !statusList.isEmpty()) {
                responseObject.addProperty("status", true);
                responseObject.add("userList", gson.toJsonTree(userList));
                responseObject.add("statusList", gson.toJsonTree(statusList));
            }
        } else {
            responseObject.addProperty("status", true);
        }

        session.close();

        String responseText = gson.toJson(responseObject);
        response.setContentType("application/json");
        response.getWriter().write(responseText);
    }
}
