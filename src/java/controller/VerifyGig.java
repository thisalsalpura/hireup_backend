/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import entity.Gig;
import entity.Gig_Status;
import hibernate.HibernateUtil;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.Mail;
import model.Util;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

/**
 *
 * @author User
 */
@WebServlet(name = "VerifyGig", urlPatterns = {"/VerifyGig"})
public class VerifyGig extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Gson gson = new Gson();
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

                if (gig.getGig_Status().getValue().equals("Pending")) {
                    Criteria criteria1 = session.createCriteria(Gig_Status.class);
                    criteria1.add(Restrictions.eq("value", "Verified"));

                    if (!criteria1.list().isEmpty()) {
                        Gig_Status gig_Status = (Gig_Status) criteria1.list().get(0);

                        gig.setGig_Status(gig_Status);
                        session.merge(gig);
                        session.beginTransaction().commit();

                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

                        String verificationEmailTemplatePath = getServletContext().getRealPath("/assets/templates/emails/VerifiedGig.html");
                        String verificationEmailTemplate = Util.loadEmailTemplate(verificationEmailTemplatePath);

                        String logoURL = "https://raw.githubusercontent.com/thisalsalpura/hireup_backend/master/web/assets/icons/logo.png";
                        String facebookURL = "https://raw.githubusercontent.com/thisalsalpura/hireup_backend/master/web/assets/icons/facebook.png";
                        String instagramURL = "https://raw.githubusercontent.com/thisalsalpura/hireup_backend/master/web/assets/icons/instagram.png";
                        String linkedinURL = "https://raw.githubusercontent.com/thisalsalpura/hireup_backend/master/web/assets/icons/linkedin.png";
                        String xtwitterURL = "https://raw.githubusercontent.com/thisalsalpura/hireup_backend/master/web/assets/icons/x-twitter.png";
                        String youtubeURL = "https://raw.githubusercontent.com/thisalsalpura/hireup_backend/master/web/assets/icons/youtube.png";

                        if (!verificationEmailTemplate.isEmpty()) {
                            String filledVerificationEmailTemplate = verificationEmailTemplate
                                    .replace("{{logo}}", logoURL)
                                    .replace("{{date}}", sdf.format(new Date()))
                                    .replace("{{name}}", gig.getUser().getFname() + " " + gig.getUser().getLname())
                                    .replace("{{gigTitle}}", gig.getTitle())
                                    .replace("{{facebookIcon}}", facebookURL)
                                    .replace("{{instagramIcon}}", instagramURL)
                                    .replace("{{linkedinIcon}}", linkedinURL)
                                    .replace("{{x-twitterIcon}}", xtwitterURL)
                                    .replace("{{youtubeIcon}}", youtubeURL);
                            
                            final String email = gig.getUser().getEmail();

                            Runnable r = new Runnable() {
                                @Override
                                public void run() {
                                    Mail.sendMail(email, "HireUp - Verified Gig", filledVerificationEmailTemplate);
                                }
                            };
                            Thread t = new Thread(r);
                            t.start();

                            responseObject.addProperty("status", true);
                            responseObject.addProperty("message", gig.getTitle() + " set as a Verified Gig.");
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
