package com.edu.utfpr.client;

import com.edu.utfpr.client.components.ChatRenderComponent;
import com.edu.utfpr.client.components.ChatTabsComponent;
import com.edu.utfpr.client.components.NewGroupDialog;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.rmi.RemoteException;

public class ChatClientGUI extends JFrame {
    protected final JFrame frame;
    private final ChatClient chatClient;
    protected JButton sendButton;
    private JButton fileButton;
    private JTextField textField;

    public ChatClientGUI(ChatClient chatClient) throws RemoteException {
        this.chatClient = chatClient;

        frame = new JFrame("WhatsUT - " + chatClient.userName);

        Container c = getContentPane();
        JPanel outerPanel = new JPanel(new BorderLayout());
        JPanel leftPanel = createChatPanel();

        outerPanel.add(createChatMessagesPanel(), BorderLayout.NORTH);
        outerPanel.add(createSendMessageInput(), BorderLayout.CENTER);
        outerPanel.add(createGroupButton(), BorderLayout.SOUTH);

        c.setLayout(new BorderLayout());
        c.add(outerPanel, BorderLayout.CENTER);
        c.add(leftPanel, BorderLayout.WEST);

        frame.add(c);
        frame.pack();
        frame.setAlwaysOnTop(true);
        frame.setLocation(150, 150);
        textField.requestFocus();

        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    public JPanel createChatMessagesPanel() {
        return new ChatRenderComponent(chatClient);
    }

    public JPanel createChatPanel() throws RemoteException {
        return new ChatTabsComponent(chatClient);
    }

    public JButton createGroupButton() {
        sendButton = new JButton("+ Criar novo grupo");
        sendButton.addActionListener(e -> {
            NewGroupDialog DialogCreateGroup = new NewGroupDialog();
            DialogCreateGroup.openCreateGroupDialog(chatClient);
        });
        sendButton.setEnabled(true);

        return sendButton;
    }

    public JPanel createSendMessageInput() {
        JPanel inputPanel = new JPanel(new BorderLayout());
        textField = new JTextField();
        textField.addActionListener(e -> sendMessage(inputPanel));

        fileButton = new JButton("📎");
        fileButton.addActionListener(e -> sendFile());

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(fileButton);

        inputPanel.add(textField, BorderLayout.CENTER);
        inputPanel.add(buttonPanel, BorderLayout.EAST);

        return inputPanel;
    }

    private void sendMessage(JPanel inputPanel) {
        String message = textField.getText();
        if (!message.trim().isEmpty()) {
            if (message.equals("/help")) {
                JOptionPane.showMessageDialog(inputPanel,
                        "Comandos de usuário: \n /members -> Ver lista de membros \n /exit -> Sair do grupo  \n \n Comandos de admin: \n /invites -> exibe a lista de invites do grupo \n /accept {UserName} -> Adiciona o usuário no grupo \n /ban {UserName} -> Remove o usuário do grupo",
                        "Lista de comandos",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                try {
                    chatClient.sendMessage(message, inputPanel);
                } catch (RemoteException ex) {
                    ex.printStackTrace();
                }
            }
            textField.setText("");
        }
    }

    private void sendFile() {
        JFileChooser fileChooser = new JFileChooser();
        int returnValue = fileChooser.showOpenDialog(frame);

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();

            try {
                chatClient.sendFile(selectedFile);
                JOptionPane.showMessageDialog(frame, "Arquivo enviado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(frame, "Erro ao enviar o arquivo: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }


}
