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
import entity.Gig_Has_Package;
import entity.Gig_Package_Type;
import entity.Gig_Search_Tag;
import entity.Gig_Search_Tag_Has_Gig;
import entity.Sub_Category;
import hibernate.HibernateUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

/**
 *
 * @author User
 */
@WebServlet(name = "AdvancedSearch", urlPatterns = {"/AdvancedSearch"})
public class AdvancedSearch extends HttpServlet {

    private static final int MAX_RESULT = 6;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        JsonObject jsonObject = gson.fromJson(request.getReader(), JsonObject.class);

        String gigTitle = jsonObject.get("gigTitle").getAsString();
        int categoryId = jsonObject.get("category").getAsInt();
        int subCategoryId = jsonObject.get("subCategory").getAsInt();
        double minPrice = jsonObject.get("minPrice").getAsDouble();
        double maxPrice = jsonObject.get("maxPrice").getAsDouble();
        int sortById = jsonObject.get("sortBy").getAsInt();

        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", false);

        Session session = HibernateUtil.getSessionFactory().openSession();

        Criteria criteria = session.createCriteria(Gig.class);

        if (!gigTitle.isEmpty()) {
            Criteria criteria1 = session.createCriteria(Gig_Search_Tag.class);
            criteria1.add(Restrictions.eq("name", gigTitle));
            if (!criteria1.list().isEmpty()) {
                List<Gig_Search_Tag> having_Search_Tags = criteria1.list();

                Criteria criteria2 = session.createCriteria(Gig_Search_Tag_Has_Gig.class);
                criteria2.add(Restrictions.in("search_Tag", having_Search_Tags));
                if (!criteria2.list().isEmpty()) {
                    List<Gig_Search_Tag_Has_Gig> relatedGigsForSearchTags = criteria2.list();
                    List<Integer> relatedGigIdsForSearchTags = new ArrayList<>();
                    for (Gig_Search_Tag_Has_Gig relatedGigForSearchTags : relatedGigsForSearchTags) {
                        relatedGigIdsForSearchTags.add(relatedGigForSearchTags.getGig().getId());
                    }

                    criteria.add(Restrictions.or(
                            Restrictions.like("title", gigTitle, MatchMode.ANYWHERE),
                            Restrictions.in("id", relatedGigIdsForSearchTags)
                    ));
                } else {
                    criteria.add(Restrictions.like("title", gigTitle, MatchMode.ANYWHERE));
                }
            } else {
                criteria.add(Restrictions.like("title", gigTitle, MatchMode.ANYWHERE));
            }
        }

        if (subCategoryId == 0 && categoryId != 0) {
            Criteria criteria1 = session.createCriteria(Category.class);
            criteria1.add(Restrictions.eq("id", categoryId));
            if (!criteria1.list().isEmpty()) {
                Category category = (Category) criteria1.list().get(0);

                Criteria criteria2 = session.createCriteria(Sub_Category.class);
                criteria2.add(Restrictions.eq("category", category));

                if (!criteria2.list().isEmpty()) {
                    List<Sub_Category> gigsWithCategory = criteria2.list();
                    criteria.add(Restrictions.in("sub_Category", gigsWithCategory));
                }
            }
        }

        if (subCategoryId != 0) {
            Criteria criteria1 = session.createCriteria(Sub_Category.class);
            criteria1.add(Restrictions.eq("id", subCategoryId));
            if (!criteria1.list().isEmpty()) {
                Sub_Category sub_Category = (Sub_Category) criteria1.list().get(0);
                criteria.add(Restrictions.eq("sub_Category", sub_Category));
            }
        }

        Criteria criteria1 = session.createCriteria(Gig_Package_Type.class);
        criteria1.add(Restrictions.eq("name", "Bronze"));
        Gig_Package_Type bronzePackage = null;
        if (!criteria1.list().isEmpty()) {
            bronzePackage = (Gig_Package_Type) criteria1.list().get(0);
        }

        criteria.createAlias("gig_Status", "gs");
        criteria.createAlias("gig_Visible_Status", "gvs");
        criteria.createAlias("user", "u");
        criteria.createAlias("u.user_Status", "us");

        criteria.add(Restrictions.eq("gs.value", "Verified"));
        criteria.add(Restrictions.eq("gvs.name", "Active"));
        criteria.add(Restrictions.eq("u.verification", "VERIFIED!"));
        criteria.add(Restrictions.eq("us.value", "Active"));

        responseObject.addProperty("status", true);

        Criteria criteria4 = session.createCriteria(Gig_Has_Package.class);
        criteria4.add(Restrictions.eq("package_Type", bronzePackage));

