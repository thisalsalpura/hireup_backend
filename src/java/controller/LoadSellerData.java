/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import entity.User;
import entity.User_As_Seller;
import entity.User_As_Seller_Has_Seller_Education;
import entity.User_As_Seller_Has_Seller_Skills;
import hibernate.HibernateUtil;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
@WebServlet(name = "LoadSellerData", urlPatterns = {"/LoadSellerData"})
public class LoadSellerData extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Gson gson = new Gson();

        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", false);

        HttpSession httpSession = request.getSession(false);

        if (httpSession != null && httpSession.getAttribute("user") != null) {
            User user = (User) httpSession.getAttribute("user");

            if (user.getUser_Status().getValue().equals("Active") && user.getVerification().equals("VERIFIED!") && user.getUser_Type().getValue().equals("Seller") && user.getDob() != null && user.getUser_Has_Address() != null && user.getLocale() != null) {
                responseObject.addProperty("status", true);
                responseObject.addProperty("name", user.getFname() + " " + user.getLname());
                responseObject.addProperty("country", user.getUser_Has_Address().getCity().getCountry().getName());

                Session session = HibernateUtil.getSessionFactory().openSession();

                Criteria c = session.createCriteria(User_As_Seller.class);
                c.add(Restrictions.eq("user", user));

                if (!c.list().isEmpty()) {
                    User_As_Seller user_As_Seller = (User_As_Seller) c.list().get(0);
                    if (user_As_Seller.getAbout() != null) {
                        responseObject.addProperty("about", user_As_Seller.getAbout());

                        Criteria criteria = session.createCriteria(User_As_Seller_Has_Seller_Skills.class);
                        criteria.add(Restrictions.eq("user_As_Seller", user_As_Seller));

                        if (!criteria.list().isEmpty()) {
                            List<User_As_Seller_Has_Seller_Skills> skillsList = criteria.list();
                            responseObject.add("skillsList", gson.toJsonTree(skillsList));
                        }

                        Criteria criteria1 = session.createCriteria(User_As_Seller_Has_Seller_Education.class);
                        criteria1.add(Restrictions.eq("user_As_Seller", user_As_Seller));

                        if (!criteria1.list().isEmpty()) {
                            Map<String, String> qualificationsMap = new HashMap<>();
                            List<User_As_Seller_Has_Seller_Education> qualificationsList = criteria1.list();

                            for (User_As_Seller_Has_Seller_Education user_As_Seller_Has_Seller_Education : qualificationsList) {
                                qualificationsMap.put(user_As_Seller_Has_Seller_Education.getSeller_Education().getCourse_name(), user_As_Seller_Has_Seller_Education.getSeller_Education().getInstitute_name());
                            }

                            responseObject.add("qualificationsMap", gson.toJsonTree(qualificationsMap));
                        }

                        responseObject.addProperty("profile", "UPDATED");
                    }
                }

                responseObject.addProperty("status", true);
                session.close();
            }
        }

        String responseText = gson.toJson(responseObject);
        response.setContentType("application/json");
        response.getWriter().write(responseText);
    }
}
