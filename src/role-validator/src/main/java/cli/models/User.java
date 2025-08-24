package cli.models;
import java.util.ArrayList;
import java.util.List;

public class User {
    public String username;
    public String department;
    public String title;
    public List<String> roles;
    //Attributes?

    public User(
        String username, 
        String department, 
        String title
    ) {
        this.username = username;
        this.department = department;
        this.title = title;
        this.roles = new ArrayList<String>();
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

