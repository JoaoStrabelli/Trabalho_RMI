package com.edu.utfpr.client.components;

import com.edu.utfpr.client.ChatClient;

import javax.swing.*;
import java.awt.*;

public class ChatRenderComponent extends JPanel {
    private JTextArea textArea;

    public ChatRenderComponent(ChatClient chatClient) {
        initComponent();

        chatClient.addChangeCurrentChatListener(chat -> {
            textArea.setText("");
            if (chat != null) {
                chat.getMessages().forEach(
                        message -> textArea.append(message.getSender().getName() + ": " + message.getContent() + "\n"));
            }
        });

        chatClient.addOnReceiveMessageListener(
                message -> textArea.append(message.getSender().getName() + ": " + message.getContent() + "\n"));
    }

    private void initComponent() {
        String welcome = "Welcome to Conversation, select a chat and start a conversation...\n";
        textArea = new JTextArea(welcome, 14, 50);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        JPanel textPanel = new JPanel(new BorderLayout());
        textPanel.add(scrollPane, BorderLayout.CENTER);

        add(textPanel);
    }

}
