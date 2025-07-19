/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package entity;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 *
 * @author User
 */
@Entity
@Table(name = "user")
public class User implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "fname", length = 45, nullable = false)
    private String fname;

    @Column(name = "lname", length = 45, nullable = false)
    private String lname;

    @Column(name = "email", length = 50, nullable = false)
    private String email;

    @Column(name = "password", length = 20, nullable = false)
    private String password;

    @Column(name = "dob", nullable = true)
    private Date dob;

    @Column(name = "joined_date", nullable = false)
    private Date joined_date;

    @Column(name = "verification", nullable = false)
    private String verification;

    @ManyToOne
    @JoinColumn(name = "locale_id", nullable = true)
    private Locale locale;

    @ManyToOne
    @JoinColumn(name = "user_type_id", nullable = false)
    private User_Type user_Type;

    @ManyToOne
    @JoinColumn(name = "user_status_id", nullable = false)
    private User_Status user_Status;

    @OneToOne
    @JoinColumn(name = "user_has_address_id", nullable = true)
    private User_Has_Address user_Has_Address;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFname() {
        return fname;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }

    public String getLname() {
        return lname;
    }

    public void setLname(String lname) {
        this.lname = lname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Date getDob() {
        return dob;
    }

    public void setDob(Date dob) {
        this.dob = dob;
    }

    public Date getJoined_date() {
        return joined_date;
    }

    public void setJoined_date(Date joined_date) {
        this.joined_date = joined_date;
    }

    public String getVerification() {
        return verification;
    }

    public void setVerification(String verification) {
        this.verification = verification;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public User_Type getUser_Type() {
        return user_Type;
    }

    public void setUser_Type(User_Type user_Type) {
        this.user_Type = user_Type;
    }

    public User_Status getUser_Status() {
        return user_Status;
    }

    public void setUser_Status(User_Status user_Status) {
        this.user_Status = user_Status;
    }

    public User_Has_Address getUser_Has_Address() {
        return user_Has_Address;
    }

    public void setUser_Has_Address(User_Has_Address user_Has_Address) {
        this.user_Has_Address = user_Has_Address;
    }
}
