package org.m2.secureservice.boundary;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RepresentationRessource {

    @GetMapping(value = "/secure")
    public String entryPoint() {
        return "Securise";
    }
    
    @GetMapping(value = "/hello")
    public String freeEntryPoint() {
        return "Open bar";
    }

}
