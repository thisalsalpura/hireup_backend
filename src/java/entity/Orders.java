/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package entity;

import com.google.gson.annotations.Expose;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 *
 * @author User
 */
@Entity
@Table(name = "orders")
public class Orders implements Serializable {

    @Id
    @Column(name = "order_id", length = 50)
    @Expose
    private String order_id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @Expose
    private User user;

    @Column(name = "placed_date", nullable = false)
    @Expose
    private Date placed_date;

    @ManyToOne
    @JoinColumn(name = "payment_status_id", nullable = false)
    private Payment_Status payment_Status;

    @ManyToOne
    @JoinColumn(name = "order_status_id", nullable = false)
    private Order_Status order_Status;

    public Orders() {
    }

    public String getOrder_id() {
        return order_id;
    }

    public void setOrder_id(String order_id) {
        this.order_id = order_id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Date getPlaced_date() {
        return placed_date;
    }

    public void setPlaced_date(Date placed_date) {
        this.placed_date = placed_date;
    }

    public Payment_Status getPayment_Status() {
        return payment_Status;
    }

    public void setPayment_Status(Payment_Status payment_Status) {
        this.payment_Status = payment_Status;
    }

    public Order_Status getOrder_Status() {
        return order_Status;
    }

    public void setOrder_Status(Order_Status order_Status) {
        this.order_Status = order_Status;
    }
}
