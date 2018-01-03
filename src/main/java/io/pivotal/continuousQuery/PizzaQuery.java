package io.pivotal.continuousQuery;

import io.pivotal.model.Name;
import io.pivotal.model.Pizza;
import io.pivotal.repository.gemfire.NameRepository;
import org.apache.geode.cache.query.CqEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.gemfire.listener.annotation.ContinuousQuery;
import org.springframework.stereotype.Component;

@Component
public class PizzaQuery {

    private NameRepository repository;

    @Autowired
    public PizzaQuery(NameRepository respository) {
        this.repository = respository;
    }

    @ContinuousQuery(name = "PestoQuery", query = "SELECT * FROM /Pizza WHERE sauce = 'pesto'", durable = true)
    public void handlePizzaChanges(CqEvent event) {

        repository.save( new Name(((Pizza) event.getNewValue()).getName()));
    }
}