        if (!criteria4.list().isEmpty()) {
            List<Gig_Has_Package> filterGigPackagesWithPriceRange = criteria4.list();
            List<Integer> filterGigsWithPriceRange = new ArrayList<>();

            for (Gig_Has_Package filterGigPackageWithPriceRange : filterGigPackagesWithPriceRange) {
                filterGigsWithPriceRange.add(filterGigPackageWithPriceRange.getGig().getId());
            }

            criteria.add(Restrictions.in("id", filterGigsWithPriceRange));

            criteria.createAlias("gigHasPackages", "ghp");
            criteria.add(Restrictions.eq("ghp.package_Type", bronzePackage));
            criteria.add(Restrictions.between("ghp.price", minPrice, maxPrice));

            if (sortById == 0) {
                criteria.addOrder(Order.asc("created_at"));
            } else if (sortById == 1) {
                criteria.addOrder(Order.asc("ghp.price"));
            } else if (sortById == 2) {
                criteria.addOrder(Order.desc("ghp.price"));
            }

            if (!criteria.list().isEmpty()) {

                List<Gig> gigsListCount = criteria.list();
                List<Gig> verifiedGigsListCount = new ArrayList<>();
                for (Gig gig : gigsListCount) {
                    if (gig.getGig_Status().getValue().equals("Verified") && gig.getGig_Visible_Status().getName().equals("Active") && gig.getUser().getVerification().equals("VERIFIED!") && gig.getUser().getUser_Status().getValue().equals("Active")) {
                        verifiedGigsListCount.add(gig);
                    }
                }

                int searchGigsListSize = verifiedGigsListCount.size();

                if (jsonObject.has("firstResult")) {
                    int firstResult = jsonObject.get("firstResult").getAsInt();
                    criteria.setFirstResult(firstResult);
                    criteria.setMaxResults(AdvancedSearch.MAX_RESULT);
                }

                List<Gig> gigsList = criteria.list();
                List<Gig> searchGigsList = new ArrayList<>();
                List<Double> searchGigsPricesList = new ArrayList<>();
                List<String> searchGigsImagesList = new ArrayList<>();
                for (Gig gig : gigsList) {
                    if (gig.getGig_Status().getValue().equals("Verified") && gig.getGig_Visible_Status().getName().equals("Active") && gig.getUser().getVerification().equals("VERIFIED!") && gig.getUser().getUser_Status().getValue().equals("Active")) {
                        searchGigsList.add(gig);

                        Criteria criteria5 = session.createCriteria(Gig_Has_Package.class);
                        criteria5.add(Restrictions.eq("gig", gig));
                        criteria5.add(Restrictions.eq("package_Type", bronzePackage));

                        if (!criteria5.list().isEmpty()) {
                            Gig_Has_Package gigBronzePackage = (Gig_Has_Package) criteria5.list().get(0);
                            searchGigsPricesList.add(gigBronzePackage.getPrice());
                        }

                        String BaseURL = "http://localhost:8080/hireup_backend/gig-images/" + gig.getId() + "/";
                        String image1URL = BaseURL + "image1.png";
                        searchGigsImagesList.add(image1URL);
                    }
                }

                for (Gig gig : searchGigsList) {
                    gig.getUser().setEmail(null);
                    gig.getUser().setPassword(null);
                    gig.getUser().setDob(null);
                    gig.getUser().setJoined_date(null);
                    gig.getUser().setVerification(null);
                    gig.getUser().setLocale(null);
                    gig.getUser().setUser_Has_Address(null);
                    gig.getUser().setUser_Type(null);
                    gig.getUser().setUser_Status(null);
                }

                if (!searchGigsList.isEmpty() && !searchGigsPricesList.isEmpty() && !searchGigsImagesList.isEmpty()) {
                    responseObject.addProperty("searchGigsListSize", searchGigsListSize);
                    responseObject.add("searchGigsList", gson.toJsonTree(searchGigsList));
                    responseObject.add("searchGigsPricesList", gson.toJsonTree(searchGigsPricesList));
                    responseObject.add("searchGigsImagesList", gson.toJsonTree(searchGigsImagesList));
                    responseObject.addProperty("message", "NEMPTY");
                } else {
                    responseObject.addProperty("message", "EMPTY");
                }
            } else {
                responseObject.addProperty("message", "EMPTY");
            }
        } else {
            responseObject.addProperty("message", "EMPTY");
        }

        session.close();

        String responseText = gson.toJson(responseObject);
        response.setContentType("application/json");
        response.getWriter().write(responseText);
    }
}
