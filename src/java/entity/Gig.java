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
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 *
 * @author User
 */
@Entity
@Table(name = "gig")
public class Gig implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "title", length = 50, nullable = false)
    private String title;

    @Lob
    @Column(name = "description", nullable = false)
    private String description;
    
    @Column(name = "created_at", nullable = false)
    private Date created_at;
    
    @ManyToOne
    @JoinColumn(name = "sub_category_id", nullable = false)
    private Sub_Category sub_Category;
    
    @ManyToOne
    @JoinColumn(name = "gig_status_id", nullable = false)
    private Gig_Status gig_Status;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public Gig() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Date created_at) {
        this.created_at = created_at;
    }

    public Sub_Category getSub_Category() {
        return sub_Category;
    }

    public void setSub_Category(Sub_Category sub_Category) {
        this.sub_Category = sub_Category;
    }

    public Gig_Status getStatus() {
        return gig_Status;
    }

    public void setStatus(Gig_Status gig_Status) {
        this.gig_Status = gig_Status;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
