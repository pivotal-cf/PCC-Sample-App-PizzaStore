package io.pivotal.repository.gemfire;

import io.pivotal.model.Pizza;
import org.springframework.data.gemfire.repository.GemfireRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PizzaRepository extends GemfireRepository<Pizza, String> {
}
