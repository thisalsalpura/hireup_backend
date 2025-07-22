/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import entity.Seller_Status;
import entity.User_As_Seller;
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
@WebServlet(name = "LoadPendingSellers", urlPatterns = {"/LoadPendingSellers"})
public class LoadPendingSellers extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Gson gson = new Gson();
        JsonObject responseObject = new JsonObject();

        responseObject.addProperty("status", false);

        Session session = HibernateUtil.getSessionFactory().openSession();

        Criteria criteria = session.createCriteria(Seller_Status.class);
        criteria.add(Restrictions.eq("value", "Pending"));

        Seller_Status status;
        if (!criteria.list().isEmpty()) {
            status = (Seller_Status) criteria.list().get(0);

            Criteria criteria1 = session.createCriteria(User_As_Seller.class);
            criteria1.add(Restrictions.eq("seller_Status", status));

            
            if (!criteria1.list().isEmpty()) {
                List<User_As_Seller> sellersList = criteria1.list();
                List<User_As_Seller> activeSellersList = new ArrayList<>();

                for (User_As_Seller user_As_Seller : sellersList) {
                    if (user_As_Seller.getUser().getUser_Status().getValue().equals("Active") && user_As_Seller.getUser().getVerification().equals("VERIFIED!")) {
                        user_As_Seller.getUser().setId(-1);
                        user_As_Seller.getUser().setPassword(null);
                        user_As_Seller.getUser().setDob(null);
                        user_As_Seller.getUser().setJoined_date(null);
                        user_As_Seller.getUser().setVerification(null);
                        user_As_Seller.getUser().setUser_Type(null);
                        user_As_Seller.getUser().setUser_Has_Address(null);
                        user_As_Seller.getUser().setLocale(null);
                        activeSellersList.add(user_As_Seller);
                    }
                }

                responseObject.addProperty("status", true);
                responseObject.add("sellerList", gson.toJsonTree(activeSellersList));
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
