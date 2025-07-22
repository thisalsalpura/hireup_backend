/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import entity.Seller_Status;
import entity.User;
import entity.User_As_Seller;
import entity.User_Type;
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
@WebServlet(name = "VerifySeller", urlPatterns = {"/VerifySeller"})
public class VerifySeller extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(request.getReader(), JsonObject.class);

        String email = jsonObject.get("email").getAsString();

        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", false);

        if (!email.isEmpty()) {
            Session session = HibernateUtil.getSessionFactory().openSession();

            Criteria criteria = session.createCriteria(User.class);
            criteria.add(Restrictions.eq("email", email));

            if (!criteria.list().isEmpty()) {
                User user = (User) criteria.list().get(0);

                Criteria criteria1 = session.createCriteria(User_As_Seller.class);
                criteria1.add(Restrictions.eq("user", user));

                if (!criteria1.list().isEmpty()) {
                    Criteria criteria2 = session.createCriteria(Seller_Status.class);
                    criteria2.add(Restrictions.eq("value", "Verified"));

                    if (!criteria2.list().isEmpty()) {
                        Seller_Status status = (Seller_Status) criteria2.list().get(0);

                        User_As_Seller seller = (User_As_Seller) criteria1.list().get(0);
                        seller.setSeller_Status(status);

                        Criteria criteria3 = session.createCriteria(User_Type.class);
                        criteria3.add(Restrictions.eq("value", "Seller"));

                        if (!criteria3.list().isEmpty()) {
                            User_Type type = (User_Type) criteria3.list().get(0);

                            user.setUser_Type(type);
                        }

                        session.merge(seller);
                        session.merge(user);
                        session.beginTransaction().commit();

                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

                        String verificationEmailTemplatePath = getServletContext().getRealPath("/assets/templates/emails/VerifiedSeller.html");
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
                                    .replace("{{name}}", user.getFname() + " " + user.getLname())
                                    .replace("{{facebookIcon}}", facebookURL)
                                    .replace("{{instagramIcon}}", instagramURL)
                                    .replace("{{linkedinIcon}}", linkedinURL)
                                    .replace("{{x-twitterIcon}}", xtwitterURL)
                                    .replace("{{youtubeIcon}}", youtubeURL);

                            Runnable r = new Runnable() {
                                @Override
                                public void run() {
                                    Mail.sendMail(email, "HireUp - Verified Seller", filledVerificationEmailTemplate);
                                }
                            };
                            Thread t = new Thread(r);
                            t.start();

                            responseObject.addProperty("status", true);
                            responseObject.addProperty("message", user.getFname() + " " + user.getLname() + " set as a Verified Seller.");
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
