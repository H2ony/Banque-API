package com.m2miage.boundary;

import com.m2miage.entity.Action;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.m2miage.entity.Demande;
import com.m2miage.entity.Demande;
import java.util.Collections;
import java.util.HashSet;
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

@RestController
@RequestMapping(value = "/demandes", produces = MediaType.APPLICATION_JSON_VALUE)
@ExposesResourceFor(Demande.class)
public class DemandeRepresentation {

    private final DemandeRessource irDemande;
    private final ActionRessource irAction;
    
    @Autowired
    public DemandeRepresentation(DemandeRessource irDemande, ActionRessource irAction) {
      this.irDemande = irDemande;
      this.irAction = irAction;
    }

    // GET all
    @GetMapping
    public ResponseEntity<?> getAllDemande() {
        Iterable<Demande> allDemande = irDemande.findAll();
        return new ResponseEntity<>(demandeToResource(allDemande), HttpStatus.OK);
    }

    // GET one
    @GetMapping(value = "/{demandeId}")
    public ResponseEntity<?> getDemande(@PathVariable("demandeId") String id) {
        // ? = Resource<Demande>
        return Optional.ofNullable(irDemande.findOne(id))
                .map(u -> new ResponseEntity<>(demandeToResource(u, true), HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }


    // POST
    @PostMapping
    public ResponseEntity<?> saveDemande(@RequestBody Demande demande) {
        demande.setId(UUID.randomUUID().toString());

        HttpHeaders responseHeaders = new HttpHeaders();
        
        
        Demande saved = irDemande.save(demande);
        
        responseHeaders.setLocation(linkTo(DemandeRepresentation.class).slash(saved.getId()).toUri());
        return new ResponseEntity<>(null, responseHeaders, HttpStatus.CREATED);
        
        
       
    }
    
    // PUT
    @PutMapping(value = "/{demandeId}")
    public ResponseEntity<?> updateInscription(@RequestBody Demande demande,@PathVariable("demandeId") String demandeId) {
        Optional<Demande> body = Optional.ofNullable(demande);
        if (!body.isPresent()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (!irDemande.exists(demandeId)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        demande.setId(demandeId);
        Demande result = irDemande.save(demande);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    
    
    /**
     * 
     * Accepte une demande et génère la prochaine action
     */   
    @Transactional
    @PutMapping(value = "/{demandeId}/accept")
    public ResponseEntity<?>acceptDemande(@PathVariable("demandeId") String demandeId) {
        Demande laDemande;
        Action newAction;
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("JPAService");
        EntityManager em = emf.createEntityManager();
        
        
        //On va rechercher la demande
        laDemande = irDemande.findOne(demandeId);
            
        if(laDemande == null ){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        else {
            
            em.getTransaction().begin();
            
            //On change l'état de la demande et on enregistre l'action
            newAction=laDemande.nextState();
            irDemande.save(laDemande);
            
            //On set l'id de la prochaine action
            newAction.setId(UUID.randomUUID().toString());
            
            //On sauvegarde l'action
            irAction.save(newAction);
            em.persist(newAction);
            
            em.flush();
            
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
            
        if(laDemande == null ){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        else {
            
            //On change l'état de la demande et on enregistre l'action
            newAction=laDemande.nextState();
            
            //On initialise la personne en charge
            newAction.setPersonneCharge(personName);
            
            //On sauvegarde la demande
            irDemande.save(laDemande);
            
            //On set l'id de la nouvelle action
            newAction.setId(UUID.randomUUID().toString());
            
            //Puis on la sauvegarde l'action
            irAction.save(newAction);
            
            return new ResponseEntity<>(demandeToResource(laDemande, true), HttpStatus.OK);
        }
        
    }
    
    // DELETE
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
                .slash("HOYET")
                .withRel("attribuer");
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
    /*
     public boolean isValidInsert(){
        
        if  ( this.adresse.isEmpty()
                    && this.credit > 0
                    && this.duree > 0
                    && this.revenu > 0
                    && this.etat.compareTo("")==0
                    && this.nom.isEmpty()
                    && this.prenom.isEmpty()
                    && !this.datenaiss.isEmpty()
            ){
            return true;
        }
        else{
            return false;
        }
    }
*/
}
