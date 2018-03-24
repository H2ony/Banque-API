package org.m2.oauthservice.boundary;

import java.security.Principal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class PrincipalRestController {

    @GetMapping(value = "/user")
    public Principal principal(Principal principal) {
        return principal;
    }
}