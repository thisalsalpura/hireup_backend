/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import entity.Gig;
import entity.Gig_Visible_Status;
import entity.User;
import hibernate.HibernateUtil;
import java.io.IOException;
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
@WebServlet(name = "ChangeToInactiveGig", urlPatterns = {"/ChangeToInactiveGig"})
public class ChangeToInactiveGig extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(request.getReader(), JsonObject.class);

        int id = jsonObject.get("id").getAsInt();

        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", false);

        HttpSession httpSession = request.getSession(false);

        if (httpSession != null && httpSession.getAttribute("user") != null) {
            User user = (User) httpSession.getAttribute("user");

            if (user.getUser_Status().getValue().equals("Active") && user.getVerification().equals("VERIFIED!") && user.getUser_Type().getValue().equals("Seller") && user.getDob() != null && user.getUser_Has_Address() != null && user.getLocale() != null) {
                Session session = HibernateUtil.getSessionFactory().openSession();

                Criteria criteria = session.createCriteria(Gig.class);
                criteria.add(Restrictions.eq("id", id));

                if (!criteria.list().isEmpty()) {
                    Gig gig = (Gig) criteria.list().get(0);

                    Criteria criteria1 = session.createCriteria(Gig_Visible_Status.class);
                    criteria1.add(Restrictions.eq("name", "Inactive"));

                    if (!criteria1.list().isEmpty()) {
                        Gig_Visible_Status visible_Status = (Gig_Visible_Status) criteria1.list().get(0);
                        gig.setGig_Visible_Status(visible_Status);

                        session.merge(gig);
                        session.beginTransaction().commit();

                        responseObject.addProperty("status", true);
                        responseObject.addProperty("message", gig.getTitle() + " set as a Inactive Gig.");
                    }
                }

                session.close();
            }
        }

        String responseText = gson.toJson(responseObject);
        response.setContentType("application/json");
        response.getWriter().write(responseText);
    }
}
