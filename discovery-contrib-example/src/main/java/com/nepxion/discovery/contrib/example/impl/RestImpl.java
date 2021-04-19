package com.nepxion.discovery.contrib.example.impl;

/**
 * <p>Title: Nepxion Discovery</p>
 * <p>Description: Nepxion Discovery</p>
 * <p>Copyright: Copyright (c) 2017-2050</p>
 * <p>Company: Nepxion</p>
 * @author Haojun Ren
 * @version 1.0
 */

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RestImpl {
    @Autowired
    private RocketMQImpl rocketMQImpl;

    @GetMapping(path = "/rest/{value}")
    public String rest(@PathVariable(value = "value") String value) {
        return value;
    }
}
