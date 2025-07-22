/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import entity.City;
import entity.Locale;
import entity.User;
import entity.User_Has_Address;
import hibernate.HibernateUtil;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import model.Util;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

/**
 *
 * @author User
 */
@WebServlet(name = "UserProfileUpdate", urlPatterns = {"/UserProfileUpdate"})
public class UserProfileUpdate extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(request.getReader(), JsonObject.class);

        String fname = jsonObject.get("fname").getAsString();
        String lname = jsonObject.get("lname").getAsString();
        String dobStr = jsonObject.get("dob").getAsString();
        String line1 = jsonObject.get("line1").getAsString();
        String line2 = jsonObject.get("line2").getAsString();
        String pcode = jsonObject.get("pcode").getAsString();
        int countryId = jsonObject.get("countryId").getAsInt();
        int cityId = jsonObject.get("cityId").getAsInt();
        int localeId = jsonObject.get("localeId").getAsInt();

        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", false);

        if (fname.isEmpty()) {
            responseObject.addProperty("message", "Please enter your First Name!");
        } else if (lname.isEmpty()) {
            responseObject.addProperty("message", "Please enter your Last Name!");
        } else if (dobStr.isEmpty()) {
            responseObject.addProperty("message", "Please enter your Date of Birth!");
        } else if (line1.isEmpty()) {
            responseObject.addProperty("message", "Please enter your Address Line 1!");
        } else if (line2.isEmpty()) {
            responseObject.addProperty("message", "Please enter your Address Line 2!");
        } else if (pcode.isEmpty()) {
            responseObject.addProperty("message", "Please enter your Postal Code!");
        } else if (!Util.isPostalCodeValid(pcode)) {
            responseObject.addProperty("message", "Invalid Postal Code!");
        } else if (countryId == 0) {
            responseObject.addProperty("message", "Please select a Country!");
        } else if (cityId == 0) {
            responseObject.addProperty("message", "Please select a City!");
        } else if (localeId == 0) {
            responseObject.addProperty("message", "Please select a Locale!");
        } else {

            HttpSession httpSession = request.getSession(false);

            if (httpSession != null && httpSession.getAttribute("user") != null) {
                User user = (User) httpSession.getAttribute("user");

                if (user.getUser_Status().getValue().equals("Active") && user.getVerification().equals("VERIFIED!")) {
                    Session session = HibernateUtil.getSessionFactory().openSession();

                    Criteria criteria = session.createCriteria(Locale.class);
                    criteria.add(Restrictions.eq("id", localeId));

                    Locale locale = null;

                    if (!criteria.list().isEmpty()) {
                        locale = (Locale) criteria.list().get(0);
                    }

                    Criteria criteria1 = session.createCriteria(City.class);
                    criteria1.add(Restrictions.eq("id", cityId));

                    City city = null;

                    if (!criteria1.list().isEmpty()) {
                        city = (City) criteria1.list().get(0);
                    }

                    Criteria criteria2 = session.createCriteria(User.class);
                    criteria2.add(Restrictions.eq("email", user.getEmail()));

                    if (!criteria2.list().isEmpty()) {
                        responseObject.addProperty("status", true);

                        User u = (User) criteria2.list().get(0);
                        u.setFname(fname);
                        u.setLname(lname);

                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        Date dob = null;
                        try {
                            dob = sdf.parse(dobStr);
                        } catch (ParseException ex) {
                            ex.printStackTrace();
                        }

                        if (dob.before(new Date())) {
                            u.setDob(dob);

                            User_Has_Address user_has_address;
                            if (u.getUser_Has_Address() != null) {
                                user_has_address = u.getUser_Has_Address();
                                user_has_address.setLine_1(line1);
                                user_has_address.setLine_2(line2);
                                user_has_address.setCity(city);
                                user_has_address.setPostal_code(pcode);
                                session.merge(user_has_address);
                            } else {
                                user_has_address = new User_Has_Address();
                                user_has_address.setLine_1(line1);
                                user_has_address.setLine_2(line2);
                                user_has_address.setCity(city);
                                user_has_address.setPostal_code(pcode);
                                session.save(user_has_address);
                            }

                            u.setUser_Has_Address(user_has_address);
                            u.setLocale(locale);

                            httpSession.setAttribute("user", u);
                            Cookie cookie = new Cookie("JSESSIONID", httpSession.getId());
                            cookie.setHttpOnly(true);
                            cookie.setPath("/");
                            cookie.setSecure(true);
                            response.addCookie(cookie);

                            session.merge(u);
                            session.beginTransaction().commit();

                            responseObject.addProperty("status", true);
                            responseObject.addProperty("message", "User profile updated Successfully!");
                        } else {
                            responseObject.addProperty("message", "Invalid Date of Birth!");
                        }
                    }

                    session.close();
                } else {
                    responseObject.addProperty("message", "You're Inactive or Unverified User!");
                }

            } else {
                responseObject.addProperty("message", "You're Session is Timeout.");
            }

        }

        String responseText = gson.toJson(responseObject);
        response.setContentType("application/json");
        response.getWriter().write(responseText);
    }
}
