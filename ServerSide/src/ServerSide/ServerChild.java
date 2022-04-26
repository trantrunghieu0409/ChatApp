package ServerSide;

import Account.User;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * ServerSide
 * Created by Hieu Tran Trung
 * Date 12/14/2021 - 12:23 AM
 * Description: ...
 */
public class ServerChild extends Thread {
    private String username = null;
    private final Socket serverSocket;
    private InputStream is;
    private OutputStream os;
    private DataOutputStream dos;
    private DataInputStream dis;
    private final TCPServer server;

    public ServerChild(TCPServer server, Socket serverSocket) {
        this.serverSocket = serverSocket;
        this.server = server;

        try {
            is = serverSocket.getInputStream();
            os = serverSocket.getOutputStream();

            dis = new DataInputStream(is);
            dos = new DataOutputStream(os);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            handleClient();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (username != null)
                handleLogout();

            dos.close();
            dis.close();
            serverSocket.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void handleClient() throws IOException {
        String receivedMessage;
        do {
            receivedMessage = dis.readLine();
            System.out.println("Received : " + receivedMessage);
            if (receivedMessage == null)
                continue;
            String[] comp = receivedMessage.split("`");
            String command = comp[0];
            if (command.equalsIgnoreCase("login")) {
                if (comp.length == 3)
                    handleLogin(comp[1], comp[2]);

            } else if (command.equalsIgnoreCase("register")) {
                if (comp.length == 3) {
                    handleRegister(comp[1], comp[2]);

                }

            } else if (command.equalsIgnoreCase("message")) {
                if (comp.length == 3)
                    handleMessage(comp[1], comp[2]);

            }
            else if (command.equalsIgnoreCase("file")) {
                if (comp.length == 4) {
                    String username = comp[1];
                    String fileName = comp[2];
                    int length = Integer.parseInt(comp[3]);
                    byte[] data = new byte[length];
                    dis.readFully(data, 0, length);
                    handleFile(username, fileName, length, data);

                }
            }
            else if (command.equalsIgnoreCase("logout")) {
                break;
            } else {
                dos.writeBytes("unknown " + receivedMessage);
                dos.writeByte('\n');
                dos.flush();
            }
        }
        while (true);
    }

    private void handleLogout() throws IOException {
        notifyOtherUser("offline");
        server.removeUser(this);
        System.out.println(username + " left");
    }

    public void handleLogin(String username, String password) throws IOException {
        String response;

        if (User.login(username, password)) {
            if (server.isOnline(username)) {
                response = "login exists";

                dos.writeBytes(response);
                dos.writeByte('\n');
                dos.flush();
            } else {
                response = "login`ok";

                dos.writeBytes(response);
                dos.writeByte('\n');
                dos.flush();
                this.username = username;
                this.getOnlineList();
                this.notifyOtherUser("online");

                server.addUser(this);
            }

        } else {
            response = "login`fail";

            dos.writeBytes(response);
            dos.writeByte('\n');
            dos.flush();
        }
    }

    public void handleRegister(String username, String password) throws IOException {
        String response;
        if (User.addUser(username, password)) {
            response = "register`ok";
        } else {
            response = "register`fail";
        }
        dos.writeBytes(response);
        dos.writeByte('\n');
        dos.flush();
    }

    public void handleMessage(String username, String message) throws IOException {
        ArrayList<ServerChild> userOnline = server.getAllServerChild();

        for (ServerChild user : userOnline) {
            if (user.getUsername() != null && user.getUsername().equals(username)) {
                user.receiveMessage(this.username, message);
                break;
            }
        }
    }

    private void handleFile(String username, String fileName, int length, byte[] data) throws IOException {
        ArrayList<ServerChild> userOnline = server.getAllServerChild();

        for (ServerChild user : userOnline) {
            if (user.getUsername() != null && user.getUsername().equals(username)) {
                user.receiveFile(this.username, fileName, length, data);
                break;
            }
        }
    }

    private void receiveFile(String username, String fileName, int length, byte[] data) throws IOException{
        String response = "file`" + username + "`" + fileName + "`" + length;

        dos.writeBytes(response);
        dos.writeByte('\n');
        dos.write(data);
        dos.flush();
    }

    public void receiveMessage(String username, String message) throws IOException {
        String response = "message`" + username + "`" + message;

        dos.writeBytes(response);
        dos.writeByte('\n');
        dos.flush();
    }

    public String getUsername() {
        return username;
    }

    public void addOnlineList(String username) throws IOException {
        String response = "online`" + username;
        dos.writeBytes(response);
        dos.writeByte('\n');
        dos.flush();
    }

    public void removeOnlineList(String username) throws IOException {
        String response = "offline`" + username;
        dos.writeBytes(response);
        dos.writeByte('\n');
        dos.flush();
    }

    private void getOnlineList() throws IOException {
        ArrayList<ServerChild> userOnline = server.getAllServerChild();

        for (ServerChild user : userOnline) {
            if (user.getUsername() == null || user.getUsername().equals(username)) {
                continue;
            }
            this.addOnlineList(user.getUsername());
        }
    }

    private void notifyOtherUser(String status) throws IOException {
        ArrayList<ServerChild> userOnline = server.getAllServerChild();

        for (ServerChild user : userOnline) {
            if (user.getUsername().equals(username)) {
                continue;
            }

            if (status.equals("online")) {
                user.addOnlineList(username);
            } else {
                user.removeOnlineList(username);
            }
        }
    }

}
