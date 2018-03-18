package com.m2miage.boundary;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.m2miage.entity.Action;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.m2miage.entity.Demande;
import com.m2miage.entity.Demande;
import java.text.DateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;
@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
@ExposesResourceFor(Demande.class)
public class DemandeRepresentation {

    private final DemandeRessource irDemande;
    private final ActionRessource irAction;
    
    @Autowired
    public DemandeRepresentation(DemandeRessource irDemande, ActionRessource irAction) {
      this.irDemande = irDemande;
      this.irAction = irAction;
    }

    /** 
     * 
     * GET all
     * @return 
     */
    @GetMapping(value = "/demandes")
    public ResponseEntity<?> getAllDemande() {
        Iterable<Demande> allDemande = irDemande.findAll();
        return new ResponseEntity<>(demandeToResource(allDemande), HttpStatus.OK);
    }

    /**
     * 
     *  GET one
     * @param id
     * @return 
     */
    @GetMapping(value = "/{demandeId}")
    public ResponseEntity<?> getDemande(@PathVariable("demandeId") String id) {
        // ? = Resource<Demande>
        return Optional.ofNullable(irDemande.findOne(id))
                .map(u -> new ResponseEntity<>(demandeToResource(u, true), HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }


    /**
     *  POST
     * @param demande
     * @return 
     */
    @PostMapping
    public ResponseEntity<?> saveDemande(@RequestBody Demande demande) {
        demande.setId(UUID.randomUUID().toString());

        HttpHeaders responseHeaders = new HttpHeaders();
        
        
        Demande saved = irDemande.save(demande);
        
        responseHeaders.setLocation(linkTo(DemandeRepresentation.class).slash(saved.getId()).toUri());
        return new ResponseEntity<>(null, responseHeaders, HttpStatus.CREATED);
        
        
       
    }
    
    /**
     * PUT
     * @param demande
     * @param demandeId
     * @return 
     */
    @PutMapping(value = "/{demandeId}")
    @JsonIgnoreProperties("ETAT")
    public ResponseEntity<?> updateInscription(@RequestBody Demande demande,@PathVariable("demandeId") String demandeId) {
        //On va rechercher l'ancienne demande
        Demande laDemande = irDemande.findOne(demandeId);
        String etat = laDemande.getEtat();
        
        //Récupèration du body
        Optional<Demande> body = Optional.ofNullable(demande);
        if (!body.isPresent()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (!irDemande.exists(demandeId)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
       
        //On récupère le lien hateoas
        Resource r = demandeToResource(laDemande, true);
        
        //Si on peut valider, on peut modifier la demande
        //si on peut attribuer, on peut encore modifier, mais après le statut ne le permet plus 
        if(isLinkPresent("valider",r.getLinks()) || isLinkPresent("attribuer",r.getLinks()) ){
            
            demande.setEtat(etat);
            demande.setId(demandeId);
            irDemande.save(demande);
            return new ResponseEntity<>(HttpStatus.ACCEPTED);
        }
        else{
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        
    }

    
    
    /**
     * 
     * Accepte une demande et génère la prochaine action
     */   
    @Transactional
    @PutMapping(value = "/{demandeId}/accept")
    public ResponseEntity<?>acceptDemande(@PathVariable("demandeId") String demandeId) {
        
        //on récupère l'uri pour la comparer au lien hateoas proposé
        //String url =  request.getRequestURI();
        
        
        Demande laDemande;
        Action newAction;
        //On va rechercher la demande
        laDemande = irDemande.findOne(demandeId);
     
      
        //On récupère le lien hateoas
        Resource r = demandeToResource( laDemande, true);
        
        //On contrôle que l'action est bien la prochaine
        if(!isLinkPresent("valider",r.getLinks())){

            return new ResponseEntity<>(demandeToResource(laDemande, true), HttpStatus.BAD_REQUEST);

        }

        if(laDemande == null ){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        else{

            //On change l'état de la demande et on enregistre l'action
            newAction=getNextAction(laDemande);

            //On raccroche la demande à l'action
            newAction.setDemande(laDemande);

            //On raccroche l'action
            if(laDemande.addActions(newAction)){
                 //On sauvegarde l'action
                irAction.save(newAction);
            }
            else{
                new ResponseEntity<>(demandeToResource(laDemande, true), HttpStatus.CONFLICT);
            }

            //On sauvegarde la demande
            irDemande.save(laDemande);

            return new ResponseEntity<>(demandeToResource(laDemande, true), HttpStatus.OK);
        }
        
        
    }

    
    /**
     * 
     * Accepte une demande et génère la prochaine action
     */   
    @Transactional
    @PutMapping(value = "/{demandeId}/delegate/{personName}")
    public ResponseEntity<?>delegateDemande(@PathVariable("demandeId") String demandeId, @PathVariable("personName") String personName) {
        Demande laDemande;
        Action newAction;
        
        //On va rechercher la demande
        laDemande = irDemande.findOne(demandeId);
        
        //On récupère le lien hateoas
        Resource r = demandeToResource( laDemande, true);
        
        //On contrôle que l'action est bien la prochaine
        if(!isLinkPresent("attribuer",r.getLinks())){

            return new ResponseEntity<>(demandeToResource(laDemande, true), HttpStatus.BAD_REQUEST);

        }
            
        if(laDemande == null ){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        else {
            
            //On change l'état de la demande et on enregistre l'action
            newAction=getNextAction(laDemande);
 
            newAction.setPersonneCharge(personName);

            //On raccroche la demande à l'action
            newAction.setDemande(laDemande);
            
            //On raccroche l'action
            if(laDemande.addActions(newAction)){
                 //On sauvegarde l'action
                irAction.save(newAction);
            }
            else{
                new ResponseEntity<>(demandeToResource(laDemande, true), HttpStatus.CONFLICT);
            }

            //On sauvegarde la demande
            irDemande.save(laDemande);
            
            return new ResponseEntity<>(demandeToResource(laDemande, true), HttpStatus.OK);
        }
        
    }
    
    /**
     * 
     * Accepte une demande et génère la prochaine action
     */   
    @Transactional
    @PutMapping(value = "/{demandeId}/decide")
    public ResponseEntity<?>delegateDemande(@PathVariable("demandeId") String demandeId) {
        Demande laDemande;
        Action newAction;
        
        //On va rechercher la demande
        laDemande = irDemande.findOne(demandeId);
           
        //On récupère le lien hateoas
        Resource r = demandeToResource( laDemande, true);
        
        //On contrôle que l'action est bien la prochaine
        if(!isLinkPresent("decider",r.getLinks())){

            return new ResponseEntity<>(demandeToResource(laDemande, true), HttpStatus.BAD_REQUEST);

        }
        
        if(laDemande == null ){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        else {
            
            //On change l'état de la demande et on enregistre l'action
            newAction=getNextAction(laDemande);

            //On raccroche la demande à l'action
            newAction.setDemande(laDemande);
            
            //On raccroche l'action
            if(laDemande.addActions(newAction)){
                 //On sauvegarde l'action
                irAction.save(newAction);
            }
            else{
                new ResponseEntity<>(demandeToResource(laDemande, true), HttpStatus.CONFLICT);
            }

            //On sauvegarde la demande
            irDemande.save(laDemande);
            
            return new ResponseEntity<>(demandeToResource(laDemande, true), HttpStatus.OK);
        }
        
    }
    
    
    /**
     * 
     * Décision sur une finale
     */   
    @Transactional
    @PutMapping(value = "/{demandeId}/jury/{decision}")
    public ResponseEntity<?>decisionDemande(@PathVariable("demandeId") String demandeId, @PathVariable("decision") String decision) {
        Demande laDemande;
        Action newAction;
        
        //On va rechercher la demande
        laDemande = irDemande.findOne(demandeId);
           
        //On récupère le lien hateoas
        Resource r = demandeToResource( laDemande, true);
        
        //On contrôle que l'action est bien la prochaine
        if(!isLinkPresent("juger",r.getLinks())){

            return new ResponseEntity<>(demandeToResource(laDemande, true), HttpStatus.BAD_REQUEST);

        }
        
        if(laDemande == null ){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        else {
            
            //On récupère la décision
            laDemande.setEtat(decision);
            
            //On change l'état de la demande et on enregistre l'action
            newAction=getNextAction(laDemande);

            //On raccroche la demande à l'action
            newAction.setDemande(laDemande);
            
            //On raccroche l'action
            if(laDemande.addActions(newAction)){
                 //On sauvegarde l'action
                irAction.save(newAction);
            }
            else{
                new ResponseEntity<>(demandeToResource(laDemande, true), HttpStatus.CONFLICT);
            }

            //On sauvegarde la demande
            irDemande.save(laDemande);
            
            return new ResponseEntity<>(demandeToResource(laDemande, true), HttpStatus.OK);
        }
        
    }
    
    /** DELETE
     * 
     * @param demandeId
     * @return 
     */
    @DeleteMapping(value = "/{demandeId}")
    public ResponseEntity<?> deleteInscription(@PathVariable String demandeId) {
        irDemande.delete(demandeId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    private Resources<Resource<Demande>> demandeToResource(Iterable<Demande> demandes) {
        Link selfLink = linkTo(methodOn(DemandeRepresentation.class).getAllDemande())
                .withSelfRel();
        List<Resource<Demande>> demandeResources = new ArrayList<>();
        demandes.forEach(demande ->demandeResources.add(demandeToResource(demande, false)));
        return new Resources<>(demandeResources, selfLink);
    }

    private Resource<Demande> demandeToResource(Demande demande, Boolean collection) {
    
        //Ressource retournée, en premier la demande et son selflink
        Resource r = new Resource(demande);
        
       //On ajoute les liens vers les actions de la demande
        for (Action a : demande.getActions()) {
            r.add(linkTo(methodOn(ActionRepresentation.class).getAction(a.getId()))
                .withRel("actions"));
            
        }
        
        //Lien vers la demande elle même
         Link selfLink; 
        
        //En fonction de l'état de la demande, on ajoute le lien vers la prochaine action
        switch(demande.getEtat()){
 
            case "[DEBUT]":
               selfLink = linkTo(methodOn(DemandeRepresentation.class).getDemande(demande.getId()))
                .slash("delegate")
                .slash("PERRIN")
                .withRel("attribuer");
                break;
                
            case "[ETUDE]":
               selfLink = linkTo(methodOn(DemandeRepresentation.class).getDemande(demande.getId()))
                .slash("decide")
                .withRel("decider");
                break;
            case "[DECISION]":
                
               if(((int)(Math.random() * (100-1)) + 1) >50) {
                    selfLink = linkTo(methodOn(DemandeRepresentation.class).getDemande(demande.getId()))
                    .slash("jury")
                    .slash("[ACCEPTATION]")
                    .withRel("juger");
               }
               else{
                   selfLink = linkTo(methodOn(DemandeRepresentation.class).getDemande(demande.getId()))
                    .slash("jury")
                    .slash("[REJET]")
                    .withRel("juger");
               }
               
                break;
                case "[FIN]":
                    selfLink = linkTo(methodOn(DemandeRepresentation.class).getDemande(demande.getId()))
                    .withSelfRel();
                 break;
            default:
                selfLink = linkTo(methodOn(DemandeRepresentation.class).getDemande(demande.getId()))
                .slash("accept")
                .withRel("valider");
                break;
        }
        
        r.add(selfLink);
        
        //On pointe vers les autres demandes
        if (collection) {
            Link collectionLink = linkTo(methodOn(DemandeRepresentation.class).getAllDemande())
                    .withRel("collection");
            
            r.add(collectionLink);
            
            return r;
        } else {
            return r;
        }
    }
    
    public Date getAujourdhuiDate(){
        Date aujourdhui = new Date();
        
        return aujourdhui;
    }
    
    public DateFormat getAujourdhuiFormat(){
        DateFormat shortDateFormat = DateFormat.getDateTimeInstance(
        DateFormat.SHORT,
        DateFormat.SHORT, new Locale("FR","fr"));
        
        return shortDateFormat; 
    }
    
    
    public Action getNextAction(Demande demande){
        Action a;
        
        //En fonction de l'état de la demande on retoune l'action qui va bien 
        switch(demande.getEtat()){
            case "[DEBUT]":
                a=new Action(UUID.randomUUID().toString(),"2", "Vérification informations", "HOYET", "Revue en cours", getAujourdhuiFormat().format(getAujourdhuiDate()));
                 //On adapte le statut de la demande 
                demande.setEtat("[ETUDE]");
            break;
            
            case "[ETUDE]":
                a=new Action(UUID.randomUUID().toString(),"3", "Vérification informations", "HOYET", "Décision en attente de validation", getAujourdhuiFormat().format(getAujourdhuiDate()));
                 //On adapte le statut de la demande 
                demande.setEtat("[DECISION]");
            break;
                
            
            case "[ACCEPTATION]":
                a=new Action(UUID.randomUUID().toString(),"4", "Notification", "HOYET", "Demande acceptée", getAujourdhuiFormat().format(getAujourdhuiDate()));
                 //On adapte le statut de la demande 
                demande.setEtat("[FIN]");
            break;
            
            case "[REJET]":
                a=new Action(UUID.randomUUID().toString(),"4", "Notification", "HOYET", "Demande refusée", getAujourdhuiFormat().format(getAujourdhuiDate()));
                 //On adapte le statut de la demande 
                demande.setEtat("[FIN]");
            break;
            
            //On tombe jamais dans ce cas grâce au lien HATEOAS
            case "[FIN]":
                a=new Action(UUID.randomUUID().toString(),"5", "Notification", "HOYET", "Erreur", getAujourdhuiFormat().format(getAujourdhuiDate()));
                demande.setEtat("[FIN]");
            break;
            
            //Par défaut, si la demande n'a pas d'état, on le met à début
            default:
                a= new Action(UUID.randomUUID().toString(),"1", "A valider", "HOYET", "En attente d'attribution", getAujourdhuiFormat().format(getAujourdhuiDate()));
                demande.setEtat("[DEBUT]");
            break;
        }
        //On raccroche la demande à l'action
        a.setDemande(demande);
        
        return a;
    }
    
    public boolean isLinkPresent(String url, List<Link> links){
        for (Link l : links) {
            if(l.getRel().compareTo(url) == 0){
                return true;
            }
        }
        return false;
        
    }
    
    
}
