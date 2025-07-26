/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package entity;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 *
 * @author User
 */

@Entity
@Table(name = "user_as_seller_has_seller_education")
public class User_As_Seller_Has_Seller_Education implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;
    
    @ManyToOne
    @JoinColumn(name = "user_as_seller_id", nullable = false)
    private User_As_Seller user_As_Seller;
    
    @ManyToOne
    @JoinColumn(name = "seller_education_id", nullable = false)
    private Seller_Education seller_Education;

    public User_As_Seller_Has_Seller_Education() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public User_As_Seller getUser_As_Seller() {
        return user_As_Seller;
    }

    public void setUser_As_Seller(User_As_Seller user_As_Seller) {
        this.user_As_Seller = user_As_Seller;
    }

    public Seller_Education getSeller_Education() {
        return seller_Education;
    }

    public void setSeller_Education(Seller_Education seller_Education) {
        this.seller_Education = seller_Education;
    }
}
