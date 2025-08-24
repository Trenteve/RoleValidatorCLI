package cli.models;

import java.util.List;

public class Resource {
    public String name;
    public List<String> requiredRoles;

    public Resource(String name, List<String> roles) {
        this.name = name;
        this.requiredRoles = roles;
    }

    public List<String> getRequiredRoles() {
        return requiredRoles;
    }
}
