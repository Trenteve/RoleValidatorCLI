package cli.models;
import java.util.List;

public class User {
    private String username;
    private String department;
    private String title;
    private List<String> roles;

    public User(
        String username, 
        String department, 
        String title,
        List<String> roles
    ) {
        this.username = username;
        this.department = department;
        this.title = title;
        this.roles = roles;
    }

    public String getUsername() {
        return username;
    }

    public String getDepartment() {
        return department;
    }

    public String getTitle() {
        return title;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void addRole(String role) {
        roles.add(role);
    }
}

