package com.edu.utfpr.client;

import com.edu.utfpr.client.components.ChatRenderComponent;
import com.edu.utfpr.client.components.ChatTabsComponent;
import com.edu.utfpr.client.components.NewGroupDialog;

import javax.swing.*;
import java.awt.*;
import java.rmi.RemoteException;

public class ChatClientGUI extends JFrame {
    protected final JFrame frame;
    private final ChatClient chatClient;
    protected JButton sendButton;
    private JTextField textField;

    public ChatClientGUI(ChatClient chatClient) throws RemoteException {
        this.chatClient = chatClient;

        frame = new JFrame("WhatsUT - " + chatClient.userName);

        Container c = getContentPane();
        JPanel outerPanel = new JPanel(new BorderLayout());
        JPanel leftPanel = createChatPanel();

        outerPanel.add(createChatMessagesPanel(), BorderLayout.NORTH);
        outerPanel.add(createSendMessageInput(), BorderLayout.CENTER);

        c.setLayout(new BorderLayout());
        c.add(outerPanel, BorderLayout.CENTER);
        c.add(leftPanel, BorderLayout.WEST);
        outerPanel.add(createGroupButton(), BorderLayout.SOUTH);

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
        textField.addActionListener(e -> {
            String message = textField.getText();
            if (!message.trim().isEmpty()) {
                if (message.equals("/help")) {
                    JOptionPane.showMessageDialog(inputPanel,
                            "Comandos de usuário: \n /members -> Ver lista de membros \n /exit -> Sair do grupo  \n \n Comandos de admin: \n /invites -> exibe a lista de invites do grupo \n /accept {UserName} -> Adiciona o usuário no grupo \n /ban {UserName} -> Remove o usuário do grupo",
                            "Lista de comandos",
                            JOptionPane.INFORMATION_MESSAGE);
                    textField.setText("");
                } else {
                    try {
                        chatClient.sendMessage(message, inputPanel);
                        textField.setText("");
                    } catch (RemoteException ex) {
                        ex.printStackTrace();
                        textField.setText("");
                    }

                }

            }
        });
        inputPanel.add(textField);
        return inputPanel;
    }
}
