/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import entity.Cart;
import entity.Gig_Has_Package;
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
import org.hibernate.criterion.Restrictions;

/**
 *
 * @author User
 */
@WebServlet(name = "LoadCartData", urlPatterns = {"/LoadCartData"})
public class LoadCartData extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", false);

        HttpSession httpSession = request.getSession(true);

        Session session = HibernateUtil.getSessionFactory().openSession();

        if (httpSession != null && httpSession.getAttribute("user") != null) {
            User user = (User) httpSession.getAttribute("user");

            if (user.getUser_Status().getValue().equals("Active") && user.getVerification().equals("VERIFIED!")) {
                Criteria criteria = session.createCriteria(Cart.class);
                criteria.add(Restrictions.eq("user", user));
                if (!criteria.list().isEmpty()) {
                    List<Cart> userCartGigs = criteria.list();
                    List<Cart> userCartGigsList = new ArrayList<>();
                    List<String> userCartGigsImagesList = new ArrayList<>();
                    for (Cart userCartGig : userCartGigs) {
                        userCartGigsList.add(userCartGig);
                        String BaseURL = "http://localhost:8080/hireup_backend/gig-images/" + userCartGig.getGig_Has_Package().getGig().getId() + "/";
                        String image1URL = BaseURL + "image1.png";
                        userCartGigsImagesList.add(image1URL);
                    }

                    responseObject.addProperty("status", true);
                    responseObject.addProperty("message", "NEMPTY");
                    responseObject.add("userCartGigsList", gson.toJsonTree(userCartGigsList));
                    responseObject.add("userCartGigsImagesList", gson.toJsonTree(userCartGigsImagesList));
                } else {
                    responseObject.addProperty("status", true);
                    responseObject.addProperty("message", "EMPTY");
                }
            }
        } else if (httpSession != null && httpSession.getAttribute("cart") != null) {
            List<Integer> gigPackagesIds = (List<Integer>) httpSession.getAttribute("cart");

            if (!gigPackagesIds.isEmpty()) {
                List<Cart> userCartGigs = new ArrayList<>();
                for (Integer gigPackagesId : gigPackagesIds) {
                    Criteria criteria = session.createCriteria(Gig_Has_Package.class);
                    criteria.add(Restrictions.eq("id", gigPackagesId));
                    if (!criteria.list().isEmpty()) {
                        Gig_Has_Package gig_Has_Package = (Gig_Has_Package) criteria.list().get(0);

                        Cart cart = new Cart();
                        cart.setGig_Has_Package(gig_Has_Package);
                        userCartGigs.add(cart);
                    }
                }

                if (!userCartGigs.isEmpty()) {
                    List<Cart> userCartGigsList = new ArrayList<>();
                    List<String> userCartGigsImagesList = new ArrayList<>();
                    for (Cart userCartGig : userCartGigs) {
                        userCartGigsList.add(userCartGig);
                        String BaseURL = "http://localhost:8080/hireup_backend/gig-images/" + userCartGig.getGig_Has_Package().getGig().getId() + "/";
                        String image1URL = BaseURL + "image1.png";
                        userCartGigsImagesList.add(image1URL);
                    }

                    responseObject.addProperty("status", true);
                    responseObject.addProperty("message", "NEMPTY");
                    responseObject.add("userCartGigsList", gson.toJsonTree(userCartGigsList));
                    responseObject.add("userCartGigsImagesList", gson.toJsonTree(userCartGigsImagesList));
                } else {
                    responseObject.addProperty("status", true);
                    responseObject.addProperty("message", "EMPTY");
                }
            } else {
                responseObject.addProperty("status", true);
                responseObject.addProperty("message", "EMPTY");
            }
        } else {
            responseObject.addProperty("status", true);
            responseObject.addProperty("message", "EMPTY");
        }

        String responseText = gson.toJson(responseObject);
        response.setContentType("application/json");
        response.getWriter().write(responseText);
    }
}
