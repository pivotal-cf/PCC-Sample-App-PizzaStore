/*
 * Copyright (C) 2018-Present Pivotal Software, Inc. All rights reserved.
 * This program and the accompanying materials are made available under
 * the terms of the under the Apache License, Version 2.0 (the "License”);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.pivotal.controller;

import io.pivotal.model.Pizza;
import io.pivotal.repository.gemfire.PizzaRepository;
import org.apache.geode.LogWriter;
import org.apache.geode.cache.client.ClientCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;
import java.util.Set;

@RestController
@DependsOn({"gemfireCache", "Pizza"})
public class AppController {

    private ClientCache gemfireCache;
    private PizzaRepository repository;

    @Autowired
    public AppController(ClientCache gemfireCache, PizzaRepository pizzaRepository) {
        this.gemfireCache = gemfireCache;
        this.repository = pizzaRepository;
    }

    @RequestMapping("/healthcheck")
    public ResponseEntity<Object> healthCheck() {
        LogWriter logger = gemfireCache.getLogger();

        Pizza plainPizza = makePlainPizza();
        Pizza fancyPizza = makeFancyPizza();
        repository.save(plainPizza);
        repository.save(fancyPizza);

        logger.info("Finished inserting the pizzas");

        Pizza found = repository.findById("plain").get();
        if (found == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        if (!found.getToppings().contains("cheese")) {
            logger.info("Where's my cheese? This is the pizza: " + found.toString());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        if (!found.getSauce().equals("red")) {
            logger.info("I ordered red sauce!! This is the pizza: " + found.toString());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping("/pestoOrder/{name}")
    public ResponseEntity<Object> pestoOrder(@PathVariable("name") String name) {

        Pizza pestoPizza = makeSuperFancyPizza(name);
        repository.save(pestoPizza);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    private Pizza makeFancyPizza() {
        Set<String> toppings = new HashSet<>();
        toppings.add("chicken");
        toppings.add("arugula");
        return new Pizza("fancy", toppings, "white");
    }

    private Pizza makePlainPizza() {
        Set<String> toppings = new HashSet<>();
        toppings.add("cheese");
        return new Pizza("plain", toppings, "red");
    }

    private Pizza makeSuperFancyPizza(String name) {
        Set<String> toppings = new HashSet<>();
        toppings.add("chicken");
        toppings.add("parmesan");
        toppings.add("cherry tomatoes");
        return new Pizza(name, toppings, "pesto");
    }

    @RequestMapping("/pizza")
    public Pizza getPizza() {
        Pizza found = repository.findById("plain").get();
        return found;
    }
}
