/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import entity.User;
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
@WebServlet(name = "SignUp", urlPatterns = {"/SignUp"})
public class SignUp extends HttpServlet {

    private void setCorsHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        response.setHeader("Access-Control-Allow-Credentials", "true");
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        setCorsHeaders(response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        setCorsHeaders(response);

        Gson gson = new Gson();
        JsonObject user = gson.fromJson(request.getReader(), JsonObject.class);

        String fname = user.get("fname").getAsString();
        String lname = user.get("lname").getAsString();
        final String email = user.get("email").getAsString();
        String password = user.get("password").getAsString();

        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", false);

        if (fname.isEmpty()) {
            responseObject.addProperty("message", "Please enter your First Name!");
        } else if (lname.isEmpty()) {
            responseObject.addProperty("message", "Please enter your Last Name!");
        } else if (email.isEmpty()) {
            responseObject.addProperty("message", "Please enter your Email Address!");
        } else if (!Util.isEmailValid(email)) {
            responseObject.addProperty("message", "Please enter valid Email Address!");
        } else if (password.isEmpty()) {
            responseObject.addProperty("message", "Please enter your Password!");
        } else if (!Util.isPasswordValid(password)) {
            responseObject.addProperty("message", "Please enter valid Password! Password must be 8-20 characters long and include Uppercase, Lowercase, Number, and Special Character.");
        } else {

            Session session = HibernateUtil.getSessionFactory().openSession();

            Criteria criteria = session.createCriteria(User.class);
            criteria.add(Restrictions.eq("email", email));

            if (!criteria.list().isEmpty()) {
                responseObject.addProperty("message", "This Email Address is Already Exists!");
            } else {
                User u = new User();
                u.setFname(fname);
                u.setLname(lname);
                u.setEmail(email);
                u.setPassword(password);
                u.setJoined_date(new Date());

                final String verificationCode = Util.generateVerificationCode(session);
                u.setVerification(verificationCode);

                session.save(u);
                session.beginTransaction().commit();

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

                String verificationEmailTemplatePath = getServletContext().getRealPath("/assets/templates/emails/UserVerification.html");
                String verificationEmailTemplate = Util.loadEmailTemplate(verificationEmailTemplatePath);

                String logoURL = "https://raw.githubusercontent.com/thisalsalpura/hireup_frontend/master/src/assets/icons/logo.svg";
                String facebookURL = "https://raw.githubusercontent.com/thisalsalpura/hireup_frontend/master/src/assets/icons/facebook.svg";
                String instagramURL = "https://raw.githubusercontent.com/thisalsalpura/hireup_frontend/master/src/assets/icons/instagram.svg";
                String linkedinURL = "https://raw.githubusercontent.com/thisalsalpura/hireup_frontend/master/src/assets/icons/linkedin.svg";
                String xtwitterURL = "https://raw.githubusercontent.com/thisalsalpura/hireup_frontend/master/src/assets/icons/x-twitter.svg";
                String youtubeURL = "https://raw.githubusercontent.com/thisalsalpura/hireup_frontend/master/src/assets/icons/youtube.svg";

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

                    System.out.println("Email body size (bytes): " + filledVerificationEmailTemplate.getBytes("UTF-8").length);

                    Runnable r = new Runnable() {
                        @Override
                        public void run() {
                            Mail.sendMail(email, "HireUp - Verification", filledVerificationEmailTemplate);
                        }
                    };
                    Thread t = new Thread(r);
                    t.start();
                }

                responseObject.addProperty("status", true);
                responseObject.addProperty("message", "User registered Successfully! Please check your Email Address for the Verification.");
            }

            session.close();

        }

        String responseText = gson.toJson(responseObject);
        response.setContentType("application/json");
        response.getWriter().write(responseText);
    }
}
