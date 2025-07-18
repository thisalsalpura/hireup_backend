/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import entity.User;
import entity.User_Status;
import entity.User_Type;
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
@WebServlet(name = "SignUp", urlPatterns = {"/SignUp"})
public class SignUp extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(request.getReader(), JsonObject.class);

        String fname = jsonObject.get("fname").getAsString();
        String lname = jsonObject.get("lname").getAsString();
        final String email = jsonObject.get("email").getAsString();
        String password = jsonObject.get("password").getAsString();

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
                User user = new User();
                user.setFname(fname);
                user.setLname(lname);
                user.setEmail(email);
                user.setPassword(password);
                user.setJoined_date(new Date());

                final String verificationCode = Util.generateVerificationCode(session);
                user.setVerification(verificationCode);

                Criteria criteria1 = session.createCriteria(User_Status.class);
                criteria1.add(Restrictions.eq("value", "Active"));
                User_Status status = (User_Status) criteria1.list().get(0);
                user.setUser_Status(status);

                Criteria criteria2 = session.createCriteria(User_Type.class);
                criteria2.add(Restrictions.eq("value", "Buyer"));
                User_Type type = (User_Type) criteria2.list().get(0);
                user.setUser_Type(type);

                session.save(user);
                session.beginTransaction().commit();

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

                    HttpSession httpSession = request.getSession();
                    httpSession.setAttribute("email", email);
                    Cookie cookie = new Cookie("JSESSIONID", httpSession.getId());
                    cookie.setHttpOnly(true);
                    cookie.setPath("/");
                    cookie.setSecure(true);
                    response.addCookie(cookie);
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
