/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import entity.Gig;
import entity.Gig_Has_Package;
import entity.Gig_Package_Type;
import entity.Sub_Category;
import entity.User;
import hibernate.HibernateUtil;
import java.io.IOException;
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
@WebServlet(name = "SaveGig", urlPatterns = {"/SaveGig"})
public class SaveGig extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(request.getReader(), JsonObject.class);

        int activStep = jsonObject.get("activStep").getAsInt();

        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", false);

        HttpSession httpSession = request.getSession(false);

        if (activStep == 1) {
            String gigTitle = jsonObject.get("gigTitle").getAsString();
            String gigDesc = jsonObject.get("gigDesc").getAsString();
            int categoryId = jsonObject.get("categoryId").getAsInt();
            int subCategoryId = jsonObject.get("subCategoryId").getAsInt();

            if (gigTitle.isEmpty()) {
                responseObject.addProperty("message", "Please enter your Gig Title!");
            } else if (gigDesc.isEmpty()) {
                responseObject.addProperty("message", "Please enter your Gig Description!");
            } else if (categoryId == 0) {
                responseObject.addProperty("message", "Please select a Category!");
            } else if (subCategoryId == 0) {
                responseObject.addProperty("message", "Please select a Sub Category!");
            } else {
                if (httpSession != null && httpSession.getAttribute("user") != null) {
                    User user = (User) httpSession.getAttribute("user");

                    if (user.getUser_Status().getValue().equals("Active") && user.getVerification().equals("VERIFIED!") && user.getUser_Type().getValue().equals("Seller")) {
                        Session session = HibernateUtil.getSessionFactory().openSession();

                        Criteria criteria = session.createCriteria(Sub_Category.class);
                        criteria.add(Restrictions.eq("id", subCategoryId));

                        if (!criteria.list().isEmpty()) {
                            Sub_Category sub_Category = (Sub_Category) criteria.list().get(0);

                            Gig gig = new Gig();
                            gig.setTitle(gigTitle);
                            gig.setDescription(gigDesc);
                            gig.setSub_Category(sub_Category);

                            httpSession.setAttribute("gig", gig);

                            responseObject.addProperty("status", true);
                            responseObject.addProperty("setStep", "2");
                        } else {
                            responseObject.addProperty("message", "Invalid Sub Category! Please try again later.");
                        }

                        session.close();
                    } else {
                        responseObject.addProperty("message", "You're Inactive or Unverified User!");
                    }
                } else {
                    responseObject.addProperty("message", "You're Session is Timeout.");
                }
            }
        }

        if (activStep == 2) {
            String bronzePrice = jsonObject.get("bronzePrice").getAsString();
            String bronzeDTime = jsonObject.get("bronzeDTime").getAsString();
            String bronzeNote = jsonObject.get("bronzeNote").getAsString();
            String silverPrice = jsonObject.get("silverPrice").getAsString();
            String silverDTime = jsonObject.get("silverDTime").getAsString();
            String silverNote = jsonObject.get("silverNote").getAsString();
            String goldPrice = jsonObject.get("goldPrice").getAsString();
            String goldDTime = jsonObject.get("goldDTime").getAsString();
            String goldNote = jsonObject.get("goldNote").getAsString();

            if (bronzePrice.isEmpty()) {
                responseObject.addProperty("message", "Please enter your Bronze Gig Price!");
            } else if (!Util.isDouble(bronzePrice)) {
                responseObject.addProperty("message", "Invalid Bronze Gig Price!");
            } else if (Double.parseDouble(bronzePrice) < 0) {
                responseObject.addProperty("message", "Invalid Bronze Gig Price!");
            } else if (bronzeDTime.isEmpty()) {
                responseObject.addProperty("message", "Please enter your Bronze Gig Delivery Time!");
            } else if (!Util.isInteger(bronzeDTime)) {
                responseObject.addProperty("message", "Invalid Bronze Gig Delivery Time!");
            } else if (Integer.parseInt(bronzeDTime) < 0) {
                responseObject.addProperty("message", "Invalid Bronze Gig Delivery Time!");
            } else if (bronzeNote.isEmpty()) {
                responseObject.addProperty("message", "Please enter your Bronze Gig Special Note!");
            } else if (silverPrice.isEmpty()) {
                responseObject.addProperty("message", "Please enter your Silver Gig Price!");
            } else if (!Util.isDouble(silverPrice)) {
                responseObject.addProperty("message", "Invalid Silver Gig Price!");
            } else if (Double.parseDouble(silverPrice) < 0) {
                responseObject.addProperty("message", "Invalid Silver Gig Price!");
            } else if (silverDTime.isEmpty()) {
                responseObject.addProperty("message", "Please enter your Silver Gig Delivery Time!");
            } else if (!Util.isInteger(silverDTime)) {
                responseObject.addProperty("message", "Invalid Silver Gig Delivery Time!");
            } else if (Integer.parseInt(silverDTime) < 0) {
                responseObject.addProperty("message", "Invalid Silver Gig Delivery Time!");
            } else if (silverNote.isEmpty()) {
                responseObject.addProperty("message", "Please enter your Silver Gig Special Note!");
            } else if (goldPrice.isEmpty()) {
                responseObject.addProperty("message", "Please enter your Gold Gig Price!");
            } else if (!Util.isDouble(goldPrice)) {
                responseObject.addProperty("message", "Invalid Gold Gig Price!");
            } else if (Double.parseDouble(goldPrice) < 0) {
                responseObject.addProperty("message", "Invalid Gold Gig Price!");
            } else if (goldDTime.isEmpty()) {
                responseObject.addProperty("message", "Please enter your Gold Gig Delivery Time!");
            } else if (!Util.isInteger(goldDTime)) {
                responseObject.addProperty("message", "Invalid Gold Gig Delivery Time!");
            } else if (Integer.parseInt(goldDTime) < 0) {
                responseObject.addProperty("message", "Invalid Gold Gig Delivery Time!");
            } else if (goldNote.isEmpty()) {
                responseObject.addProperty("message", "Please enter your Gold Gig Special Note!");
            } else {
                if (httpSession != null && httpSession.getAttribute("user") != null && httpSession.getAttribute("gig") != null) {
                    User user = (User) httpSession.getAttribute("user");

                    if (user.getUser_Status().getValue().equals("Active") && user.getVerification().equals("VERIFIED!") && user.getUser_Type().getValue().equals("Seller")) {
                        Session session = HibernateUtil.getSessionFactory().openSession();

                        Criteria criteria = session.createCriteria(Gig_Package_Type.class);
                        criteria.add(Restrictions.eq("name", "Bronze"));

                        Gig_Package_Type bronze = null;
                        if (!criteria.list().isEmpty()) {
                            bronze = (Gig_Package_Type) criteria.list().get(0);
                        }

                        Criteria criteria1 = session.createCriteria(Gig_Package_Type.class);
                        criteria1.add(Restrictions.eq("name", "Silver"));

                        Gig_Package_Type silver = null;
                        if (!criteria1.list().isEmpty()) {
                            silver = (Gig_Package_Type) criteria1.list().get(0);
                        }

                        Criteria criteria2 = session.createCriteria(Gig_Package_Type.class);
                        criteria2.add(Restrictions.eq("name", "Gold"));

                        Gig_Package_Type gold = null;
                        if (!criteria2.list().isEmpty()) {
                            gold = (Gig_Package_Type) criteria2.list().get(0);
                        }

                        Gig gig = (Gig) httpSession.getAttribute("gig");

                        Gig_Has_Package bronzePackage = new Gig_Has_Package();
                        bronzePackage.setGig(gig);
                        bronzePackage.setPackage_Type(bronze);
                        bronzePackage.setPrice(Double.parseDouble(bronzePrice));
                        bronzePackage.setDelivery_time(Integer.parseInt(bronzeDTime));
                        bronzePackage.setExtra_note(bronzeNote);

                        Gig_Has_Package silverPackage = new Gig_Has_Package();
                        silverPackage.setGig(gig);
                        silverPackage.setPackage_Type(silver);
                        silverPackage.setPrice(Double.parseDouble(silverPrice));
                        silverPackage.setDelivery_time(Integer.parseInt(silverDTime));
                        silverPackage.setExtra_note(silverNote);

                        Gig_Has_Package goldPackage = new Gig_Has_Package();
                        goldPackage.setGig(gig);
                        goldPackage.setPackage_Type(gold);
                        goldPackage.setPrice(Double.parseDouble(goldPrice));
                        goldPackage.setDelivery_time(Integer.parseInt(goldDTime));
                        goldPackage.setExtra_note(goldNote);

                        httpSession.setAttribute("bronzePackage", bronzePackage);
                        httpSession.setAttribute("silverPackage", silverPackage);
                        httpSession.setAttribute("goldPackage", goldPackage);

                        responseObject.addProperty("status", true);
                        responseObject.addProperty("setStep", "3");
                    } else {
                        responseObject.addProperty("message", "You're Inactive or Unverified User!");
                    }
                } else {
                    responseObject.addProperty("message", "You're Session is Timeout.");
                }
            }
        }

        if (httpSession != null) {
            Cookie cookie = new Cookie("JSESSIONID", httpSession.getId());
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setSecure(true);
            response.addCookie(cookie);
        }

        String responseText = gson.toJson(responseObject);
        response.setContentType("application/json");
        response.getWriter().write(responseText);
    }
}
