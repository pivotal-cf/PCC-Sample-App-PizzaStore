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

package io.pivotal.continuousQuery;

import java.util.Optional;

import org.apache.geode.cache.query.CqEvent;
import org.springframework.data.gemfire.listener.annotation.ContinuousQuery;
import org.springframework.stereotype.Component;

import io.pivotal.model.Name;
import io.pivotal.model.Pizza;
import io.pivotal.repository.gemfire.NameRepository;

@Component
public class PizzaQuery {

    private final NameRepository nameRepository;

    public PizzaQuery(NameRepository nameRepository) {
        this.nameRepository = nameRepository;
    }

    @ContinuousQuery(name = "PestoQuery", durable = true,
        query = "SELECT * FROM /Pizza WHERE sauce = 'PESTO'")
    public void handlePestoPizzaOrder(CqEvent event) {

        Optional.ofNullable(event)
            .map(CqEvent::getNewValue)
            .map(newValue -> ((Pizza) newValue).getName())
            .map(Name::of)
            .ifPresent(this.nameRepository::save);
    }
}

