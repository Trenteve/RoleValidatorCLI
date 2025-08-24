package cli;
import java.util.*;
import bsh.Interpreter;
import cli.models.*;
import java.io.File;
import java.io.FileReader;
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
            if (selection.equals("5")) {
                scanner.close();
                return;
            }
            
            switch (selection) {
                case "1":
                    System.out.println("Printing Users:");
                    try {
                        for (User user : dbContext.getAllUsers()) {
                            System.out.println(user.username);
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
                default:
                    break;
            }
            System.out.print("\nPlease enter a choice: ");
        }
        scanner.close();
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
                           + "5. Exit\n");
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
            User userWithRoles = (User) interpreter.eval(new FileReader(path));
            dbContext.insertRoles(userWithRoles);
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
            Boolean hasAccess = (Boolean) interpreter.eval(new FileReader(path));
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
}
