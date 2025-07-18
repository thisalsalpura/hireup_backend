/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import entity.User;
import entity.User_Status;
import hibernate.HibernateUtil;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import model.Mail;
import model.Util;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

/**
 *
 * @author User
 */
@WebServlet(name = "SignIn", urlPatterns = {"/SignIn"})
public class SignIn extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(request.getReader(), JsonObject.class);

        String email = jsonObject.get("email").getAsString();
        String password = jsonObject.get("password").getAsString();

        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", false);

        if (email.isEmpty()) {
            responseObject.addProperty("message", "Please enter your Email Address!");
        } else if (password.isEmpty()) {
            responseObject.addProperty("message", "Please enter your Password!");
        } else {
            Session session = HibernateUtil.getSessionFactory().openSession();

            Criteria criteria = session.createCriteria(User_Status.class);
            criteria.add(Restrictions.eq("value", "Active"));
            User_Status status = (User_Status) criteria.list().get(0);

            Criteria criteria1 = session.createCriteria(User.class);
            criteria1.add(Restrictions.eq("email", email));
            criteria1.add(Restrictions.eq("password", password));
            criteria1.add(Restrictions.eq("user_Status", status));

            if (criteria1.list().isEmpty()) {
                responseObject.addProperty("message", "Invalid Credentials!");
            } else {
                User user = (User) criteria1.list().get(0);

                String verificationCode = user.getVerification();

                responseObject.addProperty("status", true);

                HttpSession httpSession = request.getSession();

                if (!verificationCode.equals("VERIFIED!")) {
                    httpSession.setAttribute("email", email);
                    Cookie cookie = new Cookie("JSESSIONID", httpSession.getId());
                    cookie.setHttpOnly(true);
                    cookie.setPath("/");
                    cookie.setSecure(true);
                    response.addCookie(cookie);

                    responseObject.addProperty("message", "NVERIFY");

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

                    String verificationEmailTemplatePath = getServletContext().getRealPath("/assets/templates/emails/UserVerification.html");
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
                                .replace("{{code}}", verificationCode)
                                .replace("{{facebookIcon}}", facebookURL)
                                .replace("{{instagramIcon}}", instagramURL)
                                .replace("{{linkedinIcon}}", linkedinURL)
                                .replace("{{x-twitterIcon}}", xtwitterURL)
                                .replace("{{youtubeIcon}}", youtubeURL);

                        Runnable r = new Runnable() {
                            @Override
                            public void run() {
                                Mail.sendMail(email, "HireUp - Verification", filledVerificationEmailTemplate);
                            }
                        };
                        Thread t = new Thread(r);
                        t.start();
                    }
                } else {
                    httpSession.setAttribute("user", user);
                    Cookie cookie = new Cookie("JSESSIONID", httpSession.getId());
                    cookie.setHttpOnly(true);
                    cookie.setPath("/");
                    cookie.setSecure(true);
                    response.addCookie(cookie);

                    responseObject.addProperty("message", "WVERIFY");
                }
            }

            session.close();
        }

        String responseText = gson.toJson(responseObject);
        response.setContentType("application/json");
        response.getWriter().write(responseText);
    }
}
