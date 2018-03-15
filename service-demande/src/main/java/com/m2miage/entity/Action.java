/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2miage.entity;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 *
 * @author Anthony
 */
@Entity
@Table(name = "action")
public class Action implements Serializable{
    @Id
    private String id;
    private String nom;
    private String personnecharge;
    private String etat;
    private String date;
    
    @JoinColumn(name="demande_id",nullable=false)
    private Demande demande;
    private String demande_id;
  

    public Action() {
    }
    
    /*
    public Demande getD() {
        return d;
    }

    public void setD(Demande d) {
        this.d = d;
    }
*/
     public Action(String nom) {
        this.nom = nom;
    }
    
    public String getId() {
        return id;
    }
    
    public String getIdAction() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPersonneCharge() {
        return personnecharge;
    }

    public void setPersonneCharge(String personneEnCharge) {
        this.personnecharge = personneEnCharge;
    }

    public String getEtat() {
        return etat;
    }

    public void setEtat(String etat) {
        this.etat = etat;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

}