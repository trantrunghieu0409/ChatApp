package ServerSide;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class TCPServer extends Thread {
    ServerSocket serverSocket;
    ArrayList<ServerChild> userOnline = new ArrayList<>();

    public TCPServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
    }

    @Override
    public void run() {
        do {
            System.out.println("Waiting for Clients");
            Socket ss = null; //synchronous
            try {
                ss = serverSocket.accept();
                System.out.println("Talking to client " + ss + "- Port: " + ss.getPort());
                ServerChild child = new ServerChild(this, ss);
                child.start();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        while (true);
    }

    public ArrayList<ServerChild> getAllServerChild() {
        return userOnline;
    }

    public void removeUser(ServerChild child) {
        userOnline.remove(child);
    }

    public void addUser(ServerChild child) {
        userOnline.add(child);
    }

    public boolean isOnline(String username) {
        for (ServerChild child: userOnline) {
            if (child.getUsername().equals(username)) {
                return true;
            }
        }
        return false;
    }

}
