/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
import org.hibernate.Criteria;
import org.hibernate.Session;

/**
 *
 * @author User
 */
@WebServlet(name = "LoadAllGigsList", urlPatterns = {"/LoadAllGigsList"})
public class LoadAllGigsList extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", false);

        Session session = HibernateUtil.getSessionFactory().openSession();

        Criteria criteria = session.createCriteria(Gig.class);
        if (!criteria.list().isEmpty()) {
            List<Gig> gigs = criteria.list();
            List<Gig> gigList = new ArrayList<>();
            List<String> gigImageList = new ArrayList<>();
            List<String> statusList = new ArrayList<>();
            for (Gig gig : gigs) {
                gigList.add(gig);
                String BaseURL = "http://localhost:8080/hireup_backend/gig-images/" + gig.getId() + "/";
                String imageURL = BaseURL + "image1.png";
                gigImageList.add(imageURL);
                statusList.add(gig.getGig_Status().getValue());
            }

            if (!gigList.isEmpty() && !gigImageList.isEmpty() && !statusList.isEmpty()) {
                responseObject.addProperty("status", true);
                responseObject.add("gigsList", gson.toJsonTree(gigList));
                responseObject.add("gigImageList", gson.toJsonTree(gigImageList));
                responseObject.add("statusList", gson.toJsonTree(statusList));
            }
        }

        session.close();

        String responseText = gson.toJson(responseObject);
        response.setContentType("application/json");
        response.getWriter().write(responseText);
    }
}
