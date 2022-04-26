package ClientSide;

import ClientSide.FileTransfer.FileBlock;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;

public class TCPClient {

    private Socket clientSocket;
    private InputStream is;
    private OutputStream os;
    private DataInputStream dis;
    private DataOutputStream dos;
    private boolean loginStatus = false;
    private boolean registerStatus = false;


    private boolean isConnected;

    public String getUsername() {
        return username;
    }

    private String username;

    private final ArrayList<UserListener> userOnline = new ArrayList<>();
    private final ArrayList<MessageListener> messageListeners = new ArrayList<>();
    private Thread t = null;

    public TCPClient() {
        int port = 3200;
        try {
            clientSocket = new Socket("localhost", port);
        } catch (IOException e) {
            isConnected = false;
        }
        if (connectServer()) {
            System.out.println("Connect successfully");
            isConnected = true;
        } else {
            System.out.println("Connect fail");
            isConnected = false;
        }
    }

    public boolean connectServer() {
        if (clientSocket != null) {
            try {
                is = clientSocket.getInputStream();
                os = clientSocket.getOutputStream();
                dis = new DataInputStream(is);
                dos = new DataOutputStream(os);

                System.out.println("Talking to Server");
                return true;
            } catch (IOException e) {
                System.out.println("There's some error");
            }
        }
        return false;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public boolean getLoginStatus() {
        return loginStatus;
    }

    public boolean getRegisterStatus() {
        return registerStatus;
    }

    public int login(String username, String password) {
        try {
            String sentMessage = "login`" + username + "`" + password;
            dos.writeBytes(sentMessage);
            dos.writeByte('\n');
            dos.flush();

            String receivedMessage = dis.readLine();
            loginStatus = receivedMessage.equals("login`ok");

            if (loginStatus) {
                this.username = username;
                startMessageThread();
            } else {
                if (receivedMessage.equalsIgnoreCase("login exists")) return 2; // user log in already
                else return 1; // login fail
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void register(String username, String password) {
        try {
            String sentMessage = "register`" + username + "`" + password;
            dos.writeBytes(sentMessage);
            dos.writeByte('\n');
            dos.flush();

            String receivedMessage = dis.readLine();
            registerStatus = receivedMessage.equals("register`ok");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void logout() {
        try {
            String sentMessage = "logout";

            dos.writeBytes(sentMessage);
            dos.writeByte('\n');
            dos.flush();

            if (t != null && t.isAlive())
                t.interrupt();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String username, String body) {
        String sentMessage = "message`" + username + "`" + body;

        try {
            dos.writeBytes(sentMessage);
            dos.writeByte('\n');
            dos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendFile(String username, FileBlock file) {
        String sentMessage = "file`" + username + "`" + file.getFileName() + "`" + file.getLength();

        try {
            dos.writeBytes(sentMessage);
            dos.writeByte('\n');
            dos.write(file.getContent());
            dos.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void startMessageThread() {
        t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    receiveMessage();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();
    }

    public void receiveMessage() throws IOException {
        while (!t.isInterrupted()) {
            String receivedMessage;
            receivedMessage = dis.readLine();
            if (receivedMessage == null) continue;
            System.out.println("Received : " + receivedMessage);
            String[] comp = receivedMessage.split("`");
            String command = comp[0];
            if (command.equalsIgnoreCase("message")) {
                if (comp.length == 3) {
                    for (MessageListener listener : messageListeners) {
                        listener.handleMessage(comp[1], comp[2]);
                    }
                }
            } else if (command.equalsIgnoreCase("online")) {
                if (comp.length == 2) {
                    for (UserListener listener : userOnline) {
                        listener.handleOnline(comp[1]);
                    }
                }
            } else if (command.equalsIgnoreCase("offline")) {
                if (comp.length == 2) {
                    for (UserListener listener : userOnline) {
                        listener.handleOffline(comp[1]);
                    }
                }
            } else if (command.equalsIgnoreCase("file")) {
                if (comp.length == 4) {
                    String fileName = comp[2];
                    int length = Integer.parseInt(comp[3]);
                    byte[] fileContent = is.readNBytes(length);
                    for (MessageListener listener : messageListeners) {
                        listener.handleFile(comp[1], new FileBlock(fileName, length, fileContent));
                    }
                }
            } else {
                System.out.println("Unknown command: " + receivedMessage);
            }
        }

        dos.close();
        dis.close();

        clientSocket.close();
    }


    public void addMessageListener(MessageListener msg) {
        messageListeners.add(msg);
    }

    public void addUserListener(UserListener user) {
        userOnline.add(user);
    }
}
