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

package io.pivotal.cloudcache.app.cq;

import io.pivotal.cloudcache.app.repository.NameRepository;
import org.springframework.stereotype.Component;

/**
 * This class registers a continues query. When server side receives data satisfying the query a event is
 * pushed to the client application (this application).
 */
@Component
@SuppressWarnings("unused")
public class PizzaQueries {

    private final NameRepository nameRepository;

    public PizzaQueries(NameRepository nameRepository) {
        this.nameRepository = nameRepository;
    }

//    @ContinuousQuery(name = "AllPizzaOrder", query="SELECT * FROM /Pizza")
//    public void handleAnyPizzaOrder(CqEvent event) {
//        System.err.printf("PIZZA [%s]%n", event.getNewValue());
//    }
//
//    /**
//     * The handler defined in this method is executed when data satisfying the query reaches the server.
//     * @param event
//     */
//    @ContinuousQuery(name = "PestoPizzaOrdersQuery", durable = true,
//        query = "SELECT * FROM /Pizza p WHERE p.sauce.name = 'PESTO'")
//    public void handlePestoPizzaOrder(CqEvent event) {
//
//        Optional.ofNullable(event)
//            .map(CqEvent::getNewValue)
//            .filter(newValue -> newValue instanceof Pizza)
//            .map(newValue -> (Pizza) newValue)
//            .map(Pizza::getName)
//            .map(pizzaName -> {
//                System.err.printf("Pesto Pizza [%s] Ordered%n", pizzaName);
//                return pizzaName;
//            })
//            .map(Name::of)
//            .ifPresent(this.nameRepository::save);
//    }
}
