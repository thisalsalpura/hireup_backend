/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package entity;

import com.google.gson.annotations.Expose;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
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
    @Expose
    private int id;

    @Lob
    @Column(name = "title", nullable = false)
    @Expose
    private String title;

    @Lob
    @Column(name = "description", nullable = false)
    @Expose
    private String description;

    @Column(name = "created_at", nullable = false)
    @Expose
    private Date created_at;

    @ManyToOne
    @JoinColumn(name = "sub_category_id", nullable = false)
    @Expose
    private Sub_Category sub_Category;

    @ManyToOne
    @JoinColumn(name = "gig_status_id", nullable = false)
    @Expose
    private Gig_Status gig_Status;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @Expose
    private User user;

    @ManyToOne
    @JoinColumn(name = "gig_visible_status_id", nullable = false)
    @Expose
    private Gig_Visible_Status gig_Visible_Status;

    @OneToMany(mappedBy = "gig")
    private List<Gig_Has_Package> gigHasPackages;

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

    public Gig_Status getGig_Status() {
        return gig_Status;
    }

    public void setGig_Status(Gig_Status gig_Status) {
        this.gig_Status = gig_Status;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Gig_Visible_Status getGig_Visible_Status() {
        return gig_Visible_Status;
    }

    public void setGig_Visible_Status(Gig_Visible_Status gig_Visible_Status) {
        this.gig_Visible_Status = gig_Visible_Status;
    }

    public List<Gig_Has_Package> getGigHasPackages() {
        return gigHasPackages;
    }

    public void setGigHasPackages(List<Gig_Has_Package> gigHasPackages) {
        this.gigHasPackages = gigHasPackages;
    }
}
