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
        String userId = request.getParameter("custom_1");

        Properties prop = new Properties();
        try (InputStream input = new FileInputStream("C:/Users/User/Documents/NetBeansProjects/hireup_backend/.env.local")) {
            prop.load(input);
            PAYHERE_MERCHANT_SECRET = prop.getProperty("PAYHERE_MERCHANT_SECRET");

            String localHash = PayHere.generateMD5(merchant_id + order_id + payhere_amount + payhere_currency + status_code + PayHere.generateMD5(PAYHERE_MERCHANT_SECRET).toUpperCase()).toUpperCase();

            if (localHash.equalsIgnoreCase(md5sig) && Integer.parseInt(status_code) == PAYMENT_STATUS_SUCCESS) {
                Criteria criteria = session.createCriteria(User.class);
                criteria.add(Restrictions.eq("id", Integer.valueOf(userId)));
                if (!criteria.list().isEmpty()) {
                    User user = (User) criteria.list().get(0);

                    Criteria criteria1 = session.createCriteria(Cart.class);
                    criteria1.add(Restrictions.eq("user", user));
                    if (!criteria1.list().isEmpty()) {
                        List<Cart> cartList = criteria1.list();

                        Criteria criteria2 = session.createCriteria(Orders.class);
                        criteria2.setProjection(Projections.rowCount());
                        Long count = (Long) criteria2.uniqueResult();
                        String orderId = "#ORDER-" + (count + 1);

                        Criteria criteria3 = session.createCriteria(Order_Status.class);
                        criteria3.add(Restrictions.eq("value", "Pending"));
                        if (!criteria3.list().isEmpty()) {
                            Order_Status order_Status = (Order_Status) criteria3.list().get(0);

                            Criteria criteria4 = session.createCriteria(Payment_Status.class);
                            criteria4.add(Restrictions.eq("value", "Success"));
                            if (!criteria4.list().isEmpty()) {
                                Payment_Status payment_Status = (Payment_Status) criteria4.list().get(0);

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

                session.close();
                System.out.println("Payment Confirmed Successfully!");
            }
        } catch (IOException e) {
            transaction.rollback();
        } catch (Exception e) {
            transaction.rollback();
        }
    }
}
