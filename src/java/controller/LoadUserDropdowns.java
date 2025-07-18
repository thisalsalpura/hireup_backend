/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import entity.City;
import entity.Country;
import entity.Locale;
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
@WebServlet(name = "LoadUserDropdowns", urlPatterns = {"/LoadUserDropdowns"})
public class LoadUserDropdowns extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Session session = HibernateUtil.getSessionFactory().openSession();

        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", false);

        Criteria criteria = session.createCriteria(Country.class);
        List<Country> countries = criteria.list();

        Criteria criteria1 = session.createCriteria(City.class);
        List<City> cities = criteria1.list();

        Criteria criteria2 = session.createCriteria(Locale.class);
        List<Locale> locales = criteria2.list();

        session.close();

        Gson gson = new Gson();

        responseObject.add("countryList", gson.toJsonTree(countries));
        responseObject.add("cityList", gson.toJsonTree(cities));
        responseObject.add("localeList", gson.toJsonTree(locales));

        responseObject.addProperty("status", true);

        String responseText = gson.toJson(responseObject);
        response.setContentType("application/json");
        response.getWriter().write(responseText);
    }
}
