/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import entity.Category;
import entity.Sub_Category;
import hibernate.HibernateUtil;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.hibernate.Criteria;
import org.hibernate.Session;

/**
 *
 * @author User
 */
@WebServlet(name = "LoadGigDropdowns", urlPatterns = {"/LoadGigDropdowns"})
public class LoadGigDropdowns extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Session session = HibernateUtil.getSessionFactory().openSession();

        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", false);

        Criteria criteria = session.createCriteria(Category.class);
        List<Category> categorys = criteria.list();

        Criteria criteria1 = session.createCriteria(Sub_Category.class);
        List<Sub_Category> sub_Categorys = criteria1.list();

        session.close();

        Gson gson = new Gson();

        responseObject.add("categoryList", gson.toJsonTree(categorys));
        responseObject.add("subCategoryList", gson.toJsonTree(sub_Categorys));

        responseObject.addProperty("status", true);

        String responseText = gson.toJson(responseObject);
        response.setContentType("application/json");
        response.getWriter().write(responseText);
    }
}
