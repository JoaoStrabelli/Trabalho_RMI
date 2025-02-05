package com.edu.utfpr.client;

import com.edu.utfpr.core.exceptions.InvalidUserOrPasswordException;
import com.edu.utfpr.core.exceptions.UserAlreadyRegisteredException;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

public class LoginAndRegisterGUI extends JFrame {
    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private ChatClient chatClient;

    public LoginAndRegisterGUI() {
        setTitle("Conversation - Authentication");
        setSize(300, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;

        ImageIcon logoIcon = new ImageIcon(Objects.requireNonNull(getClass().getResource("/logo.png")));
        Image image = logoIcon.getImage();
        Image resizedImage = image.getScaledInstance(image.getWidth(null) / 2, image.getHeight(null) / 2,
                java.awt.Image.SCALE_SMOOTH);
        logoIcon = new ImageIcon(resizedImage);
        JLabel logoLabel = new JLabel();
        logoLabel.setIcon(logoIcon);
        constraints.gridwidth = 2;
        constraints.gridx = 0;
        constraints.gridy = 0;
        panel.add(logoLabel, constraints);

        constraints.gridwidth = 2;
        constraints.gridy = 1;
        panel.add(new JLabel("User:"), constraints);

        usernameField = new JTextField();
        constraints.gridx = 0;
        constraints.gridy = 2;
        panel.add(usernameField, constraints);

        constraints.gridy = 3;
        constraints.gridwidth = 2;
        panel.add(Box.createRigidArea(new Dimension(0, 8)), constraints);

        constraints.gridx = 0;
        constraints.gridy = 4;
        panel.add(new JLabel("Password:"), constraints);

        passwordField = new JPasswordField();
        constraints.gridy = 5;
        constraints.gridx = 0;
        panel.add(passwordField, constraints);

        constraints.gridy = 6;
        constraints.gridwidth = 2;
        panel.add(Box.createRigidArea(new Dimension(0, 16)), constraints);

        JButton loginButton = new JButton("Enter");
        loginButton.addActionListener(e1 -> loginButtonActionPerformed());
        constraints.gridy = 7;
        constraints.gridx = 0;
        panel.add(loginButton, constraints);

        constraints.gridy = 8;
        constraints.gridwidth = 2;
        panel.add(Box.createRigidArea(new Dimension(0, 4)), constraints);

        JButton registerButton = new JButton("Register");
        registerButton.addActionListener(e -> registerButtonActionPerformed());
        constraints.gridy = 9;
        constraints.gridx = 0;
        panel.add(registerButton, constraints);

        add(panel);
    }

    public static void main(String[] args) throws UnsupportedLookAndFeelException {
        FlatLightLaf.setup();

        UIManager.setLookAndFeel(new FlatMacDarkLaf());

        LoginAndRegisterGUI loginAndRegisterGUI = new LoginAndRegisterGUI();
        loginAndRegisterGUI.setVisible(true);
    }

    private void loginButtonActionPerformed() {
        try {
            chatClient = new ChatClient(usernameField.getText());
            chatClient.login(usernameField.getText(), new String(passwordField.getPassword()));

            new ChatClientGUI(chatClient);
            dispose();
        } catch (InvalidUserOrPasswordException ex) {
            JOptionPane.showMessageDialog(this, "User or password invalid", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error authenticating user", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void registerButtonActionPerformed() {
        String userName = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        password = password.trim();
        if (!userName.isEmpty() && !password.isEmpty()) {
            try {
                chatClient = new ChatClient(usernameField.getText());
                chatClient.register(usernameField.getText(), new String(passwordField.getPassword()));
                JOptionPane.showMessageDialog(this, "User registered successfully", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (UserAlreadyRegisteredException ex) {
                JOptionPane.showMessageDialog(this, "Already registered user", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error registering user", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Fill in the username and password fields correctly", "Error",
                    JOptionPane.ERROR_MESSAGE);
        }

    }
}
