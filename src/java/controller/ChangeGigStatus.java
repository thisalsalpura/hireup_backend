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
@WebServlet(name = "ChangeGigStatus", urlPatterns = {"/ChangeGigStatus"})
public class ChangeGigStatus extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        JsonObject jsonObject = gson.fromJson(request.getReader(), JsonObject.class);

        int gigId = jsonObject.get("id").getAsInt();

        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", false);

        if (gigId != 0) {
            Session session = HibernateUtil.getSessionFactory().openSession();

            Criteria criteria = session.createCriteria(Gig.class);
            criteria.add(Restrictions.eq("id", gigId));
            if (!criteria.list().isEmpty()) {
                Gig gig = (Gig) criteria.list().get(0);
                if (gig.getGig_Status().getValue().equals("Verified")) {
                    Criteria criteria1 = session.createCriteria(Gig_Status.class);
                    criteria1.add(Restrictions.eq("value", "Pending"));
                    if (!criteria1.list().isEmpty()) {
                        Gig_Status status = (Gig_Status) criteria1.list().get(0);
                        gig.setGig_Status(status);
                        responseObject.addProperty("message", "Change " + gig.getTitle() + " status to " + status.getValue());
                    }
                } else if (gig.getGig_Status().getValue().equals("Pending")) {
                    Criteria criteria1 = session.createCriteria(Gig_Status.class);
                    criteria1.add(Restrictions.eq("value", "Verified"));
                    if (!criteria1.list().isEmpty()) {
                        Gig_Status status = (Gig_Status) criteria1.list().get(0);
                        gig.setGig_Status(status);
                        responseObject.addProperty("message", "Change " + gig.getTitle() + " status to " + status.getValue());
                    }
                }

                session.merge(gig);
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
