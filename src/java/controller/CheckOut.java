/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import entity.Cart;
import entity.Orders;
import entity.User;
import hibernate.HibernateUtil;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Properties;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import model.PayHere;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

/**
 *
 * @author User
 */
@WebServlet(name = "CheckOut", urlPatterns = {"/CheckOut"})
public class CheckOut extends HttpServlet {

    private static String PAYHERE_MERCHANT_ID;
    private static String PAYHERE_MERCHANT_SECRET;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Gson gson = new Gson();

        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", false);

        Session session = HibernateUtil.getSessionFactory().openSession();

        Properties prop = new Properties();
        try (InputStream input = new FileInputStream("C:/Users/User/Documents/NetBeansProjects/hireup_backend/.env.local")) {
            prop.load(input);
            PAYHERE_MERCHANT_ID = prop.getProperty("PAYHERE_MERCHANT_ID");
            PAYHERE_MERCHANT_SECRET = prop.getProperty("PAYHERE_MERCHANT_SECRET");

            HttpSession httpSession = request.getSession(false);

            if (httpSession != null && httpSession.getAttribute("user") != null) {
                User user = (User) httpSession.getAttribute("user");

                if (user.getUser_Status().getValue().equals("Active") && user.getVerification().equals("VERIFIED!")) {

                    if (user.getDob() != null && user.getUser_Has_Address() != null && user.getLocale() != null) {

                        Criteria criteria = session.createCriteria(Cart.class);
                        criteria.add(Restrictions.eq("user", user));
                        if (!criteria.list().isEmpty()) {
                            List<Cart> cartList = criteria.list();
                            double amount = 0;
                            for (Cart cart : cartList) {
                                amount += cart.getGig_Has_Package().getPrice();
                            }

                            Criteria criteria1 = session.createCriteria(Orders.class);
                            criteria1.setProjection(Projections.rowCount());
                            Long count = (Long) criteria1.uniqueResult();
                            String orderId = "ORDER-" + (count + 1);

                            // PayHere Process
                            String merchantID = PAYHERE_MERCHANT_ID;
                            String merchantSecret = PAYHERE_MERCHANT_SECRET;
                            String currency = "LKR";
                            String formattedAmount = new DecimalFormat("0.00").format(amount * 30);
                            String merchantSecretMD5 = PayHere.generateMD5(merchantSecret);
                            String hash = PayHere.generateMD5(merchantID + orderId + formattedAmount + currency + merchantSecretMD5);
                            
                            JsonObject payHereJson = new JsonObject();
                            payHereJson.addProperty("sandbox", true);
                            payHereJson.addProperty("merchant_id", merchantID);
                            payHereJson.addProperty("return_url", "https://d4e907869e6d.ngrok-free.app/returnPayment?orderId=" + orderId);
                            payHereJson.addProperty("cancel_url", "https://d4e907869e6d.ngrok-free.app/cancelPayment");
                            payHereJson.addProperty("notify_url", "https://a5551a32e8c3.ngrok-free.app/hireup_backend/NotifyPayment");
                            payHereJson.addProperty("order_id", orderId);
                            payHereJson.addProperty("items", "Cart Gigs");
                            payHereJson.addProperty("amount", formattedAmount);
                            payHereJson.addProperty("currency", currency);
                            payHereJson.addProperty("hash", hash);
                            payHereJson.addProperty("first_name", user.getFname());
                            payHereJson.addProperty("last_name", user.getLname());
                            payHereJson.addProperty("email", user.getEmail());
                            payHereJson.addProperty("phone", "Not Provided");
                            payHereJson.addProperty("address", user.getUser_Has_Address().getLine_1() + ", " + user.getUser_Has_Address().getLine_2());
                            payHereJson.addProperty("city", user.getUser_Has_Address().getCity().getName());
                            payHereJson.addProperty("country", user.getUser_Has_Address().getCity().getCountry().getName());
                            payHereJson.addProperty("custom_1", user.getId());

                            responseObject.addProperty("status", true);
                            responseObject.addProperty("message", "Checkout Completed!");
                            responseObject.add("payHereJson", payHereJson);
                        } else {
                            responseObject.addProperty("message", "You're Cart is Empty!");
                            responseObject.addProperty("messageCode", "ECART");
                        }

                    } else {
                        responseObject.addProperty("message", "Please update your Profile Details!");
                        responseObject.addProperty("messageCode", "NPUPDATE");
                    }

                } else {
                    responseObject.addProperty("message", "You're Inactive or Unverified User!");
                    responseObject.addProperty("messageCode", "NVERIFY");
                }

            } else {
                responseObject.addProperty("message", "Please Login First!");
                responseObject.addProperty("messageCode", "NLOGIN");
            }
        } catch (IOException e) {
            responseObject.addProperty("message", "Something went wrong!");
        } catch (Exception e) {
            responseObject.addProperty("message", "Something went wrong!");
        }

        session.close();
        
        String responseText = gson.toJson(responseObject);
        response.setContentType("application/json");
        response.getWriter().write(responseText);
    }
}
