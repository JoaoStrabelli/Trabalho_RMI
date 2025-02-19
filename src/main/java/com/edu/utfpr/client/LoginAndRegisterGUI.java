package com.edu.utfpr.client;

import com.edu.utfpr.core.exceptions.InvalidUserOrPasswordException;
import com.edu.utfpr.core.exceptions.UserAlreadyRegisteredException;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;

public class LoginAndRegisterGUI extends JFrame {
    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private ChatClient chatClient;

    public LoginAndRegisterGUI() {
        setTitle("WhatsUT - Login");
        setSize(280, 280);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(5, 10, 5, 10); // Margens entre componentes

        // Campo Usuário
        constraints.gridx = 0;
        constraints.gridy = 0;
        panel.add(new JLabel("Usuário:"), constraints);

        usernameField = new JTextField();
        constraints.gridy = 1;
        panel.add(usernameField, constraints);

        // Campo Senha
        constraints.gridy = 2;
        panel.add(new JLabel("Senha:"), constraints);

        passwordField = new JPasswordField();
        constraints.gridy = 3;
        panel.add(passwordField, constraints);

        // Botão Login
        JButton loginButton = new JButton("Entrar");
        loginButton.addActionListener(e -> loginButtonActionPerformed());
        constraints.gridy = 4;
        panel.add(loginButton, constraints);

        // Botão Registro
        JButton registerButton = new JButton("Registrar");
        registerButton.addActionListener(e -> registerButtonActionPerformed());
        constraints.gridy = 5;
        panel.add(registerButton, constraints);

        add(panel);
    }

    public static void main(String[] args) throws UnsupportedLookAndFeelException {
        FlatLightLaf.setup();
        UIManager.setLookAndFeel(new FlatIntelliJLaf());
        new LoginAndRegisterGUI().setVisible(true);
    }

    private void loginButtonActionPerformed() {
        try {
            chatClient = new ChatClient(usernameField.getText());
            chatClient.login(usernameField.getText(), new String(passwordField.getPassword()));

            new ChatClientGUI(chatClient);
            dispose();
        } catch (InvalidUserOrPasswordException ex) {
            JOptionPane.showMessageDialog(this, "Usuário ou senha inválidos", "Erro", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao autenticar usuário", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void registerButtonActionPerformed() {
        String userName = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (!userName.isEmpty() && !password.isEmpty()) {
            try {
                chatClient = new ChatClient(userName);
                chatClient.register(userName, password);
                JOptionPane.showMessageDialog(this, "Usuário registrado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            } catch (UserAlreadyRegisteredException ex) {
                JOptionPane.showMessageDialog(this, "Usuário já registrado.", "Erro", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro ao registrar usuário.", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Preencha os campos de usuário e senha corretamente.", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}
