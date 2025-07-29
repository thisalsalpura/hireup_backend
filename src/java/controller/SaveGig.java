/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import entity.FAQ;
import entity.Gig;
import entity.Gig_Has_Package;
import entity.Gig_Package_Type;
import entity.Gig_Search_Tag_Has_Gig;
import entity.Gig_Search_Tag;
import entity.Gig_Status;
import entity.Gig_Visible_Status;
import entity.Sub_Category;
import entity.User;
import entity.User_As_Seller;
import hibernate.HibernateUtil;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import model.Util;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

/**
 *
 * @author User
 */
@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 2, maxFileSize = 1024 * 1024 * 5, maxRequestSize = 1024 * 1024 * 20)
@WebServlet(name = "SaveGig", urlPatterns = {"/SaveGig"})
public class SaveGig extends HttpServlet {

    private static Gson gson = new Gson();

    private void sendJsonResponse(HttpServletResponse response, JsonObject obj) throws IOException {
        String responseText = gson.toJson(obj);
        response.setContentType("application/json");
        response.getWriter().write(responseText);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String contentType = request.getContentType();
        JsonObject jsonObject = null;
        int activeStep = 0;

        if (contentType != null && contentType.contains("application/json")) {
            jsonObject = gson.fromJson(request.getReader(), JsonObject.class);
            activeStep = jsonObject.get("activeStep").getAsInt();
        } else if (contentType != null && contentType.contains("multipart/form-data")) {
            activeStep = Integer.parseInt(request.getParameter("activeStep"));
        }

        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", false);

        HttpSession httpSession = request.getSession(false);

        if (activeStep == 1) {
            responseObject.addProperty("status", false);

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

                    if (user.getUser_Status().getValue().equals("Active") && user.getVerification().equals("VERIFIED!") && user.getUser_Type().getValue().equals("Seller") && user.getDob() != null && user.getUser_Has_Address() != null && user.getLocale() != null) {
                        Session session = HibernateUtil.getSessionFactory().openSession();

                        Criteria c = session.createCriteria(User_As_Seller.class);
                        c.add(Restrictions.eq("user", user));

                        if (!c.list().isEmpty()) {
                            User_As_Seller user_As_Seller = (User_As_Seller) c.list().get(0);
                            if (user_As_Seller.getAbout() != null) {
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
                            } else {
                                responseObject.addProperty("sp", "EMPTY");
                                responseObject.addProperty("message", "Please update your Seller Profile!");
                            }
                        }

                        session.close();
                    } else {
                        responseObject.addProperty("message", "You're Inactive or Unverified User or Your profile is not Updated!");
                    }
                } else {
                    responseObject.addProperty("message", "You're Session is Timeout.");
                }
            }
        }

        if (activeStep == 2) {
            responseObject.addProperty("status", false);

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
            } else if (Double.parseDouble(bronzePrice) <= 0) {
                responseObject.addProperty("message", "Invalid Bronze Gig Price!");
            } else if (bronzeDTime.isEmpty()) {
                responseObject.addProperty("message", "Please enter your Bronze Gig Delivery Time!");
            } else if (!Util.isInteger(bronzeDTime)) {
                responseObject.addProperty("message", "Invalid Bronze Gig Delivery Time!");
            } else if (Integer.parseInt(bronzeDTime) <= 0) {
                responseObject.addProperty("message", "Invalid Bronze Gig Delivery Time!");
            } else if (bronzeNote.isEmpty()) {
                responseObject.addProperty("message", "Please enter your Bronze Gig Special Note!");
            } else if (silverPrice.isEmpty()) {
                responseObject.addProperty("message", "Please enter your Silver Gig Price!");
            } else if (!Util.isDouble(silverPrice)) {
                responseObject.addProperty("message", "Invalid Silver Gig Price!");
            } else if (Double.parseDouble(silverPrice) <= 0) {
                responseObject.addProperty("message", "Invalid Silver Gig Price!");
            } else if (silverDTime.isEmpty()) {
                responseObject.addProperty("message", "Please enter your Silver Gig Delivery Time!");
            } else if (!Util.isInteger(silverDTime)) {
                responseObject.addProperty("message", "Invalid Silver Gig Delivery Time!");
            } else if (Integer.parseInt(silverDTime) <= 0) {
                responseObject.addProperty("message", "Invalid Silver Gig Delivery Time!");
            } else if (silverNote.isEmpty()) {
                responseObject.addProperty("message", "Please enter your Silver Gig Special Note!");
            } else if (goldPrice.isEmpty()) {
                responseObject.addProperty("message", "Please enter your Gold Gig Price!");
            } else if (!Util.isDouble(goldPrice)) {
                responseObject.addProperty("message", "Invalid Gold Gig Price!");
            } else if (Double.parseDouble(goldPrice) <= 0) {
                responseObject.addProperty("message", "Invalid Gold Gig Price!");
            } else if (goldDTime.isEmpty()) {
                responseObject.addProperty("message", "Please enter your Gold Gig Delivery Time!");
            } else if (!Util.isInteger(goldDTime)) {
                responseObject.addProperty("message", "Invalid Gold Gig Delivery Time!");
            } else if (Integer.parseInt(goldDTime) <= 0) {
                responseObject.addProperty("message", "Invalid Gold Gig Delivery Time!");
            } else if (goldNote.isEmpty()) {
                responseObject.addProperty("message", "Please enter your Gold Gig Special Note!");
            } else {
                if (httpSession != null && httpSession.getAttribute("user") != null && httpSession.getAttribute("gig") != null) {
                    User user = (User) httpSession.getAttribute("user");

                    if (user.getUser_Status().getValue().equals("Active") && user.getVerification().equals("VERIFIED!") && user.getUser_Type().getValue().equals("Seller") && user.getDob() != null && user.getUser_Has_Address() != null && user.getLocale() != null) {
                        Session session = HibernateUtil.getSessionFactory().openSession();

                        Criteria c = session.createCriteria(User_As_Seller.class);
                        c.add(Restrictions.eq("user", user));

                        if (!c.list().isEmpty()) {
                            User_As_Seller user_As_Seller = (User_As_Seller) c.list().get(0);
                            if (user_As_Seller.getAbout() != null) {
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
                                responseObject.addProperty("sp", "EMPTY");
                                responseObject.addProperty("message", "Please update your Seller Profile!");
                            }
                        }

                        session.close();
                    } else {
                        responseObject.addProperty("message", "You're Inactive or Unverified User or Your profile is not Updated!");
                    }
                } else {
                    responseObject.addProperty("message", "You're Session is Timeout.");
                }
            }
        }

        if (activeStep == 3) {
            responseObject.addProperty("status", false);

            JsonArray searchNames = jsonObject.get("searchNamesList").getAsJsonArray();
            JsonArray faqs = jsonObject.get("faqsList").getAsJsonArray();

            if (httpSession != null && httpSession.getAttribute("user") != null && httpSession.getAttribute("gig") != null && httpSession.getAttribute("bronzePackage") != null && httpSession.getAttribute("silverPackage") != null && httpSession.getAttribute("goldPackage") != null) {
                User user = (User) httpSession.getAttribute("user");

                if (user.getUser_Status().getValue().equals("Active") && user.getVerification().equals("VERIFIED!") && user.getUser_Type().getValue().equals("Seller") && user.getDob() != null && user.getUser_Has_Address() != null && user.getLocale() != null) {
                    Session session = HibernateUtil.getSessionFactory().openSession();

                    Criteria c = session.createCriteria(User_As_Seller.class);
                    c.add(Restrictions.eq("user", user));

                    if (!c.list().isEmpty()) {
                        User_As_Seller user_As_Seller = (User_As_Seller) c.list().get(0);
                        if (user_As_Seller.getAbout() != null) {
                            if (searchNames.size() == 10) {
                                List<String> searchNamesList = new ArrayList<>();
                                for (int i = 0; i < searchNames.size(); i++) {
                                    String searchName = searchNames.get(i).getAsString();

                                    if (searchName.isEmpty()) {
                                        responseObject.addProperty("message", "Invalid Search Name Tags!");
                                        sendJsonResponse(response, responseObject);
                                        return;
                                    } else if (searchName.length() >= 20) {
                                        responseObject.addProperty("message", "Invalid Search Name Tags!");
                                        sendJsonResponse(response, responseObject);
                                        return;
                                    } else if (!searchName.matches("^[A-Za-z]+$")) {
                                        responseObject.addProperty("message", "Invalid Search Name Tags!");
                                        sendJsonResponse(response, responseObject);
                                        return;
                                    } else {
                                        searchNamesList.add(searchName);
                                    }
                                }

                                if (faqs.size() == 3) {
                                    Map<String, String> faqsMap = new HashMap<>();
                                    for (int i = 0; i < faqs.size(); i++) {
                                        JsonObject faqObj = faqs.get(i).getAsJsonObject();
                                        String question = faqObj.get("question").getAsString();
                                        String answer = faqObj.get("answer").getAsString();

                                        if (question.isEmpty() && answer.isEmpty()) {
                                            responseObject.addProperty("message", "Invalid FAQs!");
                                            sendJsonResponse(response, responseObject);
                                            return;
                                        } else {
                                            faqsMap.put(question, answer);
                                        }
                                    }

                                    if (!searchNamesList.isEmpty()) {
                                        if (searchNamesList.size() == 10) {
                                            if (!faqsMap.isEmpty()) {
                                                if (faqsMap.size() == 3) {
                                                    httpSession.setAttribute("searchNames", searchNamesList);
                                                    httpSession.setAttribute("faqs", faqsMap);

                                                    responseObject.addProperty("status", true);
                                                    responseObject.addProperty("setStep", "4");
                                                } else {
                                                    responseObject.addProperty("message", "Invalid FAQs!");
                                                }
                                            } else {
                                                responseObject.addProperty("message", "Invalid FAQs!");
                                            }
                                        } else {
                                            responseObject.addProperty("message", "Invalid Search Name Tags!");
                                        }
                                    } else {
                                        responseObject.addProperty("message", "Invalid Search Name Tags!");
                                    }
                                } else {
                                    responseObject.addProperty("message", "Invalid FAQs!");
                                }
                            } else {
                                responseObject.addProperty("message", "Invalid Search Name Tags!");
                            }
                        } else {
                            responseObject.addProperty("sp", "EMPTY");
                            responseObject.addProperty("message", "Please update your Seller Profile!");
                        }
                    }

                    session.close();
                } else {
                    responseObject.addProperty("message", "You're Inactive or Unverified User or Your profile is not Updated!");
                }

            } else {
                responseObject.addProperty("message", "You're Session is Timeout.");
            }
        }

        if (activeStep == 4) {
            responseObject.addProperty("status", false);

            Part part1 = request.getPart("image1");
            Part part2 = request.getPart("image2");
            Part part3 = request.getPart("image3");
            Part part4 = request.getPart("doc");

            if (part1.getSubmittedFileName() == null) {
                responseObject.addProperty("message", "Gig Image One is Required!");
            } else if (part2.getSubmittedFileName() == null) {
                responseObject.addProperty("message", "Gig Image Two is Required!");
            } else if (part3.getSubmittedFileName() == null) {
                responseObject.addProperty("message", "Gig Image Three is Required!");
            } else if (part4.getSubmittedFileName() == null) {
                responseObject.addProperty("message", "Gig Documentation is Required!");
            } else {

                if (part1.getContentType().equals("image/png") || part1.getContentType().equals("image/jpeg") || part1.getContentType().equals("image/jpg")) {
                    if (part2.getContentType().equals("image/png") || part2.getContentType().equals("image/jpeg") || part2.getContentType().equals("image/jpg")) {
                        if (part3.getContentType().equals("image/png") || part3.getContentType().equals("image/jpeg") || part3.getContentType().equals("image/jpg")) {
                            if (part4.getContentType().equals("application/pdf")) {

                                if (httpSession != null && httpSession.getAttribute("user") != null && httpSession.getAttribute("gig") != null && httpSession.getAttribute("bronzePackage") != null && httpSession.getAttribute("silverPackage") != null && httpSession.getAttribute("goldPackage") != null && httpSession.getAttribute("searchNames") != null && httpSession.getAttribute("faqs") != null) {
                                    User user = (User) httpSession.getAttribute("user");

                                    if (user.getUser_Status().getValue().equals("Active") && user.getVerification().equals("VERIFIED!") && user.getUser_Type().getValue().equals("Seller") && user.getDob() != null && user.getUser_Has_Address() != null && user.getLocale() != null) {
                                        Session session = HibernateUtil.getSessionFactory().openSession();

                                        Criteria c = session.createCriteria(User_As_Seller.class);
                                        c.add(Restrictions.eq("user", user));

                                        if (!c.list().isEmpty()) {
                                            User_As_Seller user_As_Seller = (User_As_Seller) c.list().get(0);
                                            if (user_As_Seller.getAbout() != null) {
                                                Criteria criteria = session.createCriteria(Gig_Status.class);
                                                criteria.add(Restrictions.eq("value", "Pending"));

                                                if (!criteria.list().isEmpty()) {
                                                    List<File> savedFiles = new ArrayList<>();
                                                    File gigFolder = null;

                                                    try {
                                                        BufferedImage bufferedImage1, bufferedImage2, bufferedImage3;
                                                        try (InputStream imgInputStream1 = part1.getInputStream(); ImageInputStream imageInputStream1 = ImageIO.createImageInputStream(imgInputStream1)) {
                                                            if (!ImageIO.getImageReaders(imageInputStream1).hasNext()) {
                                                                responseObject.addProperty("message", "Image 1 is not a supported image format!");
                                                                sendJsonResponse(response, responseObject);
                                                                return;
                                                            }
                                                            ImageReader reader1 = ImageIO.getImageReaders(imageInputStream1).next();
                                                            reader1.setInput(imageInputStream1);
                                                            int width1 = reader1.getWidth(0);
                                                            int height1 = reader1.getHeight(0);
                                                            reader1.dispose();
                                                            if (width1 * height1 > 5000 * 5000) {
                                                                responseObject.addProperty("message", "Image 1 is too large!");
                                                                sendJsonResponse(response, responseObject);
                                                                return;
                                                            }
                                                            try (InputStream imgInputStream1Reload = part1.getInputStream()) {
                                                                bufferedImage1 = ImageIO.read(imgInputStream1Reload);
                                                            }
                                                            if (bufferedImage1 == null) {
                                                                responseObject.addProperty("message", "Image 1 is not a valid image format or size!");
                                                                sendJsonResponse(response, responseObject);
                                                                return;
                                                            }
                                                        } catch (IOException e) {
                                                            responseObject.addProperty("message", "Error processing Image 1!");
                                                            sendJsonResponse(response, responseObject);
                                                            return;
                                                        }

                                                        try (InputStream imgInputStream2 = part2.getInputStream(); ImageInputStream imageInputStream2 = ImageIO.createImageInputStream(imgInputStream2)) {
                                                            if (!ImageIO.getImageReaders(imageInputStream2).hasNext()) {
                                                                responseObject.addProperty("message", "Image 2 is not a supported image format!");
                                                                sendJsonResponse(response, responseObject);
                                                                return;
                                                            }
                                                            ImageReader reader2 = ImageIO.getImageReaders(imageInputStream2).next();
                                                            reader2.setInput(imageInputStream2);
                                                            int width2 = reader2.getWidth(0);
                                                            int height2 = reader2.getHeight(0);
                                                            reader2.dispose();
                                                            if (width2 * height2 > 5000 * 5000) {
                                                                responseObject.addProperty("message", "Image 2 is too large!");
                                                                sendJsonResponse(response, responseObject);
                                                                return;
                                                            }
                                                            try (InputStream imgInputStream2Reload = part2.getInputStream()) {
                                                                bufferedImage2 = ImageIO.read(imgInputStream2Reload);
                                                            }
                                                            if (bufferedImage2 == null) {
                                                                responseObject.addProperty("message", "Image 2 is not a valid image format or size!");
                                                                sendJsonResponse(response, responseObject);
                                                                return;
                                                            }
                                                        } catch (IOException e) {
                                                            responseObject.addProperty("message", "Error processing Image 2!");
                                                            sendJsonResponse(response, responseObject);
                                                            return;
                                                        }

                                                        try (InputStream imgInputStream3 = part3.getInputStream(); ImageInputStream imageInputStream3 = ImageIO.createImageInputStream(imgInputStream3)) {
                                                            if (!ImageIO.getImageReaders(imageInputStream3).hasNext()) {
                                                                responseObject.addProperty("message", "Image 3 is not a supported image format!");
                                                                sendJsonResponse(response, responseObject);
                                                                return;
                                                            }
                                                            ImageReader reader3 = ImageIO.getImageReaders(imageInputStream3).next();
                                                            reader3.setInput(imageInputStream3);
                                                            int width3 = reader3.getWidth(0);
                                                            int height3 = reader3.getHeight(0);
                                                            reader3.dispose();
                                                            if (width3 * height3 > 5000 * 5000) {
                                                                responseObject.addProperty("message", "Image 3 is too large!");
                                                                sendJsonResponse(response, responseObject);
                                                                return;
                                                            }
                                                            try (InputStream imgInputStream3Reload = part3.getInputStream()) {
                                                                bufferedImage3 = ImageIO.read(imgInputStream3Reload);
                                                            }
                                                            if (bufferedImage3 == null) {
                                                                responseObject.addProperty("message", "Image 3 is not a valid image format or size!");
                                                                sendJsonResponse(response, responseObject);
                                                                return;
                                                            }
                                                        } catch (IOException e) {
                                                            responseObject.addProperty("message", "Error processing Image 3!");
                                                            sendJsonResponse(response, responseObject);
                                                            return;
                                                        }

                                                        Gig gig = (Gig) httpSession.getAttribute("gig");
                                                        Gig_Status gig_Status = (Gig_Status) criteria.list().get(0);
                                                        
                                                        Criteria criteria1 = session.createCriteria(Gig_Visible_Status.class);
                                                        criteria1.add(Restrictions.eq("name", "Active"));
                                                        
                                                        Gig_Visible_Status visible_Status = null;
                                                        if (!criteria1.list().isEmpty()) {
                                                            visible_Status = (Gig_Visible_Status) criteria1.list().get(0);
                                                        }

                                                        gig.setCreated_at(new Date());
                                                        gig.setGig_Status(gig_Status);
                                                        gig.setUser(user);
                                                        gig.setGig_Visible_Status(visible_Status);

                                                        int gigId = (int) session.save(gig);

                                                        Gig_Has_Package bronzePackage = (Gig_Has_Package) httpSession.getAttribute("bronzePackage");
                                                        Gig_Has_Package silverPackage = (Gig_Has_Package) httpSession.getAttribute("silverPackage");
                                                        Gig_Has_Package goldPackage = (Gig_Has_Package) httpSession.getAttribute("goldPackage");

                                                        session.save(bronzePackage);
                                                        session.save(silverPackage);
                                                        session.save(goldPackage);

                                                        List<String> searchNames = (List<String>) httpSession.getAttribute("searchNames");

                                                        for (String searchName : searchNames) {
                                                            Criteria criteria2 = session.createCriteria(Gig_Search_Tag.class);
                                                            criteria2.add(Restrictions.eq("name", searchName));
                                                            Gig_Search_Tag search_Tag;
                                                            if (criteria2.list().isEmpty()) {
                                                                search_Tag = new Gig_Search_Tag();
                                                                search_Tag.setName(searchName);
                                                                session.save(search_Tag);
                                                            } else {
                                                                search_Tag = (Gig_Search_Tag) criteria2.list().get(0);
                                                            }
                                                            Gig_Search_Tag_Has_Gig gig_Search_Tag_Has_Gig = new Gig_Search_Tag_Has_Gig();
                                                            gig_Search_Tag_Has_Gig.setSearch_Tag(search_Tag);
                                                            gig_Search_Tag_Has_Gig.setGig(gig);
                                                            session.save(gig_Search_Tag_Has_Gig);
                                                        }

                                                        Map<String, String> faqs = (Map<String, String>) httpSession.getAttribute("faqs");

                                                        for (Map.Entry<String, String> faq : faqs.entrySet()) {
                                                            FAQ faqEntity = new FAQ();
                                                            faqEntity.setQuestion(faq.getKey());
                                                            faqEntity.setAnswer(faq.getValue());
                                                            faqEntity.setGig(gig);
                                                            session.save(faqEntity);
                                                        }

                                                        String appPath = getServletContext().getRealPath("");
                                                        String newPath = appPath.replace("build" + File.separator + "web", "web" + File.separator + "gig-images");

                                                        gigFolder = new File(newPath, String.valueOf(gigId));
                                                        gigFolder.mkdir();

                                                        File file1 = new File(gigFolder, "image1.png");
                                                        ImageIO.write(bufferedImage1, "png", file1);
                                                        savedFiles.add(file1);

                                                        File file2 = new File(gigFolder, "image2.png");
                                                        ImageIO.write(bufferedImage2, "png", file2);
                                                        savedFiles.add(file2);

                                                        File file3 = new File(gigFolder, "image3.png");
                                                        ImageIO.write(bufferedImage3, "png", file3);
                                                        savedFiles.add(file3);

                                                        try (InputStream docInputStream = part4.getInputStream()) {
                                                            File file4 = new File(gigFolder, "document.pdf");
                                                            Files.copy(docInputStream, file4.toPath(), StandardCopyOption.REPLACE_EXISTING);
                                                            savedFiles.add(file4);
                                                        } catch (IOException e) {
                                                            responseObject.addProperty("message", "Error processing Document!");
                                                            sendJsonResponse(response, responseObject);
                                                            return;
                                                        }

                                                        session.beginTransaction().commit();

                                                        httpSession.removeAttribute("gig");
                                                        httpSession.removeAttribute("bronzePackage");
                                                        httpSession.removeAttribute("silverPackage");
                                                        httpSession.removeAttribute("goldPackage");
                                                        httpSession.removeAttribute("searchNames");
                                                        httpSession.removeAttribute("faqs");

                                                        responseObject.addProperty("status", true);
                                                        responseObject.addProperty("setStep", "FINISH");
                                                        responseObject.addProperty("message", "Gig registerd successfully, but still it in a Pending Status, after admin Verify it, then it inform to you with a Email!");
                                                        sendJsonResponse(response, responseObject);
                                                        return;
                                                    } catch (Exception e) {
                                                        for (File file : savedFiles) {
                                                            if (file.exists()) {
                                                                file.delete();
                                                            }
                                                        }

                                                        if (gigFolder != null && gigFolder.isDirectory() && gigFolder.list().length == 0) {
                                                            gigFolder.delete();
                                                        }

                                                        responseObject.addProperty("message", "Something went wrong!");
                                                        sendJsonResponse(response, responseObject);
                                                        return;
                                                    }
                                                } else {
                                                    responseObject.addProperty("message", "Something went wrong!");
                                                }
                                            } else {
                                                responseObject.addProperty("sp", "EMPTY");
                                                responseObject.addProperty("message", "Please update your Seller Profile!");
                                            }
                                        }

                                        session.close();
                                    } else {
                                        responseObject.addProperty("message", "You're Inactive or Unverified User or Your profile is not Updated!");
                                    }
                                } else {
                                    responseObject.addProperty("message", "You're Session is Timeout.");
                                }

                            } else {
                                responseObject.addProperty("message", "Invalid Document type!");
                            }
                        } else {
                            responseObject.addProperty("message", "Invalid Image Three type!");
                        }
                    } else {
                        responseObject.addProperty("message", "Invalid Image Two type!");
                    }
                } else {
                    responseObject.addProperty("message", "Invalid Image One type!");
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
