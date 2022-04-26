package Account;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Account
 * Created by Hieu Tran Trung
 * Date 12/13/2021 - 12:44 AM
 * Description: ...
 */
public class User {
    private String username;
    private String password;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public User(String username) {
        this.username = username;
        this.password = null;
    }

    public String getUsername() {
        return username;
    }

    public static boolean addUser(String username, String password) {

        try {
            if (findUser(username) != null) { // found duplicated username
                return false;
            }
        }
        catch (IOException e) {
            // in case 'account.txt' is lost, create a new one
        }
        String file = "account.txt";
        try {
            FileOutputStream fos = new FileOutputStream(file, true);
            String data = username + "`" + password + "\n";

            fos.write(data.getBytes(StandardCharsets.UTF_8));
            fos.flush();
            fos.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static User findUser(String username) throws IOException {
        String file = "account.txt";
        String data = null;
        User foundUser = null;
        FileInputStream fis = new FileInputStream(file);
        data = new String(fis.readAllBytes(), StandardCharsets.UTF_8);
        String[] st = data.split("\n");

        for (String line : st) {
            String[] comp = line.split("`");
            if (username.equals(comp[0])) {
                foundUser = new User(username, comp[1]); break;
            }
        }

        fis.close();
        return foundUser;
    }

    public static boolean login(String username, String password) {
        User u = null;
        try {
            u = findUser(username);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return u != null && u.password.equals(password);
    }
}
