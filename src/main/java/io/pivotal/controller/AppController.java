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

import java.util.Optional;

import org.apache.geode.LogWriter;
import org.apache.geode.cache.client.ClientCache;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.pivotal.model.Pizza;
import io.pivotal.repository.gemfire.PizzaRepository;

@RestController
public class AppController {

    private ClientCache gemfireCache;

    private PizzaRepository pizzaRepository;

    public AppController(ClientCache gemfireCache, PizzaRepository pizzaRepository) {
        this.gemfireCache = gemfireCache;
        this.pizzaRepository = pizzaRepository;
    }

    @RequestMapping("/healthcheck")
    public ResponseEntity<Object> healthCheck() {

        LogWriter logger = gemfireCache.getLogger();

        Pizza plainPizza = makePlainPizza();
        Pizza fancyPizza = makeFancyPizza();

        this.pizzaRepository.save(plainPizza);
        this.pizzaRepository.save(fancyPizza);

        logger.info("Finished inserting the pizzas");

        Optional<Pizza> pizza = this.pizzaRepository.findById("plain");

        if (!pizza.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if (!pizza.filter(it -> it.uses(Pizza.Sauce.TOMATO)).isPresent()) {

            logger.info(String.format("I ordered tomato sauce; Pizza was [%s]",
                pizza.map(Pizza::toString).orElse(null)));

            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        if (pizza.filter(it -> it.has(Pizza.Topping.CHEESE)).isPresent()) {

            logger.info(String.format("Where's my cheese? Pizza was [%s]",
                pizza.map(Pizza::toString).orElse(null)));

            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping("/pestoOrder/{name}")
    public ResponseEntity<Object> pestoOrder(@PathVariable("name") String name) {

        this.pizzaRepository.save(makeSuperFancyPizza(name));

        return new ResponseEntity<>(HttpStatus.OK);
    }

    private Pizza makeFancyPizza() {

        return Pizza.named("fancy")
            .having(Pizza.Sauce.ALFREDO)
            .with(Pizza.Topping.ARUGULA)
            .with(Pizza.Topping.CHICKEN);
    }

    private Pizza makePlainPizza() {
        return Pizza.named("plain").with(Pizza.Topping.CHEESE);
    }

    private Pizza makeSuperFancyPizza(String name) {

        return Pizza.named(name)
            .having(Pizza.Sauce.PESTO)
            .with(Pizza.Topping.CHICKEN)
            .with(Pizza.Topping.PARMESAN)
            .with(Pizza.Topping.CHERRY_TOMATOES);
    }

    @RequestMapping("/pizza")
    public Pizza getPlainPizza() {
        return this.pizzaRepository.findById("plain").orElse(null);
    }
}
