/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import entity.FAQ;
import entity.Gig;
import entity.Gig_Has_Package;
import entity.Gig_Package_Type;
import entity.Gig_Search_Tag_Has_Gig;
import hibernate.HibernateUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

                    Criteria criteria1 = session.createCriteria(Gig_Search_Tag_Has_Gig.class);
                    criteria1.add(Restrictions.eq("gig", gig));

                    if (!criteria1.list().isEmpty()) {
                        List<Gig_Search_Tag_Has_Gig> searchTags = criteria1.list();

                        List<String> searchTagsList = new ArrayList<>();
                        for (Gig_Search_Tag_Has_Gig searchTag : searchTags) {
                            searchTagsList.add(searchTag.getSearch_Tag().getName());
                        }

                        if (searchTagsList.size() >= 1) {
                            Criteria criteria2 = session.createCriteria(FAQ.class);
                            criteria2.add(Restrictions.eq("gig", gig));

                            if (!criteria2.list().isEmpty()) {
                                List<FAQ> faqs = criteria2.list();

                                Map<String, String> faqsMap = new HashMap<>();
                                for (FAQ faq : faqs) {
                                    faqsMap.put(faq.getQuestion(), faq.getAnswer());
                                }

                                if (faqsMap.size() >= 1) {
                                    Criteria criteria3 = session.createCriteria(Gig_Package_Type.class);
                                    criteria3.add(Restrictions.eq("name", "Bronze"));

                                    Gig_Package_Type bronzePackage = null;
                                    if (!criteria3.list().isEmpty()) {
                                        bronzePackage = (Gig_Package_Type) criteria3.list().get(0);
                                    }

                                    Criteria criteria4 = session.createCriteria(Gig_Package_Type.class);
                                    criteria4.add(Restrictions.eq("name", "Silver"));

                                    Gig_Package_Type silverPackage = null;
                                    if (!criteria4.list().isEmpty()) {
                                        silverPackage = (Gig_Package_Type) criteria4.list().get(0);
                                    }

                                    Criteria criteria5 = session.createCriteria(Gig_Package_Type.class);
                                    criteria5.add(Restrictions.eq("name", "Gold"));

                                    Gig_Package_Type goldPackage = null;
                                    if (!criteria5.list().isEmpty()) {
                                        goldPackage = (Gig_Package_Type) criteria5.list().get(0);
                                    }

                                    Criteria criteria6 = session.createCriteria(Gig_Has_Package.class);
                                    criteria6.add(Restrictions.eq("gig", gig));
                                    criteria6.add(Restrictions.eq("package_Type", bronzePackage));

                                    if (!criteria6.list().isEmpty()) {
                                        Gig_Has_Package gigBronzePackage = (Gig_Has_Package) criteria6.list().get(0);

                                        Criteria criteria7 = session.createCriteria(Gig_Has_Package.class);
                                        criteria7.add(Restrictions.eq("gig", gig));
                                        criteria7.add(Restrictions.eq("package_Type", silverPackage));

                                        if (!criteria7.list().isEmpty()) {
                                            Gig_Has_Package gigSilverPackage = (Gig_Has_Package) criteria7.list().get(0);

                                            Criteria criteria8 = session.createCriteria(Gig_Has_Package.class);
                                            criteria8.add(Restrictions.eq("gig", gig));
                                            criteria8.add(Restrictions.eq("package_Type", goldPackage));

                                            if (!criteria8.list().isEmpty()) {
                                                Gig_Has_Package gigGoldPackage = (Gig_Has_Package) criteria8.list().get(0);

                                                responseObject.add("singleGig", gson.toJsonTree(gig));

                                                Criteria cri = session.createCriteria(Gig.class);
                                                cri.add(Restrictions.eq("sub_Category", gig.getSub_Category()));

                                                String BaseURL = "http://localhost:8080/hireup_backend/gig-images/" + gigId + "/";
                                                String image1URL = BaseURL + "image1.png";
                                                String image2URL = BaseURL + "image2.png";
                                                String image3URL = BaseURL + "image3.png";
                                                String docURL = BaseURL + "document.pdf";

                                                responseObject.addProperty("gigImage1", image1URL);
                                                responseObject.addProperty("gigImage2", image2URL);
                                                responseObject.addProperty("gigImage3", image3URL);
                                                responseObject.addProperty("gigDocument", docURL);

                                                responseObject.add("gigSearchTags", gson.toJsonTree(searchTagsList));

                                                responseObject.add("gigFaqs", gson.toJsonTree(faqsMap));
                                                
                                                responseObject.add("gigBronzePackage", gson.toJsonTree(gigBronzePackage));
                                                responseObject.add("gigSilverPackage", gson.toJsonTree(gigSilverPackage));
                                                responseObject.add("gigGoldPackage", gson.toJsonTree(gigGoldPackage));

                                                List<Gig> relatedGigsList = new ArrayList<>();
                                                if (!cri.list().isEmpty()) {
                                                    List<Gig> relatedGigs = cri.list();

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
                                    }
                                }
                            }
                        }
                    }
                }
            }

            session.close();
        }

        String responseText = gson.toJson(responseObject);
        response.setContentType("application/json");
        response.getWriter().write(responseText);
    }
}
