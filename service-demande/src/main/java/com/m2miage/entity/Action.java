/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2miage.entity;

import java.io.Serializable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
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
    @Column(name="IDACTION")
    private String idAction;
    private int numero;
    private String nom;
    private String personnecharge;
    private String etat;
    private String date;

  
    
    @ManyToOne
    @JoinColumn(name="DEMANDE_ID")
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
    
     public Action(int numero){
         this.numero = numero;
    }
    
    
    public Action(String id, int numero, String nom, String personnecharge, String etat, String date) {
        this.idAction = id;
        this.numero = numero;
        this.nom = nom;
        this.personnecharge = personnecharge;
        this.etat = etat;
        this.date = date;
        
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public Demande getDemandeAction() {
        return d;
    }

    public void setDemande(Demande d) {
        this.d = d;
    }

     public Action(String nom) {
        this.nom = nom;
    }
    
    public String getId() {
        return idAction;
    }
    
    public String getIdAction() {
        return idAction;
    }

    public void setId(String id) {
        this.idAction = id;
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
    
    public boolean compareTo(Action a){
        boolean isEqual = false;
        
        if(a.nom.toLowerCase().compareTo(this.nom.toLowerCase()) == 0){
            isEqual = true;
        }
        
        return isEqual;
    }

    public String toString(){
        return "Numéro : "+String.valueOf(this.numero)+",  Nom : "+this.nom+"  Id : "+this.idAction;
    }

}