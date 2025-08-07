/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import entity.Cart;
import entity.Gig_Has_Package;
import entity.User;
import entity.User_Status;
import hibernate.HibernateUtil;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
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
import org.mindrot.jbcrypt.BCrypt;

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
        boolean rememberMe = jsonObject.get("rememberMe").getAsBoolean();

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
            criteria1.add(Restrictions.eq("user_Status", status));

            if (criteria1.list().isEmpty()) {
                responseObject.addProperty("message", "Invalid Credentials!");
            } else {
                User user = (User) criteria1.list().get(0);

                if (BCrypt.checkpw(password, user.getPassword())) {

                    String verificationCode = user.getVerification();

                    responseObject.addProperty("status", true);

                    HttpSession httpSession = request.getSession();

                    if (!verificationCode.equals("VERIFIED!")) {
                        httpSession.setAttribute("email", email);

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
                                    Mail.sendMail(email, "HireUp - User Verification", filledVerificationEmailTemplate);
                                }
                            };
                            Thread t = new Thread(r);
                            t.start();
                        }
                    } else {
                        httpSession.setAttribute("user", user);

                        responseObject.addProperty("message", "WVERIFY");
                    }

                    if (httpSession.getAttribute("cart") != null) {
                        List<Integer> cardItemsIds = (List<Integer>) httpSession.getAttribute("cart");
                        if (!cardItemsIds.isEmpty()) {
                            for (Integer cardItemsId : cardItemsIds) {
                                Criteria criteria2 = session.createCriteria(Gig_Has_Package.class);
                                criteria2.add(Restrictions.eq("id", cardItemsId));
                                if (!criteria2.list().isEmpty()) {
                                    Gig_Has_Package gig_Has_Package = (Gig_Has_Package) criteria2.list().get(0);

                                    User u = null;
                                    if (httpSession.getAttribute("user") != null) {
                                        u = (User) httpSession.getAttribute("user");
                                    } else {
                                        Criteria criteria3 = session.createCriteria(User.class);
                                        criteria3.add(Restrictions.eq("email", httpSession.getAttribute("email")));
                                        if (!criteria3.list().isEmpty()) {
                                            u = (User) criteria3.list().get(0);
                                        }
                                    }

                                    Criteria criteria3 = session.createCriteria(Cart.class);
                                    criteria3.add(Restrictions.eq("user", u));
                                    criteria3.add(Restrictions.eq("gig_Has_Package", gig_Has_Package));
                                    if (criteria3.list().isEmpty()) {
                                        Cart cart = new Cart();
                                        cart.setUser(u);
                                        cart.setGig_Has_Package(gig_Has_Package);
                                        session.save(cart);
                                        session.beginTransaction().commit();
                                    }
                                }
                            }
                        }
                        httpSession.removeAttribute("cart");
                    }

                    Cookie cookie = new Cookie("JSESSIONID", httpSession.getId());
                    cookie.setHttpOnly(true);
                    cookie.setPath("/");
                    cookie.setSecure(true);
                    response.addCookie(cookie);

                    Cookie cookie1;
                    if (rememberMe) {
                        cookie1 = new Cookie("rememberMe", email);
                        cookie1.setMaxAge(60 * 60 * 24 * 7);
                        cookie1.setHttpOnly(true);
                        cookie1.setPath("/");
                        cookie1.setSecure(true);
                        response.addCookie(cookie1);
                    } else {
                        cookie1 = new Cookie("rememberMe", "");
                        cookie1.setMaxAge(0);
                        cookie1.setPath("/");
                        response.addCookie(cookie1);
                    }

                } else {
                    responseObject.addProperty("message", "Invalid Credentials!");
                }
            }

            session.close();
        }

        String responseText = gson.toJson(responseObject);
        response.setContentType("application/json");
        response.getWriter().write(responseText);
    }
}
