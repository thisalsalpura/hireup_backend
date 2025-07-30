/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import entity.Gig;
import entity.Gig_Status;
import entity.User;
import entity.User_Status;
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
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

/**
 *
 * @author User
 */
@WebServlet(name = "LoadPendingGigs", urlPatterns = {"/LoadPendingGigs"})
public class LoadPendingGigs extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Gson gson = new Gson();
        JsonObject responseObject = new JsonObject();

        responseObject.addProperty("status", false);

        Session session = HibernateUtil.getSessionFactory().openSession();

        Criteria criteria = session.createCriteria(User_Status.class);
        criteria.add(Restrictions.eq("value", "Active"));

        if (!criteria.list().isEmpty()) {
            User_Status user_Status = (User_Status) criteria.list().get(0);

            Criteria criteria1 = session.createCriteria(User.class);
            criteria1.add(Restrictions.eq("user_Status", user_Status));

            if (!criteria1.list().isEmpty()) {
                List<User> usersList = criteria1.list();

                Criteria criteria2 = session.createCriteria(Gig_Status.class);
                criteria2.add(Restrictions.eq("value", "Pending"));

                if (!criteria2.list().isEmpty()) {
                    Gig_Status gig_Status = (Gig_Status) criteria2.list().get(0);

                    List<Gig> allPendingGigsList = new ArrayList<>();
                    List<String> allPendingGigsImagesList = new ArrayList<>();
                    for (User user : usersList) {
                        Criteria criteria3 = session.createCriteria(Gig.class);
                        criteria3.add(Restrictions.eq("user", user));
                        criteria3.add(Restrictions.eq("gig_Status", gig_Status));
                        criteria3.addOrder(Order.desc("created_at"));

                        if (!criteria3.list().isEmpty()) {
                            List<Gig> usersGigsList = criteria3.list();

                            for (Gig gig : usersGigsList) {
                                allPendingGigsList.add(gig);
                                String BaseURL = "http://localhost:8080/hireup_backend/gig-images/" + gig.getId() + "/";
                                String image1URL = BaseURL + "image1.png";
                                allPendingGigsImagesList.add(image1URL);
                            }
                        }
                    }

                    if (allPendingGigsList.size() >= 1) {
                        responseObject.addProperty("status", true);
                        responseObject.add("allPendingGigsImagesList", gson.toJsonTree(allPendingGigsImagesList));
                        responseObject.add("allPendingGigsList", gson.toJsonTree(allPendingGigsList));
                    } else {
                        responseObject.addProperty("message", "EMPTY");
                    }
                } else {
                    responseObject.addProperty("message", "EMPTY");
                }
            } else {
                responseObject.addProperty("message", "EMPTY");
            }
        }

        session.close();

        String responseText = gson.toJson(responseObject);
        response.setContentType("application/json");
        response.getWriter().write(responseText);
    }
}
