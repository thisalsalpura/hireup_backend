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
@Table(name = "gig_has_package")
public class Gig_Has_Package implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Expose
    private int id;

    @ManyToOne
    @JoinColumn(name = "gig_id", nullable = false)
    @Expose
    private Gig gig;

    @ManyToOne
    @JoinColumn(name = "gig_package_type_id", nullable = false)
    @Expose
    private Gig_Package_Type package_Type;

    @Column(name = "price", nullable = false)
    @Expose
    private double price;

    @Column(name = "delivery_time", nullable = false)
    @Expose
    private int delivery_time;

    @Column(name = "extra_note", nullable = false)
    @Expose
    private String extra_note;

    public Gig_Has_Package() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Gig getGig() {
        return gig;
    }

    public void setGig(Gig gig) {
        this.gig = gig;
    }

    public Gig_Package_Type getPackage_Type() {
        return package_Type;
    }

    public void setPackage_Type(Gig_Package_Type package_Type) {
        this.package_Type = package_Type;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getDelivery_time() {
        return delivery_time;
    }

    public void setDelivery_time(int delivery_time) {
        this.delivery_time = delivery_time;
    }

    public String getExtra_note() {
        return extra_note;
    }

    public void setExtra_note(String extra_note) {
        this.extra_note = extra_note;
    }
}
