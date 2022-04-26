package ClientSide;

import javax.swing.*;
import java.awt.event.*;

public class AccountForm extends JDialog {
    private JPanel contentPane;
    private JButton registerButton;
    private JButton cancelButton;
    private JTextField userTextField;
    private JPasswordField passwordField;
    private JPasswordField repasswordField;
    private JLabel loginLabel;
    private JTabbedPane mainTabbedPane;
    private JButton loginButton;
    private JButton cancelLoginButton;
    private JTextField userLoginTextField;
    private JPasswordField passwordLoginField;
    private final TCPClient client;

    public AccountForm(TCPClient ss) {
        super(null, java.awt.Dialog.ModalityType.TOOLKIT_MODAL);
        client = ss;
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(registerButton);

        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });
        cancelLoginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String user = userTextField.getText();
                String pass = String.valueOf(passwordField.getPassword());
                String repass = String.valueOf(repasswordField.getPassword());

                if (user.isBlank() || pass.isBlank()) {
                    JOptionPane.showMessageDialog(contentPane, "Username/Password should not be blank",
                            "Type Error", JOptionPane.ERROR_MESSAGE);
                }
                else {
                    if (!repass.equals(pass)) {
                        JOptionPane.showMessageDialog(contentPane, "Retype password does not match password",
                                "Password Error", JOptionPane.ERROR_MESSAGE);
                    }
                    else {
                        client.register(user, pass);
                        if (client.getRegisterStatus()) {
                            JOptionPane.showMessageDialog(contentPane, "Register successfully");
                        }
                        else {
                            JOptionPane.showMessageDialog(contentPane, "This username does exist\nPlease choose another username",
                                    "Invalid username", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            }
        });
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String user = userLoginTextField.getText();
                String pass = String.valueOf(passwordLoginField.getPassword());

                if (user.isBlank() || pass.isBlank()) {
                    JOptionPane.showMessageDialog(contentPane, "Username/Password should not be blank",
                            "Type Error", JOptionPane.ERROR_MESSAGE);
                }
                else {
                    int status = client.login(user, pass);
                    if (client.getLoginStatus()) {
                        JOptionPane.showMessageDialog(contentPane, "Login successfully");
                        dispose();
                    }
                    else {
                        if (status == 1) {
                            JOptionPane.showMessageDialog(contentPane, "Wrong username/password",
                                    "Login fail", JOptionPane.ERROR_MESSAGE);
                        }
                        else if (status == 2) {
                            JOptionPane.showMessageDialog(contentPane, "This user has already logged in",
                                    "Login fail", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            }
        });
    }

    private void onCancel() {
        // add your code here if necessary
        client.logout();
        dispose();
    }

}
