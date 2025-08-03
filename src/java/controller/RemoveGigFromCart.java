/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import entity.Cart;
import entity.Gig_Has_Package;
import entity.User;
import hibernate.HibernateUtil;
import java.io.IOException;
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
@WebServlet(name = "RemoveGigFromCart", urlPatterns = {"/RemoveGigFromCart"})
public class RemoveGigFromCart extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(request.getReader(), JsonObject.class);

        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", false);

        HttpSession httpSession = request.getSession(false);

        Session session = HibernateUtil.getSessionFactory().openSession();

        if (httpSession != null && httpSession.getAttribute("user") != null) {
            int cartId = jsonObject.get("cartId").getAsInt();

            User user = (User) httpSession.getAttribute("user");

            if (user.getUser_Status().getValue().equals("Active") && user.getVerification().equals("VERIFIED!")) {
                Criteria criteria = session.createCriteria(Cart.class);
                criteria.add(Restrictions.eq("id", cartId));
                if (!criteria.list().isEmpty()) {
                    Cart cart = (Cart) criteria.list().get(0);
                    session.delete(cart);
                    session.beginTransaction().commit();

                    responseObject.addProperty("status", true);
                    responseObject.addProperty("message", cart.getGig_Has_Package().getGig().getTitle() + " (" + cart.getGig_Has_Package().getPackage_Type().getName() + " Package) is Removed from Cart!");
                }
            }
        } else if (httpSession != null && httpSession.getAttribute("cart") != null) {
            int sessionCartId = jsonObject.get("sessionCartId").getAsInt();

            List<Integer> cardItemsIds = (List<Integer>) httpSession.getAttribute("cart");
            if (!cardItemsIds.isEmpty()) {
                for (Integer cardItemsId : cardItemsIds) {
                    if (cardItemsId == sessionCartId) {
                        cardItemsIds.remove(Integer.valueOf(sessionCartId));

                        Criteria criteria = session.createCriteria(Gig_Has_Package.class);
                        criteria.add(Restrictions.eq("id", sessionCartId));
                        if (!criteria.list().isEmpty()) {
                            Gig_Has_Package gig_Has_Package = (Gig_Has_Package) criteria.list().get(0);
                            responseObject.addProperty("status", true);
                            responseObject.addProperty("message", gig_Has_Package.getGig().getTitle() + " (" + gig_Has_Package.getPackage_Type().getName() + " Package) is Removed from Cart!");
                            break;
                        }
                    }
                }
            }
        }

        String responseText = gson.toJson(responseObject);
        response.setContentType("application/json");
        response.getWriter().write(responseText);
    }
}
