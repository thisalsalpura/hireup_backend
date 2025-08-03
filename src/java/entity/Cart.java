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
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 *
 * @author User
 */
@Entity
@Table(name = "cart")
public class Cart implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Expose
    private int id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @Expose
    private User user;

    @ManyToOne
    @JoinColumn(name = "gig_has_package_id")
    @Expose
    private Gig_Has_Package gig_Has_Package;

    public Cart() {
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

    public Gig_Has_Package getGig_Has_Package() {
        return gig_Has_Package;
    }

    public void setGig_Has_Package(Gig_Has_Package gig_Has_Package) {
        this.gig_Has_Package = gig_Has_Package;
    }
}
