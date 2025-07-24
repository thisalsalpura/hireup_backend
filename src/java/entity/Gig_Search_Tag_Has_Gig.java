/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package entity;

import java.io.Serializable;
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
@Table(name = "gig_search_tag_has_gig")
public class Gig_Search_Tag_Has_Gig implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    @ManyToOne
    @JoinColumn(name = "gig_search_tag_id", nullable = false)
    private Gig_Search_Tag search_Tag;
    
    @ManyToOne
    @JoinColumn(name = "gig_id", nullable = false)
    private Gig gig;

    public Gig_Search_Tag_Has_Gig() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Gig_Search_Tag getSearch_Tag() {
        return search_Tag;
    }

    public void setSearch_Tag(Gig_Search_Tag search_Tag) {
        this.search_Tag = search_Tag;
    }

    public Gig getGig() {
        return gig;
    }

    public void setGig(Gig gig) {
        this.gig = gig;
    }
}
