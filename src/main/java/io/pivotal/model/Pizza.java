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
