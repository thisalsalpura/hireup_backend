/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import entity.Cart;
import entity.Order_Gig;
import entity.Order_Status;
import entity.Orders;
import entity.Payment_Status;
import entity.User;
import hibernate.HibernateUtil;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
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
import org.hibernate.Transaction;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

/**
 *
 * @author User
 */
@WebServlet(name = "NotifyPayment", urlPatterns = {"/NotifyPayment"})
public class NotifyPayment extends HttpServlet {

    private static String PAYHERE_MERCHANT_SECRET;
    private static final int PAYMENT_STATUS_SUCCESS = 2;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();

        String merchant_id = request.getParameter("merchant_id");
        String order_id = request.getParameter("order_id");
        String payhere_amount = request.getParameter("payhere_amount");
        String payhere_currency = request.getParameter("payhere_currency");
        String status_code = request.getParameter("status_code");
        String md5sig = request.getParameter("md5sig");

        Properties prop = new Properties();
        try (InputStream input = new FileInputStream("C:/Users/User/Documents/NetBeansProjects/hireup_backend/config.properties")) {
            prop.load(input);
            PAYHERE_MERCHANT_SECRET = prop.getProperty("PAYHERE_MERCHANT_SECRET");

            String merchantSecret = PAYHERE_MERCHANT_SECRET;
            String merchantSecretMD5 = PayHere.generateMD5(merchantSecret);
            String hash = PayHere.generateMD5(merchant_id + order_id + payhere_amount + payhere_currency + merchantSecretMD5);

            if (md5sig.equals(hash) && Integer.parseInt(status_code) == PAYMENT_STATUS_SUCCESS) {
                HttpSession httpSession = request.getSession(false);
                User user = (User) httpSession.getAttribute("user");

                Criteria criteria = session.createCriteria(Cart.class);
                criteria.add(Restrictions.eq("user", user));
                if (!criteria.list().isEmpty()) {
                    List<Cart> cartList = criteria.list();

                    Criteria criteria1 = session.createCriteria(Orders.class);
                    criteria1.setProjection(Projections.rowCount());
                    Long count = (Long) criteria1.uniqueResult();
                    String orderId = "#ORDER-" + (count + 1);

                    Criteria criteria2 = session.createCriteria(Order_Status.class);
                    criteria2.add(Restrictions.eq("value", "Pending"));
                    if (!criteria2.list().isEmpty()) {
                        Order_Status order_Status = (Order_Status) criteria2.list().get(0);

                        Criteria criteria3 = session.createCriteria(Payment_Status.class);
                        criteria3.add(Restrictions.eq("value", "Success"));
                        if (!criteria3.list().isEmpty()) {
                            Payment_Status payment_Status = (Payment_Status) criteria3.list().get(0);

                            Orders orders = new Orders();
                            orders.setOrder_id(orderId);
                            orders.setUser(user);
                            orders.setPlaced_data(new Date());
                            orders.setPayment_Status(payment_Status);
                            orders.setOrder_Status(order_Status);
                            session.save(orders);

                            for (Cart cart : cartList) {
                                Order_Gig order_Gig = new Order_Gig();
                                order_Gig.setGig_Has_Package(cart.getGig_Has_Package());
                                order_Gig.setOrders(orders);
                                session.save(order_Gig);
                                session.delete(cart);
                            }

                            transaction.commit();
                        }
                    }
                }
            }
        } catch (IOException e) {
            transaction.rollback();
        } catch (Exception e) {
            transaction.rollback();
        }
    }
}
