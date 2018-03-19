package com.m2miage.boundary;

import com.m2miage.entity.Action;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.m2miage.entity.Action;
import com.m2miage.entity.Action;
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
@RequestMapping(value = "/actions", produces = MediaType.APPLICATION_JSON_VALUE)
@ExposesResourceFor(Action.class)
public class ActionRepresentation {

    private final ActionRessource irAction;
    
    @Autowired
    public ActionRepresentation(ActionRessource ir) {
      this.irAction = ir;
    }

    /**
     * GET all
     */ 
    @GetMapping
    public ResponseEntity<?> getAllAction() {
        Iterable<Action> allAction = irAction.findAll();
        return new ResponseEntity<>(actionToResource(allAction), HttpStatus.OK);
    }

    /**
    *GET one
    */
    @GetMapping(value = "/{actionId}")
    public ResponseEntity<?> getAction(@PathVariable("actionId") String id) {
        // ? = Resource<Action>
        return Optional.ofNullable(irAction.findOne(id))
                .map(u -> new ResponseEntity<>(actionToResource(u, true), HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
    

    public static Resources<Resource<Action>> actionToResource(Iterable<Action> actions) {
        Link selfLink = linkTo(methodOn(ActionRepresentation.class).getAllAction())
                .withSelfRel();
        List<Resource<Action>> actionResources = new ArrayList<>();
        actions.forEach(action ->actionResources.add(actionToResource(action, false)));
        return new Resources<>(actionResources, selfLink);
    }

    public static Resource<Action> actionToResource(Action action, Boolean collection) {
        Link selfLink = linkTo(ActionRepresentation.class)
                .slash(action.getId())
                .withSelfRel();
        if (collection) {
            Link collectionLink = linkTo(methodOn(ActionRepresentation.class).getAllAction())
                    .withRel("collection");
            
            return new Resource<>(action, selfLink, collectionLink);
        } else {
            return new Resource<>(action, selfLink);
        }
    }
    
}
