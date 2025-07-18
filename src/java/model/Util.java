/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import entity.User;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

/**
 *
 * @author User
 */
public class Util {

    // Validate Email Address
    public static boolean isEmailValid(String email) {
        return email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    }

    // Validate Password
    public static boolean isPasswordValid(String password) {
        return password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!])[A-Za-z\\d@#$%^&+=!]{8,20}$");
    }

    // Generate Verification Code
    public static String generateVerificationCode(Session session) {

        Criteria criteria = session.createCriteria(User.class);
        String code;

        do {
            code = String.format("%06d", (int) (Math.random() * 1000000));
            criteria.add(Restrictions.eq("verification", code));
        } while (!criteria.list().isEmpty());

        return code;
    }

    // Load Email Template
    public static String loadEmailTemplate(String path) {
        try {
            FileInputStream fis = new FileInputStream(path);
            InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
            BufferedReader br = new BufferedReader(isr);

            StringBuilder content = new StringBuilder();
            String line;

            while ((line = br.readLine()) != null) {
                content.append(line).append("\n");
            }

            return content.toString();
        } catch (IOException e) {
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    // Validate Postal Code
    public static boolean isPostalCodeValid(String code) {
        return code.matches("^\\d{5}$");
    }
}
