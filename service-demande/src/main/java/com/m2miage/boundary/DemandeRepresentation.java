package com.m2miage.boundary;

import com.m2miage.entity.Action;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.m2miage.entity.Demande;
import com.m2miage.entity.Demande;
import java.util.Collections;
import java.util.stream.Collectors;
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

    private final DemandeRessource ir;
    
    @Autowired
    public DemandeRepresentation(DemandeRessource ir) {
      this.ir = ir;
    }

    // GET all
    @GetMapping
    public ResponseEntity<?> getAllDemande() {
        Iterable<Demande> allDemande = ir.findAll();
        return new ResponseEntity<>(demandeToResource(allDemande), HttpStatus.OK);
    }

    // GET one
    @GetMapping(value = "/{demandeId}")
    public ResponseEntity<?> getDemande(@PathVariable("demandeId") String id) {
        // ? = Resource<Demande>
        return Optional.ofNullable(ir.findOne(id))
                .map(u -> new ResponseEntity<>(demandeToResource(u, true), HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }


    // POST
    @PostMapping
    public ResponseEntity<?> saveDemande(@RequestBody Demande demande) {
        demande.setId(UUID.randomUUID().toString());
        demande.setEtat("");
        
        HttpHeaders responseHeaders = new HttpHeaders();
        
        
        Demande saved = ir.save(demande);
        
        responseHeaders.setLocation(linkTo(DemandeRepresentation.class).slash(saved.getId()).toUri());
        return new ResponseEntity<>(null, responseHeaders, HttpStatus.CREATED);
        
        
       
    }

    // PUT
    @PutMapping(value = "/{demandeId}")
    public ResponseEntity<?> updateInscription(@RequestBody Demande demande,
            @PathVariable("demandeId") String demandeId) {
        Optional<Demande> body = Optional.ofNullable(demande);
        if (!body.isPresent()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (!ir.exists(demandeId)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        demande.setId(demandeId);
        Demande result = ir.save(demande);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // DELETE
    @DeleteMapping(value = "/{demandeId}")
    public ResponseEntity<?> deleteInscription(@PathVariable String demandeId) {
        ir.delete(demandeId);
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
        Link selfLink = linkTo(DemandeRepresentation.class)
                .slash(demande.getId())
                .withSelfRel();
        if (collection) {
            Link collectionLink = linkTo(methodOn(DemandeRepresentation.class).getAllDemande())
                    .withRel("collection");
            
            return new Resource<>(demande, selfLink, collectionLink);
        } else {
            return new Resource<>(demande, selfLink);
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
