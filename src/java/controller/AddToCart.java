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
import java.util.ArrayList;
import java.util.List;
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
@WebServlet(name = "AddToCart", urlPatterns = {"/AddToCart"})
public class AddToCart extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(request.getReader(), JsonObject.class);

        int gigId = jsonObject.get("gigId").getAsInt();
        int gigPackageId = jsonObject.get("gigPackageId").getAsInt();

        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", false);

        HttpSession httpSession = request.getSession(true);

        Session session = HibernateUtil.getSessionFactory().openSession();

        if (httpSession != null && httpSession.getAttribute("user") != null) {
            User user = (User) httpSession.getAttribute("user");

            if (user.getUser_Status().getValue().equals("Active") && user.getVerification().equals("VERIFIED!")) {
                Criteria criteria = session.createCriteria(Gig_Has_Package.class);
                criteria.add(Restrictions.eq("id", gigPackageId));
                if (!criteria.list().isEmpty()) {
                    Gig_Has_Package gig_Has_Package = (Gig_Has_Package) criteria.list().get(0);

                    Criteria criteria1 = session.createCriteria(Cart.class);
                    criteria1.add(Restrictions.eq("user", user));
                    criteria1.add(Restrictions.eq("gig_Has_Package", gig_Has_Package));
                    if (!criteria1.list().isEmpty()) {
                        Cart userCartItem = (Cart) criteria1.list().get(0);
                        session.delete(userCartItem);
                        responseObject.addProperty("status", true);
                        responseObject.addProperty("message", userCartItem.getGig_Has_Package().getGig().getTitle() + " (" + userCartItem.getGig_Has_Package().getPackage_Type().getName() + " Package) is Removed from Cart!");
                    } else {
                        Cart userCartItem = new Cart();
                        userCartItem.setUser(user);
                        userCartItem.setGig_Has_Package(gig_Has_Package);
                        session.save(userCartItem);
                        responseObject.addProperty("status", true);
                        responseObject.addProperty("message", userCartItem.getGig_Has_Package().getGig().getTitle() + " (" + userCartItem.getGig_Has_Package().getPackage_Type().getName() + " Package) is Added to Cart!");
                    }

                    session.beginTransaction().commit();
                }
            }
        } else {
            List<Integer> cartGigPackagesId = new ArrayList<>();
            if (httpSession != null && httpSession.getAttribute("cart") != null) {
                List<Integer> exsistingCartItemIds = (List<Integer>) httpSession.getAttribute("cart");
                for (Integer exsistingCartItemId : exsistingCartItemIds) {
                    cartGigPackagesId.add(exsistingCartItemId);
                }
            }

            Criteria criteria = session.createCriteria(Gig_Has_Package.class);
            criteria.add(Restrictions.eq("id", gigPackageId));

            if (!criteria.list().isEmpty()) {
                Gig_Has_Package userCartItem = (Gig_Has_Package) criteria.list().get(0);
                if (cartGigPackagesId.contains(gigPackageId)) {
                    cartGigPackagesId.remove(Integer.valueOf(gigPackageId));
                    responseObject.addProperty("status", true);
                    responseObject.addProperty("message", userCartItem.getGig().getTitle() + " (" + userCartItem.getPackage_Type().getName() + " Package) is Removed from Cart!");
                } else {
                    cartGigPackagesId.add(gigPackageId);
                    responseObject.addProperty("status", true);
                    responseObject.addProperty("message", userCartItem.getGig().getTitle() + " (" + userCartItem.getPackage_Type().getName() + " Package) is Added to Cart!");
                }
            }

            httpSession.setAttribute("cart", cartGigPackagesId);
            Cookie cookie = new Cookie("JSESSIONID", httpSession.getId());
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setSecure(true);
            response.addCookie(cookie);
        }

        session.close();

        String responseText = gson.toJson(responseObject);
        response.setContentType("application/json");
        response.getWriter().write(responseText);
    }
}
