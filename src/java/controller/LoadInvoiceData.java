/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import entity.Order_Gig;
import entity.Orders;
import entity.User;
import hibernate.HibernateUtil;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
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
@WebServlet(name = "LoadInvoiceData", urlPatterns = {"/LoadInvoiceData"})
public class LoadInvoiceData extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Gson gson = new Gson();

        String orderId = request.getParameter("orderId");

        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", false);

        if (!orderId.isEmpty()) {
            HttpSession httpSession = request.getSession(false);

            if (httpSession != null && httpSession.getAttribute("user") != null) {
                User user = (User) httpSession.getAttribute("user");

                Session session = HibernateUtil.getSessionFactory().openSession();

                Criteria criteria = session.createCriteria(Orders.class);
                criteria.add(Restrictions.eq("order_id", orderId));
                criteria.add(Restrictions.eq("user", user));
                if (!criteria.list().isEmpty()) {
                    Orders orders = (Orders) criteria.list().get(0);

                    Criteria criteria1 = session.createCriteria(Order_Gig.class);
                    criteria1.add(Restrictions.eq("orders", orders));
                    if (!criteria1.list().isEmpty()) {
                        List<Order_Gig> order_Gigs = criteria1.list();
                        double amount = 0.00;

                        for (Order_Gig order_Gig : order_Gigs) {
                            amount += order_Gig.getGig_Has_Package().getPrice();
                        }

                        try {
                            HashMap<String, Object> params = new HashMap<>();
                            params.put("Parameter1", orderId);
                            params.put("Parameter2", amount);

                            InputStream path = this.getClass().getResourceAsStream("/reports/hireup_invoice.jasper");

                            Connection con = session.doReturningWork(connection -> connection);

                            JasperPrint report = JasperFillManager.fillReport(path, params, con);

                            String appPath = getServletContext().getRealPath("");
                            String newPath = appPath.replace("build" + File.separator + "web", "web" + File.separator + "invoices");
                            String fileName = newPath + "_" + orderId + ".pdf";

                            JasperExportManager.exportReportToPdfFile(report, fileName);

                            String BaseURL = "http://localhost:8080/hireup_backend/invoices/";
                            String invoiceURL = BaseURL + orderId + ".pdf";

                            responseObject.addProperty("status", true);
                            responseObject.addProperty("invoiceURL", invoiceURL);
                        } catch (JRException | HibernateException e) {
                            responseObject.addProperty("message", "INVALID!");
                            System.out.println(e);
                        }
                    } else {
                        responseObject.addProperty("message", "INVALID!");
                        System.out.println("4");
                    }
                } else {
                    responseObject.addProperty("message", "INVALID!");
                    System.out.println("3");
                }

                session.close();
            } else {
                responseObject.addProperty("message", "INVALID!");
                System.out.println("2");
            }
        } else {
            responseObject.addProperty("message", "INVALID!");
            System.out.println("1");
        }

        String responseText = gson.toJson(responseObject);
        response.setContentType("application/json");
        response.getWriter().write(responseText);
    }
}
