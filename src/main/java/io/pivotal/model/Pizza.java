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

package io.pivotal.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.gemfire.mapping.annotation.Region;

import java.io.Serializable;
import java.util.Set;

@Region("Pizza")
public class Pizza implements Serializable {
    @Id
    String name;
    Set<String> toppings;
    String sauce;

    public Pizza(String name, Set toppings, String sauce) {
        this.name = name;
        this.toppings = toppings;
        this.sauce = sauce;
    }

    public Pizza() {
    }

    @Override
    public String toString() {
        return "Pizza{" +
                "name='" + name + '\'' +
                ", toppings=" + toppings +
                ", sauce='" + sauce + '\'' +
                '}';
    }

    public String getName() {
        return name;
    }

    public Set<String> getToppings() {
        return toppings;
    }

    public String getSauce() {
        return sauce;
    }
}
