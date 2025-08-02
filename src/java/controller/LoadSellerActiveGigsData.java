/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import entity.Gig;
import entity.Gig_Status;
import entity.Gig_Visible_Status;
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
import javax.servlet.http.HttpSession;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

/**
 *
 * @author User
 */
@WebServlet(name = "LoadSellerActiveGigsData", urlPatterns = {"/LoadSellerActiveGigsData"})
public class LoadSellerActiveGigsData extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        JsonObject responseObject = new JsonObject();

        responseObject.addProperty("status", false);

        HttpSession httpSession = request.getSession(false);

        if (httpSession != null && httpSession.getAttribute("user") != null) {
            User user = (User) httpSession.getAttribute("user");

            if (user.getUser_Status().getValue().equals("Active") && user.getVerification().equals("VERIFIED!") && user.getUser_Type().getValue().equals("Seller") && user.getDob() != null && user.getUser_Has_Address() != null && user.getLocale() != null) {
                Session session = HibernateUtil.getSessionFactory().openSession();

                Criteria criteria = session.createCriteria(Gig_Status.class);
                criteria.add(Restrictions.eq("value", "Verified"));
                
                Criteria criteria1 = session.createCriteria(Gig_Visible_Status.class);
                criteria1.add(Restrictions.eq("name", "Active"));

                if (!criteria.list().isEmpty() && !criteria1.list().isEmpty()) {
                    Gig_Status gig_Status = (Gig_Status) criteria.list().get(0);
                    Gig_Visible_Status visible_Status = (Gig_Visible_Status) criteria1.list().get(0);

                    Criteria criteria2 = session.createCriteria(Gig.class);
                    criteria2.add(Restrictions.eq("user", user));
                    criteria2.add(Restrictions.eq("gig_Status", gig_Status));
                    criteria2.add(Restrictions.eq("gig_Visible_Status", visible_Status));
                    Order order = Order.desc("created_at");
                    criteria2.addOrder(order);

                    if (!criteria2.list().isEmpty()) {
                        List<Gig> userGigs = criteria2.list();

                        List<Gig> userGigsList = new ArrayList<>();
                        List<String> userGigsImagesList = new ArrayList<>();
                        for (Gig gig : userGigs) {
                            userGigsList.add(gig);
                            String BaseURL = "http://localhost:8080/hireup_backend/gig-images/" + gig.getId() + "/";
                            String image1URL = BaseURL + "image1.png";
                            userGigsImagesList.add(image1URL);
                        }

                        if (userGigsList.size() >= 1) {
                            responseObject.addProperty("status", true);
                            responseObject.add("userGigsList", gson.toJsonTree(userGigsList));
                            responseObject.add("userGigsImagesList", gson.toJsonTree(userGigsImagesList));
                        } else {
                            responseObject.addProperty("message", "EMPTY");
                        }
                    } else {
                        responseObject.addProperty("message", "EMPTY");
                    }
                } else {
                    responseObject.addProperty("message", "EMPTY");
                }

                session.close();
            }
        }

        String responseText = gson.toJson(responseObject);
        response.setContentType("application/json");
        response.getWriter().write(responseText);
    }
}
