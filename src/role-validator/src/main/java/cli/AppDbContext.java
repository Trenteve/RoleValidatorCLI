package cli;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import cli.models.*;

public class AppDbContext {
    private static final String DB_URL = "jdbc:sqlite:./src/role-validator/rolevalidator.db";
    private Connection connection;
    public AppDbContext() {
        try {
            // Load SQLite JDBC driver
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC driver not found.");
            e.printStackTrace();
        }
    }
    
    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL);
        }
        return connection;
    }
    
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public void initializeDatabase() {
        try (Connection conn = getConnection();
            Statement stmt = conn.createStatement()) {
            
            String roleTableSql = "CREATE TABLE IF NOT EXISTS roles ("
                + "role_name TEXT PRIMARY KEY"
                + ");";

            String userTableSql = "CREATE TABLE IF NOT EXISTS users ("
                + " username TEXT PRIMARY KEY,"
                + " department TEXT NOT NULL,"
                + " title TEXT NOT NULL"
                + ");";

            String userRoleTableSQL = "CREATE TABLE IF NOT EXISTS user_roles ("
                + "username TEXT,"
                + "role_name TEXT,"
                + "PRIMARY KEY (username, role_name),"
                + "FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE,"
                + "FOREIGN KEY (role_name) REFERENCES roles(role_name) ON DELETE CASCADE"
                + ");";
            
            stmt.execute(roleTableSql);
            stmt.execute(userTableSql);
            stmt.execute(userRoleTableSQL);
            System.out.println("Database initialized successfully.");
        } catch (SQLException e) {
            System.err.println("Error initializing database.");
            e.printStackTrace();
        }
    }

    public void insertUser(User user) {
        String insertUserSQL = "INSERT INTO users(username, department, title) VALUES(?, ?, ?)";

        try (Connection conn = getConnection()) {
            PreparedStatement userStmt = conn.prepareStatement(insertUserSQL);

            userStmt.setString(1, user.getUsername());
            userStmt.setString(2, user.getDepartment());
            userStmt.setString(3, user.getTitle());
            userStmt.executeUpdate();
            System.out.println("User '" + user.getUsername() + "' inserted successfully.");
        } catch (SQLException e) {
            System.err.println("Failed to insert user: " + e.getMessage());
        }
    }

    public void insertRoles(User user) {
        String insertRoleSQL = "INSERT INTO roles(role_name) VALUES(?) ON CONFLICT(role_name) DO NOTHING";
        String insertUserRoleSQL = "INSERT INTO user_roles(username, role_name) VALUES(?, ?)";

        try (Connection conn = getConnection()) {
            PreparedStatement roleStmt = conn.prepareStatement(insertRoleSQL);
            PreparedStatement userRoleStmt = conn.prepareStatement(insertUserRoleSQL);

            // 2. Insert roles and the user-role relationship
            for (String roleName : user.getRoles()) {
                // Insert the role first (if it doesn't already exist)
                roleStmt.setString(1, roleName);
                roleStmt.executeUpdate();

                // Insert the relationship into the user_roles table
                userRoleStmt.setString(1, user.getUsername());
                userRoleStmt.setString(2, roleName);
                userRoleStmt.executeUpdate();
            }
            System.out.println("Roles for user '" + user.getUsername() + "' inserted successfully.");
        } catch (SQLException e) {
            System.err.println("Failed to insert user: " + e.getMessage());
        }
    }

    public List<User> getAllUsers() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users";

        try (Connection conn = getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String username = rs.getString("username");
                String department = rs.getString("department");
                String title = rs.getString("title");

                User user = new User(username, department, title, new ArrayList<>());
                users.add(user);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving flashcards.");
            e.printStackTrace();
            throw e;
        }

        return users;
    }

    public User getUser(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        String rolesSql = "SELECT * FROM user_roles WHERE username = ?";
        User user = null;
        String user_name = "";
        String department = "";
        String title = "";
        List<String> roles = new ArrayList<>();

        try (Connection conn = getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            PreparedStatement rolePstmt = conn.prepareStatement(rolesSql);
            
            pstmt.setString(1, username);
            rolePstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    user_name = rs.getString("username");
                    department = rs.getString("department");
                    title = rs.getString("title");
                }
            }
            try (ResultSet rs = rolePstmt.executeQuery()) {
                while (rs.next()) {
                    String role = rs.getString("role_name");
                    roles.add(role);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        catch (SQLException e) {
            System.err.println("Error retrieving users.");
            e.printStackTrace();
        }

        user = new User(user_name, department, title, roles);
        return user;
    }
}
