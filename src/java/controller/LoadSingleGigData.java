/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import entity.Gig;
import hibernate.HibernateUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.Util;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

/**
 *
 * @author User
 */
@WebServlet(name = "LoadSingleGigData", urlPatterns = {"/LoadSingleGigData"})
public class LoadSingleGigData extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Gson gson = new Gson();
        JsonObject responseObject = new JsonObject();

        responseObject.addProperty("status", false);

        String gigId = request.getParameter("id");

        if (Util.isInteger(gigId)) {
            Session session = HibernateUtil.getSessionFactory().openSession();

            Criteria criteria = session.createCriteria(Gig.class);
            criteria.add(Restrictions.eq("id", Integer.parseInt(gigId)));

            if (!criteria.list().isEmpty()) {
                Gig gig = (Gig) criteria.list().get(0);

                if (gig.getStatus().getValue().equals("Verified")) {
                    gig.getUser().setId(-1);
                    gig.getUser().setEmail(null);
                    gig.getUser().setPassword(null);
                    gig.getUser().setDob(null);
                    gig.getUser().setJoined_date(null);
                    gig.getUser().setVerification(null);
                    gig.getUser().setUser_Type(null);
                    gig.getUser().setUser_Status(null);
                    gig.getUser().setUser_Has_Address(null);
                    gig.getUser().setLocale(null);

                    responseObject.add("singleGig", gson.toJsonTree(gig));

                    Criteria criteria1 = session.createCriteria(Gig.class);
                    criteria1.add(Restrictions.eq("sub_Category", gig.getSub_Category()));

                    List<Gig> relatedGigsList = new ArrayList<>();
                    if (!criteria1.list().isEmpty()) {
                        List<Gig> relatedGigs = criteria1.list();

                        for (Gig relatedGig : relatedGigs) {
                            relatedGigsList.add(relatedGig);
                        }
                    }

                    if (relatedGigsList.size() >= 1) {
                        responseObject.addProperty("message", "HREALATED");
                        responseObject.add("relatedGigs", gson.toJsonTree(gig));
                    }

                    responseObject.addProperty("status", true);
                }
            }
            
            session.close();
        }

        String responseText = gson.toJson(responseObject);
        response.setContentType("application/json");
        response.getWriter().write(responseText);
    }
}
