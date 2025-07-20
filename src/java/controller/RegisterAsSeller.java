/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import entity.Seller_Status;
import entity.User;
import entity.User_As_Seller;
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
import org.mindrot.jbcrypt.BCrypt;

/**
 *
 * @author User
 */
@WebServlet(name = "RegisterAsSeller", urlPatterns = {"/RegisterAsSeller"})
public class RegisterAsSeller extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(request.getReader(), JsonObject.class);

        String email = jsonObject.get("email").getAsString();
        String password = jsonObject.get("password").getAsString();

        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", false);

        if (email.isEmpty()) {
            responseObject.addProperty("message", "Please enter your Email Address!");
        } else if (password.isEmpty()) {
            responseObject.addProperty("message", "Please enter your Password!");
        } else {
            HttpSession httpSession = request.getSession();
            User user = (User) httpSession.getAttribute("user");

            if (email.equals(user.getEmail())) {
                Session session = HibernateUtil.getSessionFactory().openSession();

                Criteria criteria = session.createCriteria(User.class);
                criteria.add(Restrictions.eq("email", email));

                if (criteria.list().isEmpty()) {
                    responseObject.addProperty("message", "Invalid Credentials!");
                } else {
                    User u = (User) criteria.list().get(0);

                    if (BCrypt.checkpw(password, u.getPassword())) {
                        Criteria criteria1 = session.createCriteria(User_As_Seller.class);
                        criteria1.add(Restrictions.eq("user", u));

                        if (criteria1.list().isEmpty()) {
                            Criteria criteria2 = session.createCriteria(Seller_Status.class);
                            criteria2.add(Restrictions.eq("value", "Pending"));

                            if (!criteria2.list().isEmpty()) {
                                Seller_Status seller_Status = (Seller_Status) criteria2.list().get(0);

                                User_As_Seller user_As_Seller = new User_As_Seller();
                                user_As_Seller.setUser(u);
                                user_As_Seller.setSeller_Status(seller_Status);

                                session.save(user_As_Seller);
                                session.beginTransaction().commit();

                                responseObject.addProperty("status", true);
                                responseObject.addProperty("message", "Seller registration Successfull! Still registration is in Pending Status. Comes a Email, after approve it");
                            }
                        } else {
                            responseObject.addProperty("message", "You're already registered as Seller!");
                        }
                    } else {
                        responseObject.addProperty("message", "Invalid Credentials!");
                    }
                }

                session.close();
            } else {
                responseObject.addProperty("message", "Invalid Credentials!");
            }
        }

        String responseText = gson.toJson(responseObject);
        response.setContentType("application/json");
        response.getWriter().write(responseText);
    }
}
