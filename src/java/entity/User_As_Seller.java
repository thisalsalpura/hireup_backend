/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package entity;

import com.google.gson.annotations.Expose;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 *
 * @author User
 */

@Entity
@Table(name = "user_as_seller")
public class User_As_Seller implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Expose
    private int id;
    
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    @Expose
    private User user;
    
    @Lob
    @Column(name = "about", nullable = true)
    @Expose
    private String about;
    
    @ManyToOne
    @JoinColumn(name = "seller_status_id", nullable = false)
    @Expose
    private Seller_Status seller_Status;

    public User_As_Seller() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public Seller_Status getSeller_Status() {
        return seller_Status;
    }

    public void setSeller_Status(Seller_Status seller_Status) {
        this.seller_Status = seller_Status;
    }
}
