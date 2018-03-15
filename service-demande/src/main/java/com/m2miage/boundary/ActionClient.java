package com.m2miage.boundary;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.m2miage.entity.Action;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class ActionClient {
    @Autowired
    ActionRestClient restActionClient;
    
    @HystrixCommand(fallbackMethod = "fallback")
    public Action get(String id) {
        return restActionClient.get(id);
    }

    public Action fallback(String id) {
        return new Action("non disponible");
    }
    
}