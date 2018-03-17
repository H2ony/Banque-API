/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2miage.entity;

/**
 *
 * @author Anthony
 */

import com.fasterxml.jackson.annotation.JsonProperty;
import java.text.DateFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.Date;
import java.util.Locale;

@Entity
public class Demande {

    @Id
    private String id;
    private String nom;
    private String prenom;
    private String adresse;
    private String datenaiss;
    private float revenu;
    private float credit;
    private int duree;
    private String etat;
    
    
    @ElementCollection
    @JsonProperty("actions")
    @OneToMany(mappedBy = "d")
    private Set<Action> actions = new HashSet();
   
   
    
    public Demande() {
    }

    public Demande(String id, String nom, String prenom, String adresse, String dateNaiss, float revenu3DernAnnee, float mntCreditDem, int duree, String etat) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.adresse = adresse;
        this.datenaiss = dateNaiss;
        this.revenu = revenu3DernAnnee;
        this.credit = mntCreditDem;
        this.duree = duree;
        this.etat = etat;
    }
    
    
    public Demande(Demande d) {
        this.id = d.id;
        this.nom = d.nom;
        this.prenom = d.prenom;
        this.adresse = d.adresse;
        this.datenaiss = d.datenaiss;
        this.revenu = d.revenu;
        this.credit = d.credit;
        this.duree = d.duree;
        this.etat = d.etat;
        
    }

    public Set<Action> getActions() {
        return actions;
    }

    
    public String getId() {
        return id;
    }
    public String getIdDemande() {
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

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getDatenaiss() {
        return datenaiss;
    }

    public void setDatenaiss(String datenaiss) {
        this.datenaiss = datenaiss;
    }

    public float getRevenu() {
        return revenu;
    }

    public void setRevenu(float revenu) {
        this.revenu = revenu;
    }

    public float getCredit() {
        return credit;
    }

    public void setCredit(float credit) {
        this.credit = credit;
    }

    public int getDuree() {
        return duree;
    }

    public void setDuree(int duree) {
        this.duree = duree;
    }

    public String getEtat() {
        return etat;
    }
    
    //Retourne la prochaine action à insèrer
    public Action nextState(){
        Action a = new Action();
        
        a.setD(this);
        //MARCHE PAS CA --> a.setDemande_id(this.id);
        
        Date aujourdhui = new Date();
        DateFormat shortDateFormat = DateFormat.getDateTimeInstance(
        DateFormat.SHORT,
        DateFormat.SHORT, new Locale("FR","fr"));
        
        switch(this.etat){
            case "[DEBUT]":
                a=new Action("","2", "Vérification informations", "HOYET", "Revue en cours", shortDateFormat.format(aujourdhui));
                this.etat = "[ETUDE]";
            break;
                
            //Par défaut, si la demande n'a pas d'état, on le met à début
            default:
                this.etat = "[DEBUT]";
                a= new Action("","1", "A valider", "HOYET", "En attente d'attribution", shortDateFormat.format(aujourdhui));
                //this.actions.add(a);
            break;
        }
        return a;
    }

    

    

   
    
}
