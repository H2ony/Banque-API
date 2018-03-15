package com.m2miage.boundary;

import com.m2miage.entity.Action;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient("http://localhost:8083/")
public interface ActionRestClient {

    @RequestMapping(method = RequestMethod.GET, value = "/actions/{id}")
    Action get(@PathVariable("id") String id);
}