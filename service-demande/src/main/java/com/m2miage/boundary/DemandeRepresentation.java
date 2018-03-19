package com.m2miage.boundary;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.m2miage.entity.Action;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.m2miage.entity.Demande;
import com.m2miage.entity.Demande;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
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
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.RequestParam;



@RestController
@RequestMapping(value="/demandes", produces = MediaType.APPLICATION_JSON_VALUE)
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
     * GET all demandes en fonction du statut passé en paramètre
     * @return 
     */
    @GetMapping
    public ResponseEntity<?> getAllDemande(@RequestParam(value = "statut", required = false) String leStatut) {
        Iterable<Demande> allDemande = irDemande.findAll();
        return new ResponseEntity<>(demandeToResource(allDemande,leStatut), HttpStatus.OK);
    }

    /**
     * 
     *  GET une demande en fonctione de son id
     * @param id
     * @return 
     */
    @GetMapping(value = "/{demandeId}")
    public ResponseEntity<?> getDemande(@PathVariable("demandeId") String id,HttpServletRequest req, HttpServletResponse res) {
        final Optional<String> token = Optional.ofNullable(req.getHeader(HttpHeaders.AUTHORIZATION));
        Demande d = irDemande.findOne(id);
       
        if(token.get().compareTo(d.getToken())==0){
            // ? = Resource<Demande>
            return Optional.ofNullable(d)
                    .map(u -> new ResponseEntity<>(demandeToResource(u, true,null), HttpStatus.OK))
                    .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
        }
        else{
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }
    
     /**
     * Retourne les actions pour une demande 
     *  GET 
     * @param id
     * @return 
     */
    @GetMapping(value = "/{demandeId}/actions")
    public ResponseEntity<?> getDemandeActions(@PathVariable("demandeId") String id) {
        // ? = Resource<Demande>
        Demande d = irDemande.findOne(id);
        
        return new ResponseEntity<>(ActionRepresentation.actionToResource(d.getActions()),HttpStatus.OK);
    }


    /**
     *  POST une nouvelle demande
     * @param demande
     * @return 
     */
    @PostMapping
    public ResponseEntity<?> saveDemande(@RequestBody Demande demande, HttpServletRequest req, HttpServletResponse res) {
        


        //On génère l'id de la demande 
        demande.setId(UUID.randomUUID().toString());
        
        HttpHeaders responseHeaders = new HttpHeaders();
        
        System.out.println("********************************************************");
        System.out.println("********************************************************");
        System.out.println("********************************************************");
        System.out.println("********************************************************");
        /*
        System.out.println("*******"+req.getAttribute("username").toString()+"*****");
        System.out.println("*******"+req.getHeader("username").toString()+"*****");
        */
        System.out.println("********************************************************");
        System.out.println("********************************************************");
        System.out.println("********************************************************");
        System.out.println("********************************************************");
        
        
        String token = Jwts.builder()
                .setSubject("toto")
                .setExpiration(new Date(System.currentTimeMillis() + 12000000))
                .signWith(SignatureAlgorithm.HS512, "thesecret")
                .compact();

        demande.setToken(token);
        Demande saved = irDemande.save(demande);
        
        responseHeaders.add(HttpHeaders.AUTHORIZATION, token);
        responseHeaders.setLocation(linkTo(DemandeRepresentation.class).slash(saved.getId()).toUri());
        return new ResponseEntity<>(null, responseHeaders, HttpStatus.CREATED);
        
        
       
    }
    
    /**
     * PUT une demande en fonction de son id
     * @param demande
     * @param demandeId
     * @return 
     */
    @PutMapping(value = "/{demandeId}")
    @JsonIgnoreProperties("ETAT")
    public ResponseEntity<?> updateDemande(@RequestBody Demande demande,@PathVariable("demandeId") String demandeId,HttpServletRequest req, HttpServletResponse res) {
        final Optional<String> token = Optional.ofNullable(req.getHeader(HttpHeaders.AUTHORIZATION));


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
        Resource r = demandeToResource(laDemande, true,null);
        
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
     *  POST une nouvelle action et met à jour l'état de la demande
     * @param action
     * @return 
     */
    @PostMapping(value = "/{demandeId}/actions")
    public ResponseEntity<?> saveAction(@PathVariable("demandeId") String demandeId, @RequestBody Action action) {
        HttpHeaders responseHeaders = new HttpHeaders();

        //On va rechercher l'ancienne demande
        Demande laDemande = irDemande.findOne(demandeId);

        //On récupère la prochaine action qui doit-être présente et la dernière pour set l'état
        Action nextAction = getNextAction(laDemande);
        Action previousAction = new Action(-1);
        
        if(laDemande.returnLastAction().getNumero()>=0){
            
            previousAction = irAction.findOne(laDemande.returnLastAction().getIdAction());
            
            //On change son statut à terminé
            previousAction.setEtat("terminée");
 
        }
        
        
        //Si la nouvelle action est bien celle attendue et que la dernière action est terminée, 
        if(nextAction.compareTo(action)){
            
            
            //On set l'état de l'action
            action.setId(UUID.randomUUID().toString());
            action.setNumero(nextAction.getNumero());
            action.setDemande(laDemande);
            
            
             //Si l'action est une acceptation ou un rejet
            if(action.getEtat().toLowerCase().compareTo(("acceptée").toLowerCase())==0){
               laDemande.setEtat("[ACCEPTATION]"); 
            }
            
            //Si l'action est une acceptation ou un rejet
            if(action.getEtat().toLowerCase().compareTo(("rejetée").toLowerCase())==0){
               laDemande.setEtat("[REJET]"); 
            }
            
            //On initialise avec le Bon état
            action.setEtat(nextAction.getEtat());
            
            //on sauvegarde l'ancienne action et la nouvelle
            Action saved = irAction.save(action);
            
                    
            //On l'ajoute à la demande 
            laDemande.addActions(saved);
            
            //On sauvegarde la demande(Etat à changé)
            irDemande.save(laDemande);
            
           if( previousAction.getNumero()>=0){
                irAction.save(previousAction);
            }
            
            responseHeaders.setLocation(linkTo(DemandeRepresentation.class).slash(saved.getId()).toUri());
            return new ResponseEntity<>(null, responseHeaders, HttpStatus.CREATED);
        }
        else{
            
            return new ResponseEntity<>(null, responseHeaders, HttpStatus.BAD_REQUEST);
        }
           
    }
    
    /** DELETE une demande en fonction de son id passé en paramètre
     * 
     * @param demandeId
     * @return 
     */
    @DeleteMapping(value = "/{demandeId}")
    public ResponseEntity<?> deleteInscription(@PathVariable String demandeId) {
        Demande laDemande = new Demande();
        
        //On va rechercher la demande
        laDemande = irDemande.findOne(demandeId);
        
        //On la clôture
        laDemande.setEtat("[FIN]");
        
        irDemande.save(laDemande);
        
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    private Resources<Resource<Demande>> demandeToResource(Iterable<Demande> demandes, String statut) {
        Link selfLink = linkTo(methodOn(DemandeRepresentation.class).getAllDemande(statut))
                .withSelfRel();
        List<Resource<Demande>> demandeResources = new ArrayList<>();
        demandes.forEach(
                
                    demande ->{
                        if(statut!=null){
                            if(demande.getEtat().compareTo(statut) == 0)
                                demandeResources.add(demandeToResource(demande, false, statut));
                        }
                        else{
                            demandeResources.add(demandeToResource(demande, false, statut));
                        }
                    });
                

        return new Resources<>(demandeResources, selfLink);
    }

    private Resource<Demande> demandeToResource(Demande demande, Boolean collection, String statut) {
    
        //Ressource retournée, en premier la demande et son selflink
        Resource r = new Resource(demande);
        
       //On ajoute les liens vers les actions de la demande
        for (Action a : demande.getActions()) {
            r.add(linkTo(methodOn(ActionRepresentation.class).getAction(a.getId()))
                .withRel("actions"));
            
        }
        
        //On pointe vers les autres demandes
        if (collection) {
            Link collectionLink = linkTo(methodOn(DemandeRepresentation.class).getAllDemande(null))
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
                a=new Action(UUID.randomUUID().toString(),2, "Revue en cours", "HOYET", "En cours", getAujourdhuiFormat().format(getAujourdhuiDate()));
                //On adapte le statut de la demande 
                demande.setEtat("[ETUDE]");
            break;
            
            case "[ETUDE]":
                a=new Action(UUID.randomUUID().toString(),3, "Décision en attente de validation", "HOYET", "En cours", getAujourdhuiFormat().format(getAujourdhuiDate()));
                //On adapte le statut de la demande 
                demande.setEtat("[DECISION]");
            break;
                
            
            case "[DECISION]":
                a=new Action(UUID.randomUUID().toString(),4, "Notification", "HOYET", "En cours", getAujourdhuiFormat().format(getAujourdhuiDate()));
            break;
            
            
            //On tombe jamais dans ce cas grâce au lien HATEOAS
            case "[FIN]":
                a=new Action(UUID.randomUUID().toString(),5, "Notification", "HOYET", "Erreur", getAujourdhuiFormat().format(getAujourdhuiDate()));
                demande.setEtat("[FIN]");
            break;
            
            //Par défaut, si la demande n'a pas d'état, on le met à début
            default:
                a= new Action(UUID.randomUUID().toString(),1, "En attente d'attribution", "HOYET", "En cours", getAujourdhuiFormat().format(getAujourdhuiDate()));
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
