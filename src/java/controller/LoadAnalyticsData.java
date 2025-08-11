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
import entity.Order_Gig;
import entity.User;
import entity.User_As_Seller;
import hibernate.HibernateUtil;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

/**
 *
 * @author User
 */
@WebServlet(name = "LoadAnalyticsData", urlPatterns = {"/LoadAnalyticsData"})
public class LoadAnalyticsData extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", false);

        Session session = HibernateUtil.getSessionFactory().openSession();

        Criteria criteria = session.createCriteria(Category.class);
        if (!criteria.list().isEmpty()) {
            List<Category> categories = criteria.list();
            List<Category> categoryList = new ArrayList<>();
            List<Integer> gigCountForCategory = new ArrayList<>();
            for (Category category : categories) {
                categoryList.add(category);
                Criteria criteria1 = session.createCriteria(Gig.class);
                if (!criteria1.list().isEmpty()) {
                    List<Gig> gigs = criteria1.list();
                    int i = 0;
                    for (Gig gig : gigs) {
                        if (gig.getSub_Category().getCategory().getName().equals(category.getName())) {
                            i++;
                        }
                    }
                    gigCountForCategory.add(i);
                }
            }

            if (!categoryList.isEmpty() && !gigCountForCategory.isEmpty()) {
                double totalEarning = 0.00;
                int activeGigsCount = 0;
                int activeSellerCount = 0;
                int activeUserCount = 0;

                Criteria criteria1 = session.createCriteria(Order_Gig.class);
                if (!criteria1.list().isEmpty()) {
                    List<Order_Gig> order_Gigs = criteria1.list();
                    for (Order_Gig order_Gig : order_Gigs) {
                        double orderGigPrice = order_Gig.getGig_Has_Package().getPrice();
                        totalEarning += orderGigPrice;
                    }
                }

                Criteria criteria2 = session.createCriteria(Gig.class);
                if (!criteria2.list().isEmpty()) {
                    List<Gig> gigs = criteria2.list();
                    List<Gig> gigList = new ArrayList<>();
                    for (Gig gig : gigs) {
                        if (gig == null || gig.getGig_Status() == null || gig.getGig_Status().getValue() == null || gig.getGig_Visible_Status() == null || gig.getGig_Visible_Status().getName() == null || gig.getUser() == null || gig.getUser().getVerification() == null || gig.getUser().getUser_Status() == null || gig.getUser().getUser_Status().getValue() == null) {
                            continue;
                        }

                        if (gig.getGig_Status().getValue().equals("Verified") && gig.getGig_Visible_Status().getName().equals("Active") && gig.getUser().getVerification().equals("VERIFIED!") && gig.getUser().getUser_Status().getValue().equals("Active")) {
                            gigList.add(gig);
                        }
                    }

                    if (!gigList.isEmpty()) {
                        activeGigsCount = gigList.size();
                    }
                }

                Criteria criteria3 = session.createCriteria(User.class);
                if (!criteria3.list().isEmpty()) {
                    List<User> users = criteria3.list();
                    List<User> userList = new ArrayList<>();
                    List<User> sellerList = new ArrayList<>();
                    for (User user : users) {
                        if (user == null || user.getVerification() == null || user.getUser_Status() == null || user.getUser_Status().getValue() == null) {
                            continue;
                        }

                        if (user.getVerification().equals("VERIFIED!") && user.getUser_Status().getValue().equals("Active")) {
                            userList.add(user);

                            Criteria criteria4 = session.createCriteria(User_As_Seller.class);
                            criteria4.add(Restrictions.eq("user", user));
                            if (!criteria4.list().isEmpty()) {
                                User_As_Seller user_As_Seller = (User_As_Seller) criteria4.list().get(0);
                                if (user.getUser_Type() == null || user.getUser_Type().getValue() == null || user_As_Seller.getAbout() == null || user_As_Seller.getSeller_Status() == null || user_As_Seller.getSeller_Status().getValue() == null) {
                                    continue;
                                }

                                if (user_As_Seller.getSeller_Status().getValue().equals("Verified") && !user_As_Seller.getAbout().isEmpty() && user.getUser_Type().getValue().equals("Seller")) {
                                    sellerList.add(user);
                                }
                            }
                        }
                    }

                    if (!userList.isEmpty() && !sellerList.isEmpty()) {
                        activeSellerCount = sellerList.size();
                        activeUserCount = userList.size();
                    }
                }

                try {
                    HashMap<String, Object> params = new HashMap<>();
                    params.put("Parameter1", this.getClass().getResource("/reports/hireup-logo.png").toString());
                    params.put("Parameter2", String.valueOf("$ " + totalEarning));
                    params.put("Parameter3", String.valueOf(activeGigsCount));
                    params.put("Parameter4", String.valueOf(activeSellerCount));
                    params.put("Parameter5", String.valueOf(activeUserCount));

                    InputStream path = this.getClass().getResourceAsStream("/reports/hireup_analytics_report.jasper");

                    JREmptyDataSource dataSource = new JREmptyDataSource();

                    JasperPrint report = JasperFillManager.fillReport(path, params, dataSource);

                    String appPath = getServletContext().getRealPath("");
                    String newPath = appPath.replace("build" + File.separator + "web", "web" + File.separator + "analytics-report");
                    File invoicesDir = new File(newPath);
                    if (!invoicesDir.exists()) {
                        invoicesDir.mkdirs();
                    }

                    String fileName = newPath + File.separator + "Analytics_Report.pdf";
                    JasperExportManager.exportReportToPdfFile(report, fileName);

                    String BaseURL = "http://localhost:8080/hireup_backend/analytics-report/";
                    String reportURL = BaseURL + "Analytics_Report.pdf";

                    responseObject.addProperty("status", true);
                    responseObject.add("categoryList", gson.toJsonTree(categoryList));
                    responseObject.add("gigCountForCategory", gson.toJsonTree(gigCountForCategory));
                    responseObject.addProperty("reportURL", reportURL);
                } catch (JRException | HibernateException e) {
                    responseObject.addProperty("message", "INVALID!");
                }
            }
        }

        session.close();

        String responseText = gson.toJson(responseObject);
        response.setContentType("application/json");
        response.getWriter().write(responseText);
    }
}
