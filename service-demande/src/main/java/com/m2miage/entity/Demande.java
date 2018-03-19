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
import javax.persistence.Column;

@Entity
public class Demande {

    @Id
    @Column(name="IDDEMANDE")
    private String idDemande;
    private String nom;
    private String prenom;
    private String adresse;
    private String datenaiss;
    private float revenu;
    private float credit;
    private int duree;
    @Column(name="ETAT")
    private String etat;
    private String token;

    
    
    
    @ElementCollection
    @JsonProperty("actions")
    //@OneToMany(cascade=CascadeType.ALL)
    //@JoinColumn(name="demande_id",referencedColumnName="id")
    @OneToMany(cascade=CascadeType.ALL,mappedBy="d")
    private Set<Action> actions = new HashSet();
   
   
    
    public Demande() {
    }

    public Demande(String id, String nom, String prenom, String adresse, String dateNaiss, float revenu3DernAnnee, float mntCreditDem, int duree, String etat) {
        this.idDemande = id;
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
        this.idDemande = d.idDemande;
        this.nom = d.nom;
        this.prenom = d.prenom;
        this.adresse = d.adresse;
        this.datenaiss = d.datenaiss;
        this.revenu = d.revenu;
        this.credit = d.credit;
        this.duree = d.duree;
        this.etat = d.etat;
        
    }

    
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
    public Set<Action> getActions() {
        return actions;
    }

    
    public String getId() {
        return idDemande;
    }
    public String getIdDemande() {
        return idDemande;
    }

    public void setId(String id) {
        this.idDemande = id;
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
    
    public boolean addActions(Action a){
        boolean trouve = false;
        for (Action action : this.actions) {
            
            if(action.getNumero()==a.getNumero()){
                trouve = true;
            }
            
        }
        
        //Si une action n'est pas déja enregistrée, on la save
        if(!trouve)
            this.actions.add(a);
        
        return trouve;
    }
    
    public void setEtat(String etat){
        this.etat=etat;
    }
    
    /**
     * Retourn vrai si la dernière action est terminée, faux sinon 
     * @return 
     */
    public Action returnLastAction(){
        Action action = new Action(-1);
        
        for(Action a : this.actions){
            if(a.getNumero() == this.actions.size()){
                return a;
            }
        }
        //On retourne l'action avec un numéro erreur
        return action;
    }
    
}
