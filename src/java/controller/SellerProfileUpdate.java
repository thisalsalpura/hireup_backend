/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import entity.Seller_Education;
import entity.Seller_Skills;
import entity.User;
import entity.User_As_Seller;
import entity.User_As_Seller_Has_Seller_Education;
import entity.User_As_Seller_Has_Seller_Skills;
import hibernate.HibernateUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

/**
 *
 * @author User
 */
@WebServlet(name = "SellerProfileUpdate", urlPatterns = {"/SellerProfileUpdate"})
public class SellerProfileUpdate extends HttpServlet {

    private static Gson gson = new Gson();

    private void sendJsonResponse(HttpServletResponse response, JsonObject obj) throws IOException {
        String responseText = gson.toJson(obj);
        response.setContentType("application/json");
        response.getWriter().write(responseText);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Map<String, String> qualificationsMap = new HashMap<>();
        List<String> skillsList = new ArrayList<>();

        JsonObject jsonObject = gson.fromJson(request.getReader(), JsonObject.class);

        String about = jsonObject.get("about").getAsString();
        JsonArray qualifications = jsonObject.get("qualificationList").getAsJsonArray();
        JsonArray skills = jsonObject.get("skillList").getAsJsonArray();

        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", false);

        HttpSession httpSession = request.getSession(false);

        if (httpSession != null && httpSession.getAttribute("user") != null) {
            User user = (User) httpSession.getAttribute("user");

            if (user.getUser_Status().getValue().equals("Active") && user.getVerification().equals("VERIFIED!") && user.getUser_Type().getValue().equals("Seller") && user.getDob() != null && user.getUser_Has_Address() != null && user.getLocale() != null) {
                Session session = HibernateUtil.getSessionFactory().openSession();

                Criteria c = session.createCriteria(User_As_Seller.class);
                c.add(Restrictions.eq("user", user));

                if (!c.list().isEmpty()) {
                    User_As_Seller user_As_Seller = (User_As_Seller) c.list().get(0);

                    if (about.isEmpty()) {
                        responseObject.addProperty("message", "Please enter your About!");
                    } else {
                        if (qualifications.size() >= 1 && qualifications.size() <= 2) {
                            for (int i = 0; i < qualifications.size(); i++) {
                                JsonObject qualificationObj = qualifications.get(i).getAsJsonObject();
                                String qualificationName = qualificationObj.get("qualificationName").getAsString();
                                String qualificationPlace = qualificationObj.get("qualificationPlace").getAsString();

                                if (qualificationName.isEmpty()) {
                                    responseObject.addProperty("message", "Invalid Qualification Names!");
                                    sendJsonResponse(response, responseObject);
                                    return;
                                } else if (qualificationName.length() >= 45) {
                                    responseObject.addProperty("message", "Invalid Qualification Names!");
                                    sendJsonResponse(response, responseObject);
                                    return;
                                } else if (!qualificationName.matches("^[A-Za-z\\s]+$")) {
                                    responseObject.addProperty("message", "Invalid Qualification Names!");
                                    sendJsonResponse(response, responseObject);
                                    return;
                                } else if (qualificationPlace.isEmpty()) {
                                    responseObject.addProperty("message", "Invalid Qualification Names!");
                                    sendJsonResponse(response, responseObject);
                                    return;
                                } else if (qualificationPlace.length() >= 45) {
                                    responseObject.addProperty("message", "Invalid Qualification Names!");
                                    sendJsonResponse(response, responseObject);
                                    return;
                                } else if (!qualificationPlace.matches("^[A-Za-z\\s]+$")) {
                                    responseObject.addProperty("message", "Invalid Qualification Names!");
                                    sendJsonResponse(response, responseObject);
                                    return;
                                } else {
                                    qualificationsMap.put(qualificationName, qualificationPlace);
                                }
                            }

                            if (skills.size() >= 1 && skills.size() <= 10) {
                                for (int i = 0; i < skills.size(); i++) {
                                    String skillName = skills.get(i).getAsString();

                                    if (skillName.isEmpty()) {
                                        responseObject.addProperty("message", "Invalid Skill Names!");
                                        sendJsonResponse(response, responseObject);
                                        return;
                                    } else if (skillName.length() >= 45) {
                                        responseObject.addProperty("message", "Invalid Skill Names!");
                                        sendJsonResponse(response, responseObject);
                                        return;
                                    } else if (!skillName.matches("^[A-Za-z\\s]+$")) {
                                        responseObject.addProperty("message", "Invalid Skill Names!");
                                        sendJsonResponse(response, responseObject);
                                        return;
                                    } else {
                                        skillsList.add(skillName);
                                    }
                                }

                                if (qualificationsMap.isEmpty()) {
                                    responseObject.addProperty("message", "Please enter the Qualification Names!");
                                    sendJsonResponse(response, responseObject);
                                    return;
                                } else if (qualificationsMap.size() < 1 || qualificationsMap.size() > 2) {
                                    responseObject.addProperty("message", "Invalid Qualification Names Count!");
                                    sendJsonResponse(response, responseObject);
                                    return;
                                } else if (skillsList.isEmpty()) {
                                    responseObject.addProperty("message", "Please enter the Skill Names!");
                                    sendJsonResponse(response, responseObject);
                                    return;
                                } else if (skillsList.size() < 1 || skillsList.size() > 10) {
                                    responseObject.addProperty("message", "Invalid Skill Names Count!");
                                    sendJsonResponse(response, responseObject);
                                    return;
                                } else {
                                    user_As_Seller.setAbout(about);
                                    session.merge(user_As_Seller);

                                    Criteria criteria = session.createCriteria(User_As_Seller_Has_Seller_Education.class);
                                    criteria.add(Restrictions.eq("user_As_Seller", user_As_Seller));

                                    if (!criteria.list().isEmpty()) {
                                        List<User_As_Seller_Has_Seller_Education> exsisting_User_As_Seller_Has_Seller_Educations = criteria.list();
                                        for (User_As_Seller_Has_Seller_Education exsisting_User_As_Seller_Has_Seller_Education : exsisting_User_As_Seller_Has_Seller_Educations) {
                                            session.delete(exsisting_User_As_Seller_Has_Seller_Education);
                                        }
                                    }

                                    for (Map.Entry<String, String> qualification : qualificationsMap.entrySet()) {
                                        Criteria criteria1 = session.createCriteria(Seller_Education.class);
                                        criteria1.add(Restrictions.eq("course_name", qualification.getKey()));
                                        criteria1.add(Restrictions.eq("institute_name", qualification.getValue()));

                                        Seller_Education seller_Education;
                                        if (!criteria1.list().isEmpty()) {
                                            seller_Education = (Seller_Education) criteria1.list().get(0);
                                        } else {
                                            seller_Education = new Seller_Education();
                                            seller_Education.setCourse_name(qualification.getKey());
                                            seller_Education.setInstitute_name(qualification.getValue());
                                            session.save(seller_Education);
                                        }

                                        User_As_Seller_Has_Seller_Education user_As_Seller_Has_Seller_Education = new User_As_Seller_Has_Seller_Education();
                                        user_As_Seller_Has_Seller_Education.setUser_As_Seller(user_As_Seller);
                                        user_As_Seller_Has_Seller_Education.setSeller_Education(seller_Education);
                                        session.save(user_As_Seller_Has_Seller_Education);
                                    }

                                    Criteria criteria1 = session.createCriteria(User_As_Seller_Has_Seller_Skills.class);
                                    criteria1.add(Restrictions.eq("user_As_Seller", user_As_Seller));

                                    if (!criteria1.list().isEmpty()) {
                                        List<User_As_Seller_Has_Seller_Skills> exsisting_User_As_Seller_Has_Seller_Skills = criteria1.list();
                                        for (User_As_Seller_Has_Seller_Skills exsisting_User_As_Seller_Has_Seller_Skill : exsisting_User_As_Seller_Has_Seller_Skills) {
                                            session.delete(exsisting_User_As_Seller_Has_Seller_Skill);
                                        }
                                    }

                                    for (String skill : skillsList) {
                                        Criteria criteria2 = session.createCriteria(Seller_Skills.class);
                                        criteria2.add(Restrictions.eq("name", skill));

                                        Seller_Skills seller_Skills;
                                        if (!criteria2.list().isEmpty()) {
                                            seller_Skills = (Seller_Skills) criteria2.list().get(0);
                                        } else {
                                            seller_Skills = new Seller_Skills();
                                            seller_Skills.setName(skill);
                                            session.save(seller_Skills);
                                        }

                                        User_As_Seller_Has_Seller_Skills user_As_Seller_Has_Seller_Skills = new User_As_Seller_Has_Seller_Skills();
                                        user_As_Seller_Has_Seller_Skills.setUser_As_Seller(user_As_Seller);
                                        user_As_Seller_Has_Seller_Skills.setSeller_Skills(seller_Skills);
                                        session.save(user_As_Seller_Has_Seller_Skills);
                                    }

                                    session.beginTransaction().commit();

                                    responseObject.addProperty("status", true);
                                    responseObject.addProperty("message", "Seller Profile Updated Successfully!");

                                    Criteria criteria2 = session.createCriteria(User.class);
                                    criteria2.add(Restrictions.eq("email", user.getEmail()));

                                    if (!criteria2.list().isEmpty()) {
                                        User u = (User) criteria2.list().get(0);
                                        httpSession.setAttribute("user", u);
                                        Cookie cookie = new Cookie("JSESSIONID", httpSession.getId());
                                        cookie.setHttpOnly(true);
                                        cookie.setPath("/");
                                        cookie.setSecure(true);
                                        response.addCookie(cookie);
                                    }
                                }
                            } else {
                                responseObject.addProperty("message", "Invalid Skill Names Count!");
                            }
                        } else {
                            responseObject.addProperty("message", "Invalid Educational Qualifications Count!");
                        }
                    }
                } else {
                    responseObject.addProperty("message", "Unknown Seller, Please try again later!");
                }

                session.close();
            } else {
                responseObject.addProperty("message", "You're Inactive or Unverified User or Your profile is not Updated!");
            }

        } else {
            responseObject.addProperty("message", "You're Session is Timeout.");
        }

        String responseText = gson.toJson(responseObject);
        response.setContentType("application/json");
        response.getWriter().write(responseText);
    }
}
