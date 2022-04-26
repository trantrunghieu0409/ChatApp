package ServerSide;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

/**
 * ServerSide
 * Created by Hieu Tran Trung
 * Date 12/16/2021 - 1:39 PM
 * Description: ...
 */
public class ServerGUI {
    private JButton STARTButton;
    private JPanel panel1;
    private JButton STOPButton;
    private TCPServer server;

    public ServerGUI() {
        STARTButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    server = new TCPServer(3200);
                    server.start();

                    STOPButton.setEnabled(true);
                    STARTButton.setEnabled(false);

                } catch (IOException event) {
                    JOptionPane.showMessageDialog(null, "Cannot connect to port 3200",
                            "Connect fail", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        STOPButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (server != null) {
                    server.stop();
                    System.exit(0);
                }
            }
        });
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("ServerGUI");
        frame.setContentPane(new ServerGUI().panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
