package cli;
import java.util.*;
import bsh.Interpreter;
import cli.models.*;
import java.io.File;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        Interpreter interpreter = new Interpreter();
        Scanner scanner = new Scanner(System.in);
        AppDbContext dbContext = new AppDbContext();
        dbContext.initializeDatabase();
        Map<String, Resource> allResources = createMap();

        printMenu();
        System.out.print("Please enter a choice: ");     
        while (scanner.hasNext()) {       
            String selection = scanner.next();
            if (selection.equals("7")) {
                scanner.close();
                return;
            }
            
            switch (selection) {
                case "1":
                    System.out.println("Printing Users:");
                    try {
                        for (User user : dbContext.getAllUsers()) {
                            System.out.println(user.getUsername());
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    break;
                case "2":
                    System.out.println("Printing Resources:");
                    for (String resource : allResources.keySet()) {
                        System.out.println(resource);
                    }
                    break;
                case "3":
                    checkUserAccess(scanner, interpreter, allResources, dbContext);
                    break;
                case "4":
                    addUser(scanner, interpreter, dbContext);
                    break;
                case "5":
                    addRoleToUser(scanner, interpreter, dbContext);
                    break;
                case "6":
                    runAccessCertification(scanner, interpreter, dbContext);
                    break;
                default:
                    break;
            }
            System.out.print("\nPlease enter a choice: ");
        }
        scanner.close();
    }

    private static void runAccessCertification(
        Scanner scanner, 
        Interpreter interpreter, 
        AppDbContext dbContext
    ) {
        Map<String, List<String>> approvedRoles = new HashMap<>();
        Map<String, List<String>> revokedRoles = new HashMap<>();

        try {
            List<User> users = dbContext.getAllUsers();
            System.out.println("Running Access Certification on All Users:");
            for (User user : users) {
                System.out.printf("\nUser: %s\n", user.getUsername());
                List<String> roles = new ArrayList<>();
                List<String> removedRoles = new ArrayList<>();

                for (String role : user.getRoles()) {
                    System.out.printf("Assigned Role: %s\n", role);
                    while (true) {
                        System.out.print("Type Approve or Revoke: ");
                        String choice = scanner.next();
                        if(choice.equalsIgnoreCase("Approve")) {
                            System.out.printf("Approved User %s with Role %s.\n", user.getUsername(), role);
                            roles.add(role);
                            break;
                        }
                        else if (choice.equalsIgnoreCase("Revoke")) {
                            dbContext.removeRoleFromUser(user, role);
                            removedRoles.add(role);
                            break;
                        }
                        else {
                            System.out.println("Wrong Input.");
                        }
                    }
                }
                approvedRoles.put(user.getUsername(), roles);
                revokedRoles.put(user.getUsername(), removedRoles);
                checkPolicies(interpreter, user);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        printSummary(approvedRoles, revokedRoles);
    }

    private static void checkPolicies(Interpreter interpreter, User user) {
        String path = new File("src/role-validator/src/main/java/cli").getAbsolutePath() + "/scripts/checkPolicy.bsh";
        try {
            interpreter.set("user", user);
            List<String> violations = (List<String>) interpreter.source(path);
            for (String violation : violations) {
                System.out.println(violation);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Map<String, Resource> createMap() {
        Map<String, Resource> map = new HashMap<String, Resource>();
        map.put("ERPSoftware", new Resource("ERPSoftware", List.of("DeliveryEngineer", "DeliveryManager")));
        map.put("ERPDatabase", new Resource("ERPDatabase", List.of("ITAdmin")));
        map.put("ERPMonitoring", new Resource("ERPMonitoring", List.of("ITAdmin", "ITSupport")));

        return map;
    }

    private static void printMenu() {
        System.out.println("Welcome to Role Validator CLI\n"
                           + "1. View all users\n"
                           + "2. View all resources\n"
                           + "3. Check access for a user\n"
                           + "4. Add a new user\n"
                           + "5. Add a new role to a user\n"
                           + "6. Run Access Certification\n"
                           + "7. Exit\n");
    }

    private static void printSummary(
        Map<String, List<String>> approvedRoles, 
        Map<String, List<String>> revokedRoles
    ) {
        System.out.println("\nSummary Report of All Changes%s:\n"
                        + "------------------------------");
        
        for (String user : approvedRoles.keySet()) {
            System.out.printf("Approved Roles for User %s:\n", user);
            for (String approvedRole : approvedRoles.get(user)) {
                System.out.println(approvedRole);
            }
        }
        for (String user : revokedRoles.keySet()) {
            System.out.printf("Revoked Roles for User %s:\n", user);
            for (String revokedRole : revokedRoles.get(user)) {
                System.out.println(revokedRole);
            }
        }
    }

    private static void addUser(
        Scanner scanner, 
        Interpreter interpreter, 
        AppDbContext dbContext
    ) {
        String path = new File("src/role-validator/src/main/java/cli").getAbsolutePath() + "/scripts/assignRoles.bsh";
        System.out.print("Please enter the username of the User: ");
        String username = scanner.next();

        System.out.print("Enter the department: ");
        String department = scanner.next();

        System.out.print("Enter title: ");
        String title = scanner.next();

        User user = new User(username, department, title, new ArrayList<>());
        dbContext.insertUser(user);
        try {
            interpreter.set("user", dbContext.getUser(username));
            User userWithRoles = (User) interpreter.source(path);
            dbContext.insertRoles(userWithRoles);
            checkPolicies(interpreter, userWithRoles);
        } catch (Exception e) {
            System.err.printf("Error assigning roles to user, %s\n", e.getMessage());
            e.printStackTrace();
        }
    }

    private static void checkUserAccess(
        Scanner scanner, 
        Interpreter interpreter, 
        Map<String,Resource> allResources,
        AppDbContext dbContext
    ) {
        String path = new File("src/role-validator/src/main/java/cli").getAbsolutePath() + "/scripts/checkAccess.bsh";
        System.out.print("Please enter the username of the User: ");
        String username = scanner.next();
        System.out.print("Please enter the resource: ");
        String resource = scanner.next();

        try {
            interpreter.set("user", dbContext.getUser(username));
            interpreter.set("resource", allResources.get(resource));
            Boolean hasAccess = (Boolean) interpreter.source(path);
            if (hasAccess) {
                System.out.printf("User %s has access to resource %s :)\n", username, resource);
            } 
            else {
                System.out.printf("User %s does not have access to resource %s :(\n", username, resource);
            }
        } catch (Exception e) {
            System.err.printf("Error checking user access, %s\n", e.getMessage());
            e.printStackTrace();
        }
    }

    private static void addRoleToUser(
        Scanner scanner, 
        Interpreter interpreter, 
        AppDbContext dbContext
    ) {
        System.out.print("Please enter the username of the User: ");
        String username = scanner.next();
        System.out.print("Please enter the role name: ");
        String role = scanner.next();

        try {
            User user = dbContext.getUser(username);
            user.addRole(role);
            dbContext.insertRoles(user);
            checkPolicies(interpreter, user);
        } catch (Exception e) {
            System.err.printf("Error assigning roles to user, %s\n", e.getMessage());
            e.printStackTrace();
        }

        System.out.printf("Provisioned User %s with Role %s.\n", username, role);
    }
}
