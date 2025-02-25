package com.edu.utfpr.client;

import com.edu.utfpr.core.entities.Chat;
import com.edu.utfpr.core.entities.Messages;
import com.edu.utfpr.core.entities.User;
import com.edu.utfpr.core.exceptions.InvalidUserOrPasswordException;
import com.edu.utfpr.core.exceptions.UserAlreadyRegisteredException;
import com.edu.utfpr.server.IChatServer;

import java.awt.Desktop;
import javax.swing.*;
import java.io.*;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class ChatClient extends UnicastRemoteObject implements IChatClient {
    private final String hostName = "localhost";
    private final List<Consumer<List<User>>> changeUserListListeners = new ArrayList<>();
    private final List<Consumer<List<Chat>>> changeGroupListListeners = new ArrayList<>();
    private final List<Consumer<List<Chat>>> changeMyChatsListListeners = new ArrayList<>();
    private final List<Consumer<Chat>> changeCurrentChatListeners = new ArrayList<>();
    private final List<Consumer<Messages>> onReceiveMessageListeners = new ArrayList<>();
    private final String clientServiceName;
    public Chat currentChat;
    public String userName;
    protected IChatServer server;

    public ChatClient(String userName)
            throws RemoteException, MalformedURLException, NotBoundException {
        super();
        clientServiceName = "ClientListenService_" + userName;

        Naming.rebind("rmi://" + hostName + "/" + clientServiceName, this);
        String serverServiceName = "GroupChatService";
        server = (IChatServer) Naming.lookup("rmi://" + hostName + "/" + serverServiceName);
    }

    public void register(String userName, String password)
            throws RemoteException, UserAlreadyRegisteredException {
        server.registerUser(userName, password);
    }

    public void login(String userName, String password)
            throws RemoteException, MalformedURLException, NotBoundException, InvalidUserOrPasswordException {
        server.login(userName, password, hostName, clientServiceName);
        this.userName = userName;
    }

    public List<User> getCurrentUsers() throws RemoteException {
        return server.getCurrentUsers();
    }

    public List<Chat> getAllGroups() throws RemoteException {
        return server.getAllGroups();
    }

    public List<Chat> getMyChats() throws RemoteException {
        return server.getMyChats(userName);
    }

    public void createPrivateChat(String destinationUser) throws RemoteException {
        server.createPrivateChat(userName, destinationUser);
    }

    public void createChatGroup(String chatName, String user, Boolean exitAdminMethod) throws RemoteException {
        server.createChatGroup(chatName, user, exitAdminMethod);
    }

    @Override
    public void updateUserList(List<User> currentUsers) throws RemoteException {
        for (Consumer<List<User>> listener : changeUserListListeners) {
            listener.accept(currentUsers);
        }
    }

    @Override
    public void receiveFile(String sender, String fileName, byte[] fileData, int length) throws RemoteException {
        System.out.println("Recebendo arquivo '" + fileName + "' de " + sender + " (" + length + " bytes)");

        File downloadDir = new File("downloads");
        if (!downloadDir.exists()) {
            downloadDir.mkdirs();
        }

        File receivedFile = new File(downloadDir, fileName);

        try (FileOutputStream fos = new FileOutputStream(receivedFile)) {
            fos.write(fileData, 0, length);
            System.out.println("Arquivo salvo em: " + receivedFile.getAbsolutePath());

            String fileMessage = sender + " enviou um arquivo: " + fileName;
            Messages message = new Messages(new User(sender, null), fileMessage);
            currentChat.messages.add(message);

            for (Consumer<Messages> listener : onReceiveMessageListeners) {
                listener.accept(message);
            }

            SwingUtilities.invokeLater(() -> {
                int option = JOptionPane.showConfirmDialog(
                        null,
                        "Você recebeu um arquivo de " + sender + ": " + fileName + ".\nDeseja abrir agora?",
                        "Arquivo recebido",
                        JOptionPane.YES_NO_OPTION
                );

                if (option == JOptionPane.YES_OPTION) {
                    abrirArquivo(receivedFile);
                }
            });

        } catch (IOException e) {
            System.err.println("Erro ao salvar o arquivo: " + e.getMessage());
        }
    }

    private void abrirArquivo(File file) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                if (file.exists()) {
                    desktop.open(file);
                } else {
                    JOptionPane.showMessageDialog(null, "O arquivo não foi encontrado!", "Erro", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(null, "Abrir arquivos não é suportado neste sistema!", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Erro ao abrir o arquivo: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    public void addChangeCurrentChatListener(Consumer<Chat> listener) {
        changeCurrentChatListeners.add(listener);
    }

    public void addChangeUserListListener(Consumer<List<User>> listener) {
        changeUserListListeners.add(listener);
    }

    public void addChangeGroupListListener(Consumer<List<Chat>> listener) {
        changeGroupListListeners.add(listener);
    }

    public void addChangeMyChatsListListener(Consumer<List<Chat>> listener) {
        changeMyChatsListListeners.add(listener);
    }

    public void addOnReceiveMessageListener(Consumer<Messages> listener) {
        onReceiveMessageListeners.add(listener);
    }

    public void setCurrentChat(Chat chat) throws RemoteException {
        server.getMyChats(userName).forEach(c -> {
            if (c.chatId.equals(chat.chatId)) {
                currentChat = c;
            }
        });
        for (Consumer<Chat> listener : changeCurrentChatListeners) {
            listener.accept(currentChat);
        }
    }

    public void sendMessage(String message, JPanel inputPanel) throws RemoteException {
        server.sendMessage(userName, currentChat, message, inputPanel);
    }

    @Override
    public void updatePublicGroupList(List<Chat> groups) throws RemoteException {
        for (Consumer<List<Chat>> listener : changeGroupListListeners) {
            listener.accept(groups);
        }
    }

    @Override
    public void updateChatList(List<Chat> myChats) throws RemoteException {
        for (Consumer<List<Chat>> listener : changeMyChatsListListeners) {
            listener.accept(myChats);
        }
    }

    @Override
    public void receiveMessage(Messages message) throws RemoteException {
        currentChat.messages.add(message);
        for (Consumer<Messages> listener : onReceiveMessageListeners) {
            listener.accept(message);
        }
    }

    @Override
    public UUID getCurrentChatId() throws RemoteException {
        if (currentChat == null) {
            return null;
        }
        return currentChat.chatId;
    }

    @Override
    public void sendInviteAdmin(String userName, Chat chat) throws RemoteException {
        server.createInviteGroup(userName, chat);
    }

    @Override
    public void notifyUserAccepted(String groupName) throws RemoteException {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(
                    null, // parentComponent
                    "Você foi aceito no grupo: " + groupName, // message
                    "Aceito no Grupo", // title
                    JOptionPane.INFORMATION_MESSAGE // messageType
            );
        });
    }

    public void sendFile(File file) throws RemoteException {
        if (currentChat == null) {
            System.err.println("Nenhum chat selecionado.");
            return;
        }

        try {
            byte[] fileData = Files.readAllBytes(file.toPath());

            for (User member : currentChat.members) {
                if (!member.getName().equals(userName)) { // Não envia para si mesmo
                    server.sendFile(userName, member.getName(), fileData, file.getName());
                }
            }

            System.out.println("Arquivo enviado para o chat '" + currentChat.name + "'.");

        } catch (IOException e) {
            System.err.println("Erro ao ler o arquivo: " + e.getMessage());
        }
    }
}