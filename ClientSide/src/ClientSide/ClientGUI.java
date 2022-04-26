package ClientSide;

import ClientSide.FileTransfer.FileBlock;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

/**
 * ClientSide
 * Created by Hieu Tran Trung
 * Date 12/13/2021 - 1:50 AM
 * Description: ...
 */

class UserCellRenderer extends JPanel implements ListCellRenderer<UserBlock> {
    private final JLabel newMessage = new JLabel();
    private final JLabel username = new JLabel();

    public UserCellRenderer() {
        setLayout(new BorderLayout());
        add(username, BorderLayout.WEST);
        newMessage.setForeground(Color.RED);
        add(newMessage, BorderLayout.EAST);
    }

    @Override
    public Component getListCellRendererComponent(
            JList<? extends  UserBlock> list,           // the list
            UserBlock user,            // value to display
            int index,               // cell index
            boolean isSelected,      // is the cell selected
            boolean cellHasFocus)    // does the cell have focus
    {
        username.setText(user.getUsername());
        if (user.isNewMessage()) {
            newMessage.setText("New message!");
        }
        else {
            newMessage.setText("");
        }

        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }
        setEnabled(list.isEnabled());
        setFont(list.getFont());
        setOpaque(true);
        return this;
    }

}

public class ClientGUI extends JPanel {
    private JPanel MainPanel;
    private JPanel TopPanel;
    private JPanel Status;
    private JPanel leftPanel;
    private JPanel rightPanel;
    private JPanel userPanel;
    private JButton sendButton;
    private JButton attachmentButton;
    private JTextField messageTextField;
    private JLabel userLabel;
    private JLabel statusLabel;
    private JLabel currentUserLabel;
    private JList<String> messageList;
    private JList<String> logList;
    private JList<UserBlock> userOnlineList;
    private static JFrame frame;

    private static TCPClient client;
    private final HashMap<String, DefaultListModel<String>> userChats = new HashMap<>();

