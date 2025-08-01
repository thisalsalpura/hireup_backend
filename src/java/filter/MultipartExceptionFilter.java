/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package filter;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author User
 */
@WebFilter("/*")
public class MultipartExceptionFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            HttpServletRequest servletRequest = (HttpServletRequest) request;
            HttpServletResponse servletResponse = (HttpServletResponse) response;

            servletResponse.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");
            servletResponse.setHeader("Access-Control-Allow-Methods", "POST, GET, PUT, DELETE, OPTIONS");
            servletResponse.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
            servletResponse.setHeader("Access-Control-Allow-Credentials", "true");

            if ("OPTIONS".equalsIgnoreCase(servletRequest.getMethod())) {
                servletResponse.setStatus(HttpServletResponse.SC_OK);
                return;
            }

            servletResponse.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            servletResponse.setHeader("Pragma", "no-cache");
            servletResponse.setHeader("Expires", "0");

            chain.doFilter(request, response);
        } catch (IllegalStateException e) {
            JsonObject responseObject = new JsonObject();
            responseObject.addProperty("status", false);
            responseObject.addProperty("message", "Files are too large!.");

            Gson gson = new Gson();
            String responseText = gson.toJson(responseObject);
            response.setContentType("application/json");
            response.getWriter().write(responseText);
        }
    }

    @Override
    public void destroy() {
    }
}
