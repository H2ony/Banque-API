package com.m2miage.boundary;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.m2miage.entity.Action;
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
public class ActionsRepresentation {

    private final ActionRessource ir;

    @Autowired
    public ActionsRepresentation(ActionRessource ir) {
      this.ir = ir;
    }

    // GET all
    @GetMapping
    public ResponseEntity<?> getAllActions() {
        // ? = Resources<Resource<Action>>
        Iterable<Action> allActions = ir.findAll();
        return new ResponseEntity<>(actionToResource(allActions), HttpStatus.OK);
    }

    // GET one
    @GetMapping(value = "/{actionId}")
    public ResponseEntity<?> getAction(@PathVariable("actionId") String id) {
        // ? = Resource<Action>
        return Optional.ofNullable(ir.findOne(id))
                .map(u -> new ResponseEntity<>(actionToResource(u, true), HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // POST
    @PostMapping
    public ResponseEntity<?> saveAction(@RequestBody Action action) {
        action.setId(UUID.randomUUID().toString());
        Action saved = ir.save(action);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setLocation(linkTo(ActionsRepresentation.class).slash(saved.getId()).toUri());
        return new ResponseEntity<>(null, responseHeaders, HttpStatus.CREATED);
    }

    // PUT
    @PutMapping(value = "/{actionId}")
    public ResponseEntity<?> updateInscription(@RequestBody Action action,
            @PathVariable("actionId") String actionId) {
        Optional<Action> body = Optional.ofNullable(action);
        if (!body.isPresent()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (!ir.exists(actionId)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        action.setId(actionId);
        Action result = ir.save(action);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // DELETE
    @DeleteMapping(value = "/{actionId}")
    public ResponseEntity<?> deleteInscription(@PathVariable String actionId) {
        ir.delete(actionId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    private Resources<Resource<Action>> actionToResource(Iterable<Action> actions) {
        Link selfLink = linkTo(methodOn(ActionsRepresentation.class).getAllActions())
                .withSelfRel();
        List<Resource<Action>> actionResources = new ArrayList<>();
        actions.forEach(action ->
                actionResources.add(actionToResource(action, false)));
        return new Resources<>(actionResources, selfLink);
    }

    private Resource<Action> actionToResource(Action action, Boolean collection) {
        Link selfLink = linkTo(ActionsRepresentation.class)
                .slash(action.getId())
                .withSelfRel();
        if (collection) {
            Link collectionLink = linkTo(methodOn(ActionsRepresentation.class).getAllActions())
                    .withRel("collection");
            return new Resource<>(action, selfLink, collectionLink);
        } else {
            return new Resource<>(action, selfLink);
        }
    }
}
