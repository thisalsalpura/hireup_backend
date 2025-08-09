/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import entity.Category;
import entity.Gig;
import entity.Gig_Has_Package;
import entity.Gig_Package_Type;
import entity.Order_Gig;
import entity.Orders;
import entity.Seller_Status;
import entity.Sub_Category;
import entity.User;
import entity.User_As_Seller;
import hibernate.HibernateUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

/**
 *
 * @author User
 */
@WebServlet(name = "LoadHomeData", urlPatterns = {"/LoadHomeData"})
public class LoadHomeData extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", false);

        HttpSession httpSession = request.getSession(false);

        Session session = HibernateUtil.getSessionFactory().openSession();

        if (httpSession != null && httpSession.getAttribute("user") != null) {
            User user = (User) httpSession.getAttribute("user");

            Criteria criteria = session.createCriteria(Orders.class);
            criteria.add(Restrictions.eq("user", user));
            criteria.addOrder(Order.desc("placed_data"));
            if (!criteria.list().isEmpty()) {
                Orders orders = (Orders) criteria.list().get(0);

                Criteria criteria1 = session.createCriteria(Order_Gig.class);
                criteria1.add(Restrictions.eq("orders", orders));
                if (!criteria1.list().isEmpty()) {
                    List<Order_Gig> order_Gigs = criteria1.list();

                    List<Sub_Category> sub_Categorys = new ArrayList<>();
                    for (Order_Gig order_Gig : order_Gigs) {
                        sub_Categorys.add(order_Gig.getGig_Has_Package().getGig().getSub_Category());
                    }

                    Criteria criteria2 = session.createCriteria(Gig.class);
                    criteria2.add(Restrictions.in("sub_Category", sub_Categorys));
                    if (!criteria2.list().isEmpty()) {
                        List<Gig> gigs = criteria2.list();

                        Criteria criteria3 = session.createCriteria(Gig_Package_Type.class);
                        criteria3.add(Restrictions.eq("name", "Bronze"));
                        if (!criteria3.list().isEmpty()) {
                            Gig_Package_Type bronzePackage = (Gig_Package_Type) criteria3.list().get(0);

                            List<Gig> gigList = new ArrayList<>();
                            List<Double> gigPriceList = new ArrayList<>();
                            List<String> gigimageList = new ArrayList<>();

                            for (Gig gig : gigs) {
                                if (gig.getGig_Status().getValue().equals("Verified") && gig.getGig_Visible_Status().getName().equals("Active") && gig.getUser().getVerification().equals("VERIFIED!") && gig.getUser().getUser_Status().getValue().equals("Active")) {
                                    gigList.add(gig);

                                    Criteria criteria4 = session.createCriteria(Gig_Has_Package.class);
                                    criteria4.add(Restrictions.eq("gig", gig));
                                    criteria4.add(Restrictions.eq("package_Type", bronzePackage));

                                    if (!criteria4.list().isEmpty()) {
                                        Gig_Has_Package gigBronzePackage = (Gig_Has_Package) criteria4.list().get(0);
                                        gigPriceList.add(gigBronzePackage.getPrice());
                                    }

                                    String BaseURL = "http://localhost:8080/hireup_backend/gig-images/" + gig.getId() + "/";
                                    String image1URL = BaseURL + "image1.png";
                                    gigimageList.add(image1URL);
                                }
                            }

                            for (Gig gig : gigList) {
                                gig.getUser().setEmail(null);
                                gig.getUser().setPassword(null);
                                gig.getUser().setDob(null);
                                gig.getUser().setJoined_date(null);
                                gig.getUser().setVerification(null);
                                gig.getUser().setLocale(null);
                                gig.getUser().setUser_Has_Address(null);
                                gig.getUser().setUser_Type(null);
                                gig.getUser().setUser_Status(null);
                            }

                            if (!gigList.isEmpty() && !gigPriceList.isEmpty() && !gigimageList.isEmpty()) {
                                responseObject.addProperty("message", "HRECOMMENDED");
                                responseObject.add("recommendedGigList", gson.toJsonTree(gigList));
                                responseObject.add("recommendedGigPriceList", gson.toJsonTree(gigPriceList));
                                responseObject.add("recommendedGigImageList", gson.toJsonTree(gigimageList));
                            } else {
                                responseObject.addProperty("message", "NHRECOMMENDED");
                            }
                        } else {
                            responseObject.addProperty("message", "NHRECOMMENDED");
                        }
                    } else {
                        responseObject.addProperty("message", "NHRECOMMENDED");
                    }
                } else {
                    responseObject.addProperty("message", "NHRECOMMENDED");
                }
            } else {
                responseObject.addProperty("message", "NHRECOMMENDED");
            }
        }

        Criteria criteria = session.createCriteria(Category.class);
        List<Category> categoryList = criteria.list();
        int i = 0;
        for (Category category : categoryList) {
            Criteria criteria1 = session.createCriteria(Sub_Category.class);
            criteria1.add(Restrictions.eq("category", category));
            if (!criteria1.list().isEmpty()) {
                List<Sub_Category> subCategoryList = criteria1.list();
                responseObject.add("subCategoryList" + i, gson.toJsonTree(subCategoryList));
                i++;
            }
        }

        responseObject.add("categoryList", gson.toJsonTree(categoryList));

        Criteria criteria1 = session.createCriteria(Seller_Status.class);
        criteria1.add(Restrictions.eq("value", "Verified"));
        if (!criteria1.list().isEmpty()) {
            Seller_Status seller_Status = (Seller_Status) criteria1.list().get(0);
            Criteria criteria2 = session.createCriteria(User_As_Seller.class);
            criteria2.add(Restrictions.eq("seller_Status", seller_Status));
            if (!criteria2.list().isEmpty()) {
                List<User_As_Seller> user_As_Sellers = criteria2.list();
                List<User_As_Seller> sellerList = new ArrayList<>();
                for (User_As_Seller user_As_Seller : user_As_Sellers) {
                    User user = user_As_Seller.getUser();
                    if (user == null || user.getVerification() == null || user.getUser_Status() == null || user.getUser_Status().getValue() == null || user.getUser_Type() == null || user.getUser_Type().getValue() == null || user_As_Seller.getAbout() == null) {
                        continue;
                    }

                    if (user.getVerification().equals("VERIFIED!") && user.getUser_Status().getValue().equals("Active") && user.getUser_Type().getValue().equals("Seller") && !user_As_Seller.getAbout().isEmpty()) {
                        sellerList.add(user_As_Seller);
                    }
                }
                
                if (!sellerList.isEmpty()) {
                    responseObject.add("sellerList", gson.toJsonTree(sellerList));
                }
            }
        }
        
        

        responseObject.addProperty("status", true);

        String responseText = gson.toJson(responseObject);
        response.setContentType("application/json");
        response.getWriter().write(responseText);
    }
}
