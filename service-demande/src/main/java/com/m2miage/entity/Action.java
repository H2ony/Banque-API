/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2miage.entity;

import java.io.Serializable;
import javax.persistence.CascadeType;
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
public class Action implements Serializable{
    @Id
    private String id;
    private String numero;
    private String nom;
    private String personnecharge;
    private String etat;
    private String date;

  
    
    @ManyToOne(cascade=CascadeType.ALL)
    @JoinColumn(name="demande_id")
    private Demande d;
    

    public String getPersonnecharge() {
        return personnecharge;
    }

    public void setPersonnecharge(String personnecharge) {
        this.personnecharge = personnecharge;
    }


    public String getDemandeId(){
        return this.d.getId();
        
    }
    
    public Action() {
    }
    
    public Action(String id, String numero, String nom, String personnecharge, String etat, String date) {
        this.id = id;
        this.numero = numero;
        this.nom = nom;
        this.personnecharge = personnecharge;
        this.etat = etat;
        this.date = date;
        
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public Demande getD() {
        return d;
    }

    public void setD(Demande d) {
        this.d = d;
    }

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