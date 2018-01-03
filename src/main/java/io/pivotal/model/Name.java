package io.pivotal.model;

        import org.springframework.data.annotation.Id;
        import org.springframework.data.gemfire.mapping.annotation.Region;

        import java.io.Serializable;
        import java.util.Set;

@Region("Name")
public class Name implements Serializable {
    @Id
    String name;

    public Name(String name) {
        this.name = name;
    }

    public Name() {
    }

    public String getName() {
        return name;
    }

}