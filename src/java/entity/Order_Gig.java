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
@Table(name = "order_gig")
public class Order_Gig implements Serializable{
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Expose
    private int id;
    
    @ManyToOne
    @JoinColumn(name = "gig_has_package_id", nullable = false)
    @Expose
    private Gig_Has_Package gig_Has_Package;
    
    @ManyToOne
    @JoinColumn(name = "orders_order_id", nullable = false)
    @Expose
    private Orders orders;

    public Order_Gig() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Gig_Has_Package getGig_Has_Package() {
        return gig_Has_Package;
    }

    public void setGig_Has_Package(Gig_Has_Package gig_Has_Package) {
        this.gig_Has_Package = gig_Has_Package;
    }

    public Orders getOrders() {
        return orders;
    }

    public void setOrders(Orders orders) {
        this.orders = orders;
    }
}
