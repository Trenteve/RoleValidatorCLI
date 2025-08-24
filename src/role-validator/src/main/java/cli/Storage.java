package cli;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import cli.models.*;

public class Storage {
    private static final String File_PATH = new File("").getAbsolutePath() + "Files/users.json";
    private static final Gson gson = new Gson();

    public static List<User> loadUsers() {
        try (FileReader reader = new FileReader(File_PATH)) {
            Type users = new TypeToken<List<User>>() {}.getType();
            return gson.fromJson(reader, users);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static void saveUsers(List<User> users) {
        try (FileWriter writer = new FileWriter(File_PATH)) {
            gson.toJson(users, writer);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