    ClientGUI() {
        // setup dialog list
        messageList.setFixedCellHeight(20);

        // setup user online list
        DefaultListModel<UserBlock> userOnlineModel = new DefaultListModel<>();
        userOnlineList.setModel(userOnlineModel);
        userOnlineList.setCellRenderer(new UserCellRenderer());

        // setup log of online/offline user history
        DefaultListModel<String> log = new DefaultListModel<>();
        logList.setModel(log);

        // handle other user online/offline
        client.addUserListener(new UserListener() {
            @Override
            public void handleOnline(String username) {
                userOnlineModel.addElement(new UserBlock(username));

                log.add(0, "<" + username + "> online now");
                logList.clearSelection();
                logList.setSelectedIndex(0);

                userChats.put(username, new DefaultListModel<>());
            }

            @Override
            public void handleOffline(String username) {

                log.add(0, "<" + username + "> left");
                logList.clearSelection();
                logList.setSelectedIndex(0);

                if (userLabel.getText().equals(username)) {
                    JOptionPane.showMessageDialog(rightPanel, username + " has left! :(");
                    userOnlineList.setSelectedIndex(0);
                }

                userOnlineModel.removeElement(new UserBlock(username));
                userChats.remove(username);
            }
        });

        // handle message arrived

        client.addMessageListener(new MessageListener() {
            @Override
            public void handleMessage(String username, String body) {
                userChats.get(username).addElement(username + ": " + body);

                UserBlock selectedUser = userOnlineList.getSelectedValue();
                if (selectedUser == null || !selectedUser.getUsername().equals(username)) {
                    int index = userOnlineModel.indexOf(new UserBlock(username));
                    System.out.println(index);
                    userOnlineModel.setElementAt(new UserBlock(username, true), index);
                }
            }

            @Override
            public void handleFile(String username, FileBlock file) {
                userChats.get(username).addElement(username + " sent a file");

                saveFile(username, file);
            }
        });

        // handle select user to chat
        userOnlineList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                UserBlock user = userOnlineList.getSelectedValue();

                if (user == null) {
                    userLabel.setText("None");
                    statusLabel.setText("OFFLINE");
                    statusLabel.setForeground(Color.GRAY);
                }
                else {
                    if (user.isNewMessage()) {
                        userOnlineModel.setElementAt(new UserBlock(user.getUsername()), userOnlineList.getSelectedIndex());
                    }
                    userLabel.setText(user.getUsername());
                    statusLabel.setText("ONLINE");
                    statusLabel.setForeground(Color.GREEN);
                    messageList.setModel(userChats.get(user.getUsername()));
                    messageTextField.setText("");
                }
            }
        });

        // handle send button
        sendButton.addActionListener(e -> {
            UserBlock user = userOnlineList.getSelectedValue();
            if (user != null) {
                String username = user.getUsername();

                String body = messageTextField.getText();
                if (!body.isBlank()) {
                    client.sendMessage(username, body);
                    userChats.get(username).addElement("You: " + body);
                    messageTextField.setText("");
                }
            }
        });
        attachmentButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                UserBlock user = userOnlineList.getSelectedValue();
                if (user != null)
                    onAttachment(user.getUsername());
            }
        });
    }

    public static void createAndShowGUI() {
        client = new TCPClient();

        if (!client.isConnected()) {
            JOptionPane.showMessageDialog(null, "Cannot connect to server\nMake sure to start your server first",
                    "Connect fail", JOptionPane.ERROR_MESSAGE);
            return;
        }

        frame = new JFrame();
        frame.setContentPane(new ClientGUI().MainPanel);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.pack();
        frame.setBounds(200, 100, 1200, 600);

        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                if (JOptionPane.showConfirmDialog(frame,
                        "Do you really want to exit?", "Closing Window",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){
                    client.logout();
                    System.exit(0);
                }
            }
        });

        // open login form
        JDialog account = new AccountForm(client);
        account.pack();
        account.setLocation(200, 100);
        account.setVisible(true);

        if (client.getLoginStatus()) {
            frame.setTitle("Client GUI - user: " + client.getUsername());
            frame.setVisible(true);
            // Create a Menu
            JMenu menu = new JMenu("File");

            //Create Menu Items
            JMenuItem signOut = new JMenuItem("Sign out");
            signOut.addActionListener(e -> {
                client.logout();
                frame.setVisible(false);
                frame.dispose();
                createAndShowGUI();

            });
            JMenuItem exit = new JMenuItem("Exit");
            exit.addActionListener(e -> frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING)));
            menu.add(signOut);
            menu.add(exit);

            //Create a menu bar
            JMenuBar mb = new JMenuBar();
            mb.add(menu);
            frame.setJMenuBar(mb);
        }
        else {
            client.logout();
            frame.dispose();
        }
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(ClientGUI::createAndShowGUI);
    }

    public void onAttachment(String username) {
        JDialog dialog = new JDialog(frame, "Send file to <" + username + ">");
        JTextField sourceTextField = new JTextField();
        JButton chooseFileButton = new JButton("Choose file");
        JButton sendButton = new JButton("Send");
        JButton cancelButton = new JButton("Cancel");
        JLabel label = new JLabel("You are sending a file to user <" + username +">");
        label.setFont(new Font(null, Font.ITALIC, 15));
        dialog.setLayout(new BorderLayout());

        JPanel filePanel = new JPanel();
        filePanel.setLayout(new FlowLayout());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());

        dialog.add(label, BorderLayout.NORTH);
        dialog.add(filePanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        filePanel.add(chooseFileButton);
        filePanel.add(sourceTextField);

        buttonPanel.add(sendButton);
        buttonPanel.add(cancelButton);

        sourceTextField.setEditable(false);
        sourceTextField.setPreferredSize(new Dimension(200, 25));

        final File[] file = {null};
        chooseFileButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser("/");
            fileChooser.setDialogTitle("Choose a file");
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            int value = fileChooser.showOpenDialog(null);
            if (value == JFileChooser.APPROVE_OPTION) {
                file[0] = fileChooser.getSelectedFile();
                sourceTextField.setText(file[0].getName());
            }
        });
        cancelButton.addActionListener(e -> dialog.dispose());

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (file[0] == null) {
                    sourceTextField.setText("Please choose a file first!");
                }
                else {
                    try {
                        String fileName = file[0].getName();

                        FileInputStream fis = new FileInputStream(file[0].getAbsolutePath());
                        byte[] fileContent = fis.readAllBytes();
                        fis.close();

                        client.sendFile(username, new FileBlock(fileName, fileContent.length, fileContent));
                        dialog.dispose();

                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(dialog, "Unable to upload file",
                                "Upload fail", JOptionPane.WARNING_MESSAGE);
                    }
                }
            }
        });

        dialog.pack();
        dialog.setLocationRelativeTo(MainPanel);
        dialog.setVisible(true);
    }

    public void saveFile(String username, FileBlock fileReceive) {
        JDialog dialog = new JDialog(frame, "Send file to <" + username + ">");
        JButton yesButton = new JButton("Yes");
        JButton noButton = new JButton("No");
        JLabel label = new JLabel("User <" + username +"> sent you a file" );
        label.setAlignmentX(CENTER_ALIGNMENT);
        label.setFont(new Font(null, Font.ITALIC, 15));
        JLabel fileNameLabel = new JLabel(fileReceive.getFileName());
        fileNameLabel.setAlignmentX(CENTER_ALIGNMENT);
        JLabel label1 = new JLabel("Do you want to keep this file?");
        label1.setAlignmentX(CENTER_ALIGNMENT);

        JPanel labelPanel = new JPanel();
        labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.Y_AXIS));
        labelPanel.add(label);
        labelPanel.add(fileNameLabel);
        labelPanel.add(label1);


        dialog.setLayout(new BorderLayout());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());

        dialog.add(labelPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        buttonPanel.add(yesButton);
        buttonPanel.add(noButton);

        noButton.addActionListener(e -> dialog.dispose());

        final File[] folder = {null};
        yesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser("/");
                fileChooser.setDialogTitle("Choose a folder to save file");
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

                int value = fileChooser.showSaveDialog(null);
                if (value == JFileChooser.APPROVE_OPTION) {
                    folder[0] = fileChooser.getSelectedFile();
                    String directory = folder[0].getAbsolutePath();
                    if (directory.charAt(directory.length()-1) != '\\') directory += "\\";

                    String savePath = directory + fileReceive.getFileName();

                    try {
                        FileOutputStream fos = new FileOutputStream(savePath);
                        fos.write(fileReceive.getContent());
                        fos.flush();

                        fos.close();

                        JOptionPane.showMessageDialog(dialog, "Save file as (" + savePath +  ") successfully!");
                        dialog.dispose();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        dialog.pack();
        dialog.setLocationRelativeTo(MainPanel);
        dialog.setVisible(true);
    }
}

