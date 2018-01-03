package io.pivotal.repository.gemfire;

import io.pivotal.model.Name;
import org.springframework.data.gemfire.repository.GemfireRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NameRepository extends GemfireRepository<Name, String> {
}
