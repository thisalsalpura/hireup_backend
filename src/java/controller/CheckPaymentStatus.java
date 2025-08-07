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
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
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
@WebServlet(name = "CheckPaymentStatus", urlPatterns = {"/CheckPaymentStatus"})
public class CheckPaymentStatus extends HttpServlet {

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
                        responseObject.addProperty("status", true);
                    }
                }

                session.close();
            }
        }

        String responseText = gson.toJson(responseObject);
        response.setContentType("application/json");
        response.getWriter().write(responseText);
    }
}
