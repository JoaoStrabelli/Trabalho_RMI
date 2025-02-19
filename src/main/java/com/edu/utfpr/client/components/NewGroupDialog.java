package com.edu.utfpr.client.components;

import com.edu.utfpr.client.ChatClient;

import javax.swing.*;
import java.awt.*;
import java.rmi.RemoteException;

public class NewGroupDialog {

    public void openCreateGroupDialog(ChatClient chatClient) {

        JDialog dialog = new JDialog((Frame) null, "Criar novo grupo", true);
        dialog.setSize(500, 200);
        dialog.setLayout(new BorderLayout());
        dialog.setAlwaysOnTop(true);

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridLayout(5, 2));

        JLabel nameLabel = new JLabel("Nome do grupo:");
        JTextField nameField = new JTextField();

        JRadioButton radio1 = new JRadioButton("Quando administrador sair, escolher novo administrador para o grupo");

        JRadioButton radio2 = new JRadioButton("Deletar o grupo ao administrador sair!");
        radio2.setSelected(true);

        ButtonGroup group = new ButtonGroup();
        group.add(radio1);
        group.add(radio2);

        formPanel.add(nameLabel);
        formPanel.add(nameField);

        formPanel.add(radio1);

        formPanel.add(radio2);

        dialog.add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton createButton = new JButton("Criar");
        JButton cancelButton = new JButton("Cancelar");

        createButton.addActionListener(e -> {
            String groupName = nameField.getText();
            Boolean radioRamdom = radio1.isSelected();
            if (!groupName.trim().isEmpty()) {
                try {
                    chatClient.createChatGroup(groupName, chatClient.userName, radioRamdom);
                } catch (RemoteException e1) {
                    e1.printStackTrace();
                    JOptionPane.showMessageDialog(dialog, "Erro ao criar grupo!", "Error",
                            JOptionPane.ERROR_MESSAGE);
                    dialog.dispose();
                }
                System.out.println("Group " + groupName + " Criado!");
                JOptionPane.showMessageDialog(dialog, "Grupo " + groupName + " Criado!", "Sucesso",
                        JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, "O nome do grupo não pode ser vazio!", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(createButton);
        buttonPanel.add(cancelButton);

        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }
}
